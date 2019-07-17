/*
 * $Id: PdfBoolean.java,v 1.22 2002/06/20 13:28:22 blowagie Exp $
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
 * <CODE>PdfBoolean</CODE> is the boolean object represented by the keywords <VAR>true</VAR> or <VAR>false</VAR>.
 * <P>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 4.2 (page 37).
 *
 * @see		PdfObject
 * @see		BadPdfFormatException
 */

public class PdfBoolean extends PdfObject {
    
    // static membervariables (possible values of a boolean object)
    public static final PdfBoolean PDFTRUE = new PdfBoolean(true);
    public static final PdfBoolean PDFFALSE = new PdfBoolean(false);
/** A possible value of <CODE>PdfBoolean</CODE> */
    public static final String TRUE = "true";
    
/** A possible value of <CODE>PdfBoolean</CODE> */
    public static final String FALSE = "false";
    
    // membervariables
    
/** the boolean value of this object */
    private boolean value;
    
    // constructors
    
/**
 * Constructs a <CODE>PdfBoolean</CODE>-object.
 *
 * @param		value			the value of the new <CODE>PdfObject</CODE>
 */
    
    public PdfBoolean(boolean value) {
        super(BOOLEAN);
        if (value) {
            setContent(TRUE);
        }
        else {
            setContent(FALSE);
        }
        this.value = value;
    }
    
/**
 * Constructs a <CODE>PdfBoolean</CODE>-object.
 *
 * @param		value			the value of the new <CODE>PdfObject</CODE>, represented as a <CODE>String</CODE>
 *
 * @throws		BadPdfFormatException	thrown if the <VAR>value</VAR> isn't '<CODE>true</CODE>' or '<CODE>false</CODE>'
 */
    
    public PdfBoolean(String value) throws BadPdfFormatException {
        super(BOOLEAN, value);
        if (value.equals(TRUE)) {
            this.value = true;
        }
        else if (value.equals(FALSE)) {
            this.value = false;
        }
        else {
            throw new BadPdfFormatException("The value has to be 'true' of 'false', instead of '" + value + "'.");
        }
    }
    
    // methods returning the value of this object
    
/**
 * Returns the primitive value of the <CODE>PdfBoolean</CODE>-object.
 *
 * @return		the actual value of the object.
 */
    
    public boolean booleanValue() {
        return value;
    }
}