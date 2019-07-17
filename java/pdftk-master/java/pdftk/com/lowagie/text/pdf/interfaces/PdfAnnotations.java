/*
 * $Id$
 *
 * Copyright 2006 Bruno Lowagie
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

package pdftk.com.lowagie.text.pdf.interfaces;

import pdftk.com.lowagie.text.pdf.PdfAcroForm;
import pdftk.com.lowagie.text.pdf.PdfAnnotation;
import pdftk.com.lowagie.text.pdf.PdfFormField;

public interface PdfAnnotations {

    /**
     * Use this methods to get the AcroForm object.
     * Use this method only if you know what you're doing
     * @return the PdfAcroform object of the PdfDocument
     */
    public PdfAcroForm getAcroForm();
    
    /**
     * Use this methods to add a <CODE>PdfAnnotation</CODE> or a <CODE>PdfFormField</CODE>
     * to the document. Only the top parent of a <CODE>PdfFormField</CODE>
     * needs to be added.
     * @param annot the <CODE>PdfAnnotation</CODE> or the <CODE>PdfFormField</CODE> to add
     */
    public void addAnnotation(PdfAnnotation annot);
    /**
     * Use this method to adds the <CODE>PdfAnnotation</CODE>
     * to the calculation order array.
     * @param annot the <CODE>PdfAnnotation</CODE> to be added
     */
    public void addCalculationOrder(PdfFormField annot);
    
    /**
     * Use this method to set the signature flags.
     * @param f the flags. This flags are ORed with current ones
     */
    public void setSigFlags(int f);
}
