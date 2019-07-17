/*
 * $Id$
 *
 * Copyright 2006 Bruno Lowagie
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

package pdftk.com.lowagie.text.pdf.interfaces;

import pdftk.com.lowagie.text.pdf.PdfDeveloperExtension;
import pdftk.com.lowagie.text.pdf.PdfName;

/**
 * The PDF version is described in the PDF Reference 1.7 p92
 * (about the PDF Header) and page 139 (the version entry in
 * the Catalog). You'll also find info about setting the version
 * in the book 'iText in Action' sections 2.1.3 (PDF Header)
 * and 3.3 (Version history).
 */

public interface PdfVersion {
    
    /**
	 * If the PDF Header hasn't been written yet,
	 * this changes the version as it will appear in the PDF Header.
	 * If the PDF header was already written to the OutputStream,
	 * this changes the version as it will appear in the Catalog.
	 * @param version	a character representing the PDF version
	 */
	public void setPdfVersion(char version);
    /**
	 * If the PDF Header hasn't been written yet,
	 * this changes the version as it will appear in the PDF Header,
	 * but only if the parameter refers to a higher version.
	 * If the PDF header was already written to the OutputStream,
	 * this changes the version as it will appear in the Catalog.
	 * @param version	a character representing the PDF version
	 */
	public void setAtLeastPdfVersion(char version);
	/**
	 * Sets the PDF version as it will appear in the Catalog.
	 * Note that this only has effect if you use a later version
	 * than the one that appears in the header; this method
	 * ignores the parameter if you try to set a lower version.
	 * @param version	the PDF name that will be used for the Version key in the catalog
	 */
	public void setPdfVersion(PdfName version);
	/**
	 * Adds a developer extension to the Extensions dictionary
	 * in the Catalog.
	 * @param de	an object that contains the extensions prefix and dictionary
	 * @since	2.1.6
	 */
	public void addDeveloperExtension(PdfDeveloperExtension de);
}