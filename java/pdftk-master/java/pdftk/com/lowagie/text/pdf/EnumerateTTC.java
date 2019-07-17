/*
 * $Id: EnumerateTTC.java,v 1.13 2002/06/20 13:04:40 blowagie Exp $
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

import java.io.*;
import java.util.HashMap;
import pdftk.com.lowagie.text.DocumentException;
/** Enumerates all the fonts inside a True Type Collection.
 *
 * @author  Paulo Soares (psoares@consiste.pt)
 */
class EnumerateTTC extends TrueTypeFont{

    protected String[] names;

    EnumerateTTC(String ttcFile) throws DocumentException, IOException {
        fileName = ttcFile;
        rf = new RandomAccessFileOrArray(ttcFile);
        findNames();
    }

    EnumerateTTC(byte ttcArray[]) throws DocumentException, IOException {
        fileName = "Byte array TTC";
        rf = new RandomAccessFileOrArray(ttcArray);
        findNames();
    }
    
    void findNames() throws DocumentException, IOException {
        tables = new HashMap();
        
        try {
            String mainTag = readStandardString(4);
            if (!mainTag.equals("ttcf"))
                throw new DocumentException(fileName + " is not a valid TTC file.");
            rf.skipBytes(4);
            int dirCount = rf.readInt();
            names = new String[dirCount];
            int dirPos = rf.getFilePointer();
            for (int dirIdx = 0; dirIdx < dirCount; ++dirIdx) {
                tables.clear();
                rf.seek(dirPos);
                rf.skipBytes(dirIdx * 4);
                directoryOffset = rf.readInt();
                rf.seek(directoryOffset);
                if (rf.readInt() != 0x00010000)
                    throw new DocumentException(fileName + " is not a valid TTF file.");
                int num_tables = rf.readUnsignedShort();
                rf.skipBytes(6);
                for (int k = 0; k < num_tables; ++k) {
                    String tag = readStandardString(4);
                    rf.skipBytes(4);
                    int table_location[] = new int[2];
                    table_location[0] = rf.readInt();
                    table_location[1] = rf.readInt();
                    tables.put(tag, table_location);
                }
                names[dirIdx] = getBaseFont();
            }
        }
        finally {
            if (rf != null)
                rf.close();
        }
    }
    
    String[] getNames() {
        return names;
    }

}
