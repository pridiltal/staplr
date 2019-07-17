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

class LoadableString {
  String ss = null;

  static String BufferString(String buff, int buff_ii) {
    // while( buff[buff_ii] && isspace(buff[buff_ii]) ) { ++buff_ii; }
    if (buff_ii >= buff.length()) return "";
    if (Character.isWhitespace(buff.charAt(buff_ii))) // one or no spaces before data
    ++buff_ii;
    return (buff.substring(buff_ii));
  }

  boolean LoadString(String buff, String label) {
    int label_len = label.length();
    if (buff.startsWith(label)) {
      if (ss == null) {
        ss = BufferString(buff, label_len);
      } else { // warning
        System.err.println(
            "pdftk Warning: "
                + label
                + " ("
                + ss
                + ") already loaded when reading new "
                + label
                + " ("
                + BufferString(buff, label_len)
                + ") -- skipping newer item");
      }
      return true;
    }
    return false;
  }

  LoadableString(String ss) {
    this.ss = ss;
  }

  public String toString() {
    return ss;
  }
};
