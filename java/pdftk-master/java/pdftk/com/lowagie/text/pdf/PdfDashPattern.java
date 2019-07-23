/*
 * $Id: PdfDashPattern.java,v 1.33 2003/05/02 09:01:18 blowagie Exp $
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * A <CODE>PdfDashPattern</CODE> defines a dash pattern as described in
 * the PDF Reference Manual version 1.3 p 325 (section 8.4.3).
 *
 * @see		PdfArray
 */

public class PdfDashPattern extends PdfArray {
    
    // membervariables
    
/** This is the length of a dash. */
    private float dash = -1;
    
/** This is the length of a gap. */
    private float gap = -1;
    
/** This is the phase. */
    private float phase = -1;
    
    // constructors
    
/**
 * Constructs a new <CODE>PdfDashPattern</CODE>.
 */
    
    public PdfDashPattern() {
        super();
    }
    
/**
 * Constructs a new <CODE>PdfDashPattern</CODE>.
 */
    
    public PdfDashPattern(float dash) {
        super(new PdfNumber(dash));
        this.dash = dash;
    }
    
/**
 * Constructs a new <CODE>PdfDashPattern</CODE>.
 */
    
    public PdfDashPattern(float dash, float gap) {
        super(new PdfNumber(dash));
        add(new PdfNumber(gap));
        this.dash = dash;
        this.gap = gap;
    }
    
/**
 * Constructs a new <CODE>PdfDashPattern</CODE>.
 */
    
    public PdfDashPattern(float dash, float gap, float phase) {
        super(new PdfNumber(dash));
        add(new PdfNumber(gap));
        this.dash = dash;
        this.gap = gap;
        this.phase = phase;
    }
    
    public void add(float n) {
        add(new PdfNumber(n));
    }
    
/**
 * Returns the PDF representation of this <CODE>PdfArray</CODE>.
 *
 * @return		an array of <CODE>byte</CODE>s
 */
    
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        os.write('[');

        if (dash >= 0) {
            new PdfNumber(dash).toPdf(writer, os);
            if (gap >= 0) {
                os.write(' ');
                new PdfNumber(gap).toPdf(writer, os);
            }
        }
        os.write(']');
        if (phase >=0) {
            os.write(' ');
            new PdfNumber(phase).toPdf(writer, os);
        }
    }
}