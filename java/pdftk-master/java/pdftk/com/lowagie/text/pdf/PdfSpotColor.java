/*
 * $Id: PdfSpotColor.java,v 1.45 2005/03/24 12:38:19 blowagie Exp $
 * $Name:  $
 *
 * Copyright 2001, 2002 Paulo Soares
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

import java.awt.Color;
import java.io.IOException;
/**
 * A <CODE>PdfSpotColor</CODE> defines a ColorSpace
 *
 * @see		PdfDictionary
 */

public class PdfSpotColor{
    
/*	The tint value */
    protected float tint;
    
/**	The color name */
    public PdfName name;
    
/** The alternative color space */
    public Color altcs;
    // constructors
    
    /**
     * Constructs a new <CODE>PdfSpotColor</CODE>.
     *
     * @param		name		a String value
     * @param		tint		a tint value between 0 and 1
     * @param		altcs		a altnative colorspace value
     */
    
    public PdfSpotColor(String name, float tint, Color altcs) {
        this.name = new PdfName(name);
        this.tint = tint;
        this.altcs = altcs;
    }
    
    /**
     * Gets the tint of the SpotColor.
     * @return a float
     */
    public float getTint() {
        return tint;
    }
    
    /**
     * Gets the alternative ColorSpace.
     * @return a Colot
     */
    public Color getAlternativeCS() {
        return altcs;
    }
    
    protected PdfObject getSpotObject(PdfWriter writer) throws IOException {
        PdfArray array = new PdfArray(PdfName.SEPARATION);
        array.add(name);
        PdfFunction func = null;
        if (altcs instanceof ExtendedColor) {
            int type = ((ExtendedColor)altcs).type;

	    // ssteward
	    // having trouble with unreachable bytecode in switch default (per gcj 4.4)
	    // so try a clumsier workaround
	    boolean handled_b= false;
            switch (type) {
                case ExtendedColor.TYPE_GRAY:
                    array.add(PdfName.DEVICEGRAY);
                    func = PdfFunction.type2(writer, new float[]{0, 1}, null, new float[]{0}, new float[]{((GrayColor)altcs).getGray()}, 1);
		    handled_b= true;
                    break;
                case ExtendedColor.TYPE_CMYK:
                    array.add(PdfName.DEVICECMYK);
                    CMYKColor cmyk = (CMYKColor)altcs;
                    func = PdfFunction.type2(writer, new float[]{0, 1}, null, new float[]{0, 0, 0, 0},
                        new float[]{cmyk.getCyan(), cmyk.getMagenta(), cmyk.getYellow(), cmyk.getBlack()}, 1);
		    handled_b= true;
                    break;
//                default:
//                    throw new RuntimeException("Only RGB, Gray and CMYK are supported as alternative color spaces.");
            }
	    if( !handled_b ) {
		throw new RuntimeException("Only RGB, Gray and CMYK are supported as alternative color spaces.");
	    }
        }
        else {
            array.add(PdfName.DEVICERGB);
            func = PdfFunction.type2(writer, new float[]{0, 1}, null, new float[]{1, 1, 1},
                new float[]{(float)altcs.getRed() / 255, (float)altcs.getGreen() / 255, (float)altcs.getBlue() / 255}, 1);
        }
        array.add(func.getReference());
        return array;
    }
}
