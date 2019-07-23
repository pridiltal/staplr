/*
 * Copyright 2004 by Takenori.
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

import java.awt.Font;

import pdftk.com.lowagie.text.pdf.BaseFont;
import pdftk.com.lowagie.text.pdf.DefaultFontMapper;

public class AsianFontMapper extends DefaultFontMapper {
	
	public static String ChineseSimplifiedFont = "STSong-Light";
	public static String ChineseSimplifiedEncoding_H = "UniGB-UCS2-H";
	public static String ChineseSimplifiedEncoding_V = "UniGB-UCS2-V";
	
	public static String ChineseTraditionalFont_MHei = "MHei-Medium";
	public static String ChineseTraditionalFont_MSung = "MSung-Light";
	public static String ChineseTraditionalEncoding_H = "UniCNS-UCS2-H";
	public static String ChineseTraditionalEncoding_V = "UniCNS-UCS2-V";
	
	public static String JapaneseFont_Go = "HeiseiKakuGo-W5";
	public static String JapaneseFont_Min = "HeiseiMin-W3";
	public static String JapaneseEncoding_H = "UniJIS-UCS2-H";
	public static String JapaneseEncoding_V = "UniJIS-UCS2-V";
	public static String JapaneseEncoding_HW_H = "UniJIS-UCS2-HW-H";
	public static String JapaneseEncoding_HW_V = "UniJIS-UCS2-HW-V";
	
	public static String KoreanFont_GoThic = "HYGoThic-Medium";
	public static String KoreanFont_SMyeongJo = "HYSMyeongJo-Medium";
	public static String KoreanEncoding_H = "UniKS-UCS2-H";
	public static String KoreanEncoding_V = "UniKS-UCS2-V";
	
	private String defaultFont;
	private String encoding;

	public AsianFontMapper(String font, String encoding) {
		super();
		
		this.defaultFont = font;
		this.encoding = encoding;
	}

	public BaseFont awtToPdf(Font font) {
		try {
			BaseFontParameters p = getBaseFontParameters(font.getFontName());
			if (p != null){
				return BaseFont.createFont(p.fontName, p.encoding, p.embedded, p.cached, p.ttfAfm, p.pfb);
			}else{
				return BaseFont.createFont(defaultFont, encoding, true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
