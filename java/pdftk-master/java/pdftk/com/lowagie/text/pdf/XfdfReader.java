/* -*- Mode: Java; tab-width: 4; c-basic-offset: 4 -*- */
/*
 *
 * Copyright 2004 by Leonard Rosenthol.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream; // ssteward
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Stack;

/**
 * Reads a XFDF.
 * @author Leonard Rosenthol (leonardr@pdfsages.com)
 */
public class XfdfReader implements SimpleXMLDocHandler {
	// stuff used during parsing to handle state
	private boolean foundRoot = false;
    private Stack fieldNames = new Stack();
    private Stack fieldValues = new Stack();

    // storage for the field list and their values
	HashMap	fields;
	
    // storage for the field list and their rich text; ssteward
	HashMap	fieldsRichText;
	
	// storage for the path to referenced PDF, if any
	String	fileSpec;
	
   /** Reads an XFDF form.
     * @param filename the file name of the form
     * @throws IOException on error
     */    
    public XfdfReader(String filename) throws IOException {
		InputStream fin = null; // ssteward: was FileInputStream
		try {
			// ssteward: added for stdin handling (also see RandomAccessFileOrArray.java)
			if( filename.equals("-") ) {
				fin = System.in;
			}
			else {
				fin = new FileInputStream(filename);
			}
			SimpleXMLParser.parse(this, fin);
		}
		finally {
			try{fin.close();}catch(Exception e){}
		}
    }

	// ssteward
    public XfdfReader(InputStream fin) throws IOException {
		try {
			SimpleXMLParser.parse(this, fin);
		}
		finally {
			try{fin.close();}catch(Exception e){}
		}
    }

    /** Reads an XFDF form.
     * @param xfdfIn the byte array with the form
     * @throws IOException on error
     */    
    public XfdfReader(byte xfdfIn[]) throws IOException {
        SimpleXMLParser.parse( this, new ByteArrayInputStream(xfdfIn));
   }
    
    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the value is a merged <CODE>PdfDictionary</CODE>
     * with the field content.
     * @return all the fields
     */    
    public HashMap getFields() {
        return fields;
    }
    
    /** Gets the field value.
     * @param name the fully qualified field name
     * @return the field's value
     */    
    public String getField(String name) {
        return (String)fields.get(name);
    }
    
    /** Gets the field value or <CODE>null</CODE> if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * @return the field value or <CODE>null</CODE>
     */    
    public String getFieldValue(String name) {
        String field = (String)fields.get(name);
        if (field == null)
            return null;
        else
        	return field;
    }
    
	// ssteward
	// for parity with FdfReader addition
    public String getFieldRichValue(String name) {
        String field = (String)fieldsRichText.get(name);
        if (field == null)
            return null;
        else
        	return field;
    }
    
    /** Gets the PDF file specification contained in the FDF.
     * @return the PDF file specification contained in the FDF
     */    
    public String getFileSpec() {
        return fileSpec;
    }

    /**
     * Called when a start tag is found.
     * @param tag the tag name
     * @param h the tag's attributes
     */    
    public void startElement(String tag, HashMap h)
    {
        if ( !foundRoot ) {
            if (!tag.equals("xfdf"))
                throw new RuntimeException("Root element is not Bookmark.");
            else 
            	foundRoot = true;
        }

        if ( tag.equals("xfdf") ){
    		
    	} else if ( tag.equals("f") ) {
    		fileSpec = (String)h.get( "href" );
    	} else if ( tag.equals("fields") ) {
            fields = new HashMap();		// init it!
            fieldsRichText = new HashMap();
    	} else if ( tag.equals("field") ) {
    		String	fName = (String) h.get( "name" );
    		fieldNames.push( fName );
    	} else if ( tag.equals("value") ||
					tag.equals("value-richtext") ) // ssteward
			{
				fieldValues.push( (String)"" );
			}
    }

    /**
     * Called when an end tag is found.
     * @param tag the tag name
     */    
    public void endElement(String tag) {
        if ( tag.equals("value") ||
			 tag.equals("value-richtext") ) // ssteward
			{
				String	fName = "";
				for (int k = 0; k < fieldNames.size(); ++k) {
					fName += "." + (String)fieldNames.elementAt(k);
				}
				if (fName.startsWith("."))
					fName = fName.substring(1);
				String	fVal = (String) fieldValues.pop();

				if (tag.equals("value")) { // ssteward
					fields.put( fName, fVal );
				}
				else { // rich text value
					fieldsRichText.put( fName, fVal );
				}
			}
        else if (tag.equals("field") ) {
            if (!fieldNames.isEmpty())
                fieldNames.pop();
        }
    }
    
    /**
     * Called when the document starts to be parsed.
     */    
    public void startDocument()
    {
        fileSpec = new String("");	// and this too...
    }
    /**
     * Called after the document is parsed.
     */    
    public void endDocument()
	{
    	
	}
    /**
     * Called when a text element is found.
     * @param str the text element, probably a fragment.
     */    
    public void text(String str)
    {
        if (fieldNames.isEmpty() || fieldValues.isEmpty())
            return;
        
        String val = (String)fieldValues.pop();
        val += str;
        fieldValues.push(val);
    }
}