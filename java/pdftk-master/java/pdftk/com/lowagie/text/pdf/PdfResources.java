/*
 * $Id: PdfResources.java,v 1.22 2002/06/20 13:30:25 blowagie Exp $
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
 * <CODE>PdfResources</CODE> is the PDF Resources-object.
 * <P>
 * The marking operations for drawing a page are stored in a stream that is the value of the
 * <B>Contents</B> key in the Page object's dictionary. Each marking context includes a list
 * of the named resources it uses. This resource list is stored as a dictionary that is the
 * value of the context's <B>Resources</B> key, and it serves two functions: it enumerates
 * the named resources in the contents stream, and it established the mapping from the names
 * to the objects used by the marking operations.<BR>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 7.5 (page 195-197).
 *
 * @see		PdfPage
 */

class PdfResources extends PdfDictionary {
    
    // constructor
    
/**
 * Constructs a PDF ResourcesDictionary.
 */
    
    PdfResources() {
        super();
    }
    
    // methods
    
    void add(PdfName key, PdfDictionary resource) {
        if (resource.size() == 0)
            return;
        PdfDictionary dic = (PdfDictionary)PdfReader.getPdfObject(get(key));
        if (dic == null)
            put(key, resource);
        else
            dic.putAll(resource);
    }
}