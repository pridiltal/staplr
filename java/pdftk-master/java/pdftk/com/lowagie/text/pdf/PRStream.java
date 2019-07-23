/*
 * $Id: PRStream.java,v 1.12 2002/06/20 13:30:25 blowagie Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.ArrayList; // ssteward

import pdftk.com.lowagie.text.Document;
import pdftk.com.lowagie.text.ExceptionConverter;

public class PRStream extends PdfStream {
    
    protected PdfReader reader = null;
    protected int offset = 0;
    protected int length = 0;
    
    //added by ujihara for decryption
    protected int objNum = 0;
    protected int objGen = 0;
    
    public PRStream(PRStream stream, PdfDictionary newDic) {
        reader = stream.reader;
        offset = stream.offset;
        length = stream.length;
        compressed = stream.compressed;
        compressionLevel = stream.compressionLevel;
        streamBytes = stream.streamBytes;
        bytes = stream.bytes;
        objNum = stream.objNum;
        objGen = stream.objGen;
        if (newDic != null)
            putAll(newDic);
        else
            hashMap.putAll(stream.hashMap);
    }

    public PRStream(PRStream stream, PdfDictionary newDic, PdfReader reader) {
        this(stream, newDic);
        this.reader = reader;
    }

    public PRStream(PdfReader reader, int offset) {
        this.reader = reader;
        this.offset = offset;
    }

    public PRStream(PdfReader reader, byte conts[]) {
    	this(reader, conts, DEFAULT_COMPRESSION);
    }

    /**
     * Creates a new PDF stream object that will replace a stream
     * in a existing PDF file.
     * @param	reader	the reader that holds the existing PDF
     * @param	conts	the new content
     * @param	compressionLevel	the compression level for the content
     * @since	2.1.3 (replacing the existing constructor without param compressionLevel)
     */
    public PRStream(PdfReader reader, byte[] conts, int compressionLevel) {
        this.reader = reader;
        this.offset = -1;
        if (Document.compress) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Deflater deflater = new Deflater(compressionLevel);
                DeflaterOutputStream zip = new DeflaterOutputStream(stream, deflater);
                zip.write(conts);
                zip.close();
                deflater.end();
                bytes = stream.toByteArray();
            }
            catch (IOException ioe) {
                throw new ExceptionConverter(ioe);
            }
            put(PdfName.FILTER, PdfName.FLATEDECODE);
        }
        else
            bytes = conts;
        setLength(bytes.length);
    }
    
    /**
     * Sets the data associated with the stream, either compressed or
     * uncompressed. Note that the data will never be compressed if
     * Document.compress is set to false.
     * 
     * @param data raw data, decrypted and uncompressed.
     * @param compress true if you want the stream to be compressed.
     * @since	iText 2.1.1
     */
    public void setData(byte[] data, boolean compress) {
    	setData(data, compress, DEFAULT_COMPRESSION);
    }
    
    /**
     * Sets the data associated with the stream, either compressed or
     * uncompressed. Note that the data will never be compressed if
     * Document.compress is set to false.
     * 
     * @param data raw data, decrypted and uncompressed.
     * @param compress true if you want the stream to be compressed.
     * @param compressionLevel	a value between -1 and 9 (ignored if compress == false)
     * @since	iText 2.1.3
     */
    public void setData(byte[] data, boolean compress, int compressionLevel) {
        remove(PdfName.FILTER);
	remove(PdfName.DECODEPARMS); // ssteward, pdftk 1.46
        this.offset = -1;
        if (Document.compress && compress) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Deflater deflater = new Deflater(compressionLevel);
                DeflaterOutputStream zip = new DeflaterOutputStream(stream, deflater);
                zip.write(data);
                zip.close();
                deflater.end();
                bytes = stream.toByteArray();
                this.compressionLevel = compressionLevel;
            }
            catch (IOException ioe) {
                throw new ExceptionConverter(ioe);
            }
            put(PdfName.FILTER, PdfName.FLATEDECODE);
        }
        else
            bytes = data;
        setLength(bytes.length);
    }
    
    /**Sets the data associated with the stream
     * @param data raw data, decrypted and uncompressed.
     */
    public void setData(byte[] data) {
        setData(data, true);
    }

    public void setLength(int length) {
        this.length = length;
        put(PdfName.LENGTH, new PdfNumber(length));
    }
    
    public int getOffset() {
        return offset;
    }
    
    public int getLength() {
        return length;
    }
    
    public PdfReader getReader() {
        return reader;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    public void setObjNum(int objNum, int objGen) {
        this.objNum = objNum;
        this.objGen = objGen;
    }
    
    int getObjNum() {
        return objNum;
    }
    
    int getObjGen() {
        return objGen;
    }

    // ssteward: added code to perform uncompression (filtering) or compression of streams
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {

	// 4.2.0
	byte[] b= new byte[0];
	if( ( writer.filterStreams || writer.compressStreams ) && filtersAreKnown() ) { // ssteward
	    setData( PdfReader.getStreamBytes( this ), // apply filters to yield clear text
		     writer.compressStreams, DEFAULT_COMPRESSION );
	    b= bytes;
	}
	else { // use temp
	    b= PdfReader.getStreamBytesRaw( this );
	}
        PdfEncryption crypto = null;
        if (writer != null)
            crypto = writer.getEncryption();
        PdfObject objLen = get(PdfName.LENGTH);
        int nn = b.length;
        if (crypto != null)
            nn = crypto.calculateStreamSize(nn);
        put(PdfName.LENGTH, new PdfNumber(nn));
        superToPdf(writer, os);
        put(PdfName.LENGTH, objLen);
        os.write(STARTSTREAM);
        if (length > 0) {
            if (crypto != null && !crypto.isEmbeddedFilesOnly())
                b = crypto.encryptByteArray(b);
            os.write(b);
        }
        os.write(ENDSTREAM);
    }

    // ssteward
    // do we know how to apply all of the stream filters?
    public boolean filtersAreKnown() {

	// ssteward
	//
	// this is a workaround for occasionally buggy decoding of FlateDecode streams that
	// utilize a predictor (see Debian bug 787030); the bug might be in
	// PdfReader.decodePredictor(), where the predictor is applied; 
	// this workaround isn't a complete cop-out: pdftk's "uncompress" and "compress"
	// features were intended only to help with filtering PDF page streams, which
	// don't typically use predictors;
	//
	// this workaround also neuters a bug report by pdftk user Keith Hatton; he
	// experienced a pdftk crash when trying to uncompress a PDF; the culprit
	// was an image stream with an LZWDecode filter and DecodeParms of EarlyChange 0;
	//
	if( this.contains( PdfName.DECODEPARMS ) )
	    return false;

	boolean retVal= true;

	PdfObject filter = PdfReader.getPdfObjectRelease(get(PdfName.FILTER));
	ArrayList filters = new ArrayList();
	if (filter != null) {
	    if (filter.isName())
		filters.add(filter);
	    else if (filter.isArray())
		filters = ((PdfArray)filter).getArrayList();
	}

	String name;
	for (int j = 0; j < filters.size(); ++j) {
	    name = ((PdfName)PdfReader.getPdfObjectRelease
		    ((PdfObject)filters.get(j))).toString();
	    retVal = retVal &&
		( (name.equals("/FlateDecode") || name.equals("/Fl")) ||
		  (name.equals("/ASCIIHexDecode") || name.equals("/AHx")) ||
		  (name.equals("/ASCII85Decode") || name.equals("/A85")) ||
		  (name.equals("/LZWDecode")) );
	}

	return retVal;
    }

}
