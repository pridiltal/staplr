/*
 * $Id: PRTokeniser.java,v 1.15 2002/06/20 13:30:25 blowagie Exp $
 *
 * Copyright 2003-2005 by Paulo Soares.
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

import java.util.HashMap;
import java.util.Iterator;

class PageResources {
    
    protected PdfDictionary fontDictionary = new PdfDictionary();
    protected PdfDictionary xObjectDictionary = new PdfDictionary();
    protected PdfDictionary colorDictionary = new PdfDictionary();
    protected PdfDictionary patternDictionary = new PdfDictionary();
    protected PdfDictionary shadingDictionary = new PdfDictionary();
    protected PdfDictionary extGStateDictionary = new PdfDictionary();
    protected PdfDictionary propertyDictionary = new PdfDictionary();
    protected HashMap forbiddenNames;
    protected PdfDictionary originalResources;
    protected int namePtr[] = {0};
    protected HashMap usedNames;

    PageResources() {
    }
    
    void setOriginalResources(PdfDictionary resources, int newNamePtr[]) {
        if (newNamePtr != null)
            namePtr = newNamePtr;
        originalResources = resources;
        forbiddenNames = new HashMap();
        usedNames = new HashMap();
        if (resources == null)
            return;
        for (Iterator i = resources.getKeys().iterator(); i.hasNext();) {
            PdfObject sub = PdfReader.getPdfObject(resources.get((PdfName)i.next()));
            if (sub.isDictionary()) {
                PdfDictionary dic = (PdfDictionary)sub;
                for (Iterator j = dic.getKeys().iterator(); j.hasNext();) {
                    forbiddenNames.put(j.next(), null);
                }
            }
        }
    }
    
    PdfName translateName(PdfName name) {
        PdfName translated = name;
        if (forbiddenNames != null) {
            translated = (PdfName)usedNames.get(name);
            if (translated == null) {
                while (true) {
                    translated = new PdfName("Xi" + (namePtr[0]++));
                    if (!forbiddenNames.containsKey(translated))
                        break;
                }
                usedNames.put(name, translated);
            }
        }
        return translated;
    }
    
    PdfName addFont(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        fontDictionary.put(name, reference);
        return name;
    }

    PdfName addXObject(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        xObjectDictionary.put(name, reference);
        return name;
    }

    PdfName addColor(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        colorDictionary.put(name, reference);
        return name;
    }

    void addDefaultColor(PdfName name, PdfObject obj) {
        if (obj == null || obj.isNull())
            colorDictionary.remove(name);
        else
            colorDictionary.put(name, obj);
    }

    void addDefaultColor(PdfDictionary dic) {
        colorDictionary.merge(dic);
    }

    void addDefaultColorDiff(PdfDictionary dic) {
        colorDictionary.mergeDifferent(dic);
    }

    PdfName addShading(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        shadingDictionary.put(name, reference);
        return name;
    }
    
    PdfName addPattern(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        patternDictionary.put(name, reference);
        return name;
    }

    PdfName addExtGState(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        extGStateDictionary.put(name, reference);
        return name;
    }

    PdfName addProperty(PdfName name, PdfIndirectReference reference) {
        name = translateName(name);
        propertyDictionary.put(name, reference);
        return name;
    }

    PdfDictionary getResources() {
       PdfResources resources = new PdfResources();
        if (originalResources != null)
            resources.putAll(originalResources);
        resources.put(PdfName.PROCSET, new PdfLiteral("[/PDF /Text /ImageB /ImageC /ImageI]"));
        resources.add(PdfName.FONT, fontDictionary);
        resources.add(PdfName.XOBJECT, xObjectDictionary);
        resources.add(PdfName.COLORSPACE, colorDictionary);
        resources.add(PdfName.PATTERN, patternDictionary);
        resources.add(PdfName.SHADING, shadingDictionary);
        resources.add(PdfName.EXTGSTATE, extGStateDictionary);
        resources.add(PdfName.PROPERTIES, propertyDictionary);
        return resources;
    }
    
    boolean hasResources() {
        return (fontDictionary.size() > 0
            || xObjectDictionary.size() > 0
            || colorDictionary.size() > 0
            || patternDictionary.size() > 0
            || shadingDictionary.size() > 0
            || extGStateDictionary.size() > 0
            || propertyDictionary.size() > 0);
    }
}