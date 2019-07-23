/*
 * $Id: Anchor.java,v 1.84 2005/05/03 13:03:49 blowagie Exp $
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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import pdftk.com.lowagie.text.markup.MarkupTags;
import pdftk.com.lowagie.text.markup.MarkupParser;

/**
 * An <CODE>Anchor</CODE> can be a reference or a destination of a reference.
 * <P>
 * An <CODE>Anchor</CODE> is a special kind of <CODE>Phrase</CODE>.
 * It is constructed in the same way.
 * <P>
 * Example:
 * <BLOCKQUOTE><PRE>
 * <STRONG>Anchor anchor = new Anchor("this is a link");</STRONG>
 * <STRONG>anchor.setName("LINK");</STRONG>
 * <STRONG>anchor.setReference("http://www.lowagie.com");</STRONG>
 * </PRE></BLOCKQUOTE>
 *
 * @see		Element
 * @see		Phrase
 */

public class Anchor extends Phrase implements TextElementArray, MarkupAttributes {
    
    // constant
    private static final long serialVersionUID = -852278536049236911L;

    // membervariables
    
/** This is the anchor tag. */
    public static final String ANCHOR = "anchor";
    
/** This is the name of the <CODE>Anchor</CODE>. */
    protected String name = null;
    
/** This is the reference of the <CODE>Anchor</CODE>. */
    protected String reference = null;
    
    // constructors
    
/**
 * Constructs an <CODE>Anchor</CODE> without specifying a leading.
 */
    
    public Anchor() {
        super(16);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain leading.
 *
 * @param	leading		the leading
 */
    
    public Anchor(float leading) {
        super(leading);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain <CODE>Chunk</CODE>.
 *
 * @param	chunk		a <CODE>Chunk</CODE>
 */
    
    public Anchor(Chunk chunk) {
        super(chunk);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain <CODE>String</CODE>.
 *
 * @param	string		a <CODE>String</CODE>
 */
    
    public Anchor(String string) {
        super(string);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain <CODE>String</CODE>
 * and a certain <CODE>Font</CODE>.
 *
 * @param	string		a <CODE>String</CODE>
 * @param	font		a <CODE>Font</CODE>
 */
    
    public Anchor(String string, Font font) {
        super(string, font);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain <CODE>Chunk</CODE>
 * and a certain leading.
 *
 * @param	leading		the leading
 * @param	chunk		a <CODE>Chunk</CODE>
 */
    
    public Anchor(float leading, Chunk chunk) {
        super(leading, chunk);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain leading
 * and a certain <CODE>String</CODE>.
 *
 * @param	leading		the leading
 * @param	string		a <CODE>String</CODE>
 */
    
    public Anchor(float leading, String string) {
        super(leading, string);
    }
    
/**
 * Constructs an <CODE>Anchor</CODE> with a certain leading,
 * a certain <CODE>String</CODE> and a certain <CODE>Font</CODE>.
 *
 * @param	leading		the leading
 * @param	string		a <CODE>String</CODE>
 * @param	font		a <CODE>Font</CODE>
 */
    
    public Anchor(float leading, String string, Font font) {
        super(leading, string, font);
    }
    
/**
 * Returns an <CODE>Anchor</CODE> that has been constructed taking in account
 * the value of some <VAR>attributes</VAR>.
 *
 * @param	attributes		Some attributes
 */
    
    public Anchor(Properties attributes) {
        this("", FontFactory.getFont(attributes));
        String value;
        if ((value = (String)attributes.remove(ElementTags.ITEXT)) != null) {
            Chunk chunk = new Chunk(value);
            if ((value = (String)attributes.remove(ElementTags.GENERICTAG)) != null) {
                chunk.setGenericTag(value);
            }
            add(chunk);
        }
        if ((value = (String)attributes.remove(ElementTags.LEADING)) != null) {
            setLeading(Float.valueOf(value + "f").floatValue());
        }
        else if ((value = (String)attributes.remove(MarkupTags.CSS_KEY_LINEHEIGHT)) != null) {
            setLeading(MarkupParser.parseLength(value));
        }
        if ((value = (String)attributes.remove(ElementTags.NAME)) != null) {
            setName(value);
        }
        if ((value = (String)attributes.remove(ElementTags.REFERENCE)) != null) {
            setReference(value);
        }
        if (attributes.size() > 0) setMarkupAttributes(attributes);
    }
    
    // implementation of the Element-methods
    
/**
 * Processes the element by adding it (or the different parts) to an
 * <CODE>ElementListener</CODE>.
 *
 * @param	listener	an <CODE>ElementListener</CODE>
 * @return	<CODE>true</CODE> if the element was processed successfully
 */
    
    public boolean process(ElementListener listener) {
        try {
            Chunk chunk;
            Iterator i = getChunks().iterator();
            boolean localDestination = (reference != null && reference.startsWith("#"));
            boolean notGotoOK = true;
            while (i.hasNext()) {
                chunk = (Chunk) i.next();
                if (name != null && notGotoOK && !chunk.isEmpty()) {
                    chunk.setLocalDestination(name);
                    notGotoOK = false;
                }
                if (localDestination) {
                    chunk.setLocalGoto(reference.substring(1));
                }
                listener.add(chunk);
            }
            return true;
        }
        catch(DocumentException de) {
            return false;
        }
    }
    
/**
 * Gets all the chunks in this element.
 *
 * @return	an <CODE>ArrayList</CODE>
 */
    
    public ArrayList getChunks() {
        ArrayList tmp = new ArrayList();
        Chunk chunk;
        Iterator i = iterator();
        boolean localDestination = (reference != null && reference.startsWith("#"));
        boolean notGotoOK = true;
        while (i.hasNext()) {
            chunk = (Chunk) i.next();
            if (name != null && notGotoOK && !chunk.isEmpty()) {
                chunk.setLocalDestination(name);
                notGotoOK = false;
            }
            if (localDestination) {
                chunk.setLocalGoto(reference.substring(1));
            }
            else if (reference != null)
                chunk.setAnchor(reference);
            tmp.add(chunk);
        }
        return tmp;
    }
    
/**
 * Gets the type of the text element.
 *
 * @return	a type
 */
    
    public int type() {
        return Element.ANCHOR;
    }
    
    // methods
    
/**
 * Gets an iterator of <CODE>Element</CODE>s.
 *
 * @return	an <CODE>Iterator</CODE>
 */
    
    // suggestion by by Curt Thompson
    public Iterator getElements() {
        return this.iterator();
    }
    
/**
 * Sets the name of this <CODE>Anchor</CODE>.
 *
 * @param	name		a new name
 */
    
    public void setName(String name) {
        this.name = name;
    }
    
/**
 * Sets the reference of this <CODE>Anchor</CODE>.
 *
 * @param	reference		a new reference
 */
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    // methods to retrieve information
    
/**
 * Returns the name of this <CODE>Anchor</CODE>.
 *
 * @return	a name
 */
    
    public String name() {
        return name;
    }
    
/**
 * Gets the reference of this <CODE>Anchor</CODE>.
 *
 * @return	a reference
 */
    
    public String reference() {
        return reference;
    }
    
/**
 * Gets the reference of this <CODE>Anchor</CODE>.
 *
 * @return	an <CODE>URL</CODE>
 */
    
    public URL url() {
        try {
            return new URL(reference);
        }
        catch(MalformedURLException mue) {
            return null;
        }
    }
    
/**
 * Checks if a given tag corresponds with this object.
 *
 * @param   tag     the given tag
 * @return  true if the tag corresponds
 */
    
    public static boolean isTag(String tag) {
        return ElementTags.ANCHOR.equals(tag);
    }
}
