/*
 * $Id: PdfPageEventHelper.java,v 1.23 2002/07/09 11:28:24 blowagie Exp $
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
import pdftk.com.lowagie.text.Document;
import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.Paragraph;

/**
 * Helps the use of <CODE>PdfPageEvent</CODE> by implementing all the interface methods.
 * A class can extend <CODE>PdfPageEventHelper</CODE> and only implement the
 * needed methods.
 *<p>
 * Note: do not use Document.add() inside a page event.
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class PdfPageEventHelper implements PdfPageEvent {
    
/**
 * Called when the document is opened.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 */
    public void onOpenDocument(PdfWriter writer,Document document) {
    }
    
/**
 * Called when a page is initialized.
 * <P>
 * Note that if even if a page is not written this method is still
 * called. It is preferable to use <CODE>onEndPage</CODE> to avoid
 * infinite loops.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 */
    public void onStartPage(PdfWriter writer,Document document) {
    }
    
/**
 * Called when a page is finished, just before being written to the document.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 */
    public void onEndPage(PdfWriter writer,Document document) {
    }
    
/**
 * Called when the document is closed.
 * <P>
 * Note that this method is called with the page number equal
 * to the last page plus one.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 */
    public void onCloseDocument(PdfWriter writer,Document document) {
    }
    
/**
 * Called when a Paragraph is written.
 * <P>
 * <CODE>paragraphPosition</CODE> will hold the height at which the
 * paragraph will be written to. This is useful to insert bookmarks with
 * more control.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 * @param paragraphPosition the position the paragraph will be written to
 */
    public void onParagraph(PdfWriter writer,Document document,float paragraphPosition) {
    }
    
/**
 * Called when a Paragraph is written.
 * <P>
 * <CODE>paragraphPosition</CODE> will hold the height of the end of the paragraph.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 * @param paragraphPosition the position of the end of the paragraph
 */
    public void onParagraphEnd(PdfWriter writer,Document document,float paragraphPosition) {
    }
    
/**
 * Called when a Chapter is written.
 * <P>
 * <CODE>position</CODE> will hold the height at which the
 * chapter will be written to.
 *
 * @param writer            the <CODE>PdfWriter</CODE> for this document
 * @param document          the document
 * @param paragraphPosition the position the chapter will be written to
 * @param title             the title of the Chapter
 */
    public void onChapter(PdfWriter writer,Document document,float paragraphPosition,Paragraph title) {
    }
    
/**
 * Called when the end of a Chapter is reached.
 * <P>
 * <CODE>position</CODE> will hold the height of the end of the chapter.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 * @param position the position of the end of the chapter.
 */
    public void onChapterEnd(PdfWriter writer,Document document,float position) {
    }
    
/**
 * Called when a Section is written.
 * <P>
 * <CODE>position</CODE> will hold the height at which the
 * section will be written to.
 *
 * @param writer            the <CODE>PdfWriter</CODE> for this document
 * @param document          the document
 * @param paragraphPosition the position the section will be written to
 * @param depth				the number depth of the Section
 * @param title             the title of the section
 */
    public void onSection(PdfWriter writer,Document document,float paragraphPosition,int depth,Paragraph title) {
    }
    
/**
 * Called when the end of a Section is reached.
 * <P>
 * <CODE>position</CODE> will hold the height of the section end.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 * @param position the position of the end of the section
 */
    public void onSectionEnd(PdfWriter writer,Document document,float position) {
    }
    
/**
 * Called when a <CODE>Chunk</CODE> with a generic tag is written.
 * <P>
 * It is usefull to pinpoint the <CODE>Chunk</CODE> location to generate
 * bookmarks, for example.
 *
 * @param writer the <CODE>PdfWriter</CODE> for this document
 * @param document the document
 * @param rect the <CODE>Rectangle</CODE> containing the <CODE>Chunk</CODE>
 * @param text the text of the tag
 */
    public void onGenericTag(PdfWriter writer,Document document,Rectangle rect,String text) {
    }
}
