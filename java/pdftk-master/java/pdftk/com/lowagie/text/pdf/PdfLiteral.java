/*
 * $Id: PdfLiteral.java,v 1.21 2002/06/20 13:30:25 blowagie Exp $
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

/**
 * a Literal
 */

public class PdfLiteral extends PdfObject {
    
    /**
     * Holds value of property position.
     */
    private int position;
        
    public PdfLiteral(String text) {
        super(0, text);
    }
    
    public PdfLiteral(byte b[]) {
        super(0, b);
    }

    public PdfLiteral(int size) {
        super(0, (byte[])null);
        bytes = new byte[size];
        java.util.Arrays.fill(bytes, (byte)32);
    }

    public PdfLiteral(int type, String text) {
        super(type, text);
    }
    
    public PdfLiteral(int type, byte b[]) {
        super(type, b);
    }
    
    public void toPdf(PdfWriter writer, java.io.OutputStream os) throws java.io.IOException {
        if (os instanceof OutputStreamCounter)
            position = ((OutputStreamCounter)os).getCounter();
        super.toPdf(writer, os);
    }
    
    /**
     * Getter for property position.
     * @return Value of property position.
     */
    public int getPosition() {
        return this.position;
    }
    
    /**
     * Getter for property posLength.
     * @return Value of property posLength.
     */
    public int getPosLength() {
        if (bytes != null)
            return bytes.length;
        else
            return 0;
    }
    
}