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

import pdftk.com.lowagie.text.DocumentException;
import pdftk.com.lowagie.text.pdf.PdfAction;
import pdftk.com.lowagie.text.pdf.PdfName;

/**
 * A PDF document can have an open action and other additional actions.
 */

public interface PdfDocumentActions {

    /**
     * When the document opens it will jump to the destination with
     * this name.
     * @param name the name of the destination to jump to
     */
    public void setOpenAction(String name);
    
    /**
     * When the document opens this <CODE>action</CODE> will be
     * invoked.
     * @param action the action to be invoked
     */
    public void setOpenAction(PdfAction action);
    
    /**
     * Additional-actions defining the actions to be taken in
     * response to various trigger events affecting the document
     * as a whole. The actions types allowed are: <CODE>DOCUMENT_CLOSE</CODE>,
     * <CODE>WILL_SAVE</CODE>, <CODE>DID_SAVE</CODE>, <CODE>WILL_PRINT</CODE>
     * and <CODE>DID_PRINT</CODE>.
     *
     * @param actionType the action type
     * @param action the action to execute in response to the trigger
     * @throws DocumentException on invalid action type
     */
    public void setAdditionalAction(PdfName actionType, PdfAction action) throws DocumentException;

}