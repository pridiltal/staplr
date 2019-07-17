/*
 * Copyright 2003 Paulo Soares
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

/** The graphic state dictionary.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfGState extends PdfDictionary {
    /** A possible blend mode */
    public static final PdfName BM_NORMAL = new PdfName("Normal");
    /** A possible blend mode */
    public static final PdfName BM_COMPATIBLE = new PdfName("Compatible");
    /** A possible blend mode */
    public static final PdfName BM_MULTIPLY = new PdfName("Multiply");
    /** A possible blend mode */
    public static final PdfName BM_SCREEN = new PdfName("Screen");
    /** A possible blend mode */
    public static final PdfName BM_OVERLAY = new PdfName("Overlay");
    /** A possible blend mode */
    public static final PdfName BM_DARKEN = new PdfName("Darken");
    /** A possible blend mode */
    public static final PdfName BM_LIGHTEN = new PdfName("Lighten");
    /** A possible blend mode */
    public static final PdfName BM_COLORDODGE = new PdfName("ColorDodge");
    /** A possible blend mode */
    public static final PdfName BM_COLORBURN = new PdfName("ColorBurn");
    /** A possible blend mode */
    public static final PdfName BM_HARDLIGHT = new PdfName("HardLight");
    /** A possible blend mode */
    public static final PdfName BM_SOFTLIGHT = new PdfName("SoftLight");
    /** A possible blend mode */
    public static final PdfName BM_DIFFERENCE = new PdfName("Difference");
    /** A possible blend mode */
    public static final PdfName BM_EXCLUSION = new PdfName("Exclusion");
    
    /**
     * Sets the flag whether to apply overprint for stroking.
     * @param ov
     */
    public void setOverPrintStroking(boolean ov) {
        put(PdfName.OP, ov ? PdfBoolean.PDFTRUE : PdfBoolean.PDFFALSE);
    }

    /**
     * Sets the flag whether to apply overprint for non stroking painting operations.
     * @param ov
     */
    public void setOverPrintNonStroking(boolean ov) {
        put(PdfName.op, ov ? PdfBoolean.PDFTRUE : PdfBoolean.PDFFALSE);
    }
    
    /**
     * Sets the current stroking alpha constant, specifying the constant shape or
     * constant opacity value to be used for stroking operations in the transparent
     * imaging model.
     * @param n
     */
    public void setStrokeOpacity(float n) {
        put(PdfName.CA, new PdfNumber(n));
    }
    
    /**
     * Sets the current stroking alpha constant, specifying the constant shape or
     * constant opacity value to be used for nonstroking operations in the transparent
     * imaging model.
     * @param n
     */
    public void setFillOpacity(float n) {
        put(PdfName.ca, new PdfNumber(n));
    }
    
    /**
     * The alpha source flag specifying whether the current soft mask
     * and alpha constant are to be interpreted as shape values (true)
     * or opacity values (false). 
     * @param v
     */
    public void setAlphaIsShape(boolean v) {
        put(PdfName.AIS, v ? PdfBoolean.PDFTRUE : PdfBoolean.PDFFALSE);
    }
    
    /**
     * Determines the behaviour of overlapping glyphs within a text object
     * in the transparent imaging model.
     * @param v
     */
    public void setTextKnockout(boolean v) {
        put(PdfName.TK, v ? PdfBoolean.PDFTRUE : PdfBoolean.PDFFALSE);
    }
    
    /**
     * The current blend mode to be used in the transparent imaging model.
     * @param bm
     */
    public void setBlendMode(PdfName bm) {
        put(PdfName.BM, bm);
    }
    
}
