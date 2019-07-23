/*
 * $Id: PdfNumber.java,v 1.24 2002/07/09 11:28:23 blowagie Exp $
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

/**
 * <CODE>PdfNumber</CODE> provides two types of numbers, integer and real.
 * <P>
 * Integers may be specified by signed or unsigned constants. Reals may only be
 * in decimal format.<BR>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 4.3 (page 37).
 *
 * @see		PdfObject
 * @see		BadPdfFormatException
 */

public class PdfNumber extends PdfObject {
    
/** actual value of this <CODE>PdfNumber</CODE>, represented as a <CODE>double</CODE> */
    private double value;
    
    // constructors
    
/**
 * Constructs a <CODE>PdfNumber</CODE>-object.
 *
 * @param		content			value of the new <CODE>PdfNumber</CODE>-object
 */
    
    public PdfNumber(String content) {
        super(NUMBER);
        try {
            value = Double.valueOf(content.trim()).doubleValue();
            setContent(content);
        }
        catch (NumberFormatException nfe){
            throw new RuntimeException(content + " is not a valid number - " + nfe.toString());
        }
    }
    
/**
 * Constructs a new INTEGER <CODE>PdfNumber</CODE>-object.
 *
 * @param		value				value of the new <CODE>PdfNumber</CODE>-object
 */
    
    public PdfNumber(int value) {
        super(NUMBER);
        this.value = value;
        setContent(String.valueOf(value));
    }
    
/**
 * Constructs a new REAL <CODE>PdfNumber</CODE>-object.
 *
 * @param		value				value of the new <CODE>PdfNumber</CODE>-object
 */
    
    public PdfNumber(double value) {
        super(NUMBER);
        this.value = value;
        setContent(ByteBuffer.formatDouble(value));
    }
    
/**
 * Constructs a new REAL <CODE>PdfNumber</CODE>-object.
 *
 * @param		value				value of the new <CODE>PdfNumber</CODE>-object
 */
    
    public PdfNumber(float value) {
        this((double)value);
    }
    
    // methods returning the value of this object
    
/**
 * Returns the primitive <CODE>int</CODE> value of this object.
 *
 * @return		a value
 */
    
    public int intValue() {
        return (int) value;
    }
    
/**
 * Returns the primitive <CODE>double</CODE> value of this object.
 *
 * @return		a value
 */
    
    public double doubleValue() {
        return value;
    }
    
    public float floatValue() {
        return (float)value;
    }
    
    // other methods
    
/**
 * Increments the value of the <CODE>PdfNumber</CODE>-object with 1.
 */
    
    public void increment() {
        value += 1.0;
        setContent(ByteBuffer.formatDouble(value));
    }
}