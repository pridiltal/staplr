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

class PdfBookmark {
  static final String m_prefix = "Bookmark";
  static final String m_begin_mark = "BookmarkBegin";
  static final String m_title_label = "BookmarkTitle:";
  static final String m_level_label = "BookmarkLevel:";
  static final String m_page_number_label = "BookmarkPageNumber:";
  // static const string m_empty_string;

  String m_title = null;
  int m_level = -1;
  int m_page_num = -1; // zero means no destination

  boolean valid() {
    return (0 < m_level && 0 <= m_page_num && m_title != null);
  }

  public String toString() {
    return m_begin_mark
        + System.lineSeparator()
        + m_title_label
        + " "
        + m_title
        + System.lineSeparator()
        + m_level_label
        + " "
        + m_level
        + System.lineSeparator()
        + m_page_number_label
        + " "
        + m_page_num
        + System.lineSeparator();
  }

  boolean loadTitle(String buff) {
    LoadableString loader = new LoadableString(m_title);
    boolean success = loader.LoadString(buff, m_title_label);
    m_title = loader.ss;
    return success;
  }

  boolean loadLevel(String buff) {
    LoadableInt loader = new LoadableInt(m_level);
    boolean success = loader.LoadInt(buff, m_level_label);
    m_level = loader.ii;
    return success;
  }

  boolean loadPageNum(String buff) {
    LoadableInt loader = new LoadableInt(m_page_num);
    boolean success = loader.LoadInt(buff, m_page_number_label);
    m_page_num = loader.ii;
    return success;
  }
};
//
// ostream& operator<<( ostream& ss, const PdfBookmark& bb );
