/*
 * $Id: PdfNull.java,v 1.22 2002/06/20 13:30:25 blowagie Exp $
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
 * <CODE>PdfNull</CODE> is the Null object represented by the keyword <VAR>null</VAR>.
 * <P>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 4.9 (page 53).
 *
 * @see		PdfObject
 */

public class PdfNull extends PdfObject {
    
    // static membervariables
    
/** This is an instance of the <CODE>PdfNull</CODE>-object. */
    public static final PdfNull	PDFNULL = new PdfNull();
    
/** This is the content of a <CODE>PdfNull</CODE>-object. */
    private static final String CONTENT = "null";
    
    // constructors
    
/**
 * Constructs a <CODE>PdfNull</CODE>-object.
 * <P>
 * You never need to do this yourself, you can always use the static final object <VAR>PDFNULL</VAR>.
 */
    
    public PdfNull() {
        super(m_NULL, CONTENT); // ssteward
    }
}