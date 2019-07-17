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
package pdftk.com.lowagie.text.pdf;

import pdftk.com.lowagie.text.pdf.crypto.AESCipher;
import pdftk.com.lowagie.text.pdf.crypto.ARCFOUREncryption;

public class StandardDecryption {
    protected ARCFOUREncryption arcfour;
    protected AESCipher cipher;
    private byte[] key;
    private static final int AES_128 = 4;
    private boolean aes;
    private boolean initiated;
    private byte[] iv = new byte[16];
    private int ivptr;

    /** Creates a new instance of StandardDecryption */
    public StandardDecryption(byte key[], int off, int len, int revision) {
        aes = revision == AES_128;
        if (aes) {
            this.key = new byte[len];
            System.arraycopy(key, off, this.key, 0, len);
        }
        else {
            arcfour = new ARCFOUREncryption();
            arcfour.prepareARCFOURKey(key, off, len);
        }
    }
    
    public byte[] update(byte[] b, int off, int len) {
        if (aes) {
            if (initiated)
                return cipher.update(b, off, len);
            else {
                int left = Math.min(iv.length - ivptr, len);
                System.arraycopy(b, off, iv, ivptr, left);
                off += left;
                len -= left;
                ivptr += left;
                if (ivptr == iv.length) {
                    cipher = new AESCipher(false, key, iv);
                    initiated = true;
                    if (len > 0)
                        return cipher.update(b, off, len);
                }
                return null;
            }
        }
        else {
            byte[] b2 = new byte[len];
            arcfour.encryptARCFOUR(b, off, len, b2, 0);
            return b2;
        }
    }
    
    public byte[] finish() {
        if (aes) {
            return cipher.doFinal();
        }
        else
            return null;
    }
}