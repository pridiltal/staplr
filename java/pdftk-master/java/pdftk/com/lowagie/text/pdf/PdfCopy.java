/* -*- Mode: Java; tab-width: 4; c-basic-offset: 4 -*- */
/*
 * $Id: PdfCopy.java,v 1.35 2005/05/04 14:32:21 blowagie Exp $
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
 * Co-Developer of the code is Sid Steward. Portions created by the Co-Developer
 * are Copyright (C) 2004, 2010 by Sid Steward. All Rights Reserved.
 *
 * This module by Mark Thompson. Copyright (C) 2002 Mark Thompson
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
import java.util.ArrayList;
// ssteward omit: import java.util.List;
import java.util.Iterator;
import java.io.*;
// ssteward omit: import pdftk.com.lowagie.text.ExceptionConverter;
import pdftk.com.lowagie.text.Document;
import pdftk.com.lowagie.text.DocumentException;

import java.util.HashSet;

/**
 * Make copies of PDF documents. Documents can be edited after reading and
 * before writing them out.
 * @author Mark Thompson
 */

public class PdfCopy extends PdfWriter {
    /**
     * This class holds information about indirect references, since they are
     * renumbered by iText.
     */
    static class IndirectReferences {
        PdfIndirectReference theRef = null;
        boolean hasCopied = false;
        IndirectReferences(PdfIndirectReference ref) {
            theRef = ref;
            hasCopied = false;
        }
        void setCopied() { hasCopied = true; }
        boolean getCopied() { return hasCopied; }
        PdfIndirectReference getRef() { return theRef; }
    };
    protected HashMap indirects = null;
    protected HashMap indirectMap = null;
    protected int currentObjectNum = 1;
    protected PdfReader reader = null;
	// ssteward: why does PdfCopy have an acroForm, when PdfDocument already has one
    //protected PdfIndirectReference acroForm = null;
    protected PdfIndirectReference topPageParent = null;
    protected ArrayList pageNumbersToRefs = new ArrayList();
    //protected List newBookmarks = null; // ssteward: pdftk 1.46
	protected PdfIndirectReference m_new_bookmarks = null; // ssteward: pdftk 1.46
	protected PdfIndirectReference m_new_extensions = null; // ssteward: pdftk 1.46
    
	// ssteward: pdftk 1.10; to ensure unique form field names, as pages are added
	protected HashSet fullFormFieldNames = null; // all full field names; track to prevent collision
	protected HashSet topFormFieldNames = null; // across all readers; track to prevent collision
	protected class TopFormFieldData {
		HashMap newNamesRefs = null; // map new top parent field names to refs
		HashMap newNamesKids = null; // map new top parent field names to kids PdfArray
		HashSet allNames = null; // ~all~ names, new and not-new
		public TopFormFieldData() {
			newNamesRefs= new HashMap();
			newNamesKids= new HashMap();
			allNames= new HashSet();
		}
	};
	protected HashMap topFormFieldReadersData = null;

    /**
     * A key to allow us to hash indirect references
     */
    protected static class RefKey {
        int num = 0;
        int gen = 0;
        RefKey(int num, int gen) {
            this.num = num;
            this.gen = gen;
        }
        RefKey(PdfIndirectReference ref) {
            num = ref.getNumber();
            gen = ref.getGeneration();
        }
        RefKey(PRIndirectReference ref) {
            num = ref.getNumber();
            gen = ref.getGeneration();
        }
        public int hashCode() {
            return (gen<<16)+num;
        }
        public boolean equals(Object o) {
            RefKey other = (RefKey)o;
            return this.gen == other.gen && this.num == other.num;
        }
        public String toString() {
            return "" + num + " " + gen;
        }
    }
    
    /**
     * Constructor
     * @param document
     * @param os outputstream
     */
    public PdfCopy(Document document, OutputStream os) throws DocumentException {
        super(/* ssteward omit: new PdfDocument(),*/ os);
        document.addDocListener(getPdfDocument());
        getPdfDocument().setWriter(this); // ssteward: okay
        indirectMap = new HashMap();

		// ssteward: pdftk 1.10
		fullFormFieldNames = new HashSet();
		topFormFieldNames = new HashSet();
		topFormFieldReadersData = new HashMap();
    }
    public void open() {
        super.open();
        topPageParent = getPdfIndirectReference();
        getRoot().setLinearMode(topPageParent);
    }

    /**
     * Grabs a page from the input document
     * @param reader the reader of the document
     * @param pageNumber which page to get
     * @return the page
     */
    public PdfImportedPage getImportedPage(PdfReader reader, int pageNumber) throws IOException {
        if (currentPdfReaderInstance != null) {
            if (currentPdfReaderInstance.getReader() != reader) {
                try {
                    currentPdfReaderInstance.getReader().close();
                    currentPdfReaderInstance.getReaderFile().close();
                }
                catch (IOException ioe) {
                    // empty on purpose
                }
                currentPdfReaderInstance = reader.getPdfReaderInstance(this);
            }
        }
        else {
            currentPdfReaderInstance = reader.getPdfReaderInstance(this);
        }
        return currentPdfReaderInstance.getImportedPage(pageNumber);            
    }
    
    
    /**
     * Translate a PRIndirectReference to a PdfIndirectReference
     * In addition, translates the object numbers, and copies the
     * referenced object to the output file.
     * NB: PRIndirectReferences (and PRIndirectObjects) really need to know what
     * file they came from, because each file has its own namespace. The translation
     * we do from their namespace to ours is *at best* heuristic, and guaranteed to
     * fail under some circumstances.
     */
    protected PdfIndirectReference copyIndirect(PRIndirectReference in) throws IOException, BadPdfFormatException {
        RefKey key = new RefKey(in);
        IndirectReferences iRef = (IndirectReferences)indirects.get(key);
		boolean recurse_b= true; // ssteward

        PdfIndirectReference retVal;
        if (iRef != null) {
            retVal = iRef.getRef();
            if (iRef.getCopied()) { // we've already copied this
                return retVal;
            }
        }
        else {
            retVal = body.getPdfIndirectReference();
            iRef = new IndirectReferences(retVal);
            indirects.put(key, iRef);
		}

		// ssteward; if this is a ref to a dictionary with a parent,
		// and we haven't copied the parent yet, then don't recurse
		// into this dictionary; wait to recurse via the parent;
		// this was written to fix a problem with references to pages
		// inside pdf destinations; this problem caused pdf bloat;
		// the pdf spec says broken indirect ref.s (ref, but no obj) are okay;
		//
		// update: we /do/ want to recurse up form field parents (see addPage()),
		// just /not/ pages with parents
		//
		// note: copying an indirect reference to a dictionary is
		// more suspect than copying a dictionary
		//
		// simplify this by not recursing into /any/ type==page via indirect ref?

		PdfObject in_obj= (PdfObject)PdfReader.getPdfObject( in );
		if( in_obj!= null && in_obj.isDictionary() ) {
			PdfDictionary in_dict= (PdfDictionary)in_obj;

			PdfName type= (PdfName)in_dict.get( PdfName.TYPE );
			if( type!= null && type.isName() && type.equals( PdfName.PAGE ) ) {

				PdfObject parent_obj=
					(PdfObject)in_dict.get( PdfName.PARENT );
				if( parent_obj!= null && parent_obj.isIndirect() ) {
					PRIndirectReference parent_iref= (PRIndirectReference)parent_obj;

					RefKey parent_key= new RefKey( parent_iref );
					IndirectReferences parent_ref= (IndirectReferences)indirects.get( parent_key );

					if( parent_ref== null || !parent_ref.getCopied() ) {
						// parent has not been copied yet, so we've jumped here somehow;
						recurse_b= false;
					}
				}
			}
		}

		if( recurse_b ) {
			iRef.setCopied();
			PdfObject obj = copyObject((PdfObject)PdfReader.getPdfObjectRelease(in));
			addToBody(obj, retVal);
		}

        return retVal;
    }
    
    /**
     * Translate a PRDictionary to a PdfDictionary. Also translate all of the
     * objects contained in it.
     */
    public PdfDictionary copyDictionary(PdfDictionary in)
    throws IOException, BadPdfFormatException {
        PdfDictionary out = new PdfDictionary();
        PdfName type = (PdfName)in.get(PdfName.TYPE);
        
        for (Iterator it = in.getKeys().iterator(); it.hasNext();) {
            PdfName key = (PdfName)it.next();
            PdfObject value = in.get(key);
			// System.err.println("Copy " + key); // debug
            if (type != null && PdfName.PAGE.equals(type)) {
                if (key.equals(PdfName.PARENT))
                    out.put(PdfName.PARENT, topPageParent);
                else if (!key.equals(PdfName.B))
                    out.put(key, copyObject(value));
            }
            else
                out.put(key, copyObject(value));
        }
        return out;
    }
    
    /**
     * Translate a PRStream to a PdfStream. The data part copies itself.
     */
    protected PdfStream copyStream(PRStream in) throws IOException, BadPdfFormatException {
        PRStream out = new PRStream(in, null);
        
        for (Iterator it = in.getKeys().iterator(); it.hasNext();) {
            PdfName key = (PdfName) it.next();
            PdfObject value = in.get(key);
            out.put(key, copyObject(value));
        }
        
        return out;
    }
    
    
    /**
     * Translate a PRArray to a PdfArray. Also translate all of the objects contained
     * in it
     */
    protected PdfArray copyArray(PdfArray in) throws IOException, BadPdfFormatException {
        PdfArray out = new PdfArray();
        
        for (Iterator i = in.getArrayList().iterator(); i.hasNext();) {
            PdfObject value = (PdfObject)i.next();
            out.add(copyObject(value));
        }
        return out;
    }
    
    /**
     * Translate a PR-object to a Pdf-object
     */
    protected PdfObject copyObject(PdfObject in) throws IOException,BadPdfFormatException {
        switch (in.type) {
            case PdfObject.DICTIONARY:
                // System.err.println("Dictionary: " + in.toString());
                return copyDictionary((PdfDictionary)in);
            case PdfObject.INDIRECT:
                return copyIndirect((PRIndirectReference)in);
            case PdfObject.ARRAY:
                return copyArray((PdfArray)in);
            case PdfObject.NUMBER:
            case PdfObject.NAME:
            case PdfObject.STRING:
            case PdfObject.m_NULL: // ssteward
            case PdfObject.BOOLEAN:
                return in;
            case PdfObject.STREAM:
                return copyStream((PRStream)in);
                //                return in;
            default:
                if (in.type < 0) {
                    String lit = ((PdfLiteral)in).toString();
                    if (lit.equals("true") || lit.equals("false")) {
                        return new PdfBoolean(lit);
                    }
                    return new PdfLiteral(lit);
                }
                System.err.println("CANNOT COPY type " + in.type);
                return null;
        }
    }
    
    /**
     * convenience method. Given an importedpage, set our "globals"
     */
    protected int setFromIPage(PdfImportedPage iPage) {
        int pageNum = iPage.getPageNumber();
        PdfReaderInstance inst = currentPdfReaderInstance = iPage.getPdfReaderInstance();
        reader = inst.getReader();
        setFromReader(reader);
        return pageNum;
    }
    
    /**
     * convenience method. Given a reader, set our "globals"
     */
    public void setFromReader(PdfReader reader) {
        this.reader = reader;
        indirects = (HashMap)indirectMap.get(reader);
        if (indirects == null) {
            indirects = new HashMap();
            indirectMap.put(reader,indirects);
            PdfDictionary catalog = reader.getCatalog();
            PRIndirectReference ref = (PRIndirectReference)catalog.get(PdfName.PAGES);
            indirects.put(new RefKey(ref), new IndirectReferences(topPageParent));
			/* ssteward: why PdfCopy.acroForm when PdfDocument.acroForm?
            ref = (PRIndirectReference)catalog.get(PdfName.ACROFORM);
            if (ref != null) {
                if (acroForm == null) acroForm = body.getPdfIndirectReference();
                indirects.put(new RefKey(ref), new IndirectReferences(acroForm));
            }
			*/
        }
    }
    /**
     * Add an imported page to our output
     * @param iPage an imported page
     * @throws IOException, BadPdfFormatException
     */
    public void addPage(PdfImportedPage iPage) throws IOException, BadPdfFormatException, DocumentException {
        int pageNum = setFromIPage(iPage); // sets this.reader
        
        PdfDictionary thePage = reader.getPageN(pageNum);
        PRIndirectReference origRef = reader.getPageOrigRef(pageNum);
        reader.releasePage(pageNum);
        RefKey key = new RefKey(origRef);
        PdfIndirectReference pageRef = null;
        IndirectReferences iRef = (IndirectReferences)indirects.get(key);
        // if we already have an iref for the page (we got here by another link)
        if (iRef != null) {
            pageRef = iRef.getRef();
        }
        else {
            pageRef = body.getPdfIndirectReference();
            iRef = new IndirectReferences(pageRef);
            indirects.put(key, iRef);
        }
        pageReferences.add(pageRef);
        ++currentPageNumber;
        if (! iRef.getCopied()) {
            iRef.setCopied();
			
			// ssteward
			if( !this.topFormFieldReadersData.containsKey( reader ) ) { // add
				this.topFormFieldReadersData.put( reader, new TopFormFieldData() );
			}
			TopFormFieldData readerData= (TopFormFieldData)topFormFieldReadersData.get(reader);

			// ssteward
			// if duplicate form field names are encountered
			// make names unique by inserting a new top parent "field";
			// insert this new parent into the PdfReader of the input document,
			// since any PdfWriter material may have been written already; our
			// changes to the PdfReader will be copied, below, into the PdfWriter;
			//
			{
				PdfArray annots= (PdfArray)PdfReader.getPdfObject(thePage.get(PdfName.ANNOTS));
				if( annots!= null && annots.isArray() ) {
					ArrayList annots_arr= annots.getArrayList();
					for( int ii= 0; ii< annots_arr.size(); ++ii ) {
						// an annotation may be direct or indirect; ours must be indirect
						PdfObject annot_obj= (PdfObject)annots_arr.get(ii);
						if( annot_obj!= null && annot_obj.isIndirect() ) {
							PdfIndirectReference annot_ref= (PdfIndirectReference)annot_obj;
							if( annot_ref!= null ) {
								PdfDictionary annot= (PdfDictionary)PdfReader.getPdfObject(annot_ref);
								if( annot!= null && annot.isDictionary() ) {
									PdfName subtype= (PdfName)PdfReader.getPdfObject(annot.get(PdfName.SUBTYPE));
									if( subtype!= null && subtype.isName() && subtype.equals(PdfName.WIDGET) ) {
										// we have a form field

										// get its full name
										//
										String full_name= ""; // construct a full name from partial names using '.', e.g.: foo.bar.
										String top_name= "";
										boolean is_unicode_b= false; // if names are unicode, they must all be unicode
										PdfString tt= (PdfString)PdfReader.getPdfObject(annot.get(PdfName.T));
										if( tt!= null && tt.isString() ) {
											top_name= tt.toString();
											is_unicode_b= ( is_unicode_b || PdfString.isUnicode( tt.getBytes() ) );
										}
										//
										// dig upwards, parent-wise; replace annot as we go with the
										// top-most form field dictionary
										PdfIndirectReference parent_ref= 
											(PdfIndirectReference)annot.get(PdfName.PARENT);
										while( parent_ref!= null && parent_ref.isIndirect() )
											{
												annot_ref= parent_ref;
												annot= (PdfDictionary)PdfReader.getPdfObject(annot_ref);
												parent_ref= (PdfIndirectReference)annot.get(PdfName.PARENT);

												tt= (PdfString)PdfReader.getPdfObject(annot.get(PdfName.T));
												if( tt!= null && tt.isString() ) {
													if( top_name.length()!= 0 ) {
														full_name+= top_name;
														full_name+= ".";
													}
													top_name= tt.toString();
												}

												is_unicode_b= ( is_unicode_b || PdfString.isUnicode( tt.getBytes() ) );
											}
									
										// once we have seen a top-level field parent, we wave
										// it through and assume that it harbors no illegal field duplicates;
										// this is good, because sometimes authors want a single field
										// represented by more than one annotation on the page; this logic
										// respects that programming
										//
										//System.err.println( full_name+ top_name+ "." ); // debug
										if( readerData.allNames.contains( top_name ) )
											{ // a parent we have seen or created in this reader
												this.fullFormFieldNames.add( full_name+ top_name+ "." ); // tally
											}
										else {
											if( this.fullFormFieldNames.contains( full_name+ top_name+ "." ) ) {
												// insert new, top-most parent

												// name for new parent
												int new_parent_name_ii= 1;
												String new_parent_name= Integer.toString( new_parent_name_ii );
												while( this.fullFormFieldNames.contains( full_name+ top_name+ "."+ new_parent_name+ "." ) ||
													   this.topFormFieldNames.contains( new_parent_name ) &&
													   !readerData.newNamesKids.containsKey( new_parent_name ) )
													{
														new_parent_name= Integer.toString( ++new_parent_name_ii );
													}

												PdfIndirectReference new_parent_ref= null;
												PdfArray new_parent_kids= null;
												//
												if( readerData.newNamesKids.containsKey( new_parent_name ) ) {
													// a new parent we already created
													new_parent_ref= (PdfIndirectReference)
														readerData.newNamesRefs.get( new_parent_name );
													new_parent_kids= (PdfArray)
														readerData.newNamesKids.get( new_parent_name );
												}
												else { // create a new parent using this name
													PdfDictionary new_parent= new PdfDictionary();
													PdfString new_parent_name_pdf= new PdfString( new_parent_name );
													if( is_unicode_b ) { // if names are unicode, they must all be unicode
														new_parent_name_pdf= new PdfString( new_parent_name, PdfObject.TEXT_UNICODE );
													}
													new_parent_ref= reader.getPRIndirectReference( new_parent );
													new_parent.put( PdfName.T, new_parent_name_pdf );
											
													new_parent_kids= new PdfArray();
													PdfIndirectReference new_parent_kids_ref= 
														reader.getPRIndirectReference( new_parent_kids );
													new_parent.put(PdfName.KIDS, new_parent_kids_ref);

													// tally new parent
													readerData.newNamesRefs.put( new_parent_name, new_parent_ref );
													readerData.newNamesKids.put( new_parent_name, new_parent_kids );
													readerData.allNames.add( new_parent_name );
													this.topFormFieldNames.add( new_parent_name );
												}

												// wire annot and new parent together
												annot.put( PdfName.PARENT, new_parent_ref );
												new_parent_kids.add( annot_ref ); // the new parent must point at the field, too

												// tally full field name
												this.fullFormFieldNames.add( full_name+ top_name+ "."+ new_parent_name+ "." );
											}
											else {
												// tally parent
												readerData.allNames.add( top_name );
												this.topFormFieldNames.add( top_name );

												// tally full field name
												this.fullFormFieldNames.add( full_name+ top_name+ "." );
											}
										}
									}
								}
							}
						}
					}
				}
			}

			// copy the page; this will copy our work, above, into the target document
            PdfDictionary newPage = copyDictionary(thePage);
			

			// ssteward: pdftk-1.00;
			// copy page form field ref.s into document AcroForm
			// dig down into source page to find indirect ref., then
			// look up ref. in dest page.; store ref.s in the PdfDocument acroForm,
			// not the PdfCopy acroForm (which isn't really a PdfAcroForm, anyhow);
			//
			// dig down to annot, and then dig up to topmost Parent
			{
				PdfArray annots= (PdfArray)PdfReader.getPdfObject(thePage.get(PdfName.ANNOTS));
				if( annots!= null && annots.isArray() ) {
					ArrayList annots_arr= annots.getArrayList();
					for( int ii= 0; ii< annots_arr.size(); ++ii ) {
						// an annotation may be direct or indirect; ours must be indirect
						PdfObject annot_obj= (PdfObject)annots_arr.get(ii);
						if( annot_obj!= null && annot_obj.isIndirect() ) {
							PdfIndirectReference annot_ref= (PdfIndirectReference)annots_arr.get(ii);
							if( annot_ref!= null ) {
								PdfDictionary annot= (PdfDictionary)PdfReader.getPdfObject(annot_ref);
								if( annot!= null && annot.isDictionary() ) {
									PdfName subtype= (PdfName)PdfReader.getPdfObject(annot.get(PdfName.SUBTYPE));
									if( subtype!= null && subtype.isName() && subtype.equals(PdfName.WIDGET) ) {
										// we have a form field

										// dig upwards, parent-wise
										PdfIndirectReference parent_ref= 
											(PdfIndirectReference)annot.get(PdfName.PARENT);
										while( parent_ref!= null && parent_ref.isIndirect() ) {
											annot_ref= parent_ref;
											annot= (PdfDictionary)PdfReader.getPdfObject(annot_ref);
											parent_ref= (PdfIndirectReference)annot.get(PdfName.PARENT);
										}
								
										RefKey annot_key= new RefKey(annot_ref);
										IndirectReferences annot_iRef= (IndirectReferences)indirects.get(annot_key);
										PdfAcroForm acroForm= this.getAcroForm();
										acroForm.addDocumentField( annot_iRef.getRef() );
									}
								}
							}
						}
					}
				}
			}

			// ssteward: pdftk-1.00;
			// merge the reader AcroForm/DR dictionary with the target
			//
			// pdftk-1.10: I noticed that the PdfAcroForm.isValid() apprears to stomp on this (TODO)
			//
			PdfDictionary catalog= reader.getCatalog();
			if( catalog!= null && catalog.isDictionary() ) {
				PdfDictionary acroForm= (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM));
				if( acroForm!= null && acroForm.isDictionary() ) {
					PdfDictionary dr= (PdfDictionary)PdfReader.getPdfObject(acroForm.get(PdfName.DR));
					if( dr!= null && dr.isDictionary() ) {
						PdfDictionary acroForm_target= this.getAcroForm();
						PdfDictionary dr_target= (PdfDictionary)PdfReader.getPdfObject(acroForm_target.get(PdfName.DR));
						if( dr_target== null ) {
							PdfDictionary dr_copy= copyDictionary( dr );
							acroForm_target.put( PdfName.DR, dr_copy );
						}
						else {
							for( Iterator it= dr.getKeys().iterator(); it.hasNext(); ) {
								PdfName dr_key= (PdfName)it.next();
								PdfObject dr_val= (PdfObject)dr.get(dr_key);
								if( !dr_target.contains( dr_key ) ) { // copy key/value to dr_target
									dr_target.put( dr_key, copyObject( dr_val ) );
								}
							}
						}
					}
				}
			}
			

            newPage.put(PdfName.PARENT, topPageParent);
            addToBody(newPage, pageRef);
        }
        getRoot().addPage(pageRef);
        pageNumbersToRefs.add(pageRef);
    }
    
    public PdfIndirectReference getPageReference(int page) {
        if (page < 0 || page > pageNumbersToRefs.size())
            throw new IllegalArgumentException("Invalid page number " + page);
        return (PdfIndirectReference)pageNumbersToRefs.get(page - 1);
    }

    /**
     * Copy the acroform for an input document. Note that you can only have one,
     * we make no effort to merge them.
     * @param reader The reader of the input file that is being copied
     * @throws IOException, BadPdfFormatException
     */
	/* ssteward: why PdfCopy.acroForm when PdfDocument.acroForm?
    public void copyAcroForm(PdfReader reader) throws IOException, BadPdfFormatException {
        setFromReader(reader);
        
        PdfDictionary catalog = reader.getCatalog();
        PRIndirectReference hisRef = null;
        PdfObject o = catalog.get(PdfName.ACROFORM);
        if (o != null && o.type() == PdfObject.INDIRECT)
            hisRef = (PRIndirectReference)o;
        RefKey key = new RefKey(hisRef);
        PdfIndirectReference myRef;
        IndirectReferences iRef = (IndirectReferences)indirects.get(key);
        if (iRef != null) {
            acroForm = myRef = iRef.getRef();
        }
        else {
            acroForm = myRef = body.getPdfIndirectReference();
            iRef = new IndirectReferences(myRef);
            indirects.put(key, iRef);
        }
        if (! iRef.getCopied()) {
            iRef.setCopied();
            PdfDictionary theForm = copyDictionary((PdfDictionary)PdfReader.getPdfObject(hisRef));
            PdfIndirectObject myObj = addToBody(theForm, myRef);
        }
    }
	*/
    
    /*
     * the getCatalog method is part of PdfWriter.
     * we wrap this so that we can extend it
     */
    protected PdfDictionary getCatalog( PdfIndirectReference rootObj ) throws DocumentException {
        //try {
		PdfDictionary catalog= getPdfDocument().getCatalog( rootObj ); // ssteward

			// ssteward: this just overwrites the ACROFORM entry added by
			// PdfDocument.getCatalog(); dropped, because we use PdfDocument.acroForm, above;
            //if (acroForm != null) catalog.put(PdfName.ACROFORM, acroForm);

			/* ssteward: replacing with our own bookmark-fu
            if (newBookmarks == null || newBookmarks.size() == 0)
                return catalog;
            PdfDictionary top = new PdfDictionary();
            PdfIndirectReference topRef = getPdfIndirectReference();
            Object kids[] = SimpleBookmark.iterateOutlines(this, topRef, newBookmarks, false);
            top.put(PdfName.FIRST, (PdfIndirectReference)kids[0]);
            top.put(PdfName.LAST, (PdfIndirectReference)kids[1]);
            top.put(PdfName.COUNT, new PdfNumber(((Integer)kids[2]).intValue()));
            addToBody(top, topRef);
            catalog.put(PdfName.OUTLINES, topRef);
			*/

			if( m_new_bookmarks!= null ) {
				catalog.put( PdfName.OUTLINES, m_new_bookmarks );
			}
			if( m_new_extensions!= null ) {
				catalog.put( PdfName.EXTENSIONS, m_new_extensions );
			}

            return catalog;
			/*
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
			*/
    }
    
    /**
     * Sets the bookmarks. The list structure is defined in
     * <CODE>SimpleBookmark#</CODE>.
     * @param outlines the bookmarks or <CODE>null</CODE> to remove any
     */
	/*
    public void setOutlines(List outlines) {
        newBookmarks = outlines;
    }
	*/
	// ssteward: pdftk 1.46
	public void setOutlines( PdfIndirectReference outlines ) {
		m_new_bookmarks= outlines;
	}
	public void setExtensions( PdfIndirectReference extensions ) {
		m_new_extensions= extensions;
	}

    /**
     * Signals that the <CODE>Document</CODE> was closed and that no other
     * <CODE>Elements</CODE> will be added.
     * <P>
     * The pages-tree is built and written to the outputstream.
     * A Catalog is constructed, as well as an Info-object,
     * the referencetable is composed and everything is written
     * to the outputstream embedded in a Trailer.
     */
    
    public void close() {
        if (open) {
            PdfReaderInstance ri = currentPdfReaderInstance;
            getPdfDocument().close();
            super.close();
            if (ri != null) {
                try {
                    ri.getReader().close();
                    ri.getReaderFile().close();
                }
                catch (IOException ioe) {
                    // empty on purpose
                }
            }
        }
    }
    // PdfIndirectReference add(PdfImage pdfImage) throws PdfException  { return null; } ssteward: dropped in 1.44
    public PdfIndirectReference add(PdfOutline outline) { return null; }
    public void addAnnotation(PdfAnnotation annot) {  }
    PdfIndirectReference add(PdfPage page, PdfContents contents) throws PdfException { return null; }
}
