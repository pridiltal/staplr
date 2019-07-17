/*
 * $Id: PdfFont.java,v 1.30 2002/07/09 11:28:23 blowagie Exp $
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

// import pdftk.com.lowagie.text.Image; ssteward: dropped in 1.44
import pdftk.com.lowagie.text.ExceptionConverter;

/**
 * <CODE>PdfFont</CODE> is the Pdf Font object.
 * <P>
 * Limitation: in this class only base 14 Type 1 fonts (courier, courier bold, courier oblique,
 * courier boldoblique, helvetica, helvetica bold, helvetica oblique, helvetica boldoblique,
 * symbol, times roman, times bold, times italic, times bolditalic, zapfdingbats) and their
 * standard encoding (standard, MacRoman, (MacExpert,) WinAnsi) are supported.<BR>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 7.7 (page 198-203).
 *
 * @see		PdfName
 * @see		PdfDictionary
 * @see		BadPdfFormatException
 */

class PdfFont implements Comparable {
    
    
    /** the font metrics. */
    private BaseFont font;
    
    /** the size. */
    private float size;
    
    /** an image. */
    // protected Image image; ssteward: dropped in 1.44
    
    protected float hScale = 1;
    
    // constructors
    
    PdfFont(BaseFont bf, float size) {
        this.size = size;
        font = bf;
    }
    
    // methods
    
    /**
     * Compares this <CODE>PdfFont</CODE> with another
     *
     * @param	object	the other <CODE>PdfFont</CODE>
     * @return	a value
     */
    
    public int compareTo(Object object) {
	/* ssteward: dropped in 1.44
        if (image != null)
            return 0;
	*/
        if (object == null) {
            return -1;
        }
        PdfFont pdfFont;
        try {
            pdfFont = (PdfFont) object;
            if (font != pdfFont.font) {
                return 1;
            }
            if (this.size() != pdfFont.size()) {
                return 2;
            }
            return 0;
        }
        catch(ClassCastException cce) {
            return -2;
        }
    }
    
    /**
     * Returns the size of this font.
     *
     * @return		a size
     */
    
    float size() {
	return size;
	/*  ssteward: dropped in 1.44
        if (image == null)
            return size;
        else {
            return image.scaledHeight();
        }
	*/
    }
    
    /**
     * Returns the approximative width of 1 character of this font.
     *
     * @return		a width in Text Space
     */
    
    float width() {
        return width(' ');
    }
    
    /**
     * Returns the width of a certain character of this font.
     *
     * @param		character	a certain character
     * @return		a width in Text Space
     */
    
    float width(char character) {
	return font.getWidthPoint(character, size) * hScale;
	/* ssteward: dropped in 1.44
        if (image == null)
            return font.getWidthPoint(character, size) * hScale;
        else
            return image.scaledWidth();
	*/
    }
    
    float width(String s) {
	return font.getWidthPoint(s, size) * hScale;
	/* ssteward: dropped in 1.44
        if (image == null)
            return font.getWidthPoint(s, size) * hScale;
        else
            return image.scaledWidth();
	*/
    }
    
    BaseFont getFont() {
        return font;
    }
    /*  ssteward: dropped in 1.44
    void setImage(Image image) {
        this.image = image;
    }
    */
    
    static PdfFont getDefaultFont() {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
            return new PdfFont(bf, 12);
        }
        catch (Exception ee) {
            throw new ExceptionConverter(ee);
        }
    }
    void setHorizontalScaling(float hScale) {
        this.hScale = hScale;
    }
}
