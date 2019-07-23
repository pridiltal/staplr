/*
 * $Id: PdfArray.java,v 1.37 2003/05/02 09:01:13 blowagie Exp $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * <CODE>PdfArray</CODE> is the PDF Array object.
 * <P>
 * An array is a sequence of PDF objects. An array may contain a mixture of object types.
 * An array is written as a left square bracket ([), followed by a sequence of objects,
 * followed by a right square bracket (]).<BR>
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 4.6 (page 40).
 *
 * @see		PdfObject
 */

public class PdfArray extends PdfObject {
    
    // membervariables
    
/** this is the actual array of PdfObjects */
    protected ArrayList arrayList;
    
    // constructors
    
/**
 * Constructs an empty <CODE>PdfArray</CODE>-object.
 */
    
    public PdfArray() {
        super(ARRAY);
        arrayList = new ArrayList();
    }
    
/**
 * Constructs an <CODE>PdfArray</CODE>-object, containing 1 <CODE>PdfObject</CODE>.
 *
 * @param	object		a <CODE>PdfObject</CODE> that has to be added to the array
 */
    
    public PdfArray(PdfObject object) {
        super(ARRAY);
        arrayList = new ArrayList();
        arrayList.add(object);
    }
    
    public PdfArray(float values[]) {
        super(ARRAY);
        arrayList = new ArrayList();
        add(values);
    }
    
    public PdfArray(int values[]) {
        super(ARRAY);
        arrayList = new ArrayList();
        add(values);
    }
    
    /**
     * Constructs a <CODE>PdfArray</CODE>, containing all elements of a
     * specified <CODE>ArrayList</CODE>.
     * 
     * @param l    an <CODE>ArrayList</CODE> with <CODE>PdfObject</CODE>s to be
     *   added to the array
     * @throws ClassCastException if the <CODE>ArrayList</CODE> contains
     *   something that isn't a <CODE>PdfObject</CODE>
     * @since 2.1.3
     */
    public PdfArray(ArrayList l) {
        this();
        for (Iterator i = l.iterator(); i.hasNext(); )
        	add((PdfObject)i.next());
    }

/**
 * Constructs an <CODE>PdfArray</CODE>-object, containing all the <CODE>PdfObject</CODE>s in a given <CODE>PdfArray</CODE>.
 *
 * @param	array		a <CODE>PdfArray</CODE> that has to be added to the array
 */
    
    public PdfArray(PdfArray array) {
        super(ARRAY);
        arrayList = new ArrayList(array.getArrayList());
    }
    
    // methods overriding some methods in PdfObject
    
/**
 * Returns the PDF representation of this <CODE>PdfArray</CODE>.
 *
 * @return		an array of <CODE>byte</CODE>s
 */
    
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        os.write('[');

        Iterator i = arrayList.iterator();
        PdfObject object;
        // int type = 0; // ssteward
        if (i.hasNext()) {
            object = (PdfObject) i.next();
            object.toPdf(writer, os);
        }
        while (i.hasNext()) {
            object = (PdfObject) i.next();
	    // ssteward
            //type = object.type();
            //if (type != PdfObject.ARRAY && type != PdfObject.DICTIONARY && type != PdfObject.NAME && type != PdfObject.STRING)
	    os.write(' ');
            object.toPdf(writer, os);
        }
        os.write(']');
    }
    
    /**
     * Returns a string representation of this <CODE>PdfArray</CODE>.
     * 
     * The string representation consists of a list of all
     * <CODE>PdfObject</CODE>s contained in this <CODE>PdfArray</CODE>,
     * enclosed in square brackets ("[]"). Adjacent elements are separated
     * by the characters ", " (comma and space).
     * 
     * @return the string representation of this <CODE>PdfArray</CODE>
     */
    public String toString() {
    	return arrayList.toString();
    }
    
    // methods concerning the ArrayList-membervalue
    
    /**
     * Overwrites a specified location of the array, returning the previous
     * value
     * 
     * @param idx The index of the element to be overwritten
     * @param obj new value for the specified index
     * @throws IndexOutOfBoundsException if the specified position doesn't exist
     * @return the previous value
     * @since 2.1.5
     */
    public PdfObject set(int idx, PdfObject obj) {
        return (PdfObject) arrayList.set(idx, obj);
    }

    /**
     * Remove the element at the specified position from the array.
     * 
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     * 
     * @param idx The index of the element to be removed.
     * @throws IndexOutOfBoundsException the specified position doesn't exist
     * @since 2.1.5
     */
    public PdfObject remove(int idx) {
        return (PdfObject) arrayList.remove(idx);
    }

/**
 * Returns an ArrayList containing <CODE>PdfObject</CODE>s.
 *
 * @return		an ArrayList
 */
    
    public ArrayList getArrayList() {
        return arrayList;
    }
    
/**
 * Returns the number of entries in the array.
 *
 * @return		the size of the ArrayList
 */
    
    public int size() {
        return arrayList.size();
    }
    
    /**
     * Returns <CODE>true</CODE> if the array is empty.
     * 
     * @return <CODE>true</CODE> if the array is empty
     * @since 2.1.5
     */
    public boolean isEmpty() {
        return arrayList.isEmpty();
    }

/**
 * Adds a <CODE>PdfObject</CODE> to the <CODE>PdfArray</CODE>.
 *
 * @param		object			<CODE>PdfObject</CODE> to add
 * @return		<CODE>true</CODE>
 */
    
    public boolean add(PdfObject object) {
        return arrayList.add(object);
    }
    
    public boolean add(float values[]) {
        for (int k = 0; k < values.length; ++k)
            arrayList.add(new PdfNumber(values[k]));
        return true;
    }
    
    public boolean add(int values[]) {
        for (int k = 0; k < values.length; ++k)
            arrayList.add(new PdfNumber(values[k]));
        return true;
    }
    
    /**
     * Inserts the specified element at the specified position.
     * 
     * Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index The index at which the specified element is to be inserted
     * @param element The element to be inserted
     * @throws IndexOutOfBoundsException if the specified index is larger than the
     *   last position currently set, plus 1. 
     * @since 2.1.5
     */
    public void add(int index, PdfObject element) {
        arrayList.add(index, element);
    }

/**
 * Adds a <CODE>PdfObject</CODE> to the <CODE>PdfArray</CODE>.
 * <P>
 * The newly added object will be the first element in the <CODE>ArrayList</CODE>.
 *
 * @param		object			<CODE>PdfObject</CODE> to add
 */
    
    public void addFirst(PdfObject object) {
        arrayList.add(0, object);
    }
    
/**
 * Checks if the <CODE>PdfArray</CODE> already contains a certain <CODE>PdfObject</CODE>.
 *
 * @param		object			<CODE>PdfObject</CODE> to check
 * @return		<CODE>true</CODE>
 */
    
    public boolean contains(PdfObject object) {
        return arrayList.contains(object);
    }
    
    public ListIterator listIterator() {
        return arrayList.listIterator();
    }

    /**
     * Returns the <CODE>PdfObject</CODE> with the specified index.
     * 
     * A possible indirect references is not resolved, so the returned
     * <CODE>PdfObject</CODE> may be either a direct object or an indirect
     * reference, depending on how the object is stored in the
     * <CODE>PdfArray</CODE>.  
     * 
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return A <CODE>PdfObject</CODE>
     */
    public PdfObject getPdfObject(int idx) {
        return (PdfObject)arrayList.get(idx);
    }

    /**
     * Returns the <CODE>PdfObject</CODE> with the specified index, resolving
     * a possible indirect reference to a direct object.
     * 
     * Thus this method will never return a <CODE>PdfIndirectReference</CODE>
     * object.  
     * 
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return A direct <CODE>PdfObject</CODE> or <CODE>null</CODE> 
     */
    public PdfObject getDirectObject(int idx) {
        return PdfReader.getPdfObject(getPdfObject(idx));
    }

    // DOWNCASTING GETTERS
    // @author Mark A Storer (2/17/06)
    
    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfDictionary</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolvedto a direct object.
     * If it is a <CODE>PdfDictionary</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     * 
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfDictionary</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfDictionary getAsDict(int idx) {
        PdfDictionary dict = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isDictionary())
            dict = (PdfDictionary) orig;
        return dict;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfArray</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a <CODE>PdfArray</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfArray</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfArray getAsArray(int idx) {
        PdfArray array = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isArray())
            array = (PdfArray) orig;
        return array;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfStream</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a <CODE>PdfStream</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfStream</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfStream getAsStream(int idx) {
        PdfStream stream = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isStream())
            stream = (PdfStream) orig;
        return stream;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfString</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a <CODE>PdfString</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfString</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfString getAsString(int idx) {
        PdfString string = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isString())
            string = (PdfString) orig;
        return string;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfNumber</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a <CODE>PdfNumber</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfNumber</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfNumber getAsNumber(int idx) {
        PdfNumber number = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isNumber())
            number = (PdfNumber) orig;
        return number;
    }
    
    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfName</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a <CODE>PdfName</CODE>, it is cast down and returned as such.
     * Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfName</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfName getAsName(int idx) {
        PdfName name = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isName())
            name = (PdfName) orig;
        return name;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfBoolean</CODE>,
     * resolving indirect references.
     * 
     * The object corresponding to the specified index is retrieved and
     * resolved to a direct object.
     * If it is a <CODE>PdfBoolean</CODE>, it is cast down and returned as
     * such. Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfBoolean</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfBoolean getAsBoolean(int idx) {
        PdfBoolean bool = null;
        PdfObject orig = getDirectObject(idx);
        if (orig != null && orig.isBoolean())
            bool = (PdfBoolean) orig;
        return bool;
    }

    /**
     * Returns a <CODE>PdfObject</CODE> as a <CODE>PdfIndirectReference</CODE>.
     * 
     * The object corresponding to the specified index is retrieved.
     * If it is a <CODE>PdfIndirectReference</CODE>, it is cast down and
     * returned as such. Otherwise <CODE>null</CODE> is returned.
     *     
     * @param idx The index of the <CODE>PdfObject</CODE> to be returned
     * @return the corresponding <CODE>PdfIndirectReference</CODE> object,
     *   or <CODE>null</CODE>
     */
    public PdfIndirectReference getAsIndirectObject(int idx) {
        PdfIndirectReference ref = null;
        PdfObject orig = getPdfObject(idx); // not getDirect this time.
        if (orig != null && orig.isIndirect())
            ref = (PdfIndirectReference) orig;
        return ref;
    }
}