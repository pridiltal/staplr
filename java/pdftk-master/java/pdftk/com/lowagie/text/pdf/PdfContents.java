/*
 * $Id: PdfContents.java,v 1.26 2002/06/20 13:28:22 blowagie Exp $
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

import pdftk.com.lowagie.text.DocWriter;
import pdftk.com.lowagie.text.Document;
import pdftk.com.lowagie.text.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

/**
 * <CODE>PdfContents</CODE> is a <CODE>PdfStream</CODE> containing the contents (text + graphics) of a <CODE>PdfPage</CODE>.
 */

class PdfContents extends PdfStream {
    
    static final byte SAVESTATE[] = DocWriter.getISOBytes("q\n");
    static final byte RESTORESTATE[] = DocWriter.getISOBytes("Q\n");
    static final byte ROTATE90[] = DocWriter.getISOBytes("0 1 -1 0 ");
    static final byte ROTATE180[] = DocWriter.getISOBytes("-1 0 0 -1 ");
    static final byte ROTATE270[] = DocWriter.getISOBytes("0 -1 1 0 ");
    static final byte ROTATEFINAL[] = DocWriter.getISOBytes(" cm\n");
    // constructor
    
/**
 * Constructs a <CODE>PdfContents</CODE>-object, containing text and general graphics.
 *
 * @param under the direct content that is under all others
 * @param content the graphics in a page
 * @param text the text in a page
 * @param secondContent the direct content that is over all others
 * @throws BadPdfFormatException on error
 */
    
    PdfContents(PdfContentByte under, PdfContentByte content, PdfContentByte text, PdfContentByte secondContent, Rectangle page) throws BadPdfFormatException {
        super();
        try {
            OutputStream out = null;
            streamBytes = new ByteArrayOutputStream();
            if (Document.compress)
            {
                compressed = true;
                out = new DeflaterOutputStream(streamBytes);
            }
            else
                out = streamBytes;
            int rotation = page.getRotation();
            switch (rotation) {
                case 90:
                    out.write(ROTATE90);
                    out.write(DocWriter.getISOBytes(ByteBuffer.formatDouble(page.top())));
                    out.write(' ');
                    out.write('0');
                    out.write(ROTATEFINAL);
                    break;
                case 180:
                    out.write(ROTATE180);
                    out.write(DocWriter.getISOBytes(ByteBuffer.formatDouble(page.right())));
                    out.write(' ');
                    out.write(DocWriter.getISOBytes(ByteBuffer.formatDouble(page.top())));
                    out.write(ROTATEFINAL);
                    break;
                case 270:
                    out.write(ROTATE270);
                    out.write('0');
                    out.write(' ');
                    out.write(DocWriter.getISOBytes(ByteBuffer.formatDouble(page.right())));
                    out.write(ROTATEFINAL);
                    break;
            }
            if (under.size() > 0) {
                out.write(SAVESTATE);
                under.getInternalBuffer().writeTo(out);
                out.write(RESTORESTATE);
            }
            if (content.size() > 0) {
                out.write(SAVESTATE);
                content.getInternalBuffer().writeTo(out);
                out.write(RESTORESTATE);
            }
            if (text != null) {
                out.write(SAVESTATE);
                text.getInternalBuffer().writeTo(out);
                out.write(RESTORESTATE);
            }
            if (secondContent.size() > 0) {
                secondContent.getInternalBuffer().writeTo(out);
            }
            out.close();
        }
        catch (Exception e) {
            throw new BadPdfFormatException(e.getMessage());
        }
        put(PdfName.LENGTH, new PdfNumber(streamBytes.size()));
        if (compressed)
            put(PdfName.FILTER, PdfName.FLATEDECODE);
    }
}