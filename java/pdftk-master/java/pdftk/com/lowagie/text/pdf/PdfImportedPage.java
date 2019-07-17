/*
 * $Id: PdfImportedPage.java,v 1.14 2002/06/20 13:30:25 blowagie Exp $
 * $Name:  $
 *
 * Copyright 2001, 2002 Paulo Soares
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
import java.io.IOException;

/** Represents an imported page.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfImportedPage extends pdftk.com.lowagie.text.pdf.PdfTemplate {

    PdfReaderInstance readerInstance;
    int pageNumber;
    
    PdfImportedPage(PdfReaderInstance readerInstance, PdfWriter writer, int pageNumber) {
        this.readerInstance = readerInstance;
        this.pageNumber = pageNumber;
        thisReference = writer.getPdfIndirectReference();
        bBox = readerInstance.getReader().getPageSize(pageNumber);
        type = TYPE_IMPORTED;
    }

    /** Reads the content from this <CODE>PdfImportedPage</CODE>-object from a reader.
     *
     * @return self
     *
     */
    public PdfImportedPage getFromReader() {
      return this;
    }

    public int getPageNumber() {
        return pageNumber;
    }


    /** Always throws an error. This operation is not allowed.
     * @param image dummy
     * @param a dummy
     * @param b dummy
     * @param c dummy
     * @param d dummy
     * @param e dummy
     * @param f dummy
     * @throws DocumentException  dummy */    
    /* ssteward: dropped in 1.44
    public void addImage(Image image, float a, float b, float c, float d, float e, float f) throws DocumentException {
        throwError();
    }
    */
    
    /** Always throws an error. This operation is not allowed.
     * @param template dummy
     * @param a dummy
     * @param b dummy
     * @param c dummy
     * @param d dummy
     * @param e dummy
     * @param f  dummy */    
    public void addTemplate(PdfTemplate template, float a, float b, float c, float d, float e, float f) {
        throwError();
    }
    
    /** Always throws an error. This operation is not allowed.
     * @return  dummy */    
    public PdfContentByte getDuplicate() {
        throwError();
        return null;
    }
    
    PdfStream getFormXObject() throws IOException {
         return readerInstance.getFormXObject(pageNumber);
    }
    
    public void setColorFill(PdfSpotColor sp, float tint) {
        throwError();
    }
    
    public void setColorStroke(PdfSpotColor sp, float tint) {
        throwError();
    }
    
    PdfObject getResources() {
        return readerInstance.getResources(pageNumber);
    }
    
    /** Always throws an error. This operation is not allowed.
     * @param bf dummy
     * @param size dummy */    
    public void setFontAndSize(BaseFont bf, float size) {
        throwError();
    }
    
    void throwError() {
        throw new RuntimeException("Content can not be added to a PdfImportedPage.");
    }
    
    PdfReaderInstance getPdfReaderInstance() {
        return readerInstance;
    }
}
