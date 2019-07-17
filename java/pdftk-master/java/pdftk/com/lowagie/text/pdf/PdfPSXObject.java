/*
 * Copyright 2005 Paulo Soares
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

/**
 * Implements the PostScript XObject.
 */
public class PdfPSXObject extends PdfTemplate {
    
    /** Creates a new instance of PdfPSXObject */
    protected PdfPSXObject() {
        super();
    }
    
    /**
     * Constructs a PSXObject
     * @param wr
     */
    public PdfPSXObject(PdfWriter wr) {
        super(wr);
    }

    /**
     * Gets the stream representing this object.
     *
     * @return the stream representing this object
     * @throws IOException
     */
    
    PdfStream getFormXObject() throws IOException {
        PdfStream s = new PdfStream(content.toByteArray());
        s.put(PdfName.TYPE, PdfName.XOBJECT);
        s.put(PdfName.SUBTYPE, PdfName.PS);
        s.flateCompress();
        return s;
    }
        
    /**
     * Gets a duplicate of this <CODE>PdfPSXObject</CODE>. All
     * the members are copied by reference but the buffer stays different.
     * @return a copy of this <CODE>PdfPSXObject</CODE>
     */
    
    public PdfContentByte getDuplicate() {
        PdfPSXObject tpl = new PdfPSXObject();
        tpl.writer = writer;
        tpl.pdf = pdf;
        tpl.thisReference = thisReference;
        tpl.pageResources = pageResources;
        tpl.separator = separator;
        return tpl;
    }
}
