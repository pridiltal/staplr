/*
 * $Id: PdfFormXObject.java,v 1.58 2005/07/16 16:49:22 blowagie Exp $
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
 * <CODE>PdfFormObject</CODE> is a type of XObject containing a template-object.
 */

public class PdfFormXObject extends PdfStream {
    
    // public static final variables
    
/** This is a PdfNumber representing 0. */
    public static final PdfNumber ZERO = new PdfNumber(0);
    
/** This is a PdfNumber representing 1. */
    public static final PdfNumber ONE = new PdfNumber(1);
    
/** This is the 1 - matrix. */
    public static final PdfLiteral MATRIX = new PdfLiteral("[1 0 0 1 0 0]");
    
    // membervariables
    
    
    // constructor
    
/**
 * Constructs a <CODE>PdfFormXObject</CODE>-object.
 *
 * @param		template		the template
 */
    
    PdfFormXObject(PdfTemplate template) // throws BadPdfFormatException
    {
        super();
        put(PdfName.TYPE, PdfName.XOBJECT);
        put(PdfName.SUBTYPE, PdfName.FORM);
        put(PdfName.RESOURCES, template.getResources());
        put(PdfName.BBOX, new PdfRectangle(template.getBoundingBox()));
        put(PdfName.FORMTYPE, ONE);
        if (template.getLayer() != null)
            put(PdfName.OC, template.getLayer().getRef());
        if (template.getGroup() != null)
            put(PdfName.GROUP, template.getGroup());
        PdfArray matrix = template.getMatrix();
        if (matrix == null)
            put(PdfName.MATRIX, MATRIX);
        else
            put(PdfName.MATRIX, matrix);
        bytes = template.toPdf(null);
        put(PdfName.LENGTH, new PdfNumber(bytes.length));
        flateCompress();
    }
    
}
