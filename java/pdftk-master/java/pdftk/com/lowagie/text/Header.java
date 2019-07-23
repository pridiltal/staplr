/*
 * $Id: Header.java,v 1.29 2002/07/09 10:41:33 blowagie Exp $
 * $Name:  $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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

package pdftk.com.lowagie.text;

/**
 * This is an <CODE>Element</CODE> that contains
 * some userdefined meta information about the document.
 * <P>
 * <B>Example:</B>
 * <BLOCKQUOTE><PRE>
 * <STRONG>Header header = new Header("inspired by", "William Shakespeare");</STRONG>
 * </PRE></BLOCKQUOTE>
 *
 * @see		Element
 * @see		Meta
 */

public class Header extends Meta implements Element {
    
    // membervariables
    
/** This is the content of this chunk of text. */
    private StringBuffer name;
    
    // constructors
    
/**
 * Constructs a <CODE>Meta</CODE>.
 *
 * @param	name		the name of the meta-information
 * @param	content		the content
 */
    
    public Header(String name, String content) {
        super(Element.HEADER, content);
        this.name = new StringBuffer(name);
    }
    
    // methods to retrieve information
    
/**
 * Returns the name of the meta information.
 *
 * @return	a <CODE>String</CODE>
 */
    
    public String name() {
        return name.toString();
    }
}