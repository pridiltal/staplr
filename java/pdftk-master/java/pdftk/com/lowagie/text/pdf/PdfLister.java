/*
 * $Id: PdfLister.java,v 1.33 2005/02/01 14:30:50 blowagie Exp $
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
 * This class by Mark Thompson. Copyright (C) 2002 Mark Thompson
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

import java.io.*;
import java.util.Iterator;
/**
 * List a PDF file in human-readable form (for debugging reasons mostly)
 * @author Mark Thompson
 */

public class PdfLister {

	/** the printStream you want to write the output to. */
    PrintStream out;

    /**
     * Create a new lister object.
     * @param out
     */
    public PdfLister(PrintStream out) {
        this.out = out;
    }

    /**
     * Visualizes a PDF object.
     * @param object	a pdftk.com.lowagie.text.pdf object
     */
    public void listAnyObject(PdfObject object)
    {
        switch (object.type()) {
        case PdfObject.ARRAY:
            listArray((PdfArray)object);
            break;
        case PdfObject.DICTIONARY:
            listDict((PdfDictionary) object);
            break;
        case PdfObject.STRING:
            out.println("(" + object.toString() + ")");
            break;
        default:
            out.println(object.toString());
            break;
        }
    }
    /**
     * Visualizes a PdfDictionary object.
     * @param dictionary	a pdftk.com.lowagie.text.pdf.PdfDictionary object
     */
    public void listDict(PdfDictionary dictionary)
    {
        out.println("<<");
        PdfName key;
        PdfObject value;
        for (Iterator i = dictionary.getKeys().iterator(); i.hasNext(); ) {
            key = (PdfName) i.next();
            value = (PdfObject) dictionary.get(key);
            out.print(key.toString());
            out.print(' ');
            listAnyObject(value);
        }
        out.println(">>");
    }

    /**
     * Visualizes a PdfArray object.
     * @param array	a pdftk.com.lowagie.text.pdf.PdfArray object
     */
    public void listArray(PdfArray array)
    {
        out.println('[');
        for (Iterator i = array.getArrayList().iterator(); i.hasNext(); ) {
            PdfObject item = (PdfObject)i.next();
            listAnyObject(item);
        }
        out.println(']');
    }
    /**
     * Visualizes a Stream.
     * @param stream
     * @param reader
     */
    public void listStream(PRStream stream, PdfReaderInstance reader)
    {
        try {
            listDict(stream);
            out.println("startstream");
            byte[] b = PdfReader.getStreamBytes(stream);
//                  byte buf[] = new byte[Math.min(stream.getLength(), 4096)];
//                  int r = 0;
//                  stream.openStream(reader);
//                  for (;;) {
//                      r = stream.readStream(buf, 0, buf.length);
//                      if (r == 0) break;
//                      out.write(buf, 0, r);
//                  }
//                  stream.closeStream();
            int len = b.length - 1;
            for (int k = 0; k < len; ++k) {
                if (b[k] == '\r' && b[k + 1] != '\n')
                    b[k] = (byte)'\n';
            }
            out.println(new String(b));
            out.println("endstream");
        } catch (IOException e) {
            System.err.println("I/O exception: " + e);
//          } catch (java.util.zip.DataFormatException e) {
//              System.err.println("Data Format Exception: " + e);
        }
    }
    /**
     * Visualizes an imported page
     * @param iPage
     */
    public void listPage(PdfImportedPage iPage)
    {
        int pageNum = iPage.getPageNumber();
        PdfReaderInstance readerInst = iPage.getPdfReaderInstance();
        PdfReader reader = readerInst.getReader();

        PdfDictionary page = reader.getPageN(pageNum);
        listDict(page);
        PdfObject obj = PdfReader.getPdfObject(page.get(PdfName.CONTENTS));
        switch (obj.type) {
        case PdfObject.STREAM:
            listStream((PRStream)obj, readerInst);
            break;
        case PdfObject.ARRAY:
            for (Iterator i = ((PdfArray)obj).getArrayList().iterator(); i.hasNext();) {
                PdfObject o = PdfReader.getPdfObject((PdfObject)i.next());
                listStream((PRStream)o, readerInst);
                out.println("-----------");
            }
            break;
        }
    }
}
