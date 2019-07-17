/*
 * Copyright 2002 by Josselin PUJO.
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

public class PdfTransition {
    /**
     *  Out Vertical Split
     */
    public static final int SPLITVOUT      = 1;
    /**
     *  Out Horizontal Split
     */
    public static final int SPLITHOUT      = 2;
    /**
     *  In Vertical Split
     */
    public static final int SPLITVIN      = 3;
    /**
     *  IN Horizontal Split
     */
    public static final int SPLITHIN      = 4;
    /**
     *  Vertical Blinds
     */
    public static final int BLINDV      = 5;
    /**
     *  Vertical Blinds
     */
    public static final int BLINDH      = 6;
    /**
     *  Inward Box
     */
    public static final int INBOX       = 7;
    /**
     *  Outward Box
     */
    public static final int OUTBOX      = 8;
    /**
     *  Left-Right Wipe
     */
    public static final int LRWIPE      = 9;
    /**
     *  Right-Left Wipe
     */
    public static final int RLWIPE     = 10;
    /**
     *  Bottom-Top Wipe
     */
    public static final int BTWIPE     = 11;
    /**
     *  Top-Bottom Wipe
     */
    public static final int TBWIPE     = 12;
    /**
     *  Dissolve
     */
    public static final int DISSOLVE    = 13;
    /**
     *  Left-Right Glitter
     */
    public static final int LRGLITTER   = 14;
    /**
     *  Top-Bottom Glitter
     */
    public static final int TBGLITTER  = 15;
    /**
     *  Diagonal Glitter
     */
    public static final int DGLITTER  = 16;
    
    /**
     *  duration of the transition effect
     */
    protected int duration;
    /**
     *  type of the transition effect
     */
    protected int type;
    
    /**
     *  Constructs a <CODE>Transition</CODE>.
     *
     */
    public PdfTransition() {
        this(BLINDH);
    }
    
    /**
     *  Constructs a <CODE>Transition</CODE>.
     *
     *@param  type      type of the transition effect
     */
    public PdfTransition(int type) {
        this(type,1);
    }
    
    /**
     *  Constructs a <CODE>Transition</CODE>.
     *
     *@param  type      type of the transition effect
     *@param  duration  duration of the transition effect
     */
    public PdfTransition(int type, int duration) {
        this.duration = duration;
        this.type = type;
    }
    
    
    public int getDuration() {
        return duration;
    }
    
    
    public int getType() {
        return type;
    }
    
    public PdfDictionary getTransitionDictionary() {
        PdfDictionary trans = new PdfDictionary(PdfName.TRANS);
        switch (type) {
            case SPLITVOUT:
                trans.put(PdfName.S,PdfName.SPLIT);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DM,PdfName.V);
                trans.put(PdfName.M,PdfName.O);
                break;
            case SPLITHOUT:
                trans.put(PdfName.S,PdfName.SPLIT);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DM,PdfName.H);
                trans.put(PdfName.M,PdfName.O);
                break;
            case SPLITVIN:
                trans.put(PdfName.S,PdfName.SPLIT);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DM,PdfName.V);
                trans.put(PdfName.M,PdfName.I);
                break;
            case SPLITHIN:
                trans.put(PdfName.S,PdfName.SPLIT);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DM,PdfName.H);
                trans.put(PdfName.M,PdfName.I);
                break;
            case BLINDV:
                trans.put(PdfName.S,PdfName.BLINDS);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DM,PdfName.V);
                break;
            case BLINDH:
                trans.put(PdfName.S,PdfName.BLINDS);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DM,PdfName.H);
                break;
            case INBOX:
                trans.put(PdfName.S,PdfName.BOX);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.M,PdfName.I);
                break;
            case OUTBOX:
                trans.put(PdfName.S,PdfName.BOX);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.M,PdfName.O);
                break;
            case LRWIPE:
                trans.put(PdfName.S,PdfName.WIPE);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(0));
                break;
            case RLWIPE:
                trans.put(PdfName.S,PdfName.WIPE);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(180));
                break;
            case BTWIPE:
                trans.put(PdfName.S,PdfName.WIPE);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(90));
                break;
            case TBWIPE:
                trans.put(PdfName.S,PdfName.WIPE);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(270));
                break;
            case DISSOLVE:
                trans.put(PdfName.S,PdfName.DISSOLVE);
                trans.put(PdfName.D,new PdfNumber(duration));
                break;
            case LRGLITTER:
                trans.put(PdfName.S,PdfName.GLITTER);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(0));
                break;
            case TBGLITTER:
                trans.put(PdfName.S,PdfName.GLITTER);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(270));
                break;
            case DGLITTER:
                trans.put(PdfName.S,PdfName.GLITTER);
                trans.put(PdfName.D,new PdfNumber(duration));
                trans.put(PdfName.DI,new PdfNumber(315));
                break;
        }
        return trans;
    }
}

