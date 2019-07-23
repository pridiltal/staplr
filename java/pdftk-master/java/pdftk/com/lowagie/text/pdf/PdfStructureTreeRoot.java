/*
 * $Id: PRTokeniser.java,v 1.15 2002/06/20 13:30:25 blowagie Exp $
 *
 * Copyright 2005 by Paulo Soares.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;

/**
 * The structure tree root corresponds to the highest hierarchy level in a tagged PDF.
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfStructureTreeRoot extends PdfDictionary {
    
    private HashMap parentTree = new HashMap();
    private PdfIndirectReference reference;

    /**
     * Holds value of property writer.
     */
    private PdfWriter writer;
    
    /** Creates a new instance of PdfStructureTreeRoot */
    PdfStructureTreeRoot(PdfWriter writer) {
        super(PdfName.STRUCTTREEROOT);
        this.writer = writer;
        reference = writer.getPdfIndirectReference();
    }
    
    /**
     * Maps the user tags to the standard tags. The mapping will allow a standard application to make some sense of the tagged
     * document whatever the user tags may be.
     * @param used the user tag
     * @param standard the standard tag
     */    
    public void mapRole(PdfName used, PdfName standard) {
        PdfDictionary rm = (PdfDictionary)get(PdfName.ROLEMAP);
        if (rm == null) {
            rm = new PdfDictionary();
            put(PdfName.ROLEMAP, rm);
        }
        rm.put(used, standard);
    }
    
    /**
     * Gets the writer.
     * @return the writer
     */
    public PdfWriter getWriter() {
        return this.writer;
    }

    /**
     * Gets the reference this object will be written to.
     * @return the reference this object will be written to
     */    
    public PdfIndirectReference getReference() {
        return this.reference;
    }
    
    void setPageMark(int page, PdfIndirectReference struc) {
        Integer i = new Integer(page);
        PdfArray ar = (PdfArray)parentTree.get(i);
        if (ar == null) {
            ar = new PdfArray();
            parentTree.put(i, ar);
        }
        ar.add(struc);
    }
    
    private void nodeProcess(PdfDictionary struc, PdfIndirectReference reference) throws IOException {
        PdfObject obj = struc.get(PdfName.K);
        if (obj != null && obj.isArray() && !((PdfObject)((PdfArray)obj).getArrayList().get(0)).isNumber()) {
            PdfArray ar = (PdfArray)obj;
            ArrayList a = ar.getArrayList();
            for (int k = 0; k < a.size(); ++k) {
                PdfStructureElement e = (PdfStructureElement)a.get(k);
                a.set(k, e.getReference());
                nodeProcess(e, e.getReference());
            }
        }
        if (reference != null)
            writer.addToBody(struc, reference);
    }
    
    void buildTree() throws IOException {
        HashMap numTree = new HashMap();
        for (Iterator it = parentTree.keySet().iterator(); it.hasNext();) {
            Integer i = (Integer)it.next();
            PdfArray ar = (PdfArray)parentTree.get(i);
            numTree.put(i, writer.addToBody(ar).getIndirectReference());
        }
        PdfDictionary dicTree = PdfNumberTree.writeTree(numTree, writer);
        if (dicTree != null)
            put(PdfName.PARENTTREE, writer.addToBody(dicTree).getIndirectReference());
        
        nodeProcess(this, reference);
    }
}
