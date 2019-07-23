/*
 * $Id: ColorDetails.java,v 1.11 2002/06/20 13:06:47 blowagie Exp $
 * $Name:  $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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

import java.io.IOException;
/** Each spotcolor in the document will have an instance of this class
 *
 * @author Phillip Pan (phillip@formstar.com)
 */
class ColorDetails {

    /** The indirect reference to this color
     */
    PdfIndirectReference indirectReference;
    /** The color name that appears in the document body stream
     */
    PdfName colorName;
    /** The color
     */
    PdfSpotColor spotcolor;

    /** Each spot color used in a document has an instance of this class.
     * @param colorName the color name
     * @param indirectReference the indirect reference to the font
     * @param scolor the <CODE>PDfSpotColor</CODE>
     */
    ColorDetails(PdfName colorName, PdfIndirectReference indirectReference, PdfSpotColor scolor) {
        this.colorName = colorName;
        this.indirectReference = indirectReference;
        this.spotcolor = scolor;
    }

    /** Gets the indirect reference to this color.
     * @return the indirect reference to this color
     */
    PdfIndirectReference getIndirectReference() {
        return indirectReference;
    }

    /** Gets the color name as it appears in the document body.
     * @return the color name
     */
    PdfName getColorName() {
        return colorName;
    }

    /** Gets the <CODE>SpotColor</CODE> object.
     * @return the <CODE>PdfSpotColor</CODE>
     */
    PdfObject getSpotColor(PdfWriter writer) throws IOException {
        return spotcolor.getSpotObject(writer);
    }
}
