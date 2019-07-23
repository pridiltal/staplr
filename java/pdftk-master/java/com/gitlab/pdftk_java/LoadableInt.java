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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LoadableInt {
  int ii = -1;
  boolean success;

  static int BufferInt(String buff, int buff_ii) {
    // while( buff[buff_ii] && isspace(buff[buff_ii]) ) { ++buff_ii; }
    Pattern p = Pattern.compile("\\s?(\\d+).*"); // one or no spaces before data
    Matcher m = p.matcher(buff.substring(buff_ii));
    if (m.matches()) {
      return Integer.parseInt(m.group(1));
    } else {
      return 0;
    }
  }

  boolean LoadInt(String buff, String label) {
    int label_len = label.length();
    if (buff.startsWith(label)) {
      if (ii < 0) { // uninitialized ints are -1
        ii = BufferInt(buff, label_len);
      } else { // warning
        System.err.println(
            "pdftk Warning: "
                + label
                + " ("
                + ii
                + ") not empty when reading new "
                + label
                + " ("
                + BufferInt(buff, label_len)
                + ") -- skipping newer item");
      }
      return true;
    }
    return false;
  }

  LoadableInt(int ii) {
    this.ii = ii;
  }

  public String toString() {
    return Integer.toString(ii);
  }
};
