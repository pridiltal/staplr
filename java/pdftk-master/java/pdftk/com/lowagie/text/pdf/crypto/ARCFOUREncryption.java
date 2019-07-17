/*
 * $Id$
 *
 * Copyright 2006 Paulo Soares
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
package pdftk.com.lowagie.text.pdf.crypto;

public class ARCFOUREncryption {
    private byte state[] = new byte[256];
    private int x;
    private int y;

    /** Creates a new instance of ARCFOUREncryption */
    public ARCFOUREncryption() {
    }
    
    public void prepareARCFOURKey(byte key[]) {
        prepareARCFOURKey(key, 0, key.length);
    }

    public void prepareARCFOURKey(byte key[], int off, int len) {
        int index1 = 0;
        int index2 = 0;
        for (int k = 0; k < 256; ++k)
            state[k] = (byte)k;
        x = 0;
        y = 0;
        byte tmp;
        for (int k = 0; k < 256; ++k) {
            index2 = (key[index1 + off] + state[k] + index2) & 255;
            tmp = state[k];
            state[k] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % len;
        }
    }

    public void encryptARCFOUR(byte dataIn[], int off, int len, byte dataOut[], int offOut) {
        int length = len + off;
        byte tmp;
        for (int k = off; k < length; ++k) {
            x = (x + 1) & 255;
            y = (state[x] + y) & 255;
            tmp = state[x];
            state[x] = state[y];
            state[y] = tmp;
            dataOut[k - off + offOut] = (byte)(dataIn[k] ^ state[(state[x] + state[y]) & 255]);
        }
    }

    public void encryptARCFOUR(byte data[], int off, int len) {
        encryptARCFOUR(data, off, len, data, off);
    }

    public void encryptARCFOUR(byte dataIn[], byte dataOut[]) {
        encryptARCFOUR(dataIn, 0, dataIn.length, dataOut, 0);
    }

    public void encryptARCFOUR(byte data[]) {
        encryptARCFOUR(data, 0, data.length, data, 0);
    }   
}