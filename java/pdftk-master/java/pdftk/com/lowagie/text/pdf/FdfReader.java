/* -*- Mode: Java; tab-width: 4; c-basic-offset: 4 -*- */
/*
 * Copyright 2003 by Paulo Soares.
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.URL;
/** Reads an FDF form and makes the fields available
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class FdfReader extends PdfReader {
    
    HashMap fields;
    String fileSpec;
    PdfName encoding;
    
    /** Reads an FDF form.
     * @param filename the file name of the form
     * @throws IOException on error
     */    
    public FdfReader(String filename) throws IOException {
        super(filename);
    }
    
    /** Reads an FDF form.
     * @param pdfIn the byte array with the form
     * @throws IOException on error
     */    
    public FdfReader(byte pdfIn[]) throws IOException {
        super(pdfIn);
    }
    
    /** Reads an FDF form.
     * @param url the URL of the document
     * @throws IOException on error
     */    
    public FdfReader(URL url) throws IOException {
        super(url);
    }
    
    /** Reads an FDF form.
     * @param is the <CODE>InputStream</CODE> containing the document. The stream is read to the
     * end but is not closed
     * @throws IOException on error
     */    
    public FdfReader(InputStream is) throws IOException {
        super(is);
    }
    
    protected void readPdf() throws IOException {
        fields = new HashMap();
        try {
            tokens.checkFdfHeader();
            rebuildXref();
            readDocObj();
        }
        finally {
            try {
                tokens.close();
            }
            catch (Exception e) {
                // empty on purpose
            }
        }
        readFields();
    }
    
    protected void kidNode(PdfDictionary merged, String name) {
        PdfArray kids = (PdfArray)getPdfObject(merged.get(PdfName.KIDS));
        if (kids == null || kids.getArrayList().size() == 0) {
            if (name.length() > 0)
                name = name.substring(1);
            fields.put(name, merged);
        }
        else {
            merged.remove(PdfName.KIDS);
            ArrayList ar = kids.getArrayList();
            for (int k = 0; k < ar.size(); ++k) {
                PdfDictionary dic = new PdfDictionary();
                dic.merge(merged);
                PdfDictionary newDic = (PdfDictionary)getPdfObject((PdfObject)ar.get(k));
                PdfString t = (PdfString)getPdfObject(newDic.get(PdfName.T));
                String newName = name;
                if (t != null)
                    newName += "." + t.toUnicodeString();
                dic.merge(newDic);
                dic.remove(PdfName.T);
                kidNode(dic, newName);
            }
        }
    }
    
    protected void readFields() throws IOException {
        catalog = (PdfDictionary)getPdfObject(trailer.get(PdfName.ROOT));
        PdfDictionary fdf = (PdfDictionary)getPdfObject(catalog.get(PdfName.FDF));
        PdfString fs = (PdfString)getPdfObject(fdf.get(PdfName.F));
        if (fs != null)
            fileSpec = fs.toUnicodeString();
        PdfArray fld = (PdfArray)getPdfObject(fdf.get(PdfName.FIELDS));
        if (fld == null)
            return;
        encoding = (PdfName)getPdfObject(fdf.get(PdfName.ENCODING));
        PdfDictionary merged = new PdfDictionary();
        merged.put(PdfName.KIDS, fld);
        kidNode(merged, "");
    }

    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the value is a merged <CODE>PdfDictionary</CODE>
     * with the field content.
     * @return all the fields
     */    
    public HashMap getFields() {
        return fields;
    }
    
    /** Gets the field dictionary.
     * @param name the fully qualified field name
     * @return the field dictionary
     */    
    public PdfDictionary getField(String name) {
        return (PdfDictionary)fields.get(name);
    }
    
    /** Gets the field value or <CODE>null</CODE> if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * @return the field value or <CODE>null</CODE>
     */    
    public String getFieldValue(String name) {
        PdfDictionary field = (PdfDictionary)fields.get(name);
        if (field == null)
            return null;
        PdfObject v = getPdfObject(field.get(PdfName.V));
        if (v == null)
            return null;
        if (v.isName())
            return PdfName.decodeName(((PdfName)v).toString());
        else if (v.isString()) {
            PdfString vs = (PdfString)v;
            if (encoding == null || vs.getEncoding() != null)
                return vs.toUnicodeString();
            byte b[] = vs.getBytes();
            if (b.length >= 2 && b[0] == (byte)254 && b[1] == (byte)255)
                return vs.toUnicodeString();
            try {
                if (encoding.equals(PdfName.SHIFT_JIS))
                    return new String(b, "SJIS");
                else if (encoding.equals(PdfName.UHC))
                    return new String(b, "MS949");
                else if (encoding.equals(PdfName.GBK))
                    return new String(b, "GBK");
                else if (encoding.equals(PdfName.BIGFIVE))
                    return new String(b, "Big5");
            }
            catch (Exception e) {
            }
            return vs.toUnicodeString();
        }
        return null;
    }
    
    // ssteward
    // in a PDF, the Rich Text value of a field may be stored in
    // a string or a stream; I wonder if this applied to FDF, too?
    public String getFieldRichValue(String name) {
        PdfDictionary field = (PdfDictionary)fields.get(name);
        if (field == null)
            return null;
        PdfObject rv = getPdfObject(field.get(PdfName.RV));
        if (rv == null)
            return null;
        if (rv.isName())
            return PdfName.decodeName(((PdfName)rv).toString());
        else if (rv.isString())
            return ((PdfString)rv).toUnicodeString();
        else
            return null;
	}

    /** Gets the PDF file specification contained in the FDF.
     * @return the PDF file specification contained in the FDF
     */    
    public String getFileSpec() {
        return fileSpec;
    }
}