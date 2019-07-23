/*
 * $Id: DocListener.java,v 1.52 2004/12/14 11:52:47 blowagie Exp $
 * $Name:  $
 *
 * Copyright (c) 1999, 2000, 2001, 2002 Bruno Lowagie.
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
 * A class that implements <CODE>DocListener</CODE> will perform some
 * actions when some actions are performed on a <CODE>Document</CODE>.
 *
 * @see		ElementListener
 * @see		Document
 * @see		DocWriter
 */

public interface DocListener extends ElementListener {
    
    // methods
    
/**
 * Signals that the <CODE>Document</CODE> has been opened and that
 * <CODE>Elements</CODE> can be added.
 */
    
    public void open();
    
/**
 * Sets the pagesize.
 *
 * @param	pageSize	the new pagesize
 * @return	a <CODE>boolean</CODE>
 */
    
    public boolean setPageSize(Rectangle pageSize);
    
/**
 * Signals that a <CODE>Watermark</CODE> was added to the <CODE>Document</CODE>.
 * 
 * @param watermark the Watermark object
 * @return	<CODE>true</CODE> if the element was added, <CODE>false</CODE> if not.
 */
    
    // public boolean add(Watermark watermark); ssteward: dropped in 1.44
    
/**
 * Signals that a <CODE>Watermark</CODE> was removed from the <CODE>Document</CODE>.
 */
    
    // public void removeWatermark(); ssteward: dropped in 1.44
    
/**
 * Sets the margins.
 *
 * @param	marginLeft		the margin on the left
 * @param	marginRight		the margin on the right
 * @param	marginTop		the margin on the top
 * @param	marginBottom	the margin on the bottom
 * @return	a <CODE>boolean</CODE>
 */
    
    public boolean setMargins(float marginLeft, float marginRight, float marginTop, float marginBottom);
    
    /**
     * Parameter that allows you to do margin mirroring (odd/even pages)
     * @param marginMirroring
     * @return true if succesfull
     */
    public boolean setMarginMirroring(boolean marginMirroring);
    
/**
 * Signals that an new page has to be started.
 *
 * @return	<CODE>true</CODE> if the page was added, <CODE>false</CODE> if not.
 * @throws	DocumentException	when a document isn't open yet, or has been closed
 */
    
    public boolean newPage() throws DocumentException;
    
/**
 * Changes the header of this document.
 *
 * @param	header		the new header
 */
    
    // public void setHeader(HeaderFooter header); ssteward: dropped in 1.44
    
/**
 * Resets the header of this document.
 */
    
    // public void resetHeader(); ssteward: dropped in 1.44
    
/**
 * Changes the footer of this document.
 *
 * @param	footer		the new footer
 */
    
    // public void setFooter(HeaderFooter footer); ssteward: dropped in 1.44
    
/**
 * Resets the footer of this document.
 */
    
    // public void resetFooter(); ssteward: dropped in 1.44
    
/**
 * Sets the page number to 0.
 */
    
    public void resetPageCount();
    
/**
 * Sets the page number.
 *
 * @param	pageN		the new page number
 */
    
    public void setPageCount(int pageN);
    
/**
 * Clears text wrapping around images (if applicable).
 * Method suggested by Pelikan Stephan
 * @throws DocumentException
 */
	public void clearTextWrap() throws DocumentException;
    
/**
 * Signals that the <CODE>Document</CODE> was closed and that no other
 * <CODE>Elements</CODE> will be added.
 * <P>
 * The outputstream of every writer implementing <CODE>DocListener</CODE> will be closed.
 */
    
    public void close();
}