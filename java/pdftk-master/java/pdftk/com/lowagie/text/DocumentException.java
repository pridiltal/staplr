/*
 * $Id: DocumentException.java,v 1.50 2004/12/14 11:52:46 blowagie Exp $
 * $Name:  $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 *
 */

package pdftk.com.lowagie.text;

/**
 * Signals that an error has occurred in a <CODE>Document</CODE>.
 *
 * @see		BadElementException
 * @see		Document
 * @see		DocWriter
 * @see		DocListener
 */

public class DocumentException extends Exception {

    /** A serial version UID */
    private static final long serialVersionUID = -2191131489390840739L;

    private Exception ex;

    /**
     * Creates a Document exception.
     * @param ex an exception that has to be turned into a DocumentException
     */
    public DocumentException(Exception ex) {
        this.ex = ex;
    }
    
    // constructors
    
/**
 * Constructs a <CODE>DocumentException</CODE> whithout a message.
 */
    
    public DocumentException() {
        super();
    }
    
/**
 * Constructs a <code>DocumentException</code> with a message.
 *
 * @param		message			a message describing the exception
 */
    
    public DocumentException(String message) {
        super(message);
    }

    /**
     * We print the message of the checked exception 
     * @return the error message
     */
    public String getMessage() {
        if (ex == null)
            return super.getMessage();
        else
            return ex.getMessage();
    }

    /**
     * and make sure we also produce a localized version 
     * @return a localized message
     */
    public String getLocalizedMessage() {
        if (ex == null)
            return super.getLocalizedMessage();
        else
            return ex.getLocalizedMessage();
    }

    /**
     * The toString() is changed to be prefixed with ExceptionConverter 
     * @return the String version of the exception
     */
    public String toString() {
        if (ex == null)
            return super.toString();
        else
            return split(getClass().getName()) + ": " + ex;
    }

    /** we have to override this as well */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * here we prefix, with s.print(), not s.println(), the stack
     * trace with "ExceptionConverter:" 
     * @param s a printstream object
     */
    public void printStackTrace(java.io.PrintStream s) {
        if (ex == null)
            super.printStackTrace(s);
        else {
            synchronized (s) {
                s.print(split(getClass().getName()) + ": ");
                ex.printStackTrace(s);
            }
        }
    }

    /**
     * Again, we prefix the stack trace with "ExceptionConverter:" 
     * @param s A PrintWriter object
     */
    public void printStackTrace(java.io.PrintWriter s) {
        if (ex == null)
            super.printStackTrace(s);
        else {
            synchronized (s) {
                s.print(split(getClass().getName()) + ": ");
                ex.printStackTrace(s);
            }
        }
    }

    /**
     * Removes everything in a String that comes before a '.'
     * @param s the original string
     * @return the part that comes after the dot
     */
    private static String split(String s) {
        int i = s.lastIndexOf('.');
        if (i < 0)
            return s;
        else
            return s.substring(i + 1);
    }
    
    /** requests to fill in the stack trace we will have to ignore.
     * We can't throw an exception here, because this method
     * is called by the constructor of Throwable */
//    public Throwable fillInStackTrace() {
//        if (ex == null)
//            return super.fillInStackTrace();
//        else
//            return this;
//    }

}
