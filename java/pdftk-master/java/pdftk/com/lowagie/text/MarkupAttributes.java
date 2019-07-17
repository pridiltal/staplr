/*
 * Copyright 2002 by Matt Benson.
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


import java.util.Properties;
import java.util.Set;


/**
 * Defines the interface for an <CODE>Element</CODE> with markup attributes--
 * that is, random String-to-String properties for representation in markup
 * languages such as HTML and XML.
 *
 * @author <a href="mailto:orangeherbert@users.sourceforge.net">Matt Benson</a>
 */
public interface MarkupAttributes extends pdftk.com.lowagie.text.Element {
    
/**
 * Sets the specified attribute.
 *
 * @param name    <CODE>String</CODE> attribute name.
 * @param value   <CODE>String</CODE> attribute value.
 */
    public void setMarkupAttribute(String name, String value);
    
/**
 * Sets the markupAttributes.
 *
 * @param   markupAttributes    a <CODE>Properties</CODE>-object containing markupattributes 
 */
    public void setMarkupAttributes(Properties markupAttributes);
    
/**
 * Returns the value of the specified attribute.
 *
 * @param name   <CODE>String</CODE> attribute name.
 * @return <CODE>String</CODE>.
 */
    public String getMarkupAttribute(String name);
    
/**
 * Returns a <CODE>Set</CODE> of <CODE>String</CODE> attribute names for the
 * <CODE>MarkupAttributes</CODE> implementor.
 *
 * @return <CODE>Set</CODE>.
 */
    public Set getMarkupAttributeNames(); // ssteward: dropped in 1.44
    
/**
 * Return a <CODE>Properties</CODE>-object containing all the markupAttributes.
 *
 * @return <CODE>Properties</CODE>
 */
    public Properties getMarkupAttributes();
    
}