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

class PageRange {
  int beg, end;
  final int m_num_pages;
  final String m_argv;

  PageRange(int num_pages, String argv) {
    m_num_pages = num_pages;
    m_argv = argv;
  }

  int parse_bound(String bound) {
    if (bound.equals("end")) return m_num_pages;
    return Integer.parseInt(bound);
  }

  void out_of_range_error(String where) {
    System.err.println("Error: Range " + where + " page number exceeds size of PDF");
    System.err.println("   here: " + m_argv);
    System.err.println("   input PDF has: " + m_num_pages + " pages.");
    System.err.println("   Exiting.");
  }

  boolean parse(String pre_reverse, String pre_range, String post_reverse, String post_range) {

    beg = 0; // default value
    if (!pre_range.isEmpty()) {
      beg = parse_bound(pre_range);

      if (m_num_pages < beg) {
        // error: page number out of range
        out_of_range_error("start");
        return false;
      }

      boolean pre_reverse_b = (!pre_reverse.isEmpty()); // single lc 'r' before page range
      if (pre_reverse_b) // above test ensures good value here
      beg = m_num_pages - beg + 1;
    }

    end = beg; // default value
    if (post_range != null && !post_range.isEmpty()) {
      end = parse_bound(post_range);

      if (m_num_pages < end) {
        // error: page number out of range
        out_of_range_error("end");
        return false;
      }

      boolean post_reverse_b = (!post_reverse.isEmpty()); // single lc 'r' before page range
      if (post_reverse_b) // above test ensures good value here
      end = m_num_pages - end + 1;
    }

    return true;
  }
};
