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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.lang3.StringEscapeUtils;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;

class data_import {

  static PdfData LoadDataFile(InputStream input_stream) {
    PdfData pdf_data_p = new PdfData();
    Scanner ifs = new Scanner(input_stream);

    String buff = "";

    String buff_prev = "";
    int buff_prev_len = 0;

    PdfInfo info = new PdfInfo();
    boolean info_b = false;

    PdfBookmark bookmark = new PdfBookmark();
    boolean bookmark_b = false;

    boolean eof = !ifs.hasNextLine();

    while (!eof) {
      eof = !ifs.hasNextLine();
      if (eof) buff = "";
      else buff = ifs.nextLine();

      if (eof
          || buff.startsWith(PdfInfo.m_begin_mark)
          || buff.startsWith(PdfBookmark.m_begin_mark)
          || buff.startsWith(report.PdfPageLabel.m_begin_mark)
          || !buff_prev.isEmpty()
              && !buff.startsWith(buff_prev)) { // start of a new record or end of file
        // pack data and reset

        if (info_b) {
          if (info.valid()) {
            pdf_data_p.m_info.add(info);
          } else { // warning
            System.err.println("pdftk Warning: data info record not valid -- skipped; data:");
            System.err.print(info);
          }
        } else if (bookmark_b) {
          if (bookmark.valid()) {
            pdf_data_p.m_bookmarks.add(bookmark);
          } else { // warning
            System.err.println("pdftk Warning: data bookmark record not valid -- skipped; data:");
            System.err.print(bookmark);
          }
        }

        // reset
        buff_prev = "";
        buff_prev_len = 0;
        //
        info = new PdfInfo();
        info_b = false;
        //
        bookmark = new PdfBookmark();
        bookmark_b = false;
      }

      // whitespace or comment; skip
      if (buff.isEmpty() || buff.startsWith("#")) {
        continue;
      }

      // info record
      else if (buff.startsWith(PdfInfo.m_prefix)) {
        buff_prev_len = PdfInfo.m_prefix.length();
        info_b = true;

        if (buff.startsWith(PdfInfo.m_begin_mark) || info.loadKey(buff) || info.loadValue(buff)) {
          // success
        } else { // warning
          System.err.println("pdftk Warning: unexpected Info case in LoadDataFile(); continuing");
        }
      }

      // bookmark record
      else if (buff.startsWith(PdfBookmark.m_prefix)) {
        buff_prev_len = PdfBookmark.m_prefix.length();
        bookmark_b = true;

        if (buff.startsWith(PdfBookmark.m_begin_mark)
            || bookmark.loadTitle(buff)
            || bookmark.loadLevel(buff)
            || bookmark.loadPageNum(buff)) {
          // success
        } else { // warning
          System.err.println(
              "pdftk Warning: unexpected Bookmark case in LoadDataFile(); continuing");
        }
      }

      // page label record
      else if (buff.startsWith(report.PdfPageLabel.m_prefix)) {
        buff_prev_len = 0;
        // TODO
      }

      // page media record
      else if (buff.startsWith(report.PdfPageMedia.m_prefix)) {
        buff_prev_len = 0;
        // TODO
      }

      // pdf id
      else if (buff.startsWith(PdfData.m_prefix)) {
        buff_prev_len = 0; // not a record

        if (pdf_data_p.loadID0(buff) || pdf_data_p.loadID1(buff)) {
          // success
        } else { // warning
          System.err.println("pdftk Warning: unexpected PdfID case in LoadDataFile(); continuing");
        }
      }

      // number of pages
      else if (pdf_data_p.loadNumPages(buff)) {
        buff_prev_len = 0; // not a record
      } else { // warning
        System.err.println("pdftk Warning: unexpected case 1 in LoadDataFile(); continuing");
      }

      buff_prev = buff.substring(0, buff_prev_len);
    }

    if (buff_prev_len != 0) { // warning; some incomplete record hasn't been packed
      System.err.println("pdftk Warning in LoadDataFile(): incomplete record;");
    }

    return pdf_data_p;
  }

  static boolean UpdateInfo(PdfReader reader_p, InputStream ifs, boolean utf8_b) {
    boolean ret_val_b = true;

    PdfData pdf_data = LoadDataFile(ifs);
    if (pdf_data != null) {

      { // trailer data
        PdfDictionary trailer_p = reader_p.getTrailer();
        if (trailer_p != null) {

          // bookmarks
          if (!pdf_data.m_bookmarks.isEmpty()) {

            // build bookmarks
            PdfDictionary outlines_p = new PdfDictionary(PdfName.OUTLINES);
            if (outlines_p != null) {
              PRIndirectReference outlines_ref_p = reader_p.getPRIndirectReference(outlines_p);

              int num_bookmarks_total =
                  bookmarks.BuildBookmarks(
                      reader_p,
                      pdf_data.m_bookmarks.listIterator(),
                      outlines_p,
                      outlines_ref_p,
                      0,
                      utf8_b);

              PdfObject root_po = reader_p.getPdfObject(trailer_p.get(PdfName.ROOT));
              if (root_po != null && root_po.isDictionary()) {
                PdfDictionary root_p = (PdfDictionary) root_po;
                if (root_p.contains(PdfName.OUTLINES)) {
                  // erase old bookmarks
                  PdfObject old_outlines_p =
reader_p.getPdfObject(root_p.get(PdfName.OUTLINES));
                  if (old_outlines_p != null && old_outlines_p.isDictionary()) {
                    bookmarks.RemoveBookmarks(reader_p, (PdfDictionary) old_outlines_p);
                  }
                }
                // insert into document
                root_p.put(PdfName.OUTLINES, outlines_ref_p);
              }
            }
          }

          // metadata
          if (!pdf_data.m_info.isEmpty()) {
            PdfObject info_po = reader_p.getPdfObject(trailer_p.get(PdfName.INFO));
            if (info_po != null && info_po.isDictionary()) {
              PdfDictionary info_p = (PdfDictionary) info_po;

              for (PdfInfo it : pdf_data.m_info) {
                if (it.m_value.isEmpty()) {
                  info_p.remove(new PdfName(it.m_key));
                } else {
                  if (utf8_b) { // UTF-8 encoded input
                    // patch by Quentin Godfroy <godfroy@clipper.ens.fr>
                    // and Chris Adams <cadams@salk.edu>
                    info_p.put(new PdfName(it.m_key), new PdfString(it.m_value));
                  } else { // XML entities input
                    String jvs = XmlStringToJcharArray(it.m_value);
                    info_p.put(new PdfName(it.m_key), new PdfString(jvs));
                  }
                }
              }
            } else { // error
              System.err.println("pdftk Error in UpdateInfo(): no Info dictionary found;");
              ret_val_b = false;
            }
          }
        } else { // error
          System.err.println("pdftk Error in UpdateInfo(): no document trailer found;");
          ret_val_b = false;
        }
      }

    } else { // error
      System.err.println("pdftk Error in UpdateInfo(): LoadDataFile() failure;");
    }
    // cerr << pdf_data; // debug

    return ret_val_b;
  }

  //////
  ////
  // created for data import, maybe useful for export, too

  //
  static class PdfInfo {
    static final String m_prefix = "Info";
    static final String m_begin_mark = "InfoBegin";
    static final String m_key_label = "InfoKey:";
    static final String m_value_label = "InfoValue:";

    String m_key = null;
    String m_value = null;

    boolean valid() {
      return (m_key != null && m_value != null);
    }

    public String toString() {
      return m_begin_mark
          + System.lineSeparator()
          + m_key_label
          + " "
          + m_key
          + System.lineSeparator()
          + m_value_label
          + " "
          + m_value
          + System.lineSeparator();
    }

    boolean loadKey(String buff) {
      LoadableString loader = new LoadableString(m_key);
      boolean success = loader.LoadString(buff, m_key_label);
      m_key = loader.ss;
      return success;
    }

    boolean loadValue(String buff) {
      LoadableString loader = new LoadableString(m_value);
      boolean success = loader.LoadString(buff, m_value_label);
      m_value = loader.ss;
      return success;
    }
  };

  static class PdfData {
    ArrayList<PdfInfo> m_info = new ArrayList<PdfInfo>();
    ArrayList<PdfBookmark> m_bookmarks = new ArrayList<PdfBookmark>();

    static final String m_prefix = "PdfID";
    static final String m_id_0_label = "PdfID0:";
    static final String m_id_1_label = "PdfID1:";
    static final String m_num_pages_label = "NumberOfPages:";

    int m_num_pages = -1;

    String m_id_0 = null;
    String m_id_1 = null;

    public String toString() {
      StringBuilder ss = new StringBuilder();
      for (PdfInfo vit : m_info) {
        ss.append(vit);
      }
      ss.append("PdfID0: " + m_id_0 + System.lineSeparator());
      ss.append("PdfID1: " + m_id_1 + System.lineSeparator());
      ss.append("NumberOfPages: " + m_num_pages + System.lineSeparator());
      for (PdfBookmark vit : m_bookmarks) {
        ss.append(vit);
      }
      return ss.toString();
    }

    boolean loadNumPages(String buff) {
      LoadableInt loader = new LoadableInt(m_num_pages);
      boolean success = loader.LoadInt(buff, m_num_pages_label);
      m_num_pages = loader.ii;
      return success;
    }

    boolean loadID0(String buff) {
      LoadableString loader = new LoadableString(m_id_0);
      boolean success = loader.LoadString(buff, m_id_0_label);
      m_id_0 = loader.ss;
      return success;
    }

    boolean loadID1(String buff) {
      LoadableString loader = new LoadableString(m_id_1);
      boolean success = loader.LoadString(buff, m_id_1_label);
      m_id_1 = loader.ss;
      return success;
    }
  };

  static String XmlStringToJcharArray(String jvs) {
    return StringEscapeUtils.unescapeXml(jvs);
  }
};
