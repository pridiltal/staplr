/*
 * $Id$
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
 * Co-Developer of the code is Sid Steward. Portions created by the Co-Developer
 * are Copyright (C) 2010 by Sid Steward. All Rights Reserved.
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
import java.io.OutputStream;

/**
 * A <CODE>PdfString</CODE>-class is the PDF-equivalent of a JAVA-<CODE>String</CODE>-object.
 * <P>
 * A string is a sequence of characters delimited by parenthesis.
 * If a string is too long to be conveniently placed on a single line, it may
 * be split across multiple lines by using the backslash character (\) at the
 * end of a line to indicate that the string continues on the following line.
 * Within a string, the backslash character is used as an escape to specify
 * unbalanced parenthesis, non-printing ASCII characters, and the backslash
 * character itself. Use of the \<I>ddd</I> escape sequence is the preferred
 * way to represent characters outside the printable ASCII character set.<BR>
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.3 (page 53-56).
 *
 * @see PdfObject
 * @see BadPdfFormatException
 */
public class PdfString extends PdfObject {
    
    // CLASS VARIABLES
    
    /** The value of this object. */
    // these start out *uninitialized*
    protected String value= null;
    //
    protected byte[] originalBytes= null;
    
    /** The encoding. */
    // the encoding indicates how to encode the data in the PDF file;
    // this starts out *uninitialized*
    // encodings:
    // null => not set
    // NOTHING => literal bytes; used with hex output
    // TEXT_UNICODE => UTF-16
    // TEXT_PDFDOCENCODING => PdfDoc encoding
    //
    protected String encoding= null;

    protected int objNum = 0;
    
    protected int objGen = 0;
    
    protected boolean hexWriting = false;

    // CONSTRUCTORS
    
    /**
     * Constructs an empty <CODE>PdfString</CODE>-object.
     */
    public PdfString() {
        super(STRING);
    }
    
    /**
     * Constructs a <CODE>PdfString</CODE>-object containing a string in the
     * standard encoding <CODE>TEXT_PDFDOCENCODING</CODE>.
     *
     * @param value    the content of the string
     */
    public PdfString(String value) {
        super(STRING);
        this.value = value;
	// guess the encoding
    }
    
    /**
     * Constructs a <CODE>PdfString</CODE>-object containing a string in the
     * specified encoding.
     *
     * @param value    the content of the string
     * @param encoding an encoding
     */
    
    // ssteward: rewrite for pdftk 1.45
    public PdfString(String value, String encoding) {
        super(STRING);
        this.value = value;
        this.encoding = encoding;
    }
    
    /**
     * Constructs a <CODE>PdfString</CODE>-object.
     *
     * @param bytes    an array of <CODE>byte</CODE>
     */
    // ssteward: rewrite for pdftk 1.45
    public PdfString(byte[] bytes) {
        super(STRING);
	this.bytes = bytes;
	// guess the encoding
    }
    
    // construct from bytes, not value;
    // ssteward: rewrite for pdftk 1.45
    public PdfString(byte[] bytes, String encoding) {
        super(STRING);
	this.bytes = bytes;
	this.encoding = encoding;
    }

    // methods overriding some methods in PdfObject
    
    /**
     * Writes the PDF representation of this <CODE>PdfString</CODE> as an array
     * of <CODE>byte</CODE> to the specified <CODE>OutputStream</CODE>.
     * 
     * @param writer for backwards compatibility
     * @param os The <CODE>OutputStream</CODE> to write the bytes to.
     */
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        byte b[] = getBytes();
        PdfEncryption crypto = null;
        if (writer != null)
            crypto = writer.getEncryption();
        if (crypto != null && !crypto.isEmbeddedFilesOnly())
            b = crypto.encryptByteArray(b);
        if (hexWriting) {
            ByteBuffer buf = new ByteBuffer();
            buf.append('<');
            int len = b.length;
            for (int k = 0; k < len; ++k)
                buf.appendHex(b[k]);
            buf.append('>');
            os.write(buf.toByteArray());
        }
        else
            os.write(PdfContentByte.escapeString(b));
    }
    
    /**
     * Returns the <CODE>String</CODE> value of this <CODE>PdfString</CODE>-object.
     *
     * @return A <CODE>String</CODE>
     */
    public String toString() {
        return getValue();
    }
    
    // sets bytes and encoding based on value, if necessary
    // ssteward: rewrite for pdftk 1.45
    public byte[] getBytes() {
        if( bytes== null && value!= null ) { // convert from value
	    if( encoding== null ) {
		if( PdfEncodings.isPdfDocEncoding( value ) ) {
		    encoding= TEXT_PDFDOCENCODING;
		}
		else {
		    encoding= TEXT_UNICODE;
		}
	    }
	    bytes= PdfEncodings.convertToBytes( value, encoding );
        }
        return bytes;
    }

    // ssteward: added for pdftk 1.45
    private String getValue() {
	if( value== null && bytes!= null ) { // convert from bytes
	    if( encoding== null ) {
		if( isUnicode( bytes ) ) {
		    encoding= TEXT_UNICODE;
		}
		else {
		    encoding= TEXT_PDFDOCENCODING;
		}
	    }
	    value= PdfEncodings.convertToString( bytes, encoding );
	}
	return value;
    }

    // other methods
    
    /**
     * Returns the Unicode <CODE>String</CODE> value of this
     * <CODE>PdfString</CODE>-object.
     *
     * @return A <CODE>String</CODE>
     */
    // ssteward: rewrite for pdftk 1.45
    public String toUnicodeString() {
	return getValue();
    }
    
    /**
     * Gets the encoding of this string.
     *
     * @return a <CODE>String</CODE>
     */
    public String getEncoding() {
        return encoding;
    }
    
    void setObjNum(int objNum, int objGen) {
        this.objNum = objNum;
        this.objGen = objGen;
    }
    
    /**
     * Decrypt an encrypted <CODE>PdfString</CODE>
     */
    // ssteward: rewrite for pdftk 1.45
    void decrypt(PdfReader reader) {
        PdfEncryption decrypt = reader.getDecrypt();
        if (decrypt != null) {

	    getBytes();
	    originalBytes= new byte[ bytes.length ];
	    System.arraycopy( bytes, 0, originalBytes, 0, bytes.length );

            decrypt.setHashKey(objNum, objGen);
            bytes = decrypt.decryptByteArray(bytes);

	    // reset
	    value = null;
	    encoding = null;
        }
    }
   
    
    // ssteward: rewrite for pdftk 1.45
    public byte[] getOriginalBytes() {
        if( originalBytes!= null ) {
	    return originalBytes;
	}
	return getBytes();
    }
    
    public PdfString setHexWriting(boolean hexWriting) {
        this.hexWriting = hexWriting;
        return this;
    }
    
    public boolean isHexWriting() {
        return hexWriting;
    }

    // ssteward: rewrite for pdftk 1.45
    public static boolean isUnicode( byte[] bb ) {
	return( bb.length >= 2 && bb[0] == (byte)254 && bb[1] == (byte)255 );
    }
}
