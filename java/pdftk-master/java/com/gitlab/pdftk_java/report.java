/*
 *   This file is part of the pdftk port to java
 *
 *   Copyright (c) Marc Vinyals 2017-2018
 *
 *   The program is a java port of PDFtk, the PDF Toolkit
 *   Copyright (c) 2003-2013 Steward and Lee, LLC
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   The program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gitlab.pdftk_java;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.NumericEntityEscaper;
import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;
import pdftk.com.lowagie.text.pdf.PRStream;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfBoolean;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;

class report {

  // Escape both Xml and Unicode entities
  // see https://commons.apache.org/proper/commons-lang/javadocs/api-3.6/org/apache/commons/lang3/StringEscapeUtils.html#escapeXml-java.lang.String-
  static CharSequenceTranslator XmlUnicodeEscaper =
    StringEscapeUtils.ESCAPE_XML10.with(
      NumericEntityEscaper.between(0x7f, Integer.MAX_VALUE));

  static String OutputXmlString(String jss_p) {
    return XmlUnicodeEscaper.translate(jss_p);
  }

  static String OutputUtf8String(String jss_p) {
    return jss_p;
  }

  static String OutputPdfString(PdfString pdfss_p, boolean utf8_b) {
    if (pdfss_p != null && pdfss_p.isString()) {
      String jss_p = pdfss_p.toUnicodeString();
      if (utf8_b) {
        return OutputUtf8String(jss_p);
      } else {
        return OutputXmlString(jss_p);
      }
    }
    return "";
  }

  static String OutputPdfName(PdfName pdfnn_p) {
    if (pdfnn_p != null && pdfnn_p.isName()) {
      String jnn_p = new String(pdfnn_p.getBytes());
      jnn_p = PdfName.decodeName(jnn_p);
      return OutputXmlString(jnn_p);
    }
    return "";
  }

  static String OutputPdfStringOrName(PdfObject pdfoo_p, boolean utf8_b) {
    if (pdfoo_p != null && pdfoo_p.isString()) {
      return OutputPdfString((PdfString) pdfoo_p, utf8_b);
    } else if (pdfoo_p != null && pdfoo_p.isName()) {
      return OutputPdfName((PdfName) pdfoo_p);
    }
    return null;
  }

  static class FormField {
    String m_ft = ""; // type
    String m_tt = ""; // name
    String m_tu = ""; // alt. name
    int m_ff = 0; // flags
    Set<String> m_vv = new HashSet<String>(); // value -- may be an array
    String m_dv = ""; // default value

    // variable-text features
    int m_qq = 0; // quadding (justification)
    String m_ds = ""; // default style (rich text)
    byte[] m_rv = new byte[0]; // rich text value

    int m_maxlen = 0;

    // for checkboxes and such
    Set<String> m_states = new HashSet<String>(); // possible states
    // states as (value,display) pairs
    Set<List<String>> m_states_value_display = new HashSet<List<String>>();
    String m_state = "";

    FormField() {}

    void copyFrom(FormField copy) {
      m_ft = copy.m_ft;
      m_tt = copy.m_tt;
      m_tu = copy.m_tu;
      m_ff = copy.m_ff;
      m_vv = new HashSet<String>(copy.m_vv);
      m_dv = copy.m_dv;
      m_qq = copy.m_qq;
      m_ds = copy.m_ds;
      m_rv = Arrays.copyOf(copy.m_rv, copy.m_rv.length);
      m_maxlen = copy.m_maxlen;
      m_states = new HashSet<String>(copy.m_states);
      m_states_value_display = new HashSet<List<String>>();
      for (List<String> l : copy.m_states_value_display) {
        m_states_value_display.add(new ArrayList<String>(l));
      }
      m_state = copy.m_state;
    }

    FormField(FormField copy) {
      copyFrom(copy);
    }

    void addOptions(PdfReader reader_p, PdfArray opts_p, boolean utf8_b) {
      ArrayList<PdfObject> opts_a = opts_p.getArrayList();
      for (PdfObject opts_ii : opts_a) {
        PdfObject opt_p = reader_p.getPdfObject(opts_ii);
        if (opt_p == null) continue;
        if (opt_p.isString()) {
          // Option is a text string
          m_states.add(OutputPdfString((PdfString) opt_p, utf8_b));
        } else if (opt_p.isArray()) {
          // Option is an array (value, display)
          ArrayList<PdfObject> opt_value_display_p = ((PdfArray) opt_p).getArrayList();
          ArrayList<String> opt_value_display_a = new ArrayList<String>();
          for (PdfObject subopt_p : opt_value_display_p) {
            if (subopt_p.isString()) {
              opt_value_display_a.add(OutputPdfString((PdfString) subopt_p, utf8_b));
            }
          }
          if (opt_value_display_a.size() != 2) continue;
          m_states_value_display.add(opt_value_display_a);
        }
      }
    }

    void addApStates(PdfReader reader_p, PdfDictionary ap_p, PdfName state_name) {
      if (ap_p.contains(state_name)) {
        PdfObject n_p = reader_p.getPdfObject(ap_p.get(state_name));
        if (n_p != null && n_p.isDictionary()) {
          Set<PdfName> n_set_p = ((PdfDictionary) n_p).getKeys();
          for (PdfName key_p : n_set_p) {
            m_states.add(OutputPdfName(key_p));
          }
        }
      }
    }
  };

  static void OutputFormField(PrintStream ofs, FormField ff) {
    ofs.println("---"); // delim
    ofs.println("FieldType: " + ff.m_ft);
    ofs.println("FieldName: " + ff.m_tt);
    if (!ff.m_tu.isEmpty()) ofs.println("FieldNameAlt: " + ff.m_tu);
    ofs.println("FieldFlags: " + ff.m_ff);
    for (String it : ff.m_vv) {
      ofs.println("FieldValue: " + it);
    }
    if (!ff.m_dv.isEmpty()) ofs.println("FieldValueDefault: " + ff.m_dv);

    ofs.print("FieldJustification: ");
    switch (ff.m_qq) {
      case 0:
        ofs.println("Left");
        break;
      case 1:
        ofs.println("Center");
        break;
      case 2:
        ofs.println("Right");
        break;
      default:
        ofs.println(ff.m_qq);
        break;
    }

    if (!ff.m_ds.isEmpty()) ofs.println("FieldStyleDefault: " + ff.m_ds);
    if (ff.m_rv.length > 0) {
      ofs.print("FieldValueRichText: ");
      try {
        ofs.write(ff.m_rv);
      } catch (IOException e) {
      }
      ofs.println();
    }
    if (0 < ff.m_maxlen) ofs.println("FieldMaxLength: " + ff.m_maxlen);

    for (String it : ff.m_states) {
      ofs.println("FieldStateOption: " + it);
    }
    for (List<String> it : ff.m_states_value_display) {
      ofs.println("FieldStateOption: " + it.get(0));
      ofs.println("FieldStateOptionDisplay: " + it.get(1));
    }
  }

  static boolean ReportAcroFormFields(
      PrintStream ofs,
      PdfArray kids_array_p,
      FormField acc_state,
      PdfReader reader_p,
      boolean utf8_b) {
    FormField prev_state = new FormField(acc_state);
    boolean ret_val_b = false;

    ArrayList<PdfObject> kids_p = kids_array_p.getArrayList();
    if (kids_p != null) {
      for (PdfObject kids_ii : kids_p) {

        PdfObject kid_po = reader_p.getPdfObject(kids_ii);
        if (kid_po != null && kid_po.isDictionary()) {
          PdfDictionary kid_p = (PdfDictionary) kid_po;

          // field type
          if (kid_p.contains(PdfName.FT)) {
            PdfObject ft_p = reader_p.getPdfObject(kid_p.get(PdfName.FT));
            if (ft_p != null && ft_p.isName()) {

              if (ft_p.equals(PdfName.BTN)) { // button
                acc_state.m_ft = "Button";
              } else if (ft_p.equals(PdfName.TX)) { // text
                acc_state.m_ft = "Text";
              } else if (ft_p.equals(PdfName.CH)) { // choice
                acc_state.m_ft = "Choice";
              } else if (ft_p.equals(PdfName.SIG)) { // signature
                acc_state.m_ft = "Signature";
              } else { // warning
                System.err.println(
                    "pdftk Warning in ReportAcroFormFields(): unexpected field type;");
              }
            }
          }

          // field name; special inheritance rule: prepend parent name
          if (kid_p.contains(PdfName.T)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.T));
            if (pdfs_p != null && pdfs_p.isString()) {
              if (!acc_state.m_tt.isEmpty()) {
                acc_state.m_tt = acc_state.m_tt + ".";
              }
              acc_state.m_tt = acc_state.m_tt +
                OutputPdfString((PdfString) pdfs_p, utf8_b);
            }
          }

          // field alt. name
          if (kid_p.contains(PdfName.TU)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.TU));
            if (pdfs_p != null && pdfs_p.isString()) {
              acc_state.m_tu = OutputPdfString((PdfString) pdfs_p, utf8_b);
            }
          } else {
            acc_state.m_tu = "";
          }

          // field flags; inheritable
          if (kid_p.contains(PdfName.FF)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.FF));
            if (pdfs_p != null && pdfs_p.isNumber()) {
              acc_state.m_ff = ((PdfNumber) pdfs_p).intValue();
            }
          }

          // field value; inheritable; may be string or name
          if (kid_p.contains(PdfName.V)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.V));

            if (pdfs_p == null) continue;
            String maybe_output = OutputPdfStringOrName(pdfs_p, utf8_b);
            if (maybe_output != null) {
              acc_state.m_vv.add(maybe_output);
            } else if (pdfs_p.isArray()) {
              // multiple selections
              ArrayList<PdfObject> vv_p = ((PdfArray) pdfs_p).getArrayList();
              for (PdfObject vv_ii : vv_p) {
                PdfObject pdfs_p_2 = reader_p.getPdfObject(vv_ii);
                String maybe_output_2 = OutputPdfStringOrName(pdfs_p_2, utf8_b);
                if (maybe_output_2 != null) {
                  acc_state.m_vv.add(maybe_output_2);
                }
              }
            }
          }

          // default value; inheritable
          if (kid_p.contains(PdfName.DV)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.DV));
            String maybe_output = OutputPdfStringOrName(pdfs_p, utf8_b);
            if (maybe_output != null) {
              acc_state.m_dv = maybe_output;
            }
          }

          // quadding; inheritable
          if (kid_p.contains(PdfName.Q)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.Q));
            if (pdfs_p != null && pdfs_p.isNumber()) {
              acc_state.m_qq = ((PdfNumber) pdfs_p).intValue();
            }
          }

          // default style
          if (kid_p.contains(PdfName.DS)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.DS));
            if (pdfs_p != null && pdfs_p.isString()) {
              acc_state.m_ds = OutputPdfString((PdfString) pdfs_p, utf8_b);
            }
          } else {
            acc_state.m_ds = "";
          }

          // rich text value; may be a string or a stream
          if (kid_p.contains(PdfName.RV)) {
            PdfObject pdfo_p = reader_p.getPdfObject(kid_p.get(PdfName.RV));
            if (pdfo_p != null && pdfo_p.isString()) { // string
              String name_oss = OutputPdfString((PdfString) pdfo_p, utf8_b);
              acc_state.m_rv = name_oss.getBytes(StandardCharsets.UTF_8);
            } else if (pdfo_p != null && pdfo_p.isStream()) { // stream
              acc_state.m_rv = ((PRStream) pdfo_p).getBytes();
            }
          } else {
            acc_state.m_rv = new byte[0];
          }

          // maximum length; inheritable
          if (kid_p.contains(PdfName.MAXLEN)) {
            PdfObject pdfs_p = reader_p.getPdfObject(kid_p.get(PdfName.MAXLEN));
            if (pdfs_p != null && pdfs_p.isNumber()) {
              acc_state.m_maxlen = ((PdfNumber) pdfs_p).intValue();
            }
          }

          // available states
          if (kid_p.contains(PdfName.AP)) {
            PdfObject ap_po = reader_p.getPdfObject(kid_p.get(PdfName.AP));
            if (ap_po != null && ap_po.isDictionary()) {
              PdfDictionary ap_p = (PdfDictionary) ap_po;

              // this is one way to cull button option names: iterate over
              // appearance state names
              acc_state.addApStates(reader_p, ap_p, PdfName.N);
              acc_state.addApStates(reader_p, ap_p, PdfName.D);
              acc_state.addApStates(reader_p, ap_p, PdfName.R);
            }
          }

          // list-box / combo-box possible states
          if (kid_p.contains(PdfName.OPT)) {
            PdfObject kid_opts_p = reader_p.getPdfObject(kid_p.get(PdfName.OPT));
            if (kid_opts_p != null && kid_opts_p.isArray()) {
              acc_state.addOptions(reader_p, (PdfArray) kid_opts_p, utf8_b);
            }
          }

          if (kid_p.contains(PdfName.KIDS)) { // recurse
            PdfObject kid_kids_p = reader_p.getPdfObject(kid_p.get(PdfName.KIDS));
            if (kid_kids_p != null && kid_kids_p.isArray()) {

              boolean kids_have_names_b =
                ReportAcroFormFields(ofs, (PdfArray) kid_kids_p, acc_state, reader_p, utf8_b);

              if (!kids_have_names_b && kid_p.contains(PdfName.T)) {
                // dump form field
                OutputFormField(ofs, acc_state);
              }

              // reset state;
              acc_state.copyFrom(prev_state);

              // record field names found in subtree
              ret_val_b |= kids_have_names_b;
            } else { // error
            }
          } else if (kid_p.contains(PdfName.T)) {
            // term. field; dump form field
            OutputFormField(ofs, acc_state);

            // reset state;
            acc_state.copyFrom(prev_state);

            // record presense of field name
            ret_val_b = true;
          }
        }
      }
    } else { // warning
      System.err.println("pdftk Warning in ReportAcroFormFields(): unable to get ArrayList;");
    }

    return ret_val_b;
  }

  static void ReportAcroFormFields(PrintStream ofs, PdfReader reader_p, boolean utf8_b) {
    PdfDictionary catalog_p = reader_p.catalog;
    if (catalog_p != null && catalog_p.isDictionary()) {

      PdfObject acro_form_p = reader_p.getPdfObject(catalog_p.get(PdfName.ACROFORM));
      if (acro_form_p != null && acro_form_p.isDictionary()) {

        PdfObject fields_p = reader_p.getPdfObject(((PdfDictionary) acro_form_p).get(PdfName.FIELDS));
        if (fields_p != null && fields_p.isArray()) {

          // enter recursion
          FormField root_field_state = new FormField();
          ReportAcroFormFields(ofs, (PdfArray) fields_p, root_field_state, reader_p, utf8_b);
        }
      }
    } else { // error
      System.err.println("pdftk Error in ReportAcroFormFields(): unable to access PDF catalog;");
    }
  }

  static void ReportAction(
      PrintStream ofs, PdfReader reader_p, PdfDictionary action_p, boolean utf8_b, String prefix) {
    if (action_p.contains(PdfName.S)) {
      PdfObject s_p = reader_p.getPdfObject(action_p.get(PdfName.S));

      // URI action
      if (s_p.equals(PdfName.URI)) {
        ofs.println(prefix + "ActionSubtype: URI");

        // report URI
        if (action_p.contains(PdfName.URI)) {
          PdfObject uri_p = reader_p.getPdfObject(action_p.get(PdfName.URI));
          if (uri_p != null && uri_p.isString()) {

            ofs.println(prefix + "ActionURI: " +
                        OutputPdfString((PdfString) uri_p, utf8_b));
          }
        }

        // report IsMap
        if (action_p.contains(PdfName.ISMAP)) {
          PdfObject ismap_p = reader_p.getPdfObject(action_p.get(PdfName.ISMAP));
          if (ismap_p != null &&
              ismap_p.isBoolean() &&
              ((PdfBoolean) ismap_p).booleanValue()) {
            ofs.println(prefix + "ActionIsMap: true");
          }
          else {
            ofs.println(prefix + "ActionIsMap: false");
          }
        }
      }
    }

    // subsequent actions? can be a single action or an array
    if (action_p.contains(PdfName.NEXT)) {
      PdfObject next_p = reader_p.getPdfObject(action_p.get(PdfName.NEXT));
      if (next_p != null && next_p.isDictionary()) {
        ReportAction(ofs, reader_p, (PdfDictionary) next_p, utf8_b, prefix);
      } else if (next_p != null && next_p.isArray()) {
        ArrayList<PdfObject> actions_p = ((PdfArray) next_p).getArrayList();
        for (PdfObject ii : actions_p) {
          PdfObject next_action_p = reader_p.getPdfObject(ii);
          if (next_action_p != null && next_action_p.isDictionary())
            ReportAction(ofs, reader_p, (PdfDictionary) next_action_p, utf8_b, prefix); // recurse
        }
      }
    }
  }

  static final int LLx = 0;
  static final int LLy = 1;
  static final int URx = 2;
  static final int URy = 3;

  static void ReportAnnot(
      PrintStream ofs,
      PdfReader reader_p,
      int page_num,
      PdfDictionary page_p,
      PdfDictionary annot_p,
      boolean utf8_b) {
    // report things common to all annots

    // subtype
    PdfObject subtype_p = reader_p.getPdfObject(annot_p.get(PdfName.SUBTYPE));
    if (subtype_p != null && subtype_p.isName()) {
      ofs.println("AnnotSubtype: " + OutputPdfName((PdfName) subtype_p));
    }

    ////
    // rect

    // get raw rect from annot
    float[] rect = {0.0f, 0.0f, 0.0f, 0.0f};
    PdfObject rect_p = reader_p.getPdfObject(annot_p.get(PdfName.RECT));
    if (rect_p != null && rect_p.isArray()) {
      ArrayList<PdfObject> rect_al_p = ((PdfArray) rect_p).getArrayList();
      if (rect_al_p.size() == 4) {

        for (int ii = 0; ii < 4; ++ii) {
          PdfObject coord_p = reader_p.getPdfObject(rect_al_p.get(ii));
          if (coord_p != null && coord_p.isNumber()) {
            rect[ii] = (float) ((PdfNumber) coord_p).floatValue();
          }
          else {
            rect[ii] = -1; // error value
          }
        }
      }
    }

    // transform rect according to page crop box
    // grab width and height for later xform
    float page_crop_width = 0;
    float page_crop_height = 0;
    {
      Rectangle page_crop_p = reader_p.getCropBox(page_num);
      rect[0] = rect[0] - page_crop_p.left();
      rect[1] = rect[1] - page_crop_p.bottom();
      rect[2] = rect[2] - page_crop_p.left();
      rect[3] = rect[3] - page_crop_p.bottom();

      page_crop_width = (float) (page_crop_p.right() - page_crop_p.left());
      page_crop_height = (float) (page_crop_p.top() - page_crop_p.bottom());
    }

    // create new rect based on page rotation
    int page_rot = (int) (reader_p.getPageRotation(page_num)) % 360;
    float[] rot_rect = {0.0f, 0.0f, 0.0f, 0.0f};
    switch (page_rot) {
      case 90:
        rot_rect[0] = rect[LLy];
        rot_rect[1] = page_crop_width - rect[URx];
        rot_rect[2] = rect[URy];
        rot_rect[3] = page_crop_width - rect[LLx];
        break;

      case 180:
        rot_rect[0] = page_crop_width - rect[URx];
        rot_rect[1] = page_crop_height - rect[URy];
        rot_rect[2] = page_crop_width - rect[LLx];
        rot_rect[3] = page_crop_height - rect[LLy];
        break;

      case 270:
        rot_rect[0] = page_crop_height - rect[URy];
        rot_rect[1] = rect[LLx];
        rot_rect[2] = page_crop_height - rect[LLy];
        rot_rect[3] = rect[URx];
        break;

      default: // 0 deg
        rot_rect[0] = rect[0];
        rot_rect[1] = rect[1];
        rot_rect[2] = rect[2];
        rot_rect[3] = rect[3];
        break;
    }

    // output rotated rect
    ofs.println(
        "AnnotRect: " + rot_rect[0] + " " + rot_rect[1] + " " + rot_rect[2] + " " + rot_rect[3]);
  }

  static void ReportAnnots(PrintStream ofs, PdfReader reader_p, boolean utf8_b) {
    reader_p.resetReleasePage();

    ////
    // document information

    // document page count
    ofs.println("NumberOfPages: " + (int) reader_p.getNumberOfPages());

    // document base url
    PdfObject uri_p = reader_p.getPdfObject(reader_p.catalog.get(PdfName.URI));
    if (uri_p != null && uri_p.isDictionary()) {

      PdfObject base_p = reader_p.getPdfObject(((PdfDictionary) uri_p).get(PdfName.BASE));
      if (base_p != null && base_p.isString()) {
        ofs.println("PdfUriBase: " + OutputPdfString((PdfString) base_p, utf8_b));
      }
    }

    ////
    // iterate over pages

    for (int ii = 1; ii <= reader_p.getNumberOfPages(); ++ii) {
      PdfDictionary page_p = reader_p.getPageN(ii);

      PdfObject annots_p = reader_p.getPdfObject(page_p.get(PdfName.ANNOTS));
      if (annots_p != null && annots_p.isArray()) {

        ArrayList<PdfObject> annots_al_p = ((PdfArray) annots_p).getArrayList();

        // iterate over annotations
        for (PdfObject jj : annots_al_p) {

          PdfObject annot_po = reader_p.getPdfObject(jj);
          if (annot_po != null && annot_po.isDictionary()) {
            PdfDictionary annot_p = (PdfDictionary) annot_po;

            PdfObject type_p = reader_p.getPdfObject(annot_p.get(PdfName.TYPE));
            if (type_p.equals(PdfName.ANNOT)) {

              PdfObject subtype_p = reader_p.getPdfObject(annot_p.get(PdfName.SUBTYPE));

              // link annotation
              if (subtype_p.equals(PdfName.LINK)) {

                ofs.println("---"); // delim
                ReportAnnot(ofs, reader_p, ii, page_p, annot_p, utf8_b); // base annot items
                ofs.println("AnnotPageNumber: " + ii);

                // link-specific items
                if (annot_p.contains(PdfName.A)) { // action
                  PdfObject action_p = reader_p.getPdfObject(annot_p.get(PdfName.A));
                  if (action_p != null && action_p.isDictionary()) {

                    ReportAction(ofs, reader_p, (PdfDictionary) action_p, utf8_b, "Annot");
                  }
                }
              }
            }
          }
        }
      }
      reader_p.releasePage(ii);
    }
    reader_p.resetReleasePage();
  }

  //
  static class PdfPageLabel {
    static final String m_prefix = "PageLabel";
    static final String m_begin_mark = "PageLabelBegin";
    // TODO
  };

  //
  class PdfPageMedia {
    static final String m_prefix = "PageMedia";
    static final String m_begin_mark = "PageMediaBegin";
    // TODO
  };

  static void ReportOutlines(
      PrintStream ofs, PdfDictionary outline_p, PdfReader reader_p, boolean utf8_b) {
    ArrayList<PdfBookmark> bookmark_data = new ArrayList<PdfBookmark>();
    bookmarks.ReadOutlines(bookmark_data, outline_p, 0, reader_p, utf8_b);

    for (PdfBookmark it : bookmark_data) {
      ofs.print(it);
    }
  }

  static void ReportInfo(
      PrintStream ofs, PdfReader reader_p, PdfDictionary info_p, boolean utf8_b) {
    if (info_p != null && info_p.isDictionary()) {
      Set<PdfName> keys_p = info_p.getKeys();

      // iterate over Info keys
      for (PdfName key_p : keys_p) {

        int key_len = key_p.getBytes().length - 1; // minus one for init. slash

        PdfObject value_p = reader_p.getPdfObject(info_p.get(key_p));

        // don't output empty keys or values
        if (0 < key_len
            && value_p.isString()
            && 0 < ((PdfString) value_p).toUnicodeString().length()) { // ouput
          ofs.println(data_import.PdfInfo.m_begin_mark);

          ofs.println(data_import.PdfInfo.m_key_label + " " + OutputPdfName(key_p));

          ofs.println(
              data_import.PdfInfo.m_value_label
                  + " "
                  + OutputPdfString((PdfString) value_p, utf8_b));
        }
      }

    } else { // error
    }
  }

  static void ReportPageLabels(
      PrintStream ofs, PdfDictionary numtree_node_p, PdfReader reader_p, boolean utf8_b)
        // if *numtree_node_p has Nums, report them;
        // else if *numtree_node_p has Kids, recurse
        // output 1-based page numbers; that's what we do for bookmarks
      {
    PdfObject nums_p = reader_p.getPdfObject(numtree_node_p.get(PdfName.NUMS));
    if (nums_p != null && nums_p.isArray()) {
      // report page numbers

      ArrayList<PdfObject> labels_p = ((PdfArray) nums_p).getArrayList();
      for (Iterator<PdfObject> labels_ii = labels_p.iterator(); labels_ii.hasNext(); ) {

        // label index
        PdfObject index_p = reader_p.getPdfObject(labels_ii.next());

        // label dictionary
        PdfObject label_po = reader_p.getPdfObject(labels_ii.next());

        if (index_p != null &&
            index_p.isNumber() &&
            label_po != null &&
            label_po.isDictionary()) {
          PdfDictionary label_p = (PdfDictionary) label_po;
          ofs.println(PdfPageLabel.m_begin_mark);

          // PageLabelNewIndex
          ofs.println("PageLabelNewIndex: " + (long) (((PdfNumber) index_p).intValue()) + 1);

          { // PageLabelStart
            ofs.print("PageLabelStart: ");
            PdfObject start_p = reader_p.getPdfObject(label_p.get(PdfName.ST));
            if (start_p != null && start_p.isNumber()) {
              ofs.println((long) ((PdfNumber) start_p).intValue());
            } else {
              ofs.println("1"); // the default
            }
          }

          { // PageLabelPrefix
            PdfObject prefix_p = reader_p.getPdfObject(label_p.get(PdfName.P));
            if (prefix_p != null && prefix_p.isString()) {
              ofs.println("PageLabelPrefix: " + OutputPdfString((PdfString) prefix_p, utf8_b));
            }
          }

          { // PageLabelNumStyle
            PdfName r_p = new PdfName("r");
            PdfName a_p = new PdfName("a");

            PdfObject style_p = reader_p.getPdfObject(label_p.get(PdfName.S));
            ofs.print("PageLabelNumStyle: ");
            if (style_p != null && style_p.isName()) {
              if (style_p.equals(PdfName.D)) {
                ofs.println("DecimalArabicNumerals");
              } else if (style_p.equals(PdfName.R)) {
                ofs.println("UppercaseRomanNumerals");
              } else if (style_p.equals(r_p)) {
                ofs.println("LowercaseRomanNumerals");
              } else if (style_p.equals(PdfName.A)) {
                ofs.println("UppercaseLetters");
              } else if (style_p.equals(a_p)) {
                ofs.println("LowercaseLetters");
              } else { // error
                ofs.println("[PDFTK ERROR]");
              }
            } else { // default
              ofs.println("NoNumber");
            }
          }

        }
        else { // error
          ofs.println("[PDFTK ERROR: INVALID label_p IN ReportPageLabelNode]");
        }
      }
    }
    else { // try recursing
      PdfObject kids_p = reader_p.getPdfObject(numtree_node_p.get(PdfName.KIDS));
      if (kids_p != null && kids_p.isArray()) {

        ArrayList<PdfObject> kids_ar_p = ((PdfArray) kids_p).getArrayList();
        for (PdfObject kids_ii : kids_ar_p) {

          PdfObject kid_p = reader_p.getPdfObject(kids_ii);
          if (kid_p != null && kid_p.isDictionary()) {

              // recurse
            ReportPageLabels(ofs, (PdfDictionary) kid_p, reader_p, utf8_b);
          } else { // error
            ofs.println("[PDFTK ERROR: INVALID kid_p]");
          }
        }
      } else { // error; a number tree must have one or the other
        ofs.println("[PDFTK ERROR: INVALID PAGE LABEL NUMBER TREE]");
      }
    }
  }

  static void ReportOnPdf(PrintStream ofs, PdfReader reader_p, boolean utf8_b) {
    { // trailer data
      PdfDictionary trailer_p = reader_p.getTrailer();
      if (trailer_p != null) {

        { // metadata
          PdfObject info_p = reader_p.getPdfObject(trailer_p.get(PdfName.INFO));
          if (info_p != null && info_p.isDictionary()) {

            ReportInfo(ofs, reader_p, (PdfDictionary) info_p, utf8_b);
          } else { // warning
            System.err.println("Warning: no info dictionary found");
          }
        }

        { // pdf ID; optional
          PdfObject id_p = reader_p.getPdfObject(trailer_p.get(PdfName.ID));
          if (id_p != null && id_p.isArray()) {

            ArrayList<PdfObject> id_al_p = ((PdfArray) id_p).getArrayList();

            for (int ii = 0; ii < id_al_p.size(); ++ii) {
              ofs.print("PdfID" + ii + ": ");

              PdfObject id_ss_p = reader_p.getPdfObject(id_al_p.get(ii));
              if (id_ss_p != null && id_ss_p.isString()) {

                byte[] bb = ((PdfString) id_ss_p).getBytes();
                if (bb != null && bb.length > 0) {
                  for (byte bb_ss : bb) {
                    ofs.printf("%02x", bb_ss);
                  }
                }
              } else { // error
                System.err.println("pdftk Error in ReportOnPdf(): invalid pdf id array string;");
              }

              ofs.println();
            }
          }
        }

      } else { // error
        System.err.println("pdftk Error in ReportOnPdf(): invalid trailer;");
      }
    }

    int numPages = reader_p.getNumberOfPages();

    { // number of pages and outlines
      PdfObject catalog_p = reader_p.catalog;
      if (catalog_p != null && catalog_p.isDictionary()) {

        // number of pages
        /*
        itext::PdfDictionary* pages_p= (itext::PdfDictionary*)
          reader_p->getPdfObject( catalog_p->get( itext::PdfName::PAGES ) );
        if( pages_p && pages_p->isDictionary() ) {

          itext::PdfNumber* count_p= (itext::PdfNumber*)
            reader_p->getPdfObject( pages_p->get( itext::PdfName::COUNT ) );
          if( count_p && count_p->isNumber() ) {

            ofs << "NumberOfPages: " << (unsigned int)count_p->intValue() << endl;
          }
          else { // error
            cerr << "pdftk Error in ReportOnPdf(): invalid count_p;" << endl;
          }
        }
        else { // error
          cerr << "pdftk Error in ReportOnPdf(): invalid pages_p;" << endl;
        }
        */
        ofs.println("NumberOfPages: " + numPages);

        // outlines; optional
        PdfObject outlines_p =
          reader_p.getPdfObject(((PdfDictionary) catalog_p).get(PdfName.OUTLINES));
        if (outlines_p != null && outlines_p.isDictionary()) {

          PdfObject top_outline_p =
            reader_p.getPdfObject(((PdfDictionary) outlines_p).get(PdfName.FIRST));
          if (top_outline_p != null && top_outline_p.isDictionary()) {

            ReportOutlines(ofs, (PdfDictionary) top_outline_p, reader_p, utf8_b);
          } else { // error
            // okay, not a big deal
            // cerr << "Internal Error: invalid top_outline_p in ReportOnPdf()" << endl;
          }
        }

      } else { // error
        System.err.println("pdftk Error in ReportOnPdf(): couldn't find catalog;");
      }
    }

    { // page metrics, rotation, stamptkData
      for (int ii = 1; ii <= numPages; ++ii) {
        PdfDictionary page_p = reader_p.getPageN(ii);

        ofs.println(PdfPageMedia.m_begin_mark);
        ofs.println("PageMediaNumber: " + ii);

        ofs.println("PageMediaRotation: " + reader_p.getPageRotation(page_p));

        NumberFormat c_format = NumberFormat.getInstance(Locale.ROOT);

        Rectangle page_rect_p = reader_p.getPageSize(page_p);
        if (page_rect_p != null) {
          ofs.println(
              "PageMediaRect: "
                  + c_format.format(page_rect_p.left())
                  + " "
                  + c_format.format(page_rect_p.bottom())
                  + " "
                  + c_format.format(page_rect_p.right())
                  + " "
                  + c_format.format(page_rect_p.top()));
          ofs.println(
              "PageMediaDimensions: "
                  + c_format.format(page_rect_p.right() - page_rect_p.left())
                  + " "
                  + c_format.format(page_rect_p.top() - page_rect_p.bottom()));
        }

        Rectangle page_crop_p = reader_p.getBoxSize(page_p, PdfName.CROPBOX);
        if (page_crop_p != null
            && !(page_crop_p.left() == page_rect_p.left()
                && page_crop_p.bottom() == page_rect_p.bottom()
                && page_crop_p.right() == page_rect_p.right()
                && page_crop_p.top() == page_rect_p.top())) {
          ofs.println(
              "PageMediaCropRect: "
                  + c_format.format(page_crop_p.left())
                  + " "
                  + c_format.format(page_crop_p.bottom())
                  + " "
                  + c_format.format(page_crop_p.right())
                  + " "
                  + c_format.format(page_crop_p.top()));
        }

        PdfString stamptkData_p = page_p.getAsString(PdfName.STAMPTKDATA);
        if (stamptkData_p != null) {
          ofs.println("PageMediaStamptkData: " + OutputPdfString(stamptkData_p, utf8_b));
        }

        reader_p.releasePage(ii);
      }
    }

    { // page labels (a/k/a logical page numbers)
      PdfDictionary catalog_p = reader_p.catalog;
      if (catalog_p != null) {

        PdfObject pagelabels_p =
reader_p.getPdfObject(catalog_p.get(PdfName.PAGELABELS));
        if (pagelabels_p != null && pagelabels_p.isDictionary()) {

          ReportPageLabels(ofs, (PdfDictionary) pagelabels_p, reader_p, utf8_b);
        }
      } else { // error
        System.err.println("pdftk Error in ReportOnPdf(): couldn't find catalog (2);");
      }
    }
  } // end: ReportOnPdf
};
