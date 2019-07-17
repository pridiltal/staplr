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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pdftk.com.lowagie.text.Document;
import pdftk.com.lowagie.text.DocumentException;
import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.pdf.AcroFields;
import pdftk.com.lowagie.text.pdf.FdfReader;
import pdftk.com.lowagie.text.pdf.FdfWriter;
import pdftk.com.lowagie.text.pdf.PdfAnnotation;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfBoolean;
import pdftk.com.lowagie.text.pdf.PdfContentByte;
import pdftk.com.lowagie.text.pdf.PdfCopy;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfFileSpecification;
import pdftk.com.lowagie.text.pdf.PdfImportedPage;
import pdftk.com.lowagie.text.pdf.PdfIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNameTree;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfStamperImp;
import pdftk.com.lowagie.text.pdf.PdfWriter;
import pdftk.com.lowagie.text.pdf.XfdfReader;

class TK_Session {

  boolean m_valid_b = false;
  boolean m_authorized_b = true;
  boolean m_input_pdf_readers_opened_b = false; // have m_input_pdf readers been opened?
  boolean m_verbose_reporting_b = false;
  boolean m_ask_about_warnings_b = pdftk.ASK_ABOUT_WARNINGS; // set default at compile-time

  // typedef unsigned long PageNumber;
  enum PageRotate {
    NORTH(0),
    EAST(90),
    SOUTH(180),
    WEST(270);
    final int value;

    PageRotate(int value) {
      this.value = value;
    }
  }; // DF rotation
  // typedef bool PageRotateAbsolute; // DF absolute / relative rotation

  static class InputPdf {
    String m_filename = "";
    String m_password = "";
    boolean m_authorized_b = true;

    // keep track of which pages get output under which readers,
    // because one reader mayn't output the same page twice;
    static class PagesReader {
      HashSet<Integer> first = new HashSet<Integer>();
      PdfReader second;

      PagesReader(PdfReader second) {
        this.second = second;
      }
    };

    ArrayList<PagesReader> m_readers = new ArrayList<PagesReader>();

    int m_num_pages = 0;
  };
  // pack input PDF in the order they're given on the command line
  ArrayList<InputPdf> m_input_pdf = new ArrayList<InputPdf>();
  // typedef vector< InputPdf >::size_type InputPdfIndex;

  // store input PDF handles here
  HashMap<String, Integer> m_input_pdf_index = new HashMap<String, Integer>();

  InputPdf.PagesReader add_reader(InputPdf input_pdf_p, boolean keep_artifacts_b) {
    boolean open_success_b = true;
    InputPdf.PagesReader pr = null;

    try {
      PdfReader reader = null;
      if (input_pdf_p.m_filename.equals("PROMPT")) {
        input_pdf_p.m_filename =
            pdftk.prompt_for_filename("Please enter a filename for an input PDF:");
      }
      if (input_pdf_p.m_password.isEmpty()) {
        reader = new PdfReader(input_pdf_p.m_filename);
      } else {
        if (input_pdf_p.m_password.equals("PROMPT")) {
          input_pdf_p.m_password =
              pdftk.prompt_for_password("open", "the input PDF:\n   " + input_pdf_p.m_filename);
        }

        byte[] password =
            passwords.utf8_password_to_pdfdoc(
                input_pdf_p.m_password, false); // allow user to enter greatest selection of chars

        if (password != null) {
          reader = new PdfReader(input_pdf_p.m_filename, password);
          if (reader == null) {
            System.err.println("Error: Unexpected null from open_reader()");
            return null; // <--- return
          }
        } else { // bad password
          System.err.println("Error: Password used to decrypt input PDF:");
          System.err.println("   " + input_pdf_p.m_filename);
          System.err.println("   includes invalid characters.");
          return null; // <--- return
        }
      }

      if (!keep_artifacts_b) {
        // generally useful operations
        reader.consolidateNamedDestinations();
        reader.removeUnusedObjects();
        // reader->shuffleSubsetNames(); // changes the PDF subset names, but not the PostScript
        // font names
      }

      input_pdf_p.m_num_pages = reader.getNumberOfPages();

      // keep tally of which pages have been laid claim to in this reader;
      // when creating the final PDF, this tally will be decremented
      pr = new InputPdf.PagesReader(reader);
      input_pdf_p.m_readers.add(pr);

      input_pdf_p.m_authorized_b =
          true; // instead of:  ( !reader->encrypted || reader->ownerPasswordUsed );

      if (open_success_b && reader.encrypted && !reader.ownerPasswordUsed) {
        System.err.println("WARNING: The creator of the input PDF:");
        System.err.println("   " + input_pdf_p.m_filename);
        System.err.println(
            "   has set an owner password (which is not required to handle this PDF).");
        System.err.println("   You did not supply this password. Please respect any copyright.");
      }

      if (!input_pdf_p.m_authorized_b) {
        open_success_b = false;
      }
    } catch (IOException ioe_p) { // file open error
      if (ioe_p.getMessage().equals("Bad password")) {
        input_pdf_p.m_authorized_b = false;
      } else if (ioe_p.getMessage().indexOf("not found") != -1) {
        System.err.println("Error: Unable to find file.");
      } else { // unexpected error
        System.err.println("Error: Unexpected Exception in open_reader()");
        ioe_p.printStackTrace(); // debug
      }
      open_success_b = false;
    } catch (Throwable t_p) { // unexpected error
      System.err.println("Error: Unexpected Exception in open_reader()");
      t_p.printStackTrace(); // debug

      open_success_b = false;
    }

    if (!input_pdf_p.m_authorized_b && m_ask_about_warnings_b) {
      // prompt for a new password
      System.err.println("The password you supplied for the input PDF:");
      System.err.println("   " + input_pdf_p.m_filename);
      System.err.println("   did not work.  This PDF is encrypted, and you must supply the");
      System.err.println("   owner or the user password to open it. To quit, enter a blank");
      System.err.println("   password at the next prompt.");

      input_pdf_p.m_password =
          pdftk.prompt_for_password("open", "the input PDF:\n   " + input_pdf_p.m_filename);
      if (!input_pdf_p.m_password.isEmpty()) { // reset flags try again
        input_pdf_p.m_authorized_b = true;
        return (add_reader(input_pdf_p)); // <--- recurse, return
      }
    }

    // report
    if (!open_success_b) { // file open error
      System.err.println("Error: Failed to open PDF file: ");
      System.err.println("   " + input_pdf_p.m_filename);
      if (!input_pdf_p.m_authorized_b) {
        System.err.println("   OWNER OR USER PASSWORD REQUIRED, but not given (or incorrect)");
      }
    }

    // update session state
    m_authorized_b = m_authorized_b && input_pdf_p.m_authorized_b;

    return open_success_b ? pr : null;
  }

  InputPdf.PagesReader add_reader(InputPdf input_pdf_p) {
    return add_reader(input_pdf_p, false);
  }

  boolean open_input_pdf_readers() {
    // try opening the input files and init m_input_pdf readers
    boolean open_success_b = true;

    if (!m_input_pdf_readers_opened_b) {
      if (m_operation == keyword.filter_k && m_input_pdf.size() == 1) {
        // don't touch input pdf -- preserve artifacts
        open_success_b = (add_reader(m_input_pdf.get(0), true) != null);
      } else {
        for (InputPdf it : m_input_pdf) {
          open_success_b = (add_reader(it) != null) && open_success_b;
        }
      }
      m_input_pdf_readers_opened_b = open_success_b;
    }

    return open_success_b;
  }

  ArrayList<String> m_input_attach_file_filename = new ArrayList<String>();
  int m_input_attach_file_pagenum = 0;

  String m_update_info_filename = "";
  boolean m_update_info_utf8_b = false;
  String m_update_xmp_filename = "";

  enum keyword {
    none_k,

    // the operations
    cat_k, // combine pages from input PDFs into a single output
    shuffle_k, // like cat, but interleaves pages from input ranges
    burst_k, // split a single, input PDF into individual pages
    barcode_burst_k, // barcode_burst project
    filter_k, // apply 'filters' to a single, input PDF based on output args
    dump_data_k, // no PDF output
    dump_data_utf8_k,
    dump_data_fields_k,
    dump_data_fields_utf8_k,
    dump_data_annots_k,
    generate_fdf_k,
    unpack_files_k, // unpack files from input; no PDF output

    // these are treated the same as operations,
    // but they are processed using the filter operation
    fill_form_k, // read FDF file and fill PDF form fields
    attach_file_k, // attach files to output
    update_info_k,
    update_info_utf8_k, // if info isn't utf-8, it is encoded using xml entities
    update_xmp_k,
    background_k, // promoted from output option to operation in pdftk 1.10
    multibackground_k, // feature added by Bernhard R. Link <brlink@debian.org>, Johann Felix Soden
    // <johfel@gmx.de>
    stamp_k,
    multistamp_k, // feature added by Bernhard R. Link <brlink@debian.org>, Johann Felix Soden
    // <johfel@gmx.de>
    rotate_k, // rotate given pages as directed

    // optional attach_file argument
    attach_file_to_page_k,

    // cat page range keywords
    even_k,
    odd_k,

    output_k,

    // encryption & decryption
    input_pw_k,
    owner_pw_k,
    user_pw_k,
    user_perms_k,

    // output arg.s, only
    encrypt_40bit_k,
    encrypt_128bit_k,

    // user permissions
    perm_printing_k,
    perm_modify_contents_k,
    perm_copy_contents_k,
    perm_modify_annotations_k,
    perm_fillin_k,
    perm_screen_readers_k,
    perm_assembly_k,
    perm_degraded_printing_k,
    perm_all_k,

    // filters
    filt_uncompress_k,
    filt_compress_k,

    // forms
    flatten_k,
    need_appearances_k,
    drop_xfa_k,
    drop_xmp_k,
    keep_first_id_k,
    keep_final_id_k,

    // pdftk options
    verbose_k,
    dont_ask_k,
    do_ask_k,

    // page rotation
    rot_north_k,
    rot_east_k,
    rot_south_k,
    rot_west_k,
    rot_left_k,
    rot_right_k,
    rot_upside_down_k
  };

  static keyword is_keyword(String ss) {
    ss = ss.toLowerCase();

    // operations
    if (ss.equals("cat")) {
      return keyword.cat_k;
    } else if (ss.equals("shuffle")) {
      return keyword.shuffle_k;
    } else if (ss.equals("burst")) {
      return keyword.burst_k;
    } else if (ss.equals("filter")) {
      return keyword.filter_k;
    } else if (ss.equals("dump_data")
        || ss.equals("dumpdata")
        || ss.equals("data_dump")
        || ss.equals("datadump")) {
      return keyword.dump_data_k;
    } else if (ss.equals("dump_data_utf8")) {
      return keyword.dump_data_utf8_k;
    } else if (ss.equals("dump_data_fields")) {
      return keyword.dump_data_fields_k;
    } else if (ss.equals("dump_data_fields_utf8")) {
      return keyword.dump_data_fields_utf8_k;
    } else if (ss.equals("dump_data_annots")) {
      return keyword.dump_data_annots_k;
    } else if (ss.equals("generate_fdf")
        || ss.equals("fdfgen")
        || ss.equals("fdfdump")
        || ss.equals("dump_data_fields_fdf")) {
      return keyword.generate_fdf_k;
    } else if (ss.equals("fill_form") || ss.equals("fillform")) {
      return keyword.fill_form_k;
    } else if (ss.equals("attach_file") || ss.equals("attach_files") || ss.equals("attachfile")) {
      return keyword.attach_file_k;
    } else if (ss.equals("unpack_file") || ss.equals("unpack_files") || ss.equals("unpackfiles")) {
      return keyword.unpack_files_k;
    } else if (ss.equals("update_info") || ss.equals("updateinfo")) {
      return keyword.update_info_k;
    } else if (ss.equals("update_info_utf8") || ss.equals("updateinfoutf8")) {
      return keyword.update_info_utf8_k;
    }
    /* requires more testing and work
    else if( strcmp( ss_copy, "update_xmp" ) ||
             strcmp( ss_copy, "updatexmp" ) ) {
      return update_xmp_k;
    }
    */
    else if (ss.equals("background")) {
      // pdftk 1.10: making background an operation
      // (and preserving old behavior for backwards compatibility)
      return keyword.background_k;
    } else if (ss.equals("multibackground")) {
      return keyword.multibackground_k;
    } else if (ss.equals("multistamp")) {
      return keyword.multistamp_k;
    } else if (ss.equals("stamp")) {
      return keyword.stamp_k;
    } else if (ss.equals("rotate")) {
      return keyword.rotate_k;
    }

    // cat range keywords
    else if (ss.startsWith("even")) { // note: strncmp
      return keyword.even_k;
    } else if (ss.startsWith("odd")) { // note: strncmp
      return keyword.odd_k;
    }

    // file attachment option
    else if (ss.equals("to_page") || ss.equals("topage")) {
      return keyword.attach_file_to_page_k;
    } else if (ss.equals("output")) {
      return keyword.output_k;
    }

    // encryption & decryption; depends on context
    else if (ss.equals("owner_pw") || ss.equals("ownerpw")) {
      return keyword.owner_pw_k;
    } else if (ss.equals("user_pw") || ss.equals("userpw")) {
      return keyword.user_pw_k;
    } else if (ss.equals("input_pw") || ss.equals("inputpw")) {
      return keyword.input_pw_k;
    } else if (ss.equals("allow")) {
      return keyword.user_perms_k;
    }

    // expect these only in output section
    else if (ss.equals("encrypt_40bit")
        || ss.equals("encrypt_40bits")
        || ss.equals("encrypt40bit")
        || ss.equals("encrypt40bits")
        || ss.equals("encrypt40_bit")
        || ss.equals("encrypt40_bits")
        || ss.equals("encrypt_40_bit")
        || ss.equals("encrypt_40_bits")) {
      return keyword.encrypt_40bit_k;
    } else if (ss.equals("encrypt_128bit")
        || ss.equals("encrypt_128bits")
        || ss.equals("encrypt128bit")
        || ss.equals("encrypt128bits")
        || ss.equals("encrypt128_bit")
        || ss.equals("encrypt128_bits")
        || ss.equals("encrypt_128_bit")
        || ss.equals("encrypt_128_bits")) {
      return keyword.encrypt_128bit_k;
    }

    // user permissions; must follow user_perms_k;
    else if (ss.equals("printing")) {
      return keyword.perm_printing_k;
    } else if (ss.equals("modifycontents")) {
      return keyword.perm_modify_contents_k;
    } else if (ss.equals("copycontents")) {
      return keyword.perm_copy_contents_k;
    } else if (ss.equals("modifyannotations")) {
      return keyword.perm_modify_annotations_k;
    } else if (ss.equals("fillin")) {
      return keyword.perm_fillin_k;
    } else if (ss.equals("screenreaders")) {
      return keyword.perm_screen_readers_k;
    } else if (ss.equals("assembly")) {
      return keyword.perm_assembly_k;
    } else if (ss.equals("degradedprinting")) {
      return keyword.perm_degraded_printing_k;
    } else if (ss.equals("allfeatures")) {
      return keyword.perm_all_k;
    } else if (ss.equals("uncompress")) {
      return keyword.filt_uncompress_k;
    } else if (ss.equals("compress")) {
      return keyword.filt_compress_k;
    } else if (ss.equals("flatten")) {
      return keyword.flatten_k;
    } else if (ss.equals("need_appearances")) {
      return keyword.need_appearances_k;
    } else if (ss.equals("drop_xfa")) {
      return keyword.drop_xfa_k;
    } else if (ss.equals("drop_xmp")) {
      return keyword.drop_xmp_k;
    } else if (ss.equals("keep_first_id")) {
      return keyword.keep_first_id_k;
    } else if (ss.equals("keep_final_id")) {
      return keyword.keep_final_id_k;
    } else if (ss.equals("verbose")) {
      return keyword.verbose_k;
    } else if (ss.equals("dont_ask") || ss.equals("dontask")) {
      return keyword.dont_ask_k;
    } else if (ss.equals("do_ask")) {
      return keyword.do_ask_k;
    }

    // more cat range keywords
    else if (ss.equals("north")) {
      return keyword.rot_north_k;
    } else if (ss.equals("south")) {
      return keyword.rot_south_k;
    } else if (ss.equals("east")) {
      return keyword.rot_east_k;
    } else if (ss.equals("west")) {
      return keyword.rot_west_k;
    } else if (ss.equals("left")) {
      return keyword.rot_left_k;
    } else if (ss.equals("right")) {
      return keyword.rot_right_k;
    } else if (ss.equals("down")) {
      return keyword.rot_upside_down_k;
    }

    return keyword.none_k;
  }

  static keyword consume_keyword(StringBuilder ssb) {
    String ss = new String(ssb).toLowerCase();
    // cat range keywords
    if (ss.startsWith("even")) { // note: strncmp
      ssb.delete(0, 4);
      return keyword.even_k;
    } else if (ss.startsWith("odd")) { // note: strncmp
      ssb.delete(0, 3);
      return keyword.odd_k;
    } else {
      ssb.setLength(0);
      return is_keyword(ss);
    }
  }

  keyword m_operation = keyword.none_k;

  class PageRef {
    int m_input_pdf_index;
    int m_page_num; // 1-based
    PageRotate m_page_rot; // DF rotation
    boolean m_page_abs; // DF absolute / relative rotation

    PageRef(int input_pdf_index, int page_num) {
      m_input_pdf_index = input_pdf_index;
      m_page_num = page_num;
      m_page_rot = PageRotate.NORTH;
      m_page_abs = false;
    }

    PageRef(int input_pdf_index, int page_num, PageRotate page_rot, boolean page_abs) {
      m_input_pdf_index = input_pdf_index;
      m_page_num = page_num;
      m_page_rot = page_rot;
      m_page_abs = page_abs;
    }
  };

  ArrayList<ArrayList<PageRef>> m_page_seq =
      new ArrayList<ArrayList<PageRef>>(); // one vector for each given page range

  String m_form_data_filename = "";
  String m_background_filename = "";
  String m_stamp_filename = "";
  String m_output_filename = "";
  boolean m_output_utf8_b = false;
  String m_output_owner_pw = "";
  String m_output_user_pw = "";
  int m_output_user_perms = 0;
  boolean m_multistamp_b = false; // use all pages of input stamp PDF, not just the first
  boolean m_multibackground_b = false; // use all pages of input background PDF, not just the first
  boolean m_output_uncompress_b = false;
  boolean m_output_compress_b = false;
  boolean m_output_flatten_b = false;
  boolean m_output_need_appearances_b = false;
  boolean m_output_drop_xfa_b = false;
  boolean m_output_drop_xmp_b = false;
  boolean m_output_keep_first_id_b = false;
  boolean m_output_keep_final_id_b = false;
  boolean m_cat_full_pdfs_b = true; // we are merging entire docs, not select pages

  enum encryption_strength {
    none_enc,
    bits40_enc,
    bits128_enc
  };

  encryption_strength m_output_encryption_strength = encryption_strength.none_enc;

  TK_Session(String[] args) {
    ArgState arg_state = ArgState.input_files_e;

    // set one and only one to true when p/w used; use to
    // enforce rule that either all p/w use handles or no p/w use handles
    boolean password_using_handles_not_b = false;
    boolean password_using_handles_b = false;

    int password_input_pdf_index = 0;

    boolean fail_b = false;

    // first, look for our "dont_ask" or "do_ask" keywords, since this
    // setting must be known before we begin opening documents, etc.
    for (String argv : args) {
      keyword kw = is_keyword(argv);
      if (kw == keyword.dont_ask_k) {
        m_ask_about_warnings_b = false;
      } else if (kw == keyword.do_ask_k) {
        m_ask_about_warnings_b = true;
      }
    }

    // iterate over cmd line arguments
    for (String argv : args) {

      if (fail_b || arg_state == ArgState.done_e) break;
      keyword arg_keyword = is_keyword(argv);

      // these keywords can be false hits because of their loose matching requirements;
      // since they are suffixes to page ranges, their appearance here is most likely a false match;
      if (arg_keyword == keyword.even_k || arg_keyword == keyword.odd_k) {
        arg_keyword = keyword.none_k;
      }

      switch (arg_state) {
        case input_files_e:
        case input_pw_e:
          {
            // look for keywords that would advance our state,
            // and then handle the specifics of the above cases

            if (arg_keyword == keyword.input_pw_k) { // input PDF passwords keyword

              arg_state = ArgState.input_pw_e;
            } else if (arg_keyword == keyword.cat_k) {
              m_operation = keyword.cat_k;
              arg_state = ArgState.page_seq_e; // collect page sequeces
            } else if (arg_keyword == keyword.shuffle_k) {
              m_operation = keyword.shuffle_k;
              arg_state = ArgState.page_seq_e; // collect page sequeces
            } else if (arg_keyword == keyword.burst_k) {
              m_operation = keyword.burst_k;
              arg_state = ArgState.output_args_e; // makes "output <fn>" bit optional
            } else if (arg_keyword == keyword.filter_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.output_e; // look for an output filename
            } else if (arg_keyword == keyword.dump_data_k) {
              m_operation = keyword.dump_data_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_utf8_k) {
              m_operation = keyword.dump_data_k;
              m_output_utf8_b = true;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_fields_k) {
              m_operation = keyword.dump_data_fields_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_fields_utf8_k) {
              m_operation = keyword.dump_data_fields_k;
              m_output_utf8_b = true;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_k) {
              m_operation = keyword.dump_data_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_annots_k) {
              m_operation = keyword.dump_data_annots_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.generate_fdf_k) {
              m_operation = keyword.generate_fdf_k;
              m_output_utf8_b = true;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.fill_form_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.form_data_filename_e; // look for an FDF filename
            } else if (arg_keyword == keyword.attach_file_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.attach_file_filename_e;
            } else if (arg_keyword == keyword.attach_file_to_page_k) {
              arg_state = ArgState.attach_file_pagenum_e;
            } else if (arg_keyword == keyword.unpack_files_k) {
              m_operation = keyword.unpack_files_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.update_info_k) {
              m_operation = keyword.filter_k;
              m_update_info_utf8_b = false;
              arg_state = ArgState.update_info_filename_e;
            } else if (arg_keyword == keyword.update_info_utf8_k) {
              m_operation = keyword.filter_k;
              m_update_info_utf8_b = true;
              arg_state = ArgState.update_info_filename_e;
            }
            /*
            else if( arg_keyword== update_xmp_k ) {
              m_operation= filter_k;
              arg_state= update_xmp_filename_e;
            }
            */
            else if (arg_keyword == keyword.background_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.background_filename_e;
            } else if (arg_keyword == keyword.multibackground_k) {
              m_operation = keyword.filter_k;
              m_multibackground_b = true;
              arg_state = ArgState.background_filename_e;
            } else if (arg_keyword == keyword.stamp_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.stamp_filename_e;
            } else if (arg_keyword == keyword.multistamp_k) {
              m_operation = keyword.filter_k;
              m_multistamp_b = true;
              arg_state = ArgState.stamp_filename_e;
            } else if (arg_keyword == keyword.rotate_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.page_seq_e; // collect page sequeces
            } else if (arg_keyword == keyword.output_k) { // we reached the output section
              arg_state = ArgState.output_filename_e;
            } else if (arg_keyword == keyword.none_k) {
              // here is where the two cases (input_files_e, input_pw_e) diverge

              String handle, data;
              {
                Pattern p = Pattern.compile("(?:([A-Z]+)=)?(.*)");
                Matcher m = p.matcher(argv);
                m.matches();
                handle = m.group(1);
                data = m.group(2);
              }

              if (arg_state == ArgState.input_files_e) {
                // input_files_e:
                // expecting input handle=filename pairs, or
                // an input filename w/o a handle
                //
                // treat argv[ii] like an optional input handle and filename
                // like this: [<handle>=]<filename>

                InputPdf input_pdf = new InputPdf();
                input_pdf.m_filename = data;

                if (handle == null) { // no handle
                  m_input_pdf.add(input_pdf);
                } else { // use given handle for filename; test, first

                  // look up handle
                  Integer it = m_input_pdf_index.get(handle);
                  if (it != null) { // error: alreay in use

                    System.err.println("Error: Handle given here: ");
                    System.err.println("      " + argv);
                    System.err.println("   is already associated with: ");
                    System.err.println("      " + m_input_pdf.get(it).m_filename);
                    System.err.println("   Exiting.");
                    fail_b = true;
                  } else { // add handle/filename association
                    m_input_pdf.add(input_pdf);
                    m_input_pdf_index.put(handle, m_input_pdf.size() - 1);
                  }
                }
              } // end: arg_state== input_files_e
              else if (arg_state == ArgState.input_pw_e) {
                // expecting input handle=password pairs, or
                // an input PDF password w/o a handle
                //
                // treat argv[ii] like an input handle and password
                // like this <handle>=<password>; if no handle is
                // given, assign passwords to input in order;

                // if handles not used for input PDFs, then assume
                // any equals signs found in p/w are part of p/w
                if (m_input_pdf_index.size() == 0) {
                  handle = "";
                  data = argv;
                }

                if (handle.isEmpty()) { // no equal sign; try using default handles
                  if (password_using_handles_b) { // error: expected a handle

                    System.err.println("Error: Expected a user-supplied handle for this input");
                    System.err.println("   PDF password: " + argv);
                    System.err.println();
                    System.err.println("   Handles must be supplied with ~all~ input");
                    System.err.println("   PDF passwords, or with ~no~ input PDF passwords.");
                    System.err.println("   If no handles are supplied, then passwords are applied");
                    System.err.println("   according to input PDF order.");
                    System.err.println();
                    System.err.println("   Handles are given like this: <handle>=<password>, and");
                    System.err.println(
                        "   they must be single, upper case letters, like: A, B, etc.");
                    fail_b = true;
                  } else {
                    password_using_handles_not_b = true;

                    if (password_input_pdf_index < m_input_pdf.size()) {
                      m_input_pdf.get(password_input_pdf_index).m_password = argv;
                      ++password_input_pdf_index;
                    } else { // error
                      System.err.println("Error: more input passwords than input PDF documents.");
                      System.err.println("   Exiting.");
                      fail_b = true;
                    }
                  }
                } else { // handle given; use for password
                  if (password_using_handles_not_b) { // error; remark and set fail_b

                    System.err.println("Error: Expected ~no~ user-supplied handle for this input");
                    System.err.println("   PDF password: " + argv);
                    System.err.println();
                    System.err.println("   Handles must be supplied with ~all~ input");
                    System.err.println("   PDF passwords, or with ~no~ input PDF passwords.");
                    System.err.println("   If no handles are supplied, then passwords are applied");
                    System.err.println("   according to input PDF order.");
                    System.err.println();
                    System.err.println("   Handles are given like this: <handle>=<password>, and");
                    System.err.println(
                        "   they must be single, upper case letters, like: A, B, etc.");
                    fail_b = true;
                  } else {
                    password_using_handles_b = true;

                    // look up this handle
                    Integer it = m_input_pdf_index.get(handle);
                    if (it != null) { // found

                      if (m_input_pdf.get(it).m_password.isEmpty()) {
                        m_input_pdf.get(it).m_password = data; // set
                      } else { // error: password already given

                        System.err.println("Error: Handle given here: ");
                        System.err.println("      " + argv);
                        System.err.println("   is already associated with this password: ");
                        System.err.println("      " + m_input_pdf.get(it).m_password);
                        System.err.println("   Exiting.");
                        fail_b = true;
                      }
                    } else { // error: no input file matches this handle

                      System.err.println("Error: Password handle: " + argv);
                      System.err.println("   is not associated with an input PDF file.");
                      System.err.println("   Exiting.");
                      fail_b = true;
                    }
                  }
                }
              } // end: arg_state== input_pw_e
              else { // error
                System.err.println("Error: Internal error: unexpected arg_state.  Exiting.");
                fail_b = true;
              }
            } else { // error: unexpected keyword; remark and set fail_b
              System.err.println("Error: Unexpected command-line data: ");
              System.err.println("      " + argv);
              if (arg_state == ArgState.input_files_e) {
                System.err.println("   where we were expecting an input PDF filename,");
                System.err.println("   operation (e.g. \"cat\") or \"input_pw\".  Exiting.");
              } else {
                System.err.println("   where we were expecting an input PDF password");
                System.err.println("   or operation (e.g. \"cat\").  Exiting.");
              }
              fail_b = true;
            }
          }
          break;

        case page_seq_e:
          {
            if (m_page_seq.isEmpty()) {
              // we just got here; validate input filenames

              if (m_input_pdf.isEmpty()) { // error; remark and set fail_b
                System.err.println("Error: No input files.  Exiting.");
                fail_b = true;
                break;
              }

              // try opening input PDF readers
              if (!open_input_pdf_readers()) { // failure
                fail_b = true;
                break;
              }
            } // end: first pass init. pdf files

            if (arg_keyword == keyword.output_k) {
              arg_state = ArgState.output_filename_e; // advance state
            } else if (arg_keyword == keyword.none_k) { // treat argv[ii] like a page sequence

              boolean even_pages_b = false;
              boolean odd_pages_b = false;

              Pattern p = Pattern.compile("([A-Z]*)(r?)(end|[0-9]*)(-(r?)(end|[0-9]*))?(.*)");
              Matcher m = p.matcher(argv);
              m.matches();
              String handle = m.group(1);
              String pre_reverse = m.group(2);
              String pre_range = m.group(3);
              String hyphen = m.group(4);
              String post_reverse = m.group(5);
              String post_range = m.group(6);
              String keywords = m.group(7);

              int range_pdf_index = 0;
              { // defaults to first input document
                if (!handle.isEmpty()) {
                  // validate handle
                  Integer it = m_input_pdf_index.get(handle);
                  if (it == null) { // error

                    System.err.println("Error: Given handle has no associated file: ");
                    System.err.println("   " + handle + ", used here: " + argv);
                    System.err.println("   Exiting.");
                    fail_b = true;
                    break;
                  } else {
                    range_pdf_index = it;
                  }
                }
              }

              PageRange page_num =
                  new PageRange(m_input_pdf.get(range_pdf_index).m_num_pages, argv);
              if (!page_num.parse(pre_reverse, pre_range, post_reverse, post_range)) {
                fail_b = true;
                break;
              }

              // DF declare rotate vars
              PageRotate page_rotate = PageRotate.NORTH;
              boolean page_rotate_absolute = false;

              StringBuilder trailing_keywords = new StringBuilder(keywords);
              // trailing keywords (excluding "end" which should have been handled above)
              while (trailing_keywords.length()
                  > 0) { // possibly more than one keyword, e.g., 3-endevenwest

                // read keyword
                arg_keyword = consume_keyword(trailing_keywords);

                if (arg_keyword == keyword.even_k) {
                  even_pages_b = true;
                } else if (arg_keyword == keyword.odd_k) {
                  odd_pages_b = true;
                } else if (arg_keyword == keyword.rot_north_k) {
                  page_rotate = PageRotate.NORTH; // rotate 0
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_east_k) {
                  page_rotate = PageRotate.EAST; // rotate 90
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_south_k) {
                  page_rotate = PageRotate.SOUTH; // rotate 180
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_west_k) {
                  page_rotate = PageRotate.WEST; // rotate 270
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_left_k) {
                  page_rotate = PageRotate.WEST; // rotate -90
                  page_rotate_absolute = false;
                } else if (arg_keyword == keyword.rot_right_k) {
                  page_rotate = PageRotate.EAST; // rotate +90
                  page_rotate_absolute = false;
                } else if (arg_keyword == keyword.rot_upside_down_k) {
                  page_rotate = PageRotate.SOUTH; // rotate +180
                  page_rotate_absolute = false;
                } else { // error
                  System.err.println("Error: Unexpected text in page range end, here: ");
                  System.err.println("   " + argv /*(argv[ii]+ jj)*/);
                  System.err.println("   Exiting.");
                  System.err.println("   Acceptable keywords, for example: \"even\" or \"odd\".");
                  System.err.println("   To rotate pages, use: \"north\" \"south\" \"east\"");
                  System.err.println("       \"west\" \"left\" \"right\" or \"down\"");
                  fail_b = true;
                  break;
                }
              }

              ////
              // pack this range into our m_page_seq;

              if (page_num.beg == 0 && page_num.end == 0) { // ref the entire document
                page_num.beg = 1;
                page_num.end = m_input_pdf.get(range_pdf_index).m_num_pages;

                // test that it's a /full/ pdf
                m_cat_full_pdfs_b = m_cat_full_pdfs_b && (!even_pages_b && !odd_pages_b);
              } else if (page_num.beg == 0 || page_num.end == 0) { // error
                System.err.println("Error: Input page numbers include 0 (zero)");
                System.err.println("   The first PDF page is 1 (one)");
                System.err.println("   Exiting.");
                fail_b = true;
                break;
              } else // the user specified select pages
              m_cat_full_pdfs_b = false;

              ArrayList<PageRef> temp_page_seq = new ArrayList<PageRef>();
              boolean reverse_sequence_b = (page_num.end < page_num.beg);
              if (reverse_sequence_b) { // swap
                int temp = page_num.end;
                page_num.end = page_num.beg;
                page_num.beg = temp;
              }

              for (int kk = page_num.beg; kk <= page_num.end; ++kk) {
                if ((!even_pages_b || ((kk % 2) == 0)) && (!odd_pages_b || ((kk % 2) == 1))) {
                  if (kk <= m_input_pdf.get(range_pdf_index).m_num_pages) {

                    // look to see if this page of this document
                    // has already been referenced; if it has,
                    // create a new reader; associate this page
                    // with a reader;
                    //
                    boolean associated = false;
                    for (InputPdf.PagesReader it : m_input_pdf.get(range_pdf_index).m_readers) {
                      if (!it.first.contains(kk)) { // kk not assoc. w/ this reader
                        it.first.add(kk); // create association
                        associated = true;
                        break;
                      }
                    }
                    //
                    if (!associated) {
                      // need to create a new reader for kk
                      InputPdf.PagesReader new_reader =
                          add_reader(m_input_pdf.get(range_pdf_index));
                      if (new_reader != null) {
                        new_reader.first.add(kk);
                      } else {
                        System.err.println("Internal Error: unable to add reader");
                        fail_b = true;
                        break;
                      }
                    }

                    //
                    temp_page_seq.add(
                        new PageRef(
                            range_pdf_index, kk, page_rotate, page_rotate_absolute)); // DF rotate

                  } else { // error; break later to get most feedback
                    System.err.println("Error: Page number: " + kk);
                    System.err.println(
                        "   does not exist in file: "
                            + m_input_pdf.get(range_pdf_index).m_filename);
                    fail_b = true;
                  }
                }
              }
              if (fail_b) break;

              if (reverse_sequence_b) {
                Collections.reverse(temp_page_seq);
              }

              m_page_seq.add(temp_page_seq);

            } else { // error
              System.err.println("Error: expecting page ranges.  Instead, I got:");
              System.err.println("   " + argv);
              fail_b = true;
              break;
            }
          }
          break;

        case form_data_filename_e:
          {
            if (arg_keyword == keyword.none_k) { // treat argv[ii] like an FDF file filename

              if (m_form_data_filename.isEmpty()) {
                m_form_data_filename = argv;
              } else { // error
                System.err.println("Error: Multiple fill_form filenames given: ");
                System.err.println("   " + m_form_data_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }

              // advance state
              arg_state = ArgState.output_e; // look for an output filename
            } else { // error
              System.err.println("Error: expecting a form data filename,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }
          } // end: case form_data_filename_e
          break;

        case attach_file_filename_e:
          {
            // keep packing filenames until we reach an expected keyword

            if (arg_keyword == keyword.attach_file_to_page_k) {
              arg_state = ArgState.attach_file_pagenum_e; // advance state
            } else if (arg_keyword == keyword.output_k) {
              arg_state = ArgState.output_filename_e; // advance state
            } else if (arg_keyword == keyword.none_k) {
              // pack argv[ii] into our list of attachment filenames
              m_input_attach_file_filename.add(argv);
            } else { // error
              System.err.println("Error: expecting an attachment filename,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }
          }
          break;

        case attach_file_pagenum_e:
          {
            if (argv.equals("PROMPT")) { // query the user, later
              m_input_attach_file_pagenum = -1;
            } else if (argv.equals("end")) { // attach to the final page
              m_input_attach_file_pagenum = -2;
            } else {
              try {
                m_input_attach_file_pagenum = Integer.parseInt(argv);
              } catch (NumberFormatException e) { // error
                System.err.println("Error: expecting a (1-based) page number.  Instead, I got:");
                System.err.println("   " + argv);
                System.err.println("Exiting.");
                fail_b = true;
              }
            }

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          } // end: case attach_file_pagenum_e
          break;

        case update_info_filename_e:
          {
            if (arg_keyword == keyword.none_k) {
              if (m_update_info_filename.isEmpty()) {
                m_update_info_filename = argv;
              } else { // error
                System.err.println("Error: Multiple update_info filenames given: ");
                System.err.println("   " + m_update_info_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error
              System.err.println("Error: expecting an INFO file filename,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          } // end: case update_info_filename_e
          break;

          /*
          case update_xmp_filename_e : {
            if( arg_keyword== none_k ) {
                if( m_update_xmp_filename.empty() ) {
                  m_update_xmp_filename= argv[ii];
                }
                else { // error
                  cerr << "Error: Multiple update_xmp filenames given: " << endl;
                  cerr << "   " << m_update_xmp_filename << " and " << argv[ii] << endl;
                  cerr << "Exiting." << endl;
                  fail_b= true;
                  break;
                }
              }
            else { // error
              cerr << "Error: expecting an XMP file filename," << endl;
              cerr << "   instead I got this keyword: " << argv[ii] << endl;
              cerr << "Exiting." << endl;
              fail_b= true;
              break;
            }

            // advance state
            arg_state= output_e; // look for an output filename

          } // end: case update_xmp_filename_e
          break;
          */

        case output_e:
          {
            if (m_input_pdf.isEmpty()) { // error; remark and set fail_b
              System.err.println("Error: No input files.  Exiting.");
              fail_b = true;
              break;
            }

            if (arg_keyword == keyword.output_k) {
              arg_state = ArgState.output_filename_e; // advance state
            } else { // error
              System.err.println("Error: expecting \"output\" keyword.  Instead, I got:");
              System.err.println("   " + argv);
              fail_b = true;
              break;
            }
          }
          break;

        case output_filename_e:
          {
            // we have closed all possible input operations and arguments;
            // see if we should perform any default action based on the input state
            //
            if (m_operation == keyword.none_k) {
              if (1 < m_input_pdf.size()) {
                // no operation given for multiple input PDF, so combine them
                m_operation = keyword.cat_k;
              } else {
                m_operation = keyword.filter_k;
              }
            }

            // try opening input PDF readers (in case they aren't already)
            if (!open_input_pdf_readers()) { // failure
              fail_b = true;
              break;
            }

            if ((m_operation == keyword.cat_k || m_operation == keyword.shuffle_k)) {
              if (m_page_seq.isEmpty()) {
                // combining pages, but no sequences given; merge all input PDFs in order
                for (int ii = 0; ii < m_input_pdf.size(); ++ii) {
                  InputPdf input_pdf = m_input_pdf.get(ii);

                  ArrayList<PageRef> temp_page_seq = new ArrayList<PageRef>();
                  for (int jj = 1; jj <= input_pdf.m_num_pages; ++jj) {
                    temp_page_seq.add(new PageRef(ii, jj)); // DF rotate
                    input_pdf
                        .m_readers
                        .get(input_pdf.m_readers.size() - 1)
                        .first
                        .add(jj); // create association
                  }
                  m_page_seq.add(temp_page_seq);
                }
              }
              /* no longer necessary -- are upstream testing is smarter
              else { // page ranges or docs (e.g. A B A) were given
                m_cat_full_pdfs_b= false; // TODO: handle cat A B A case for bookmarks
              }
              */
            }

            if (m_output_filename.isEmpty()) {
              m_output_filename = argv;

              if (!m_output_filename.equals(
                  "-")) { // input and output may both be "-" (stdin and stdout)
                // simple-minded test to see if output matches an input filename
                for (InputPdf it : m_input_pdf) {
                  if (it.m_filename.equals(m_output_filename)) {
                    System.err.println("Error: The given output filename: " + m_output_filename);
                    System.err.println("   matches an input filename.  Exiting.");
                    fail_b = true;
                    break;
                  }
                }
              }
            } else { // error
              System.err.println("Error: Multiple output filenames given: ");
              System.err.println("   " + m_output_filename + " and " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // advance state
            arg_state = ArgState.output_args_e;
          }
          break;

        case output_args_e:
          {
            // output args are order-independent but must follow "output <fn>", if present;
            // we are expecting any of these keywords:
            // owner_pw_k, user_pw_k, user_perms_k ...
            // added output_k case in pdftk 1.10; this permits softer "output <fn>" enforcement
            //

            ArgStateMutable arg_state_m = new ArgStateMutable();
            arg_state_m.value = arg_state;
            if (handle_some_output_options(arg_keyword, arg_state_m)) {
              arg_state = arg_state_m.value;
            } else {
              System.err.println("Error: Unexpected data in output section: ");
              System.err.println("      " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }
          }
          break;

        case output_owner_pw_e:
          {
            if (m_output_owner_pw.isEmpty()) {

              if (argv.equals("PROMPT") || !argv.equals(m_output_user_pw)) {
                m_output_owner_pw = argv;
              } else { // error: identical user and owner password
                // are interpreted by Acrobat (per the spec.) that
                // the doc has no owner password
                System.err.println("Error: The user and owner passwords are the same.");
                System.err.println("   PDF Viewers interpret this to mean your PDF has");
                System.err.println("   no owner password, so they must be different.");
                System.err.println("   Or, supply no owner password to pdftk if this is");
                System.err.println("   what you desire.");
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error: we already have an output owner pw
              System.err.println("Error: Multiple output owner passwords given: ");
              System.err.println("   " + m_output_owner_pw + " and " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // revert state
            arg_state = ArgState.output_args_e;
          }
          break;

        case output_user_pw_e:
          {
            if (m_output_user_pw.isEmpty()) {
              if (argv.equals("PROMPT") || !m_output_owner_pw.equals(argv)) {
                m_output_user_pw = argv;
              } else { // error: identical user and owner password
                // are interpreted by Acrobat (per the spec.) that
                // the doc has no owner password
                System.err.println("Error: The user and owner passwords are the same.");
                System.err.println("   PDF Viewers interpret this to mean your PDF has");
                System.err.println("   no owner password, so they must be different.");
                System.err.println("   Or, supply no owner password to pdftk if this is");
                System.err.println("   what you desire.");
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error: we already have an output user pw
              System.err.println("Error: Multiple output user passwords given: ");
              System.err.println("   " + m_output_user_pw + " and " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // revert state
            arg_state = ArgState.output_args_e;
          }
          break;

        case output_user_perms_e:
          {

            // we may be given any number of permission arguments,
            // so keep an eye out for other, state-altering keywords
            ArgStateMutable arg_state_m = new ArgStateMutable();
            arg_state_m.value = arg_state;
            if (handle_some_output_options(arg_keyword, arg_state_m)) {
              arg_state = arg_state_m.value;
              break;
            }

            switch (arg_keyword) {

                // possible permissions
              case perm_printing_k:
                // if both perm_printing_k and perm_degraded_printing_k
                // are given, then perm_printing_k wins;
                m_output_user_perms |= PdfWriter.AllowPrinting;
                break;
              case perm_modify_contents_k:
                // Acrobat 5 and 6 don't set both bits, even though
                // they both respect AllowModifyContents --> AllowAssembly;
                // so, no harm in this;
                m_output_user_perms |= (PdfWriter.AllowModifyContents | PdfWriter.AllowAssembly);
                break;
              case perm_copy_contents_k:
                // Acrobat 5 _does_ allow the user to allow copying contents
                // yet hold back screen reader perms; this is counter-intuitive,
                // and Acrobat 6 does not allow Copy w/o SceenReaders;
                m_output_user_perms |= (PdfWriter.AllowCopy | PdfWriter.AllowScreenReaders);
                break;
              case perm_modify_annotations_k:
                m_output_user_perms |= (PdfWriter.AllowModifyAnnotations | PdfWriter.AllowFillIn);
                break;
              case perm_fillin_k:
                m_output_user_perms |= PdfWriter.AllowFillIn;
                break;
              case perm_screen_readers_k:
                m_output_user_perms |= PdfWriter.AllowScreenReaders;
                break;
              case perm_assembly_k:
                m_output_user_perms |= PdfWriter.AllowAssembly;
                break;
              case perm_degraded_printing_k:
                m_output_user_perms |= PdfWriter.AllowDegradedPrinting;
                break;
              case perm_all_k:
                m_output_user_perms =
                    (PdfWriter.AllowPrinting
                        | // top quality printing
                        PdfWriter.AllowModifyContents
                        | PdfWriter.AllowCopy
                        | PdfWriter.AllowModifyAnnotations
                        | PdfWriter.AllowFillIn
                        | PdfWriter.AllowScreenReaders
                        | PdfWriter.AllowAssembly);
                break;

              default: // error: unexpected matter
                System.err.println("Error: Unexpected data in output section: ");
                System.err.println("      " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
            }
          }
          break;

        case background_filename_e:
          {
            if (arg_keyword == keyword.none_k) {
              if (m_background_filename.isEmpty()) {
                m_background_filename = argv;
              } else { // error
                System.err.println("Error: Multiple background filenames given: ");
                System.err.println("   " + m_background_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error
              System.err.println("Error: expecting a PDF filename for background operation,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // revert state
            // this is more liberal than used with other operations, since we want
            // to preserve backward-compatibility with pdftk 1.00 where "background"
            // was documented as an output option; in pdftk 1.10 we changed it to
            // an operation
            arg_state = ArgState.output_args_e;
          }
          break;

        case stamp_filename_e:
          {
            if (arg_keyword == keyword.none_k) {
              if (m_stamp_filename.isEmpty()) {
                m_stamp_filename = argv;
              } else { // error
                System.err.println("Error: Multiple stamp filenames given: ");
                System.err.println("   " + m_stamp_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error
              System.err.println("Error: expecting a PDF filename for stamp operation,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          }
          break;

        default:
          { // error
            System.err.println("Internal Error: Unexpected arg_state.  Exiting.");
            fail_b = true;
            break;
          }
      } // end: switch(arg_state)
    } // end: iterate over command-line arguments

    if (fail_b) {
      System.err.println("Errors encountered.  No output created.");
      m_valid_b = false;

      m_input_pdf.clear();

      // preserve other data members for diagnostic dump
    } else {
      m_valid_b = true;

      if (!m_input_pdf_readers_opened_b) {
        open_input_pdf_readers();
      }
    }
  }

  boolean is_valid() {
    return (m_valid_b
        && (m_operation == keyword.dump_data_k
            || m_operation == keyword.dump_data_fields_k
            || m_operation == keyword.dump_data_annots_k
            || m_operation == keyword.generate_fdf_k
            || m_authorized_b)
        && !m_input_pdf.isEmpty()
        && m_input_pdf_readers_opened_b
        && (m_operation == keyword.cat_k
            || m_operation == keyword.shuffle_k
            || m_operation == keyword.burst_k
            || m_operation == keyword.filter_k
            || m_operation == keyword.dump_data_k
            || m_operation == keyword.dump_data_utf8_k
            || m_operation == keyword.dump_data_fields_k
            || m_operation == keyword.dump_data_fields_utf8_k
            || m_operation == keyword.dump_data_annots_k
            || m_operation == keyword.generate_fdf_k
            || m_operation == keyword.unpack_files_k)
        &&

        // these op.s require a single input PDF file
        (!(m_operation == keyword.burst_k || m_operation == keyword.filter_k)
            || (m_input_pdf.size() == 1))
        &&

        // these op.s do not require an output filename
        (m_operation == keyword.burst_k
            || m_operation == keyword.dump_data_k
            || m_operation == keyword.dump_data_fields_k
            || m_operation == keyword.dump_data_annots_k
            || m_operation == keyword.generate_fdf_k
            || m_operation == keyword.unpack_files_k
            || !m_output_filename.isEmpty()));
  }

  void dump_session_data() {
    if (!m_verbose_reporting_b) return;

    if (!m_input_pdf_readers_opened_b) {
      System.out.println("Input PDF Open Errors");
      return;
    }

    //
    if (is_valid()) {
      System.out.println("Command Line Data is valid.");
    } else {
      System.out.println("Command Line Data is NOT valid.");
    }

    // input files
    System.out.println();
    System.out.println("Input PDF Filenames & Passwords in Order\n( <filename>[, <password>] ) ");
    if (m_input_pdf.isEmpty()) {
      System.out.println("   No input PDF filenames have been given.");
    } else {
      for (InputPdf it : m_input_pdf) {
        System.out.print("   " + it.m_filename);
        if (!it.m_password.isEmpty()) {
          System.out.print(", " + it.m_password);
        }

        if (!it.m_authorized_b) {
          System.out.print(", OWNER OR USER PASSWORD REQUIRED, but not given (or incorrect)");
        }

        System.out.println();
      }
    }

    // operation
    System.out.println();
    System.out.println("The operation to be performed: ");
    switch (m_operation) {
      case cat_k:
        System.out.println("   cat - Catenate given page ranges into a new PDF.");
        break;
      case shuffle_k:
        System.out.println("   shuffle - Interleave given page ranges into a new PDF.");
        break;
      case burst_k:
        System.out.println("   burst - Split a single, input PDF into individual pages.");
        break;
      case filter_k:
        System.out.println(
            "   filter - Apply 'filters' to a single, input PDF based on output args.");
        System.out.println("      (When the operation is omitted, this is the default.)");
        break;
      case dump_data_k:
        System.out.println("   dump_data - Report statistics on a single, input PDF.");
        break;
      case dump_data_fields_k:
        System.out.println("   dump_data_fields - Report form field data on a single, input PDF.");
        break;
      case dump_data_annots_k:
        System.out.println("   dump_data_annots - Report annotation data on a single, input PDF.");
        break;
      case generate_fdf_k:
        System.out.println("   generate_fdf - Generate a dummy FDF file from a PDF.");
        break;
      case unpack_files_k:
        System.out.println("   unpack_files - Copy PDF file attachments into given directory.");
        break;
      case none_k:
        System.out.println("   NONE - No operation has been given.  See usage instructions.");
        break;
      default:
        System.out.println("   INTERNAL ERROR - An unexpected operation has been given.");
        break;
    }

    // pages
    /*
    cout << endl;
    cout << "The following pages will be operated on, in the given order." << endl;
    if( m_page_seq.empty() ) {
      cout << "   No pages or page ranges have been given." << endl;
    }
    else {
      for( vector< PageRef >::const_iterator it= m_page_seq.begin();
           it!= m_page_seq.end(); ++it )
        {
          map< string, InputPdf >::const_iterator jt=
            m_input_pdf.find( it->m_handle );
          if( jt!= m_input_pdf.end() ) {
            cout << "   Handle: " << it->m_handle << "  File: " << jt->second.m_filename;
            cout << "  Page: " << it->m_page_num << endl;
          }
          else { // error
            cout << "   Internal Error: handle not found in m_input_pdf: " << it->m_handle << endl;
          }
        }
    }
    */

    // output file; may be PDF or text
    System.out.println();
    System.out.println("The output file will be named:");
    if (m_output_filename.isEmpty()) {
      System.out.println("   No output filename has been given.");
    } else {
      System.out.println("   " + m_output_filename);
    }

    // output encryption
    System.out.println();
    boolean output_encrypted_b =
        m_output_encryption_strength != encryption_strength.none_enc
            || !m_output_user_pw.isEmpty()
            || !m_output_owner_pw.isEmpty();

    System.out.println("Output PDF encryption settings:");
    if (output_encrypted_b) {
      System.out.println("   Output PDF will be encrypted.");

      switch (m_output_encryption_strength) {
        case none_enc:
          System.out.println("   Encryption strength not given. Defaulting to: 128 bits.");
          break;
        case bits40_enc:
          System.out.println("   Given output encryption strength: 40 bits");
          break;
        case bits128_enc:
          System.out.println("   Given output encryption strength: 128 bits");
          break;
      }

      System.out.println();
      {
        if (m_output_user_pw.isEmpty()) System.out.println("   No user password given.");
        else System.out.println("   Given user password: " + m_output_user_pw);
        if (m_output_owner_pw.isEmpty()) System.out.println("   No owner password given.");
        else System.out.println("   Given owner password: " + m_output_owner_pw);
        //
        // the printing section: Top Quality or Degraded, but not both;
        // AllowPrinting is a superset of both flag settings
        if ((m_output_user_perms & PdfWriter.AllowPrinting) == PdfWriter.AllowPrinting)
          System.out.println("   ALLOW Top Quality Printing");
        else if ((m_output_user_perms & PdfWriter.AllowPrinting) == PdfWriter.AllowDegradedPrinting)
          System.out.println("   ALLOW Degraded Printing (Top-Quality Printing NOT Allowed)");
        else System.out.println("   Printing NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowModifyContents) == PdfWriter.AllowModifyContents)
          System.out.println("   ALLOW Modifying of Contents");
        else System.out.println("   Modifying of Contents NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowCopy) == PdfWriter.AllowCopy)
          System.out.println("   ALLOW Copying of Contents");
        else System.out.println("   Copying of Contents NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowModifyAnnotations)
            == PdfWriter.AllowModifyAnnotations)
          System.out.println("   ALLOW Modifying of Annotations");
        else System.out.println("   Modifying of Annotations NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowFillIn) == PdfWriter.AllowFillIn)
          System.out.println("   ALLOW Fill-In");
        else System.out.println("   Fill-In NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowScreenReaders) == PdfWriter.AllowScreenReaders)
          System.out.println("   ALLOW Screen Readers");
        else System.out.println("   Screen Readers NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowAssembly) == PdfWriter.AllowAssembly)
          System.out.println("   ALLOW Assembly");
        else System.out.println("   Assembly NOT Allowed");
      }
    } else {
      System.out.println("   Output PDF will not be encrypted.");
    }

    // compression filter
    System.out.println();
    if (m_operation != keyword.filter_k
        || output_encrypted_b
        || !(m_output_compress_b || m_output_uncompress_b)) {
      System.out.println("No compression or uncompression being performed on output.");
    } else {
      if (m_output_compress_b) {
        System.out.println("Compression will be applied to some PDF streams.");
      } else {
        System.out.println("Some PDF streams will be uncompressed.");
      }
    }
  }

  void attach_files(PdfReader input_reader_p, PdfWriter writer_p) throws IOException {
    if (!m_input_attach_file_filename.isEmpty()) {

      if (m_input_attach_file_pagenum == -1) { // our signal to prompt the user for a pagenum
        System.out.println("Please enter the page number you want to attach these files to.");
        System.out.println("   The first page is 1.  The final page is \"end\".");
        System.out.println("   To attach files at the document level, just press Enter.");

        Scanner s = new Scanner(System.in);
        String buff = s.nextLine();
        if (buff.isEmpty()) { // attach to document
          m_input_attach_file_pagenum = 0;
        }
        if (buff.equals("end")) { // the final page
          m_input_attach_file_pagenum = input_reader_p.getNumberOfPages();
        } else {
          Pattern p = Pattern.compile("([0-9]*).*");
          Matcher m = p.matcher(buff);
          m.matches();
          try {
            m_input_attach_file_pagenum = Integer.valueOf(m.group(1));
          } catch (NumberFormatException e) {
            m_input_attach_file_pagenum = 0;
          }
        }
      } else if (m_input_attach_file_pagenum == -2) { // the final page ("end")
        m_input_attach_file_pagenum = input_reader_p.getNumberOfPages();
      }

      if (m_input_attach_file_pagenum != 0) { // attach to a page using annotations
        final int trans = 27;
        final int margin = 18;

        if (0 < m_input_attach_file_pagenum
            && m_input_attach_file_pagenum <= input_reader_p.getNumberOfPages()) {

          PdfDictionary page_p = input_reader_p.getPageN(m_input_attach_file_pagenum);
          if (page_p != null && page_p.isDictionary()) {

            Rectangle crop_box_p = input_reader_p.getCropBox(m_input_attach_file_pagenum);
            float corner_top = crop_box_p.top() - margin;
            float corner_left = crop_box_p.left() + margin;

            PdfObject annots_po = input_reader_p.getPdfObject(page_p.get(PdfName.ANNOTS));
            boolean annots_new_b = false;
            if (annots_po == null) { // create Annots array
              annots_po = new PdfArray();
              annots_new_b = true;
            }
            if (annots_po.isArray()) {
              // grab corner_top and corner_left from the bottom right of the newest annot
              PdfArray annots_p = (PdfArray) annots_po;
              ArrayList<PdfObject> annots_array_p = annots_p.getArrayList();
              for (PdfObject ii : annots_array_p) {
                PdfObject annot_p = input_reader_p.getPdfObject(ii);
                if (annot_p != null && annot_p.isDictionary()) {
                  PdfObject annot_bbox_p =
                      input_reader_p.getPdfObject(((PdfDictionary) annot_p).get(PdfName.RECT));
                  if (annot_bbox_p != null && annot_bbox_p.isArray()) {
                    ArrayList<PdfObject> bbox_array_p = ((PdfArray) annot_bbox_p).getArrayList();
                    if (bbox_array_p.size() == 4) {
                      corner_top = ((PdfNumber) bbox_array_p.get(1)).floatValue();
                      corner_left = ((PdfNumber) bbox_array_p.get(2)).floatValue();
                    }
                  }
                }
              }
              for (String vit : m_input_attach_file_filename) {
                if (vit.equals("PROMPT")) {
                  vit = pdftk.prompt_for_filename("Please enter a filename for attachment:");
                }

                String filename = attachments.drop_path(vit);

                // wrap our location over page bounds, if needed
                if (crop_box_p.right() < corner_left + trans) {
                  corner_left = crop_box_p.left() + margin;
                }
                if (corner_top - trans < crop_box_p.bottom()) {
                  corner_top = crop_box_p.top() - margin;
                }

                Rectangle annot_bbox_p =
                    new Rectangle(corner_left, corner_top - trans, corner_left + trans, corner_top);

                PdfAnnotation annot_p =
                    PdfAnnotation.createFileAttachment(
                        writer_p,
                        annot_bbox_p,
                        filename, // contents
                        null,
                        vit, // the file path
                        filename); // display name

                PdfIndirectReference ref_p = writer_p.addToBody(annot_p).getIndirectReference();

                annots_p.add(ref_p);

                // advance the location of our annotation
                corner_left += trans;
                corner_top -= trans;
              }
              if (annots_new_b) { // add new Annots array to page dict
                PdfIndirectReference ref_p = writer_p.addToBody(annots_p).getIndirectReference();
                page_p.put(PdfName.ANNOTS, ref_p);
              }
            }
          } else { // error
            System.err.println("Internal Error: unable to get page dictionary");
          }
        } else { // error
          System.err.print("Error: page number " + m_input_attach_file_pagenum);
          System.err.println(" is not present in the input PDF.");
        }
      } else { // attach to document using the EmbeddedFiles name tree
        PdfDictionary catalog_p = input_reader_p.catalog; // to top, Root dict
        if (catalog_p != null && catalog_p.isDictionary()) {

          // the Names dict
          PdfObject names_po = input_reader_p.getPdfObject(catalog_p.get(PdfName.NAMES));
          boolean names_new_b = false;
          if (names_po == null) { // create Names dict
            names_po = new PdfDictionary();
            names_new_b = true;
          }
          if (names_po != null && names_po.isDictionary()) {
            PdfDictionary names_p = (PdfDictionary) names_po;

            // the EmbeddedFiles name tree (ref. 1.5, sec. 3.8.5), which is a dict at top
            PdfObject emb_files_tree_p =
                input_reader_p.getPdfObject(names_p.get(PdfName.EMBEDDEDFILES));
            HashMap<String, PdfIndirectReference> emb_files_map_p = null;
            boolean emb_files_tree_new_b = false;
            if (emb_files_tree_p != null) { // read current name tree of attachments into a map
              emb_files_map_p = PdfNameTree.readTree((PdfDictionary) emb_files_tree_p);
            } else { // create material
              emb_files_map_p = new HashMap<String, PdfIndirectReference>();
              emb_files_tree_new_b = true;
            }

            ////
            // add matter to name tree

            for (String vit : m_input_attach_file_filename) {
              if (vit.equals("PROMPT")) {
                vit = pdftk.prompt_for_filename("Please enter a filename for attachment:");
              }

              String filename = attachments.drop_path(vit);

              PdfFileSpecification filespec_p = null;
              try {
                // create the file spec. from file
                filespec_p =
                    PdfFileSpecification.fileEmbedded(
                        writer_p, vit, // the file path
                        filename, // the display name
                        null);
              } catch (IOException ioe_p) { // file open error
                System.err.println("Error: Failed to open attachment file: ");
                System.err.println("   " + vit);
                System.err.println("   Skipping this file.");
                continue;
              }

              // add file spec. to PDF via indirect ref.
              PdfIndirectReference ref_p = writer_p.addToBody(filespec_p).getIndirectReference();

              // contruct a name, if necessary, to prevent possible key collision on the name tree
              String key_p = vit;
              for (int counter = 1;
                  emb_files_map_p.containsKey(key_p);
                  ++counter) { // append a unique suffix
                key_p = vit + "-" + counter;
              }

              // add file spec. to map
              emb_files_map_p.put(key_p, ref_p);
            }

            if (!emb_files_map_p.isEmpty()) {
              // create a name tree from map
              PdfDictionary emb_files_tree_new_p = PdfNameTree.writeTree(emb_files_map_p, writer_p);

              if (emb_files_tree_new_b && emb_files_tree_new_p != null) {
                // adding new material
                PdfIndirectReference ref_p =
                    writer_p.addToBody(emb_files_tree_new_p).getIndirectReference();
                names_p.put(PdfName.EMBEDDEDFILES, ref_p);
              } else if (emb_files_tree_p != null && emb_files_tree_new_p != null) {
                // supplementing old material
                ((PdfDictionary) emb_files_tree_p).merge(emb_files_tree_new_p);
              } else { // error
                System.err.println("Internal Error: no valid EmbeddedFiles tree to add to PDF.");
              }

              if (names_new_b) {
                // perform addToBody only after packing new names_p into names_p;
                // use the resulting ref. to pack our new Names dict. into the catalog (Root)
                PdfIndirectReference ref_p = writer_p.addToBody(names_p).getIndirectReference();
                catalog_p.put(PdfName.NAMES, ref_p);
              }
            }
          } else { // error
            System.err.println("Internal Error: couldn't read or create PDF Names dictionary.");
          }
        } else { // error
          System.err.println("Internal Error: couldn't read input PDF Root dictionary.");
          System.err.println("   File attachment failed; no new files attached to output.");
        }
      }
    }
  }

  void unpack_files(PdfReader input_reader_p) {
    // output pathname; PROMPT if necessary
    String output_pathname = attachments.normalize_pathname(m_output_filename);

    { // unpack document attachments
      PdfDictionary catalog_p = input_reader_p.catalog; // to top, Root dict
      if (catalog_p != null && catalog_p.isDictionary()) {

        // the Names dict
        PdfObject names_p = input_reader_p.getPdfObject(catalog_p.get(PdfName.NAMES));
        if (names_p != null && names_p.isDictionary()) {

          // the EmbeddedFiles name tree (ref. 1.5, sec. 3.8.5), which is a dict at top
          PdfObject emb_files_tree_p =
              input_reader_p.getPdfObject(((PdfDictionary) names_p).get(PdfName.EMBEDDEDFILES));
          HashMap<Object, PdfObject> emb_files_map_p = null;
          if (emb_files_tree_p != null && emb_files_tree_p.isDictionary()) {
            // read current name tree of attachments into a map
            emb_files_map_p = PdfNameTree.readTree((PdfDictionary) emb_files_tree_p);

            for (PdfObject value_p : emb_files_map_p.values()) {
              PdfObject filespec_p = input_reader_p.getPdfObject(value_p);
              if (filespec_p != null && filespec_p.isDictionary()) {

                attachments.unpack_file(
                    input_reader_p, (PdfDictionary) filespec_p, output_pathname, m_ask_about_warnings_b);
              }
            }
          }
        }
      }
    }

    { // unpack page attachments
      int num_pages = input_reader_p.getNumberOfPages();
      for (int ii = 1; ii <= num_pages; ++ii) { // 1-based page ref.s

        PdfDictionary page_p = input_reader_p.getPageN(ii);
        if (page_p != null && page_p.isDictionary()) {

          PdfObject annots_p = input_reader_p.getPdfObject(page_p.get(PdfName.ANNOTS));
          if (annots_p != null && annots_p.isArray()) {

            ArrayList<PdfObject> annots_array_p = ((PdfArray)annots_p).getArrayList();
            for (PdfObject jj : annots_array_p) {
              PdfObject annot_po = input_reader_p.getPdfObject(jj);
              if (annot_po != null && annot_po.isDictionary()) {
                PdfDictionary annot_p = (PdfDictionary) annot_po;

                PdfObject subtype_p =
input_reader_p.getPdfObject(annot_p.get(PdfName.SUBTYPE));
                if (subtype_p != null && subtype_p.equals(PdfName.FILEATTACHMENT)) {

                  PdfObject filespec_p = input_reader_p.getPdfObject(annot_p.get(PdfName.FS));
                  if (filespec_p != null && filespec_p.isDictionary()) {

                    attachments.unpack_file(
                        input_reader_p, (PdfDictionary) filespec_p, output_pathname, m_ask_about_warnings_b);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  ////
  // when uncompressing a PDF, we add this marker to every page,
  // so the PDF is easier to navigate; when compressing a PDF,
  // we remove this marker

  static final String g_page_marker = "pdftk_PageNum";

  static void add_mark_to_page(PdfReader reader_p, int page_index, int page_num) {
    PdfName page_marker_p = new PdfName(g_page_marker);
    PdfDictionary page_p = reader_p.getPageN(page_index);
    if (page_p != null && page_p.isDictionary()) {
      page_p.put(page_marker_p, new PdfNumber(page_num));
    }
  }

  static void add_marks_to_pages(PdfReader reader_p) {
    int num_pages = reader_p.getNumberOfPages();
    for (int ii = 1; ii <= num_pages; ++ii) { // 1-based page ref.s
      add_mark_to_page(reader_p, ii, ii);
    }
  }

  static void remove_mark_from_page(PdfReader reader_p, int page_num) {
    PdfName page_marker_p = new PdfName(g_page_marker);
    PdfDictionary page_p = reader_p.getPageN(page_num);
    if (page_p != null && page_p.isDictionary()) {
      page_p.remove(page_marker_p);
    }
  }

  static void remove_marks_from_pages(PdfReader reader_p) {
    int num_pages = reader_p.getNumberOfPages();
    for (int ii = 1; ii <= num_pages; ++ii) { // 1-based page ref.s
      remove_mark_from_page(reader_p, ii);
    }
  }

  static void apply_rotation_to_page(
      PdfReader reader_p, int page_num, int rotation, boolean absolute) {
    // DF rotate
    PdfDictionary page_p = reader_p.getPageN(page_num);
    if (!absolute) {
      rotation = reader_p.getPageRotation(page_num) + rotation;
    }
    rotation = rotation % 360;
    page_p.remove(PdfName.ROTATE);
    if (rotation != PageRotate.NORTH.value) { // default rotation
      page_p.put(PdfName.ROTATE, new PdfNumber(rotation));
    }
  }

  ErrorCode create_output_page(PdfCopy writer_p, PageRef page_ref, int output_page_count) {
    ErrorCode ret_val = ErrorCode.NO_ERROR;

    // get the reader associated with this page ref.
    if (page_ref.m_input_pdf_index < m_input_pdf.size()) {
      InputPdf page_pdf = m_input_pdf.get(page_ref.m_input_pdf_index);

      if (m_verbose_reporting_b) {
        System.out.print(
            "   Adding page "
                + page_ref.m_page_num
                + " X"
                + page_ref.m_page_rot
                + "X "); // DF rotate
        System.out.println(" from " + page_pdf.m_filename);
      }

      // take the first, associated reader and then disassociate
      PdfReader input_reader_p = null;
      for (InputPdf.PagesReader mt : page_pdf.m_readers) {
        if (mt.first.contains(page_ref.m_page_num)) { // assoc. found
          input_reader_p = mt.second;
          mt.first.remove(page_ref.m_page_num); // remove this assoc.
          break;
        }
      }

      if (input_reader_p != null) {

        //
        if (m_output_uncompress_b) {
          add_mark_to_page(input_reader_p, page_ref.m_page_num, output_page_count + 1);
        } else if (m_output_compress_b) {
          remove_mark_from_page(input_reader_p, page_ref.m_page_num);
        }

        // DF rotate
        apply_rotation_to_page(
            input_reader_p, page_ref.m_page_num, page_ref.m_page_rot.value, page_ref.m_page_abs);

        //
        try {
          PdfImportedPage page_p = writer_p.getImportedPage(input_reader_p, page_ref.m_page_num);
          try {
            writer_p.addPage(page_p);
          } catch (DocumentException e) {
            System.err.print("Internal Error: addPage() failed for: ");
            System.err.println(page_ref.m_page_num + " in file: " + page_pdf.m_filename);
            ret_val = ErrorCode.BUG;
          }
        } catch (IOException e) { // error
          System.err.print("Internal Error: getImportedPage() failed for: ");
          System.err.println(page_ref.m_page_num + " in file: " + page_pdf.m_filename);
          ret_val = ErrorCode.BUG;
        }
      } else { // error
        System.err.print("Internal Error: no reader found for page: ");
        System.err.println(page_ref.m_page_num + " in file: " + page_pdf.m_filename);
        ret_val = ErrorCode.BUG;
      }
    } else { // error
      System.err.println("Internal Error: Unable to find handle in m_input_pdf.");
      ret_val = ErrorCode.BUG;
    }

    return ret_val;
  }

  static char GetPdfVersionChar(PdfName version_p) {
    char version_cc = PdfWriter.VERSION_1_4; // default

    if (version_p != null)
      if (version_p.equals(PdfName.VERSION_1_4)) version_cc = PdfWriter.VERSION_1_4;
      else if (version_p.equals(PdfName.VERSION_1_5)) version_cc = PdfWriter.VERSION_1_5;
      else if (version_p.equals(PdfName.VERSION_1_6)) version_cc = PdfWriter.VERSION_1_6;
      else if (version_p.equals(PdfName.VERSION_1_7)) version_cc = PdfWriter.VERSION_1_7;
      else if (version_p.equals(PdfName.VERSION_1_3)) version_cc = PdfWriter.VERSION_1_3;
      else if (version_p.equals(PdfName.VERSION_1_2)) version_cc = PdfWriter.VERSION_1_2;
      else if (version_p.equals(PdfName.VERSION_1_1)) version_cc = PdfWriter.VERSION_1_1;
      else if (version_p.equals(PdfName.VERSION_1_0)) version_cc = PdfWriter.VERSION_1_0;

    return version_cc;
  }

  ErrorCode create_output() {
    ErrorCode ret_val = ErrorCode.NO_ERROR; // default: no error

    if (is_valid()) {

      /*
      bool rdfcat_available_b= false;
      { // is rdfcat available?  first character should be a digit;
        // grab stderr to keep messages appearing to user;
        // 2>&1 might not work on older versions of Windows (e.g., 98);
        FILE* pp= popen( "rdfcat --version 2>&1", "r" );
        if( pp ) {
          int cc= fgetc( pp );
          if( '0'<= cc && cc<= '9' ) {
            rdfcat_available_b= true;
          }
          pclose( pp );
        }
      }
      */

      if (m_verbose_reporting_b) {
        System.out.println();
        System.out.println("Creating Output ...");
      }

      String creator = "pdftk-java " + pdftk.PDFTK_VER;

      if (m_output_owner_pw.equals("PROMPT")) {
        m_output_owner_pw = pdftk.prompt_for_password("owner", "the output PDF");
      }
      if (m_output_user_pw.equals("PROMPT")) {
        m_output_user_pw = pdftk.prompt_for_password("user", "the output PDF");
      }

      byte[] output_owner_pw_p = new byte[0];
      if (!m_output_owner_pw.isEmpty()) {
        output_owner_pw_p = passwords.utf8_password_to_pdfdoc(m_output_owner_pw, true);
        if (output_owner_pw_p == null) { // error
          System.err.println("Error: Owner password used to encrypt output PDF includes");
          System.err.println("   invalid characters.");
          System.err.println("   No output created.");
          ret_val = ErrorCode.ERROR;
        }
      }

      byte[] output_user_pw_p = new byte[0];
      if (!m_output_user_pw.isEmpty()) {
        output_user_pw_p = passwords.utf8_password_to_pdfdoc(m_output_user_pw, true);
        if (output_user_pw_p == null) { // error
          System.err.println("Error: User password used to encrypt output PDF includes");
          System.err.println("   invalid characters.");
          System.err.println("   No output created.");
          ret_val = ErrorCode.ERROR;
        }
      }

      if (ret_val != ErrorCode.NO_ERROR) return ret_val; // <--- exit

      try {
        switch (m_operation) {
          case cat_k:
          case shuffle_k:
            { // catenate pages or shuffle pages
              Document output_doc_p = new Document();

              OutputStream ofs_p =
                  pdftk.get_output_stream(m_output_filename, m_ask_about_warnings_b);

              if (ofs_p == null) { // file open error
                ret_val = ErrorCode.ERROR;
                break;
              }
              PdfCopy writer_p = new PdfCopy(output_doc_p, ofs_p);

              // update to suit any features that we add, e.g. encryption;
              char max_version_cc = PdfWriter.VERSION_1_2;

              //
              output_doc_p.addCreator(creator);

              // un/compress output streams?
              if (m_output_uncompress_b) {
                writer_p.filterStreams = true;
                writer_p.compressStreams = false;
              } else if (m_output_compress_b) {
                writer_p.filterStreams = false;
                writer_p.compressStreams = true;
              }

              // encrypt output?
              if (m_output_encryption_strength != encryption_strength.none_enc
                  || !m_output_owner_pw.isEmpty()
                  || !m_output_user_pw.isEmpty()) {
                // if no stregth is given, default to 128 bit,
                boolean bit128_b = (m_output_encryption_strength != encryption_strength.bits40_enc);

                writer_p.setEncryption(
                    output_user_pw_p, output_owner_pw_p, m_output_user_perms, bit128_b);

                if (bit128_b) max_version_cc = PdfWriter.VERSION_1_4;
                else // 1.1 probably okay, here
                max_version_cc = PdfWriter.VERSION_1_3;
              }

              // copy file ID?
              if (m_output_keep_first_id_b || m_output_keep_final_id_b) {
                PdfReader input_reader_p =
                    m_output_keep_first_id_b
                        ? m_input_pdf.get(0).m_readers.get(0).second
                        : m_input_pdf.get(m_input_pdf.size() - 1).m_readers.get(0).second;

                PdfDictionary trailer_p = input_reader_p.getTrailer();

                PdfObject file_id_p = input_reader_p.getPdfObject(trailer_p.get(PdfName.ID));
                if (file_id_p != null && file_id_p.isArray()) {

                  writer_p.setFileID(file_id_p);
                }
              }

              // set output PDF version to the max PDF ver of all the input PDFs;
              // also find the maximum extension levels, if present -- this can
              // only be added /after/ opening the document;
              //
              // collected extensions information; uses PdfName::hashCode() for key
              HashMap<PdfName, PdfName> ext_base_versions = new HashMap<PdfName, PdfName>();
              HashMap<PdfName, Integer> ext_levels = new HashMap<PdfName, Integer>();
              for (InputPdf it : m_input_pdf) {
                PdfReader reader_p = it.m_readers.get(0).second;

                ////
                // PDF version number

                // version in header
                if (max_version_cc < reader_p.getPdfVersion())
                  max_version_cc = reader_p.getPdfVersion();

                // version override in catalog; used only if greater than header version, per PDF
                // spec;
                PdfDictionary catalog_p = reader_p.getCatalog();
                if (catalog_p.contains(PdfName.VERSION)) {

                  PdfName version_p =
                      (PdfName) reader_p.getPdfObject(catalog_p.get(PdfName.VERSION));
                  char version_cc = GetPdfVersionChar(version_p);

                  if (max_version_cc < version_cc) max_version_cc = version_cc;
                }

                ////
                // PDF extensions

                if (catalog_p.contains(PdfName.EXTENSIONS)) {
                  PdfObject extensions_po = reader_p.getPdfObject(catalog_p.get(PdfName.EXTENSIONS));
                  if (extensions_po != null && extensions_po.isDictionary()) {
                    PdfDictionary extensions_p = (PdfDictionary) extensions_po;

                    // iterate over developers
                    Set<PdfObject> keys_p = extensions_p.getKeys();
                    for (PdfObject kit : keys_p) {
                      PdfName developer_p = (PdfName) reader_p.getPdfObject(kit);

                      PdfObject dev_exts_po = reader_p.getPdfObject(extensions_p.get(developer_p));
                      if (dev_exts_po != null && dev_exts_po.isDictionary()) {
                        PdfDictionary dev_exts_p = (PdfDictionary) dev_exts_po;

                        if (dev_exts_p.contains(PdfName.BASEVERSION)
                            && dev_exts_p.contains(PdfName.EXTENSIONLEVEL)) {
                          // use the greater base version or the greater extension level

                          PdfName base_version_p =
                              (PdfName) reader_p.getPdfObject(dev_exts_p.get(PdfName.BASEVERSION));
                          PdfNumber ext_level_p =
                              (PdfNumber)
                                  reader_p.getPdfObject(dev_exts_p.get(PdfName.EXTENSIONLEVEL));

                          if (!ext_base_versions.containsKey(developer_p)
                              || GetPdfVersionChar(ext_base_versions.get(developer_p))
                                  < GetPdfVersionChar(
                                      base_version_p)) { // new developer or greater base version
                            ext_base_versions.put(developer_p, base_version_p);
                            ext_levels.put(developer_p, ext_level_p.intValue());
                          } else if (GetPdfVersionChar(ext_base_versions.get(developer_p))
                                  == GetPdfVersionChar(base_version_p)
                              && ext_levels.get(developer_p)
                                  < ext_level_p
                                      .intValue()) { // greater extension level for current base
                            // version
                            ext_levels.put(developer_p, ext_level_p.intValue());
                          }
                        }
                      }
                    }
                  }
                }
              }
              // set the pdf version
              writer_p.setPdfVersion(max_version_cc);

              // open the doc
              output_doc_p.open();

              // set any pdf version extensions we might have found
              if (!ext_base_versions.isEmpty()) {
                PdfDictionary extensions_dict_p = new PdfDictionary();
                PdfIndirectReference extensions_ref_p = writer_p.getPdfIndirectReference();
                for (Map.Entry<PdfName, PdfName> it : ext_base_versions.entrySet()) {
                  PdfDictionary ext_dict_p = new PdfDictionary();
                  ext_dict_p.put(PdfName.BASEVERSION, it.getValue());
                  ext_dict_p.put(
                      PdfName.EXTENSIONLEVEL, new PdfNumber(ext_levels.get(it.getKey())));

                  extensions_dict_p.put(it.getKey(), ext_dict_p);
                }

                writer_p.addToBody(extensions_dict_p, extensions_ref_p);
                writer_p.setExtensions(extensions_ref_p);
              }

              if (m_operation == keyword.shuffle_k) {
                int max_seq_length = 0;
                for (ArrayList<PageRef> jt : m_page_seq) {
                  max_seq_length = (max_seq_length < jt.size()) ? jt.size() : max_seq_length;
                }

                int output_page_count = 0;
                // iterate over ranges
                for (int ii = 0; (ii < max_seq_length && ret_val == ErrorCode.NO_ERROR); ++ii) {
                  // iterate over ranges
                  for (ArrayList<PageRef> jt : m_page_seq) {
                    if (ret_val != ErrorCode.NO_ERROR) break;
                    if (ii < jt.size()) {
                      ret_val = create_output_page(writer_p, jt.get(ii), output_page_count);
                      ++output_page_count;
                    }
                  }
                }
              } else { // cat_k

                int output_page_count = 0;
                // iterate over page ranges
                for (ArrayList<PageRef> jt : m_page_seq) {
                  if (ret_val != ErrorCode.NO_ERROR) break;
                  // iterate over pages in page range
                  for (PageRef it : jt) {
                    if (ret_val != ErrorCode.NO_ERROR) break;
                    ret_val = create_output_page(writer_p, it, output_page_count);
                    ++output_page_count;
                  }
                }

                // first impl added a bookmark for each input PDF and then
                // added any of that PDFs bookmarks under that; now it
                // appends input PDF bookmarks, which is more attractive;
                // OTOH, some folks might want pdftk to add bookmarks for
                // input PDFs, esp if they don't have bookmarks -- TODO
                // but then, it would be nice to allow the user to specify
                // a label -- using the PDF filename is unattractive;
                if (m_cat_full_pdfs_b) { // add bookmark info
                  // cerr << "cat full pdfs!" << endl; // debug

                  PdfDictionary output_outlines_p = new PdfDictionary(PdfName.OUTLINES);
                  PdfIndirectReference output_outlines_ref_p = writer_p.getPdfIndirectReference();

                  PdfDictionary after_child_p = null;
                  PdfIndirectReference after_child_ref_p = null;

                  int page_count = 1;
                  int num_bookmarks_total = 0;
                  /* used for adding doc bookmarks
                  itext::PdfDictionary* prev_p= 0;
                  itext::PdfIndirectReference* prev_ref_p= 0;
                  */
                  // iterate over page ranges; each full PDF has one page seq in m_page_seq;
                  // using m_page_seq instead of m_input_pdf, so the doc order is right
                  for (ArrayList<PageRef> jt : m_page_seq) {
                    PdfReader reader_p =
                        m_input_pdf.get(jt.get(0).m_input_pdf_index).m_readers.get(0).second;
                    long reader_page_count =
                        m_input_pdf.get(jt.get(0).m_input_pdf_index).m_num_pages;

                    /* used for adding doc bookmarks
                    itext::PdfDictionary* item_p= new itext::PdfDictionary();
                    itext::PdfIndirectReference* item_ref_p= writer_p->getPdfIndirectReference();

                    item_p->put( itext::PdfName::PARENT, outlines_ref_p );
                    item_p->put( itext::PdfName::TITLE,
                                 new itext::PdfString( JvNewStringUTF( (*it).m_filename.c_str() ) ) );

                    // wire into linked list
                    if( prev_p ) {
                      prev_p->put( itext::PdfName::NEXT, item_ref_p );
                      item_p->put( itext::PdfName::PREV, prev_ref_p );
                    }
                    else { // first item; wire into outlines dict
                      output_outlines_p->put( itext::PdfName::FIRST, item_ref_p );
                    }

                    // the destination
                    itext::PdfDestination* dest_p= new itext::PdfDestination(itext::PdfDestination::FIT);
                    itext::PdfIndirectReference* page_ref_p= writer_p->getPageReference( page_count );
                    if( page_ref_p ) {
                      dest_p->addPage( page_ref_p );
                    }
                    item_p->put( itext::PdfName::DEST, dest_p );
                    */

                    // pdf bookmarks -> children
                    {
                      PdfDictionary catalog_p = reader_p.getCatalog();
                      PdfObject outlines_p = reader_p.getPdfObject(catalog_p.get(PdfName.OUTLINES));
                      if (outlines_p != null && outlines_p.isDictionary()) {

                        PdfObject top_outline_p =
                            reader_p.getPdfObject(((PdfDictionary) outlines_p).get(PdfName.FIRST));
                        if (top_outline_p != null && top_outline_p.isDictionary()) {

                          ArrayList<PdfBookmark> bookmark_data = new ArrayList<PdfBookmark>();
                          int rr = bookmarks.ReadOutlines(
                              bookmark_data, (PdfDictionary) top_outline_p, 0, reader_p, true);
                          if (rr == 0 && !bookmark_data.isEmpty()) {

                            // passed in by reference, so must use variable:
                            bookmarks.BuildBookmarksState state =
                                new bookmarks.BuildBookmarksState();
                            state.final_child_p = after_child_p;
                            state.final_child_ref_p = after_child_ref_p;
                            state.num_bookmarks_total = num_bookmarks_total;
                            bookmarks.BuildBookmarks(
                                writer_p,
                                bookmark_data.listIterator(),
                                // item_p, item_ref_p, // used for adding doc bookmarks
                                output_outlines_p,
                                output_outlines_ref_p,
                                after_child_p,
                                after_child_ref_p,
                                0,
                                page_count - 1, // page offset is 0-based
                                0,
                                true,
                                state);
                            after_child_p = state.final_child_p;
                            after_child_ref_p = state.final_child_ref_p;
                            num_bookmarks_total = state.num_bookmarks_total;
                          }
                          /*
                          else if( rr!= 0 )
                          cerr << "ReadOutlines error" << endl; // debug
                          else
                          cerr << "empty bookmark data" << endl; // debug
                          */
                        }
                      }
                      /*
                      else
                        cerr << "no outlines" << endl; // debug
                      */
                    }

                    /* used for adding doc bookmarks
                    // finished with prev; add to body
                    if( prev_p )
                      writer_p->addToBody( prev_p, prev_ref_p );

                    prev_p= item_p;
                    prev_ref_p= item_ref_p;
                    */

                    page_count += reader_page_count;
                  }
                  /* used for adding doc bookmarks
                  if( prev_p ) { // wire into outlines dict
                    // finished with prev; add to body
                    writer_p->addToBody( prev_p, prev_ref_p );

                    output_outlines_p->put( itext::PdfName::LAST, prev_ref_p );
                    output_outlines_p->put( itext::PdfName::COUNT, new itext::PdfNumber( (jint)m_input_pdf.size() ) );
                  }
                  */

                  if (num_bookmarks_total != 0) { // we encountered bookmarks

                    // necessary for serial appending to outlines
                    if (after_child_p != null && after_child_ref_p != null)
                      writer_p.addToBody(after_child_p, after_child_ref_p);

                    writer_p.addToBody(output_outlines_p, output_outlines_ref_p);
                    writer_p.setOutlines(output_outlines_ref_p);
                  }
                }
              }

              output_doc_p.close();
              writer_p.close();
            }
            break;

          case burst_k:
            { // burst input into pages

              // we should have been given only a single, input file
              if (1 < m_input_pdf.size()) { // error
                System.err.println("Error: Only one input PDF file may be given for \"burst\" op.");
                System.err.println("   No output created.");
                ret_val = ErrorCode.ERROR;
                break;
              }

              // grab the first reader, since there's only one
              PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;
              int input_num_pages = m_input_pdf.get(0).m_num_pages;

              if (m_output_filename.equals("PROMPT")) {
                m_output_filename =
                    pdftk.prompt_for_filename(
                        "Please enter a filename pattern for the PDF pages (e.g. pg_%04d.pdf):");
              }
              if (m_output_filename.isEmpty()) {
                m_output_filename = "pg_%04d.pdf";
              }
              try {
                String.format(m_output_filename, 1);
              }
              catch(UnknownFormatConversionException e) {
                System.err.println("Error: Invalid output pattern:");
                System.err.println("   " + m_output_filename);
                System.err.println("   No output created.");
                ret_val = ErrorCode.ERROR;
                break;
              }

              // locate the input PDF Info dictionary that holds metadata
              PdfDictionary input_info_p = null;
              {
                PdfDictionary input_trailer_p = input_reader_p.getTrailer();
                if (input_trailer_p != null) {
                  PdfObject input_info_po =
                      input_reader_p.getPdfObject(input_trailer_p.get(PdfName.INFO));
                  if (input_info_po != null && input_info_po.isDictionary()) {
                    // success
                    input_info_p = (PdfDictionary) input_info_po;
                  }
                }
              }

              for (int ii = 0; ii < input_num_pages; ++ii) {

                // the filename
                String jv_output_filename_p = String.format(m_output_filename, ii + 1);

                Document output_doc_p = new Document();
                FileOutputStream ofs_p = new FileOutputStream(jv_output_filename_p);
                PdfCopy writer_p = new PdfCopy(output_doc_p, ofs_p);

                output_doc_p.addCreator(creator);

                // un/compress output streams?
                if (m_output_uncompress_b) {
                  writer_p.filterStreams = true;
                  writer_p.compressStreams = false;
                } else if (m_output_compress_b) {
                  writer_p.filterStreams = false;
                  writer_p.compressStreams = true;
                }

                // encrypt output?
                if (m_output_encryption_strength != encryption_strength.none_enc
                    || !m_output_owner_pw.isEmpty()
                    || !m_output_user_pw.isEmpty()) {
                  // if no stregth is given, default to 128 bit,
                  boolean bit128_b =
                      (m_output_encryption_strength != encryption_strength.bits40_enc);

                  writer_p.setEncryption(
                      output_user_pw_p, output_owner_pw_p, m_output_user_perms, bit128_b);
                }

                output_doc_p.open(); // must open writer before copying (possibly) indirect object
                // Call setFromReader() after open(),
                // otherwise topPageParent is not properly set.
                // See https://gitlab.com/pdftk-java/pdftk/issues/18
                writer_p.setFromReader(input_reader_p);

                { // copy the Info dictionary metadata
                  if (input_info_p != null) {
                    PdfDictionary writer_info_p = writer_p.getInfo();
                    if (writer_info_p != null) {
                      PdfDictionary info_copy_p = writer_p.copyDictionary(input_info_p);
                      if (info_copy_p != null) {
                        writer_info_p.putAll(info_copy_p);
                      }
                    }
                  }
                  byte[] input_reader_xmp_p = input_reader_p.getMetadata();
                  if (input_reader_xmp_p != null) {
                    writer_p.setXmpMetadata(input_reader_xmp_p);
                  }
                }

                PdfImportedPage page_p = writer_p.getImportedPage(input_reader_p, ii + 1);
                writer_p.addPage(page_p);

                output_doc_p.close();
                writer_p.close();
              }

              ////
              // dump document data

              String doc_data_fn = "doc_data.txt";
              if (!m_output_filename.isEmpty()) {
                int loc = m_output_filename.lastIndexOf(File.separatorChar);
                if (loc >= 0) {
                  doc_data_fn =
                      m_output_filename.substring(0, loc) + File.separatorChar + doc_data_fn;
                }
              }
              try {
                PrintStream ofs = pdftk.get_print_stream(doc_data_fn, m_output_utf8_b);
                report.ReportOnPdf(ofs, input_reader_p, m_output_utf8_b);
              } catch (FileNotFoundException e) { // error
                System.err.println("Error: unable to open file for output: doc_data.txt");
                ret_val = ErrorCode.ERROR;
              }
            }
            break;

          case filter_k:
            { // apply operations to given PDF file

              // we should have been given only a single, input file
              if (1 < m_input_pdf.size()) { // error
                System.err.println("Error: Only one input PDF file may be given for this");
                System.err.println("   operation.  Maybe you meant to use the \"cat\" operator?");
                System.err.println("   No output created.");
                ret_val = ErrorCode.ERROR;
                break;
              }

              // try opening the FDF file before we get too involved;
              // if input is stdin ("-"), don't pass it to both the FDF and XFDF readers
              FdfReader fdf_reader_p = null;
              XfdfReader xfdf_reader_p = null;
              if (m_form_data_filename.equals(
                  "PROMPT")) { // handle case where user enters '-' or (empty) at the prompt
                m_form_data_filename =
                    pdftk.prompt_for_filename("Please enter a filename for the form data:");
              }
              if (!m_form_data_filename.isEmpty()) { // we have form data to process
                if (m_form_data_filename.equals("-")) { // form data on stdin
                  // JArray<jbyte>* in_arr= itext::RandomAccessFileOrArray::InputStreamToArray(
                  // java::System::in );

                  // first try fdf
                  try {
                    fdf_reader_p = new FdfReader(System.in);
                  } catch (IOException ioe_p) { // file open error

                    // maybe it's xfdf?
                    try {
                      xfdf_reader_p = new XfdfReader(System.in);
                    } catch (IOException ioe2_p) { // file open error
                      System.err.println("Error: Failed read form data on stdin.");
                      System.err.println("   No output created.");
                      ret_val = ErrorCode.ERROR;
                      // ioe_p->printStackTrace(); // debug
                      break;
                    }
                  }
                } else { // form data file

                  // first try fdf
                  try {
                    fdf_reader_p = new FdfReader(m_form_data_filename);
                  } catch (IOException ioe_p) { // file open error
                    // maybe it's xfdf?
                    try {
                      xfdf_reader_p = new XfdfReader(m_form_data_filename);
                    } catch (IOException ioe2_p) { // file open error
                      System.err.println("Error: Failed to open form data file: ");
                      System.err.println("   " + m_form_data_filename);
                      System.err.println("   No output created.");
                      ret_val = ErrorCode.ERROR;
                      // ioe_p->printStackTrace(); // debug
                      break;
                    }
                  }
                }
              }

              // try opening the PDF background or stamp before we get too involved
              PdfReader mark_p = null;
              boolean background_b = true; // set false for stamp
              //
              // background
              if (m_background_filename.equals("PROMPT")) {
                m_background_filename =
                    pdftk.prompt_for_filename("Please enter a filename for the background PDF:");
              }
              if (!m_background_filename.isEmpty()) {
                try {
                  mark_p = new PdfReader(m_background_filename);
                  mark_p.removeUnusedObjects();
                  // reader->shuffleSubsetNames(); // changes the PDF subset names, but not the
                  // PostScript font names
                } catch (IOException ioe_p) { // file open error
                  System.err.println("Error: Failed to open background PDF file: ");
                  System.err.println("   " + m_background_filename);
                  System.err.println("   No output created.");
                  ret_val = ErrorCode.ERROR;
                  break;
                }
              }
              //
              // stamp
              if (mark_p == null) {
                if (m_stamp_filename.equals("PROMPT")) {
                  m_stamp_filename =
                      pdftk.prompt_for_filename("Please enter a filename for the stamp PDF:");
                }
                if (!m_stamp_filename.isEmpty()) {
                  background_b = false;
                  try {
                    mark_p = new PdfReader(m_stamp_filename);
                    mark_p.removeUnusedObjects();
                    // reader->shuffleSubsetNames(); // changes the PDF subset names, but not the
                    // PostScript font names
                  } catch (IOException ioe_p) { // file open error
                    System.err.println("Error: Failed to open stamp PDF file: ");
                    System.err.println("   " + m_stamp_filename);
                    System.err.println("   No output created.");
                    ret_val = ErrorCode.ERROR;
                    break;
                  }
                }
              }

              //
              OutputStream ofs_p =
                  pdftk.get_output_stream(m_output_filename, m_ask_about_warnings_b);
              if (ofs_p == null) { // file open error
                System.err.println("Error: unable to open file for output: " + m_output_filename);
                ret_val = ErrorCode.ERROR;
                break;
              }

              //
              PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

              // drop the xfa?
              if (m_output_drop_xfa_b) {
                PdfDictionary catalog_p = input_reader_p.catalog;
                if (catalog_p != null && catalog_p.isDictionary()) {

                  PdfObject acro_form_p =
                      input_reader_p.getPdfObject(catalog_p.get(PdfName.ACROFORM));
                  if (acro_form_p != null && acro_form_p.isDictionary()) {

                    ((PdfDictionary) acro_form_p).remove(PdfName.XFA);
                  }
                }
              }

              // drop the xmp?
              if (m_output_drop_xmp_b) {
                PdfDictionary catalog_p = input_reader_p.catalog;
                if (catalog_p != null) {

                  catalog_p.remove(PdfName.METADATA);
                }
              }

              //
              PdfStamperImp writer_p =
                  new PdfStamperImp(input_reader_p, ofs_p, '\0', false /* append mode */);

              // update the info?
              if (m_update_info_filename.equals("PROMPT")) {
                m_update_info_filename =
                    pdftk.prompt_for_filename("Please enter an Info file filename:");
              }
              if (!m_update_info_filename.isEmpty()) {
                if (m_update_info_filename.equals("-")) {
                  if (!data_import.UpdateInfo(input_reader_p, System.in, m_update_info_utf8_b)) {
                    System.err.println("Warning: no Info added to output PDF.");
                    ret_val = ErrorCode.WARNING;
                  }
                } else {
                  try {
                    FileInputStream ifs = new FileInputStream(m_update_info_filename);
                    if (!data_import.UpdateInfo(input_reader_p, ifs, m_update_info_utf8_b)) {
                      System.err.println("Warning: no Info added to output PDF.");
                      ret_val = ErrorCode.WARNING;
                    }
                  } catch (FileNotFoundException e) { // error
                    System.err.println(
                        "Error: unable to open FDF file for input: " + m_update_info_filename);
                    ret_val = ErrorCode.ERROR;
                    break;
                  }
                }
              }

              /*
              // update the xmp?
              if( !m_update_xmp_filename.empty() ) {
                if( rdfcat_available_b ) {
                  if( m_update_xmp_filename== "PROMPT" ) {
                    prompt_for_filename( "Please enter an Info file filename:",
                                         m_update_xmp_filename );
                  }
                  if( !m_update_xmp_filename.empty() ) {
                    UpdateXmp( input_reader_p, m_update_xmp_filename );
                  }
                }
                else { // error
                  cerr << "Error: to use this feature, you must install the rdfcat program." << endl;
                  cerr << "   Perhaps the replace_xmp feature would suit you, instead?" << endl;
                  break;
                }
              }
              */

              // rotate pages?
              if (!m_page_seq.isEmpty()) {
                for (ArrayList<PageRef> jt : m_page_seq) {
                  for (PageRef kt : jt) {
                    apply_rotation_to_page(
                        input_reader_p, kt.m_page_num, kt.m_page_rot.value, kt.m_page_abs);
                  }
                }
              }

              // un/compress output streams?
              if (m_output_uncompress_b) {
                add_marks_to_pages(input_reader_p);
                writer_p.filterStreams = true;
                writer_p.compressStreams = false;
              } else if (m_output_compress_b) {
                remove_marks_from_pages(input_reader_p);
                writer_p.filterStreams = false;
                writer_p.compressStreams = true;
              }

              // encrypt output?
              if (m_output_encryption_strength != encryption_strength.none_enc
                  || !m_output_owner_pw.isEmpty()
                  || !m_output_user_pw.isEmpty()) {

                // if no stregth is given, default to 128 bit,
                // (which is incompatible w/ Acrobat 4)
                boolean bit128_b = (m_output_encryption_strength != encryption_strength.bits40_enc);

                writer_p.setEncryption(
                    output_user_pw_p, output_owner_pw_p, m_output_user_perms, bit128_b);
              }

              // fill form fields?
              if (fdf_reader_p != null || xfdf_reader_p != null) {
                if (input_reader_p.getAcroForm() != null) { // we really have a form to fill

                  AcroFields fields_p = writer_p.getAcroFields();
                  fields_p.setGenerateAppearances(true); // have iText create field appearances
                  if ((fdf_reader_p != null && fields_p.setFields(fdf_reader_p))
                      || (xfdf_reader_p != null
                          && fields_p.setFields(xfdf_reader_p))) { // Rich Text input found

                    // set the PDF so that Acrobat will create appearances;
                    // this might appear contradictory to our setGenerateAppearances( true ) call,
                    // above; setting this, here, allows us to keep the generated appearances,
                    // in case the PDF is opened somewhere besides Acrobat; yet, Acrobat/Reader
                    // will create the Rich Text appearance if it has a chance
                    m_output_need_appearances_b = true;
                    /*
                    itext::PdfDictionary* catalog_p= input_reader_p->catalog;
                    if( catalog_p && catalog_p->isDictionary() ) {

                      itext::PdfDictionary* acro_form_p= (itext::PdfDictionary*)
                        input_reader_p->getPdfObject( catalog_p->get( itext::PdfName::ACROFORM ) );
                      if( acro_form_p && acro_form_p->isDictionary() ) {

                        acro_form_p->put( itext::PdfName::NEEDAPPEARANCES, itext::PdfBoolean::PDFTRUE );
                      }
                    }
                    */
                  }
                } else { // warning
                  System.err.println(
                      "Warning: input PDF is not an acroform, so its fields were not filled.");
                  ret_val = ErrorCode.WARNING;
                }
              }

              // flatten form fields?
              writer_p.setFormFlattening(m_output_flatten_b);

              // cue viewer to render form field appearances?
              if (m_output_need_appearances_b) {
                PdfDictionary catalog_p = input_reader_p.catalog;
                if (catalog_p != null && catalog_p.isDictionary()) {
                  PdfObject acro_form_p =
                      input_reader_p.getPdfObject(catalog_p.get(PdfName.ACROFORM));
                  if (acro_form_p != null && acro_form_p.isDictionary()) {
                    ((PdfDictionary) acro_form_p).put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE);
                  }
                }
              }

              // add background/watermark?
              if (mark_p != null) {

                int mark_num_pages = 1; // default: use only the first page of mark
                if (m_multistamp_b || m_multibackground_b) { // use all pages of mark
                  mark_num_pages = mark_p.getNumberOfPages();
                }

                // the mark information; initialized inside loop
                PdfImportedPage mark_page_p = null;
                Rectangle mark_page_size_p = null;
                int mark_page_rotation = 0;

                // iterate over document's pages, adding mark_page as
                // a layer above (stamp) or below (watermark) the page content;
                // scale mark_page and move it so it fits within the document's page;
                //
                int num_pages = input_reader_p.getNumberOfPages();
                for (int ii = 0; ii < num_pages; ) {
                  ++ii; // page refs are 1-based, not 0-based

                  // the mark page and its geometry
                  if (ii <= mark_num_pages) {
                    mark_page_size_p = mark_p.getCropBox(ii);
                    mark_page_rotation = mark_p.getPageRotation(ii);
                    for (int mm = 0; mm < mark_page_rotation; mm += 90) {
                      mark_page_size_p = mark_page_size_p.rotate();
                    }

                    // create a PdfTemplate from the first page of mark
                    // (PdfImportedPage is derived from PdfTemplate)
                    mark_page_p = writer_p.getImportedPage(mark_p, ii);
                  }

                  // the target page geometry
                  Rectangle doc_page_size_p = input_reader_p.getCropBox(ii);
                  int doc_page_rotation = input_reader_p.getPageRotation(ii);
                  for (int mm = 0; mm < doc_page_rotation; mm += 90) {
                    doc_page_size_p = doc_page_size_p.rotate();
                  }

                  float h_scale = doc_page_size_p.width() / mark_page_size_p.width();
                  float v_scale = doc_page_size_p.height() / mark_page_size_p.height();
                  float mark_scale = (h_scale < v_scale) ? h_scale : v_scale;

                  float h_trans =
                      (float)
                          (doc_page_size_p.left()
                              - mark_page_size_p.left() * mark_scale
                              + (doc_page_size_p.width() - mark_page_size_p.width() * mark_scale)
                                  / 2.0);
                  float v_trans =
                      (float)
                          (doc_page_size_p.bottom()
                              - mark_page_size_p.bottom() * mark_scale
                              + (doc_page_size_p.height() - mark_page_size_p.height() * mark_scale)
                                  / 2.0);

                  PdfContentByte content_byte_p =
                      (background_b) ? writer_p.getUnderContent(ii) : writer_p.getOverContent(ii);

                  if (mark_page_rotation == 0) {
                    content_byte_p.addTemplate(
                        mark_page_p, mark_scale, 0, 0, mark_scale, h_trans, v_trans);
                  } else if (mark_page_rotation == 90) {
                    content_byte_p.addTemplate(
                        mark_page_p,
                        0,
                        -1 * mark_scale,
                        mark_scale,
                        0,
                        h_trans,
                        v_trans + mark_page_size_p.height() * mark_scale);
                  } else if (mark_page_rotation == 180) {
                    content_byte_p.addTemplate(
                        mark_page_p,
                        -1 * mark_scale,
                        0,
                        0,
                        -1 * mark_scale,
                        h_trans + mark_page_size_p.width() * mark_scale,
                        v_trans + mark_page_size_p.height() * mark_scale);
                  } else if (mark_page_rotation == 270) {
                    content_byte_p.addTemplate(
                        mark_page_p,
                        0,
                        mark_scale,
                        -1 * mark_scale,
                        0,
                        h_trans + mark_page_size_p.width() * mark_scale,
                        v_trans);
                  }
                }
              }

              // attach file to document?
              if (!m_input_attach_file_filename.isEmpty()) {
                this.attach_files(input_reader_p, writer_p);
              }

              // performed in add_reader(), but this eliminates objects after e.g. drop_xfa,
              // drop_xmp
              input_reader_p.removeUnusedObjects();

              // done; write output
              writer_p.close();
            }
            break;

          case dump_data_fields_k:
          case dump_data_annots_k:
          case dump_data_k:
            { // report on input document

              // we should have been given only a single, input file
              if (1 < m_input_pdf.size()) { // error
                System.err.println(
                    "Error: Only one input PDF file may be used for the dump_data operation");
                System.err.println("   No output created.");
                ret_val = ErrorCode.ERROR;
                break;
              }

              PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

              try {
                PrintStream ofs = pdftk.get_print_stream(m_output_filename, m_output_utf8_b);
                if (m_operation == keyword.dump_data_k) {
                  report.ReportOnPdf(ofs, input_reader_p, m_output_utf8_b);
                } else if (m_operation == keyword.dump_data_fields_k) {
                  report.ReportAcroFormFields(ofs, input_reader_p, m_output_utf8_b);
                } else if (m_operation == keyword.dump_data_annots_k) {
                  report.ReportAnnots(ofs, input_reader_p, m_output_utf8_b);
                }
              } catch (FileNotFoundException e) { // error
                System.err.println("Error: unable to open file for output: " + m_output_filename);
              }
            }
            break;

          case generate_fdf_k:
            { // create a dummy FDF file that would work with the input PDF form

              // we should have been given only a single, input file
              if (1 < m_input_pdf.size()) { // error
                System.err.println(
                    "Error: Only one input PDF file may be used for the generate_fdf operation");
                System.err.println("   No output created.");
                ret_val = ErrorCode.ERROR;
                break;
              }

              PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

              OutputStream ofs_p =
                  pdftk.get_output_stream(m_output_filename, m_ask_about_warnings_b);
              if (ofs_p != null) {
                FdfWriter writer_p = new FdfWriter();
                input_reader_p.getAcroFields().exportAsFdf(writer_p);
                writer_p.writeTo(ofs_p);
                // no writer_p->close() function

                // delete writer_p; // OK? GC? -- NOT okay!
              } else { // error: get_output_stream() reports error
                ret_val = ErrorCode.ERROR;
                break;
              }
            }
            break;

          case unpack_files_k:
            { // copy PDF file attachments into current directory

              // we should have been given only a single, input file
              if (1 < m_input_pdf.size()) { // error
                System.err.println(
                    "Error: Only one input PDF file may be given for \"unpack_files\" op.");
                System.err.println("   No output created.");
                ret_val = ErrorCode.ERROR;
                break;
              }

              PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

              this.unpack_files(input_reader_p);
            }
            break;
          default:
            // error
            System.err.println("Unexpected pdftk Error in create_output()");
            ret_val = ErrorCode.BUG;
            break;
        }
      } catch (Throwable t_p) {
        System.err.println("Unhandled Java Exception in create_output():");
        t_p.printStackTrace();
        ret_val = ErrorCode.BUG;
      }
    } else { // error
      ret_val = ErrorCode.ERROR;
    }

    return ret_val;
  }

  private enum ArgState {
    input_files_e,
    input_pw_e,

    page_seq_e,
    form_data_filename_e,

    attach_file_filename_e,
    attach_file_pagenum_e,

    update_info_filename_e,
    update_xmp_filename_e,

    output_e, // state where we expect output_k, next
    output_filename_e,

    output_args_e, // output args are order-independent; switch here
    output_owner_pw_e,
    output_user_pw_e,
    output_user_perms_e,

    background_filename_e,
    stamp_filename_e,

    done_e
  };

  class ArgStateMutable {
    ArgState value;
  }

  // convenience function; return true iff handled
  private boolean handle_some_output_options(TK_Session.keyword kw, ArgStateMutable arg_state_p) {
    switch (kw) {
      case output_k:
        // added this case for the burst operation and "output" support;
        // also helps with backward compatibility of the "background" feature
        // change state
        arg_state_p.value = ArgState.output_filename_e;
        break;

        // state-altering keywords
      case owner_pw_k:
        // change state
        arg_state_p.value = ArgState.output_owner_pw_e;
        break;
      case user_pw_k:
        // change state
        arg_state_p.value = ArgState.output_user_pw_e;
        break;
      case user_perms_k:
        // change state
        arg_state_p.value = ArgState.output_user_perms_e;
        break;

        ////
        // no arguments to these keywords, so the state remains unchanged
      case encrypt_40bit_k:
        m_output_encryption_strength = encryption_strength.bits40_enc;
        break;
      case encrypt_128bit_k:
        m_output_encryption_strength = encryption_strength.bits128_enc;
        break;
      case filt_uncompress_k:
        m_output_uncompress_b = true;
        break;
      case filt_compress_k:
        m_output_compress_b = true;
        break;
      case flatten_k:
        m_output_flatten_b = true;
        break;
      case need_appearances_k:
        m_output_need_appearances_b = true;
        break;
      case drop_xfa_k:
        m_output_drop_xfa_b = true;
        break;
      case drop_xmp_k:
        m_output_drop_xmp_b = true;
        break;
      case keep_first_id_k:
        m_output_keep_first_id_b = true;
        break;
      case keep_final_id_k:
        m_output_keep_final_id_b = true;
        break;
      case verbose_k:
        m_verbose_reporting_b = true;
        break;
      case dont_ask_k:
        m_ask_about_warnings_b = false;
        break;
      case do_ask_k:
        m_ask_about_warnings_b = true;
        break;

      case background_k:
        if (m_operation != keyword.filter_k) { // warning
          System.err.println(
              "Warning: the \"background\" output option works only in filter mode.");
          System.err.println("  This means it won't work in combination with \"cat\", \"burst\",");
          System.err.println("  \"attach_file\", etc.  To run pdftk in filter mode, simply omit");
          System.err.println(
              "  the operation, e.g.: pdftk in.pdf output out.pdf background back.pdf");
          System.err.println(
              "  Or, use background as an operation; this is the preferred technique:");
          System.err.println("    pdftk in.pdf background back.pdf output out.pdf");
        }
        // change state
        arg_state_p.value = ArgState.background_filename_e;
        break;

      default: // not handled here; no change to *arg_state_p
        return false;
    }

    return true;
  }
};
