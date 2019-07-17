/*
 * $Id: PdfBorderDictionary.java,v 1.22 2002/06/20 13:28:20 blowagie Exp $
 * $Name:  $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
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

package pdftk.com.lowagie.text.pdf;

/**
 * A <CODE>PdfBorderDictionary</CODE> define the appearance of a Border (Annotations).
 *
 * @see		PdfDictionary
 */

public class PdfBorderDictionary extends PdfDictionary {
    
    public static final int STYLE_SOLID = 0;
    public static final int STYLE_DASHED = 1;
    public static final int STYLE_BEVELED = 2;
    public static final int STYLE_INSET = 3;
    public static final int STYLE_UNDERLINE = 4;
    // constructors
    
/**
 * Constructs a <CODE>PdfBorderDictionary</CODE>.
 */
    
    public PdfBorderDictionary(float borderWidth, int borderStyle, PdfDashPattern dashes) {
        put(PdfName.W, new PdfNumber(borderWidth));
        switch (borderStyle) {
            case STYLE_SOLID:
                put(PdfName.S, PdfName.S);
                break;
            case STYLE_DASHED:
                if (dashes != null)
                    put(PdfName.D, dashes);
                put(PdfName.S, PdfName.D);
                break;
            case STYLE_BEVELED:
                put(PdfName.S, PdfName.B);
                break;
            case STYLE_INSET:
                put(PdfName.S, PdfName.I);
                break;
            case STYLE_UNDERLINE:
                put(PdfName.S, PdfName.U);
                break;
            default:
                throw new IllegalArgumentException("Invalid border style.");
        }
    }
    
    public PdfBorderDictionary(float borderWidth, int borderStyle) {
        this(borderWidth, borderStyle, null);
    }
}