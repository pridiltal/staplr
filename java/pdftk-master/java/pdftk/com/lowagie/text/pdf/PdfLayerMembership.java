/*
 * Copyright 2004 by Paulo Soares.
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

import java.util.HashSet;
import java.util.Collection;

/**
 * Content typically belongs to a single optional content group,
 * and is visible when the group is <B>ON</B> and invisible when it is <B>OFF</B>. To express more
 * complex visibility policies, content should not declare itself to belong to an optional
 * content group directly, but rather to an optional content membership dictionary
 * represented by this class.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfLayerMembership extends PdfDictionary implements PdfOCG {
    
    /**
     * Visible only if all of the entries are <B>ON</B>.
     */    
    public static PdfName ALLON = new PdfName("AllOn");
    /**
     * Visible if any of the entries are <B>ON</B>.
     */    
    public static PdfName ANYON = new PdfName("AnyOn");
    /**
     * Visible if any of the entries are <B>OFF</B>.
     */    
    public static PdfName ANYOFF = new PdfName("AnyOff");
    /**
     * Visible only if all of the entries are <B>OFF</B>.
     */    
    public static PdfName ALLOFF = new PdfName("AllOff");

    PdfIndirectReference ref;
    PdfArray members = new PdfArray();
    HashSet layers = new HashSet();
    
    /**
     * Creates a new, empty, membership layer.
     * @param writer the writer
     */    
    public PdfLayerMembership(PdfWriter writer) {
        super(PdfName.OCMD);
        put(PdfName.OCGS, members);
        ref = writer.getPdfIndirectReference();
    }
    
    /**
     * Gets the <CODE>PdfIndirectReference</CODE> that represents this membership layer.
     * @return the <CODE>PdfIndirectReference</CODE> that represents this layer
     */    
    public PdfIndirectReference getRef() {
        return ref;
    }
    
    /**
     * Adds a new member to the layer.
     * @param layer the new member to the layer
     */    
    public void addMember(PdfLayer layer) {
        if (!layers.contains(layer)) {
            members.add(layer.getRef());
            layers.add(layer);
        }
    }
    
    /**
     * Gets the member layers.
     * @return the member layers
     */    
    public Collection getLayers() {
        return layers;
    }
    
    /**
     * Sets the visibility policy for content belonging to this
     * membership dictionary. Possible values are ALLON, ANYON, ANYOFF and ALLOFF.
     * The default value is ANYON.
     * @param type the visibility policy
     */    
    public void setVisibilityPolicy(PdfName type) {
        put(PdfName.P, type);
    }
    
    /**
     * Gets the dictionary representing the membership layer. It just returns <CODE>this</CODE>.
     * @return the dictionary representing the layer
     */    
    public PdfObject getPdfObject() {
        return this;
    }
}
