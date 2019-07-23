/*
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
 * The ExceptionConverter changes a checked exception into an
 * unchecked exception.
 */
public class ExceptionConverter extends RuntimeException {

    /** A serial version UID */
    private static final long serialVersionUID = 8657630363395849399L;

    /** we keep a handle to the wrapped exception */
    private Exception ex;
    /** prefix for the exception */
    private String prefix;

    /**
     * Construct a RuntimeException based on another Exception
     * @param ex the exception that has to be turned into a RuntimeException
     */
    public ExceptionConverter(Exception ex) {
        this.ex = ex;
        prefix = (ex instanceof RuntimeException) ? "" : "ExceptionConverter: ";
    }

    /**
     * and allow the user of ExceptionConverter to get a handle to it. 
     * @return the original exception
     */
    public Exception getException() {
        return ex;
    }

    /**
     * We print the message of the checked exception 
     * @return message of the original exception
     */
    public String getMessage() {
        return ex.getMessage();
    }

    /**
     * and make sure we also produce a localized version
     * @return localized version of the message
     */
    public String getLocalizedMessage() {
        return ex.getLocalizedMessage();
    }

    /**
     * The toString() is changed to be prefixed with ExceptionConverter 
     * @return Stringversion of the exception
     */
    public String toString() {
        return prefix + ex;
    }

    /** we have to override this as well */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * here we prefix, with s.print(), not s.println(), the stack
     * trace with "ExceptionConverter:" 
     * @param s
     */
    public void printStackTrace(java.io.PrintStream s) {
        synchronized (s) {
            s.print(prefix);
            ex.printStackTrace(s);
        }
    }

    /**
     * Again, we prefix the stack trace with "ExceptionConverter:" 
     * @param s
     */
    public void printStackTrace(java.io.PrintWriter s) {
        synchronized (s) {
            s.print(prefix);
            ex.printStackTrace(s);
        }
    }

    /**
     * requests to fill in the stack trace we will have to ignore.
     * We can't throw an exception here, because this method
     * is called by the constructor of Throwable 
     * @return a Throwable
     */
    public Throwable fillInStackTrace() {
        return this;
    }
}