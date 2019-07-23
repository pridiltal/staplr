/*
 * $Id: RandomAccessFileOrArray.java,v 1.48 2005/09/11 08:38:18 blowagie Exp $
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
 * Co-Developer of the code is Sid Steward. Portions created by the Co-Developer
 * are Copyright (C) 2013 by Sid Steward. All Rights Reserved.
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

import java.io.DataInputStream;
import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
/** An implementation of a RandomAccessFile for input only
 * that accepts a file or a byte array as data source.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class RandomAccessFileOrArray implements DataInput {
    
    ////
    // uses either rf or arrayIn, but not both

    String filename = null; // set only if we expect to use rf (not arrayIn)
    RandomAccessFile rf = null; // may be null even if filename is set

    byte arrayIn[] = null; // set only if we aren't using filename/rf
    int arrayInPtr = 0;

    private byte back = 0;
    private boolean isBack = false;
    
    private int startOffset = 0;

    ////
    //

    //
    public RandomAccessFileOrArray(String filename) throws IOException {
    	this(filename, false);
    }
    
    //
    public RandomAccessFileOrArray(String filename, boolean forceRead) throws IOException {
	if (filename == null)
	    throw new IllegalArgumentException
		("null filename passed into RandomAccessFileOrArray()");

        File file = new File(filename);

        if (!file.canRead()) {
            if (filename.startsWith("file:/") || filename.startsWith("http://") || 
		filename.startsWith("https://") || filename.startsWith("jar:"))
		{
		    // copied from RandomAccessFileOrArray(URL):
		    InputStream is = new URL(filename).openStream();
		    try {
			this.inputStreamToArray(is);
		    }
		    finally {
			try { is.close(); } catch (IOException ioe) {}
		    }
		}
            else {
                InputStream is;
		if( filename.equals("-") ) {
		    // ssteward, pdftk 1.10; patch provided by Bart Orbons 
		    // to permit stdin input via pdftk
		    is = System.in;
		}
		else {
		    is = BaseFont.getResourceStream(filename);
		}

                if (is == null)
                    throw new IOException(filename + " not found as file or resource.");

                try {
                    this.inputStreamToArray(is);
                }
                finally {
                    try { is.close(); } catch (IOException ioe) {}
                }
            }
        }
        else if (forceRead) {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                this.inputStreamToArray(is);
            }
            finally {
                try { if (is != null) { is.close(); } } catch (Exception e) {}
            }
        }
	else {
	    rf = new RandomAccessFile(file, "r");
	    if (rf == null)
		throw new IOException("Unable to open: " + filename);

	    this.filename = filename; // set only if we're using rf
	}
    }

    public RandomAccessFileOrArray(URL url) throws IOException {
	if (url == null)
	    throw new IllegalArgumentException
		("null url passed into RandomAccessFileOrArray()");

        InputStream is = url.openStream();
        try {
            this.inputStreamToArray(is);
        }
        finally {
            try { is.close(); } catch (IOException ioe) {}
        }
    }

    public RandomAccessFileOrArray(InputStream is) throws IOException {
        this.inputStreamToArray(is);
    }
    
    public RandomAccessFileOrArray(byte arrayIn[]) {
        this.arrayIn = arrayIn;
    }

    // copy constructor
    public RandomAccessFileOrArray(RandomAccessFileOrArray file) throws IOException {
	if (file == null)
	    throw new IllegalArgumentException
		("null file passed into RandomAccessFileOrArray() copy constructor");

	if( file.filename != null ) {
	    this.filename = file.filename;
	    this.rf = null;
	}
	else if( file.arrayIn != null ) {
	    this.arrayIn = file.arrayIn;
	    this.arrayInPtr = 0;
	}

	this.startOffset = file.startOffset;
    }

    ////
    //

    //
    private void inputStreamToArray(InputStream is) throws IOException {
        byte bb[] = new byte[8192];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
	int len = 0;
        while ( 1< (len = is.read(bb)) ) {
            out.write(bb, 0, len);
        }
	this.arrayIn = out.toByteArray(); // allocates a new byte array
	this.arrayInPtr = 0;
    }

    // added pushBack isBack test since we hold only one byte
    public void pushBack(byte b) throws IOException {
	if (isBack)
	    throw new IOException("Tried to pushBack a byte when isBack is true.");
        back = b;
        isBack = true;
    }
    public int popBack() {
	int retVal = -1; // no back
	if (isBack) {
	    retVal = back & 0xff;
	    // clear
	    back = 0;
	    isBack = false;
	}
	return retVal;
    }
    public boolean isBack() {
	return isBack;
    }
    public int getBack() {
	return back & 0xff;
    }
    public void clearBack() {
	back = 0;
	isBack = false;
    }
    
    // reads one byte
    public int read() throws IOException {
	int retVal = -1;
        if (isBack) {
            retVal = getBack();
	    clearBack();
        }
        else if (rf != null) {
	    retVal = rf.read();
	}
        else if (arrayIn != null &&
		 0<= arrayInPtr && arrayInPtr< arrayIn.length) {
	    retVal = arrayIn[arrayInPtr++] & 0xff;
	}
	return retVal;
    }

    // bb: buffer to read into
    // off: index of b where to begin
    // len: max number of bytes to copy
    // return: number of bytes read, or -1 on end
    public int read( byte[] bb, int off, int len ) throws IOException {
	if( bb== null )
	    throw new IllegalArgumentException("read() argument bb is null.");

        if( len== 0 || bb.length== 0 )
            return 0; // nothing read; test before (bb.length<= off)

	if( len< 0 || off< 0 || bb.length<= off || bb.length< off+ len )
	    throw new IllegalArgumentException
		("read() arguments are out of bounds: len: "+ len+
		 " off: "+ off+ " bb.length: "+ bb.length);

	int retVal= -1; // default: end of data

	// test for pushed-back byte
        if( isBack ) { // prepend byte; reset later
	    bb[ off++ ]= back;
	    --len;
        }

        if( rf!= null ) { // use rf
	    retVal= rf.read( bb, off, len ); // could return -1
        }

        else if( arrayIn!= null ) { // use array
	    if( 0<= arrayInPtr && arrayInPtr< arrayIn.length ) {

		if( arrayIn.length< arrayInPtr+ len )
		    len= arrayIn.length- arrayInPtr; // take only what's left

		if( 0< len ) {
		    System.arraycopy( arrayIn, arrayInPtr, bb, off, len );
		    arrayInPtr+= len;
		    retVal= len;
		}
		// else end of array data
	    }
	    // else end of array data
        }

	if( isBack )
	    if( retVal== -1 )
		retVal= 1;
	    else
		retVal+= 1;
	clearBack();

	return retVal;
    }
    
    //
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    //
    public void readFully( byte bb[], int off, int len ) throws IOException {
        int bytesRead= 0;
	int nn= 0;
        while( bytesRead< len ) { // len may be 0
            nn= this.read( bb, off+ bytesRead, len- bytesRead );
            if( nn< 0 )
                throw new EOFException();
            bytesRead+= nn;
        }
    }
    public void readFully( byte bb[] ) throws IOException {
        readFully( bb, 0, bb.length );
    }

    // calls clearBack()
    public void reOpen() throws IOException {
        if (filename != null && rf == null) {
            rf = new RandomAccessFile(filename, "r");
	    if (rf == null) {
		throw new IOException("Unable to reOpen: " + filename);
	    }
	}
	this.seek(0); // calls clearBack()
    }
    
    // might call clearBack()
    protected void insureOpen() throws IOException {
        if (filename != null && rf == null) {
            reOpen();
        }
    }

    //
    public boolean isOpen() {
        return (filename == null || rf != null);
    }

    //
    public void close() throws IOException {
        if (rf != null) {
            rf.close();
            rf = null;
        }
	// preserves this.filename and this.arrayIn

	clearBack();
    }
    
    //
    public int length() throws IOException {
        if (filename != null) {
            insureOpen();
            return (int)rf.length() - startOffset;
        }
        else if (arrayIn != null) {
            return arrayIn.length - startOffset;
	}
	return 0;
    }
    
    //
    public int getFilePointer() throws IOException {
	insureOpen(); // might call clearBack(), so call first
        int nn = isBack ? 1 : 0;
        if (filename != null) {
            return (int)rf.getFilePointer() - nn - startOffset;
        }
        else if (arrayIn != null) {
            return arrayInPtr - nn - startOffset;
	}
	return 0; // no data -- shouldn't happen
    }

    //
    public void seek(int pos) throws IOException {
        pos += startOffset;

	clearBack();

        if (filename != null) {
	    // not calling insureOpen() b/c it calls reOpen() which calls this.seek(0)
	    if (rf == null) {
		rf = new RandomAccessFile(filename, "r");
		if (rf == null) {
		    throw new IOException("Unable to open: " + filename + " in seek()");
		}
	    }
            rf.seek(pos);
        }
        else if (arrayIn != null) {
            arrayInPtr = pos;
	}
    }

    //
    public void seek(long pos) throws IOException {
        seek((int)pos);
    }
    
    //
    public int skipBytes(int nn) throws IOException {
        if (nn <= 0) {
            return 0;
        }
	if (nn == 1 && isBack) {
	    clearBack();
	    return 1;
	}
	// no need for additional isBack logic b/c helper functions already consider it
        
        int pos = this.getFilePointer(); // considers isBack and startOffset
        int len = this.length(); // considers startOffset
        int newpos = pos + nn;
        if (newpos > len) {
            newpos = len; // seek(len) ensures that next read() returns -1
        }
        this.seek(newpos); // considers startOffset
        
        return newpos - pos;
    }

    //
    public long skip(long n) throws IOException {
        return skipBytes((int)n);
    }

    //
    public int getStartOffset() {
        return this.startOffset;
    }    
    public void setStartOffset(int startOffset) throws IOException {
        this.startOffset = startOffset;
	this.seek(0);
    }

    ////
    //

    public boolean readBoolean() throws IOException {
        int ch = this.read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }
    
    public byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }
    
    public int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }
    
    public short readShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + ch2);
    }
    
    /**
     * Reads a signed 16-bit number from this stream in little-endian order.
     * The method reads two
     * bytes from this stream, starting at the current stream pointer.
     * If the two bytes read, in order, are
     * <code>b1</code> and <code>b2</code>, where each of the two values is
     * between <code>0</code> and <code>255</code>, inclusive, then the
     * result is equal to:
     * <blockquote><pre>
     *     (short)((b2 &lt;&lt; 8) | b1)
     * </pre></blockquote>
     * <p>
     * This method blocks until the two bytes are read, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     the next two bytes of this stream, interpreted as a signed
     *             16-bit number.
     * @exception  EOFException  if this stream reaches the end before reading
     *               two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final short readShortLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch2 << 8) + (ch1 << 0));
    }
    
    public int readUnsignedShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1 << 8) + ch2;
    }
    
    /**
     * Reads an unsigned 16-bit number from this stream in little-endian order.
     * This method reads
     * two bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are
     * <code>b1</code> and <code>b2</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1, b2&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (b2 &lt;&lt; 8) | b1
     * </pre></blockquote>
     * <p>
     * This method blocks until the two bytes are read, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     the next two bytes of this stream, interpreted as an
     *             unsigned 16-bit integer.
     * @exception  EOFException  if this stream reaches the end before reading
     *               two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final int readUnsignedShortLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch2 << 8) + (ch1 << 0);
    }
    
    public char readChar() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + ch2);
    }
    
    /**
     * Reads a Unicode character from this stream in little-endian order.
     * This method reads two
     * bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are
     * <code>b1</code> and <code>b2</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1,&nbsp;b2&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (char)((b2 &lt;&lt; 8) | b1)
     * </pre></blockquote>
     * <p>
     * This method blocks until the two bytes are read, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     the next two bytes of this stream as a Unicode character.
     * @exception  EOFException  if this stream reaches the end before reading
     *               two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final char readCharLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch2 << 8) + (ch1 << 0));
    }
    
    public int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }
    
    /**
     * Reads a signed 32-bit integer from this stream in little-endian order.
     * This method reads 4
     * bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are <code>b1</code>,
     * <code>b2</code>, <code>b3</code>, and <code>b4</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (b4 &lt;&lt; 24) | (b3 &lt;&lt; 16) + (b2 &lt;&lt; 8) + b1
     * </pre></blockquote>
     * <p>
     * This method blocks until the four bytes are read, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     the next four bytes of this stream, interpreted as an
     *             <code>int</code>.
     * @exception  EOFException  if this stream reaches the end before reading
     *               four bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final int readIntLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }
    
    /**
     * Reads an unsigned 32-bit integer from this stream. This method reads 4
     * bytes from the stream, starting at the current stream pointer.
     * If the bytes read, in order, are <code>b1</code>,
     * <code>b2</code>, <code>b3</code>, and <code>b4</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (b1 &lt;&lt; 24) | (b2 &lt;&lt; 16) + (b3 &lt;&lt; 8) + b4
     * </pre></blockquote>
     * <p>
     * This method blocks until the four bytes are read, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return     the next four bytes of this stream, interpreted as a
     *             <code>long</code>.
     * @exception  EOFException  if this stream reaches the end before reading
     *               four bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final long readUnsignedInt() throws IOException {
        long ch1 = this.read();
        long ch2 = this.read();
        long ch3 = this.read();
        long ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    
    public final long readUnsignedIntLE() throws IOException {
        long ch1 = this.read();
        long ch2 = this.read();
        long ch3 = this.read();
        long ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }
    
    public long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }
    
    public final long readLongLE() throws IOException {
        int i1 = readIntLE();
        int i2 = readIntLE();
        return ((long)i2 << 32) + (i1 & 0xFFFFFFFFL);
    }
    
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    public final float readFloatLE() throws IOException {
        return Float.intBitsToFloat(readIntLE());
    }
    
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    
    public final double readDoubleLE() throws IOException {
        return Double.longBitsToDouble(readLongLE());
    }
    
    public String readLine() throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;
        
        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    int cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input.append((char)c);
                    break;
            }
        }
        
        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }
    
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
    
}
