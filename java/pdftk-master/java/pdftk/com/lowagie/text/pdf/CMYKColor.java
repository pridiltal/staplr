/*
 * $Id: CMYKColor.java,v 1.43 2005/05/02 11:12:44 blowagie Exp $
 * $Name:  $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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
 *
 * @author  Paulo Soares (psoares@consiste.pt)
 */
public class CMYKColor extends ExtendedColor {

    /** A serial version UID */
    private static final long serialVersionUID = 5940378778276468452L;

    float cyan;
    float magenta;
    float yellow;
    float black;

    /**
     * Constructs a CMYK Color beased on 4 colorvalues (values are integers from 0 to 255).
     * @param intCyan
     * @param intMagenta
     * @param intYellow
     * @param intBlack
     */
    public CMYKColor(int intCyan, int intMagenta, int intYellow, int intBlack) {
        this((float)intCyan / 255f, (float)intMagenta / 255f, (float)intYellow / 255f, (float)intBlack / 255f);
    }

    /**
     * Construct a CMYK Color.
     * @param floatCyan
     * @param floatMagenta
     * @param floatYellow
     * @param floatBlack
     */
    public CMYKColor(float floatCyan, float floatMagenta, float floatYellow, float floatBlack) {
        super(TYPE_CMYK, 1f - floatCyan - floatBlack, 1f - floatMagenta - floatBlack, 1f - floatYellow - floatBlack);
        cyan = normalize(floatCyan);
        magenta = normalize(floatMagenta);
        yellow = normalize(floatYellow);
        black = normalize(floatBlack);
    }
    
    /**
     * @return the cyan value
     */
    public float getCyan() {
        return cyan;
    }

    /**
     * @return the magenta value
     */
    public float getMagenta() {
        return magenta;
    }

    /**
     * @return the yellow value
     */
    public float getYellow() {
        return yellow;
    }

    /**
     * @return the black value
     */
    public float getBlack() {
        return black;
    }

}
