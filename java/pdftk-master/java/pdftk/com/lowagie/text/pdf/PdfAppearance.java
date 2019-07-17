/* -*- Mode: Java; tab-width: 4; c-basic-offset: 4 -*- */
/*
 * Copyright 2002 Paulo Soares
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
import pdftk.com.lowagie.text.Rectangle;
import java.util.HashMap;

/**
 * Implements the appearance stream to be used with form fields..
 */

public class PdfAppearance extends PdfTemplate {
    
    public static final HashMap stdFieldFontNames = new HashMap();
    static {
        stdFieldFontNames.put("Courier-BoldOblique", new PdfName("CoBO"));
        stdFieldFontNames.put("Courier-Bold", new PdfName("CoBo"));
        stdFieldFontNames.put("Courier-Oblique", new PdfName("CoOb"));
        stdFieldFontNames.put("Courier", new PdfName("Cour"));
        stdFieldFontNames.put("Helvetica-BoldOblique", new PdfName("HeBO"));
        stdFieldFontNames.put("Helvetica-Bold", new PdfName("HeBo"));
        stdFieldFontNames.put("Helvetica-Oblique", new PdfName("HeOb"));
        stdFieldFontNames.put("Helvetica", new PdfName("Helv"));
        stdFieldFontNames.put("Symbol", new PdfName("Symb"));
        stdFieldFontNames.put("Times-BoldItalic", new PdfName("TiBI"));
        stdFieldFontNames.put("Times-Bold", new PdfName("TiBo"));
        stdFieldFontNames.put("Times-Italic", new PdfName("TiIt"));
        stdFieldFontNames.put("Times-Roman", new PdfName("TiRo"));
        stdFieldFontNames.put("ZapfDingbats", new PdfName("ZaDb"));
        stdFieldFontNames.put("HYSMyeongJo-Medium", new PdfName("HySm"));
        stdFieldFontNames.put("HYGoThic-Medium", new PdfName("HyGo"));
        stdFieldFontNames.put("HeiseiKakuGo-W5", new PdfName("KaGo"));
        stdFieldFontNames.put("HeiseiMin-W3", new PdfName("KaMi"));
        stdFieldFontNames.put("MHei-Medium", new PdfName("MHei"));
        stdFieldFontNames.put("MSung-Light", new PdfName("MSun"));
        stdFieldFontNames.put("STSong-Light", new PdfName("STSo"));
        stdFieldFontNames.put("MSungStd-Light", new PdfName("MSun"));
        stdFieldFontNames.put("STSongStd-Light", new PdfName("STSo"));
        stdFieldFontNames.put("HYSMyeongJoStd-Medium", new PdfName("HySm"));
        stdFieldFontNames.put("KozMinPro-Regular", new PdfName("KaMi"));
    }
    
    /**
     *Creates a <CODE>PdfAppearance</CODE>.
     */
    
    PdfAppearance() {
        super();
        separator = ' ';
    }
    
    PdfAppearance(PdfIndirectReference iref) {
        thisReference = iref;
    }
    
    /**
     * Creates new PdfTemplate
     *
     * @param wr the <CODE>PdfWriter</CODE>
     */
    
    PdfAppearance(PdfWriter wr) {
        super(wr);
        separator = ' ';
    }
    
    /**
     * Set the font and the size for the subsequent text writing.
     *
     * @param bf the font
     * @param size the font size in points
     */
    public void setFontAndSize(BaseFont bf, float size) {
        checkWriter();
        state.size = size;
        if (bf.getFontType() == BaseFont.FONT_TYPE_DOCUMENT) {
            state.fontDetails = new FontDetails(null, ((DocumentFont)bf).getIndirectReference(), bf);
        }
        else
            state.fontDetails = writer.addSimple(bf);
        PdfName psn = (PdfName)stdFieldFontNames.get(bf.getPostscriptFontName());
        if (psn == null) {
            psn = new PdfName(bf.getPostscriptFontName());
            bf.setSubset(false);
        }
        PageResources prs = getPageResources();
//        PdfName name = state.fontDetails.getFontName();
        prs.addFont(psn, state.fontDetails.getIndirectReference());
        content.append(psn.getBytes()).append(' ').append(size).append(" Tf").append_i(separator);
    }

    public PdfContentByte getDuplicate() {
        PdfAppearance tpl = new PdfAppearance();
        tpl.writer = writer;
        tpl.pdf = pdf;
        tpl.thisReference = thisReference;
        tpl.pageResources = pageResources;
        tpl.bBox = new Rectangle(bBox);
        tpl.group = group;
        tpl.layer = layer;
        if (matrix != null) {
            tpl.matrix = new PdfArray(matrix);
        }
        tpl.separator = separator;
        return tpl;
    }
}