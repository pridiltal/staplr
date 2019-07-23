/*
 * $Id: SplitCharacter.java,v 1.12 2002/06/20 13:30:25 blowagie Exp $
 * $Name:  $
 *
 * Copyright 2001, 2002 by Paulo Soares
 *
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package pdftk.com.lowagie.text;

import pdftk.com.lowagie.text.pdf.PdfChunk;

/** Interface for customizing the split character.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public interface SplitCharacter {
    
    /**
     * Returns <CODE>true</CODE> if the character can split a line. The splitting implementation
     * is free to look ahead or look behind characters to make a decision.
     * <p>
     * The default implementation is:
     * <p>
     * <pre>
     * public boolean isSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) {
     *    char c;
     *    if (ck == null)
     *        c = cc[current];
     *    else
     *        c = ck[Math.min(current, ck.length - 1)].getUnicodeEquivalent(cc[current]);
     *    if (c <= ' ' || c == '-') {
     *        return true;
     *    }
     *    if (c < 0x2e80)
     *        return false;
     *    return ((c >= 0x2e80 && c < 0xd7a0)
     *    || (c >= 0xf900 && c < 0xfb00)
     *    || (c >= 0xfe30 && c < 0xfe50)
     *    || (c >= 0xff61 && c < 0xffa0));
     * }
     * </pre>
     * @param start the lower limit of <CODE>cc</CODE> inclusive
     * @param current the pointer to the character in <CODE>cc</CODE>
     * @param end the upper limit of <CODE>cc</CODE> exclusive
     * @param cc an array of characters at least <CODE>end</CODE> sized
     * @param ck an array of <CODE>PdfChunk</CODE>. The main use is to be able to call
     * {@link PdfChunk#getUnicodeEquivalent(char)}. It may be <CODE>null</CODE>
     * or shorter than <CODE>end</CODE>. If <CODE>null</CODE> no convertion takes place.
     * If shorter than <CODE>end</CODE> the last element is used
     * @return <CODE>true</CODE> if the character(s) can split a line
     */
    
    public boolean isSplitCharacter(int start, int current, int end, char cc[], PdfChunk ck[]);
}
