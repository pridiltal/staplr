/*
 * Copyright 2002 by Paulo Soares.
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

/** Implements the signature dictionary.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfSignature extends PdfDictionary {

    /** Creates new PdfSignature */
    public PdfSignature(PdfName filter, PdfName subFilter) {
        super(PdfName.SIG);
        put(PdfName.FILTER, filter);
        put(PdfName.SUBFILTER, subFilter);
    }
    
    public void setByteRange(int range[]) {
        PdfArray array = new PdfArray();
        for (int k = 0; k < range.length; ++k)
            array.add(new PdfNumber(range[k]));
        put(PdfName.BYTERANGE, array);
    }
    
    public void setContents(byte contents[]) {
        put(PdfName.CONTENTS, new PdfString(contents, PdfObject.NOTHING).setHexWriting(true)); // ssteward: added encoding
    }
    
    public void setCert(byte cert[]) {
        put(PdfName.CERT, new PdfString(cert, PdfObject.NOTHING)); // ssteward: added encoding
    }
    
    public void setName(String name) {
        put(PdfName.NAME, new PdfString(name, PdfObject.TEXT_UNICODE));
    }

    public void setDate(PdfDate date) {
        put(PdfName.M, date);
    }

    public void setLocation(String name) {
        put(PdfName.LOCATION, new PdfString(name, PdfObject.TEXT_UNICODE));
    }

    public void setReason(String name) {
        put(PdfName.REASON, new PdfString(name, PdfObject.TEXT_UNICODE));
    }
    
    public void setContact(String name) {
        put(PdfName.CONTACTINFO, new PdfString(name, PdfObject.TEXT_UNICODE));
    }
}