/*
 *   This file is part of the pdftk port to java
 *
 *   Copyright (c) Marc Vinyals 2017-2018
 *
 *   The program is a java port of PDFtk, the PDF Toolkit
 *   Copyright (c) 2003-2013 Steward and Lee, LLC
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   The program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gitlab.pdftk_java;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/*
  password character mapping by Acrobat (pre-1.7 PDF)

  * characters in PDFDocEncoding use PDFDocEncoding
  * some PDFDocEncoding characters are not accepted as input into Acrobat/Reader --
      this is consistent with the next rule:
  * unicode 0x200 and up: Acrobat/Reader prohibits input
  * some chars below 0x200 are mapped to . (period)
  * some chars below 0x200 are mapped to non-accented ASCII char
  * many points are mapped to Windows CP-1250

  These were tested on Windows 7 using Acrobat 7.  From what I've read, Adobe's mapping
  of characters that aren't in PDFDocEncoding is platform dependent.  So this map should
  only be used for mapping PDF passwords for decryption.  When encrypting, it would be
  best to enforce a limited set of characters for the purpose of platform independence.

*/

class passwords {

  // unicode 0x100 - 0x1FF
  static final int[] unicode_latin_extended_windows_map = {
    // latin extended a
    'A', //  U+0100   Ā   Latin Capital Letter A with macron
    'a', //  U+0101   ā   Latin Small Letter A with macron
    0xC3, // U+0102   Ă   Latin Capital Letter A with breve
    0xC4, // U+0103   ă   Latin Small Letter A with breve
    0xA5, // U+0104   Ą   Latin Capital Letter A with ogonek
    0xB9, // U+0105   ą   Latin Small Letter A with ogonek
    0xC6, // U+0106   Ć   Latin Capital Letter C with acute
    0xE6, // U+0107   ć   Latin Small Letter C with acute
    '.', //  U+0108   Ĉ   Latin Capital Letter C with circumflex
    '.', //  U+0109   ĉ   Latin Small Letter C with circumflex
    '.', //  U+010A   Ċ   Latin Capital Letter C with dot above
    '.', //  U+010B   ċ   Latin Small Letter C with dot above
    0xC8, // U+010C   Č   Latin Capital Letter C with caron
    0xE8, // U+010D   č   Latin Small Letter C with caron
    0xCF, // U+010E   Ď   Latin Capital Letter D with caron
    0xEF, // U+010F   ď   Latin Small Letter D with caron
    0xD0, // U+0110   Đ   Latin Capital Letter D with stroke
    0xF0, // U+0111   đ   Latin Small Letter D with stroke
    'E', //  U+0112   Ē   Latin Capital Letter E with macron
    'e', //  U+0113   ē   Latin Small Letter E with macron
    '.', //  U+0114   Ĕ   Latin Capital Letter E with breve
    '.', //  U+0115   ĕ   Latin Small Letter E with breve
    'E', //  U+0116   Ė   Latin Capital Letter E with dot above
    'e', //  U+0117   ė   Latin Small Letter E with dot above
    0xCA, // U+0118   Ę   Latin Capital Letter E with ogonek
    0xEA, // U+0119   ę   Latin Small Letter E with ogonek
    0xCC, // U+011A   Ě   Latin Capital Letter E with caron
    0xEC, // U+011B   ě   Latin Small Letter E with caron
    '.', //  U+011C   Ĝ   Latin Capital Letter G with circumflex
    '.', //  U+011D   ĝ   Latin Small Letter G with circumflex
    'G', //  U+011E   Ğ   Latin Capital Letter G with breve
    'g', //  U+011F   ğ   Latin Small Letter G with breve
    '.', //  U+0120   Ġ   Latin Capital Letter G with dot above
    '.', //  U+0121   ġ   Latin Small Letter G with dot above
    'G', //  U+0122   Ģ   Latin Capital Letter G with cedilla
    'g', //  U+0123   ģ   Latin Small Letter G with cedilla
    '.', //  U+0124   Ĥ   Latin Capital Letter H with circumflex
    '.', //  U+0125   ĥ   Latin Small Letter H with circumflex
    '.', //  U+0126   Ħ   Latin Capital Letter H with stroke
    '.', //  U+0127   ħ   Latin Small Letter H with stroke
    '.', //  U+0128   Ĩ   Latin Capital Letter I with tilde
    '.', //  U+0129   ĩ   Latin Small Letter I with tilde
    'I', //  U+012A   Ī   Latin Capital Letter I with macron
    'i', //  U+012B   ī   Latin Small Letter I with macron
    '.', //  U+012C   Ĭ   Latin Capital Letter I with breve
    '.', //  U+012D   ĭ   Latin Small Letter I with breve
    'I', //  U+012E   Į   Latin Capital Letter I with ogonek
    'i', //  U+012F   į   Latin Small Letter I with ogonek
    'I', //  U+0130   İ   Latin Capital Letter I with dot above
    'i', //  U+0131   ı   Latin Small Letter dotless I
    '.', //  U+0132   Ĳ   Latin Capital Ligature IJ
    '.', //  U+0133   ĳ   Latin Small Ligature IJ
    '.', //  U+0134   Ĵ   Latin Capital Letter J with circumflex
    '.', //  U+0135   ĵ   Latin Small Letter J with circumflex
    'K', //  U+0136   Ķ   Latin Capital Letter K with cedilla
    'k', //  U+0137   ķ   Latin Small Letter K with cedilla
    '.', //  U+0138   ĸ   Latin Small Letter Kra
    0xC5, // U+0139   Ĺ   Latin Capital Letter L with acute
    0xE5, // U+013A   ĺ   Latin Small Letter L with acute
    'L', //  U+013B   Ļ   Latin Capital Letter L with cedilla
    'l', //  U+013C   ļ   Latin Small Letter L with cedilla
    0xBC, // U+013D   Ľ   Latin Capital Letter L with caron
    0xBE, // U+013E   ľ   Latin Small Letter L with caron
    '.', //  U+013F   Ŀ   Latin Capital Letter L with middle dot
    '.', //  U+0140   ŀ   Latin Small Letter L with middle dot
    0xA3, // U+0141   Ł   Latin Capital Letter L with stroke
    0xB3, // U+0142   ł   Latin Small Letter L with stroke
    0xD1, // U+0143   Ń   Latin Capital Letter N with acute
    0xF1, // U+0144   ń   Latin Small Letter N with acute
    'N', //  U+0145   Ņ   Latin Capital Letter N with cedilla
    'n', //  U+0146   ņ   Latin Small Letter N with cedilla
    0xD2, // U+0147   Ň   Latin Capital Letter N with caron
    0xF2, // U+0148   ň   Latin Small Letter N with caron
    '.', //  U+0149   ŉ   Latin Small Letter N preceded by apostrophe[1]
    '.', //  U+014A   Ŋ   Latin Capital Letter Eng
    '.', //  U+014B   ŋ   Latin Small Letter Eng
    'O', //  U+014C   Ō   Latin Capital Letter O with macron
    'o', //  U+014D   ō   Latin Small Letter O with macron
    '.', //  U+014E   Ŏ   Latin Capital Letter O with breve
    '.', //  U+014F   ŏ   Latin Small Letter O with breve
    0xD5, // U+0150   Ő   Latin Capital Letter O with double acute
    0xF5, // U+0151   ő   Latin Small Letter O with double acute
    0226, // U+0152   Œ   Latin Capital Ligature OE (mapped to PDFDocEncoding)
    0234, // U+0153   œ   Latin Small Ligature OE (mapped to PDFDocEncoding)
    0xC0, // U+0154   Ŕ   Latin Capital Letter R with acute
    0xE0, // U+0155   ŕ   Latin Small Letter R with acute
    'R', //  U+0156   Ŗ   Latin Capital Letter R with cedilla
    'r', //  U+0157   ŗ   Latin Small Letter R with cedilla
    0xD8, // U+0158   Ř   Latin Capital Letter R with caron
    0xF8, // U+0159   ř   Latin Small Letter R with caron
    0x8C, // U+015A   Ś   Latin Capital Letter S with acute
    0x9C, // U+015B   ś   Latin Small Letter S with acute
    '.', //  U+015C   Ŝ   Latin Capital Letter S with circumflex
    '.', //  U+015D   ŝ   Latin Small Letter S with circumflex
    0xAA, // U+015E   Ş   Latin Capital Letter S with cedilla
    0xBA, // U+015F   ş   Latin Small Letter S with cedilla
    0x8A, // U+0160   Š   Latin Capital Letter S with caron
    0x9A, // U+0161   š   Latin Small Letter S with caron
    0xDE, // U+0162   Ţ   Latin Capital Letter T with cedilla
    0xFE, // U+0163   ţ   Latin Small Letter T with cedilla
    0x8D, // U+0164   Ť   Latin Capital Letter T with caron
    0x9D, // U+0165   ť   Latin Small Letter T with caron
    'T', //  U+0166   Ŧ   Latin Capital Letter T with stroke
    't', //  U+0167   ŧ   Latin Small Letter T with stroke
    '.', //  U+0168   Ũ   Latin Capital Letter U with tilde
    '.', //  U+0169   ũ   Latin Small Letter U with tilde
    'U', //  U+016A   Ū   Latin Capital Letter U with macron
    'u', //  U+016B   ū   Latin Small Letter U with macron
    '.', //  U+016C   Ŭ   Latin Capital Letter U with breve
    '.', //  U+016D   ŭ   Latin Small Letter U with breve
    0xD9, // U+016E   Ů   Latin Capital Letter U with ring above
    0xF9, // U+016F   ů   Latin Small Letter U with ring above
    0xDB, // U+0170   Ű   Latin Capital Letter U with double acute
    0xFB, // U+0171   ű   Latin Small Letter U with double acute
    'U', //  U+0172   Ų   Latin Capital Letter U with ogonek
    'u', //  U+0173   ų   Latin Small Letter U with ogonek
    '.', //  U+0174   Ŵ   Latin Capital Letter W with circumflex
    '.', //  U+0175   ŵ   Latin Small Letter W with circumflex
    '.', //  U+0176   Ŷ   Latin Capital Letter Y with circumflex
    '.', //  U+0177   ŷ   Latin Small Letter Y with circumflex
    0230, // U+0178   Ÿ   Latin Capital Letter Y with diaeresis (mapped to PDFDocEncoding)
    0x8F, // U+0179   Ź   Latin Capital Letter Z with acute
    0x9F, // U+017A   ź   Latin Small Letter Z with acute
    0xAF, // U+017B   Ż   Latin Capital Letter Z with dot above
    0xBF, // U+017C   ż   Latin Small Letter Z with dot above
    0231, // U+017D   Ž   Latin Capital Letter Z with caron (mapped to PDFDocEncoding)
    0236, // U+017E   ž   Latin Small Letter Z with caron (mapped to PDFDocEncoding)
    '.', //  U+017F   ſ   Latin Small Letter long S

    // portion of latin extended b
    'b', //  U+0180   ƀ  &#384;   Latin Small Letter B with stroke
    '.', //  U+0181   Ɓ  &#385;   Latin Capital Letter B with hook
    '.', //  U+0182   Ƃ  &#386;   Latin Capital Letter B with top bar
    '.', //  U+0183   ƃ  &#387;   Latin Small Letter B with top bar
    '.', //  U+0184   Ƅ  &#388;   Latin Capital Letter Tone Six
    '.', //  U+0185   ƅ  &#389;   Latin Small Letter Tone Six
    '.', //  U+0186   Ɔ  &#390;   Latin Capital Letter Open O
    '.', //  U+0187   Ƈ  &#391;   Latin Capital Letter C with hook
    '.', //  U+0188   ƈ  &#392;   Latin Small Letter C with hook
    0xD0, // U+0189   Ɖ  &#393;   Latin Capital Letter African D
    '.', //  U+018A   Ɗ  &#394;   Latin Capital Letter D with hook
    '.', //  U+018B   Ƌ  &#395;   Latin Capital Letter D with top bar
    '.', //  U+018C   ƌ  &#396;   Latin Small Letter D with top bar
    '.', //  U+018D   ƍ  &#397;   Latin Small Letter Turned Delta
    '.', //  U+018E   Ǝ  &#398;   Latin Capital Letter Reversed E
    '.', //  U+018F   Ə  &#399;   Latin Capital Letter Schwa
    '.', //  U+0190   Ɛ  &#400;   Latin Capital Letter Open E
    0x83, // U+0191   Ƒ  &#401;   Latin Capital Letter F with hook (from win cp-1252)(maybe)
    0x83, // U+0192   ƒ  &#402;   Latin Small Letter F with hook (from win cp-1252)(maybe)
    '.', //  U+0193   Ɠ  &#403;   Latin Capital Letter G with hook
    '.', //  U+0194   Ɣ  &#404;   Latin Capital Letter Gamma
    '.', //  U+0195   ƕ  &#405;   Latin Small Letter HV
    '.', //  U+0196   Ɩ  &#406;   Latin Capital Letter Iota
    'I', //  U+0197   Ɨ  &#407;   Latin Capital Letter I with stroke
    '.', //  U+0198   Ƙ  &#408;   Latin Capital Letter K with hook
    '.', //  U+0199   ƙ  &#409;   Latin Small Letter K with hook
    'l', //  U+019A   ƚ  &#410;   Latin Small Letter L with bar
    '.', //  U+019B   ƛ  &#411;   Latin Small Letter Lambda with stroke
    '.', //  U+019C   Ɯ  &#412;   Latin Capital Letter Turned M
    '.', //  U+019D   Ɲ  &#413;   Latin Capital Letter N with left hook
    '.', //  U+019E   ƞ  &#414;   Latin Small Letter N with long right leg
    'O', //  U+019F   Ɵ  &#415;   Latin Capital Letter O with middle tilde
    'O', //  U+01A0   Ơ  &#416;   Latin Capital Letter O with horn
    'o', //  U+01A1   ơ  &#417;   Latin Small Letter O with horn
    '.', //  U+01A2   Ƣ  &#418;   Latin Capital Letter OI (= Latin Capital Letter Gha)
    '.', //  U+01A3   ƣ  &#419;   Latin Small Letter OI (= Latin Small Letter Gha)
    '.', //  U+01A4   Ƥ  &#420;   Latin Capital Letter P with hook
    '.', //  U+01A5   ƥ  &#421;   Latin Small Letter P with hook
    '.', //  U+01A6   Ʀ  &#422;   Latin Letter YR
    '.', //  U+01A7   Ƨ  &#423;   Latin Capital Letter Tone Two
    '.', //  U+01A8   ƨ  &#424;   Latin Small Letter Tone Two
    '.', //  U+01A9   Ʃ  &#425;   Latin Capital Letter Esh
    '.', //  U+01AA   ƪ  &#426;   Latin Letter Reversed Esh Loop
    't', //  U+01AB   ƫ  &#427;   Latin Small Letter T with palatal hook
    '.', //  U+01AC   Ƭ  &#428;   Latin Capital Letter T with hook
    '.', //  U+01AD   ƭ  &#429;   Latin Small Letter T with hook
    'T', //  U+01AE   Ʈ  &#430;   Latin Capital Letter T with retroflex hook
    'U', //  U+01AF   Ư  &#431;   Latin Capital Letter U with horn
    'u', //  U+01B0   ư  &#432;   Latin Small Letter U with horn
    '.', //  U+01B1   Ʊ  &#433;   Latin Capital Letter Upsilon
    '.', //  U+01B2   Ʋ  &#434;   Latin Capital Letter V with hook
    '.', //  U+01B3   Ƴ  &#435;   Latin Capital Letter Y with hook
    '.', //  U+01B4   ƴ  &#436;   Latin Small Letter Y with hook
    '.', //  U+01B5   Ƶ  &#437;   Latin Capital Letter Z with stroke
    '.', //  U+01B6   ƶ  &#438;   Latin Small Letter Z with stroke
    '.', //  U+01B7   Ʒ  &#439;   Latin Capital Letter Ezh
    '.', //  U+01B8   Ƹ  &#440;   Latin Capital Letter Ezh reversed
    '.', //  U+01B9   ƹ  &#441;   Latin Small Letter Ezh reversed
    '.', //  U+01BA   ƺ  &#442;   Latin Small Letter Ezh with tail
    '.', //  U+01BB   ƻ  &#443;   Latin Letter Two with stroke
    '.', //  U+01BC   Ƽ  &#444;   Latin Capital Letter Tone Five
    '.', //  U+01BD   ƽ  &#445;   Latin Small Letter Tone Five
    '.', //  U+01BE   ƾ  &#446;   Latin Letter Inverted Glottal Stop with stroke
    '.', //  U+01BF   ƿ  &#447;   Latin Letter Wynn
    '|', //  U+01C0   ǀ  &#448;   Latin Letter Dental Click

    // I couldn't figure this one out
    '.', //  U+01C1   ǁ  &#449;   Latin Letter Lateral Click
    '.', //  U+01C2   ǂ  &#450;   Latin Letter Alveolar Click
    '!', //  U+01C3   ǃ  &#451;   Latin Letter Retroflex Click
    '.', //  U+01C4   Ǆ  &#452;   Latin Capital Letter DZ with caron
    '.', //  U+01C5   ǅ  &#453;   Latin Capital Letter D with Small Letter Z with caron
    '.', //  U+01C6   ǆ  &#454;   Latin Small Letter DZ with caron
    '.', //  U+01C7   Ǉ  &#455;   Latin Capital Letter LJ
    '.', //  U+01C8   ǈ  &#456;   Latin Capital Letter L with Small Letter J
    '.', //  U+01C9   ǉ  &#457;   Latin Small Letter LJ
    '.', //  U+01CA   Ǌ  &#458;   Latin Capital Letter NJ
    '.', //  U+01CB   ǋ  &#459;   Latin Capital Letter N with Small Letter J
    '.', //  U+01CC   ǌ  &#460;   Latin Small Letter NJ
    '.', //  U+01CD   Ǎ  &#461;   Latin Capital Letter A with caron
    '.', //  U+01CE   ǎ  &#462;   Latin Small Letter A with caron
    '.', //  U+01CF   Ǐ  &#463;   Latin Capital Letter I with caron
    '.', //  U+01D0   ǐ  &#464;   Latin Small Letter I with caron
    '.', //  U+01D1   Ǒ  &#465;   Latin Capital Letter O with caron
    '.', //  U+01D2   ǒ  &#466;   Latin Small Letter O with caron
    '.', //  U+01D3   Ǔ  &#467;   Latin Capital Letter U with caron
    '.', //  U+01D4   ǔ  &#468;   Latin Small Letter U with caron
    '.', //  U+01D5   Ǖ  &#469;   Latin Capital Letter U with diaeresis and macron
    '.', //  U+01D6   ǖ  &#470;   Latin Small Letter U with diaeresis and macron
    '.', //  U+01D7   Ǘ  &#471;   Latin Capital Letter U with diaeresis and acute
    '.', //  U+01D8   ǘ  &#472;   Latin Small Letter U with diaeresis and acute
    '.', //  U+01D9   Ǚ  &#473;   Latin Capital Letter U with diaeresis and caron
    '.', //  U+01DA   ǚ  &#474;   Latin Small Letter U with diaeresis and caron
    '.', //  U+01DB   Ǜ  &#475;   Latin Capital Letter U with diaeresis and grave
    '.', //  U+01DC   ǜ  &#476;   Latin Small Letter U with diaeresis and grave
    '.', //  U+01DD   ǝ  &#477;   Latin Small Letter Turned E
    'A', //  U+01DE   Ǟ  &#478;   Latin Capital Letter A with diaeresis and macron
    'a', //  U+01DF   ǟ  &#479;   Latin Small Letter A with diaeresis and macron
    '.', //  U+01E0   Ǡ  &#480;   Latin Capital Letter A with dot above and macron
    '.', //  U+01E1   ǡ  &#481;   Latin Small Letter A with dot above and macron
    '.', //  U+01E2   Ǣ  &#482;   Latin Capital Letter Æ with macron
    '.', //  U+01E3   ǣ  &#483;   Latin Small Letter Æ with macron
    'G', //  U+01E4   Ǥ  &#484;   Latin Capital Letter G with stroke
    'g', //  U+01E5   ǥ  &#485;   Latin Small Letter G with stroke
    '.', //  U+01E6   Ǧ  &#486;   Latin Capital Letter G with caron
    '.', //  U+01E7   ǧ  &#487;   Latin Small Letter G with caron
    '.', //  U+01E8   Ǩ  &#488;   Latin Capital Letter K with caron
    '.', //  U+01E9   ǩ  &#489;   Latin Small Letter K with caron
    '.', //  U+01EA   Ǫ  &#490;   Latin Capital Letter O with ogonek
    '.', //  U+01EB   ǫ  &#491;   Latin Small Letter O with ogonek
    'O', //  U+01EC   Ǭ  &#492;   Latin Capital Letter O with ogonek and macron
    'o', //  U+01ED   ǭ  &#493;   Latin Small Letter O with ogonek and macron
    '.', //  U+01EE   Ǯ  &#494;   Latin Capital Letter Ezh with caron
    '.', //  U+01EF   ǯ  &#495;   Latin Small Letter Ezh with caron
    '.', //  U+01F0   ǰ  &#496;   Latin Small Letter J with caron
    '.', //  U+01F1   Ǳ  &#497;   Latin Capital Letter DZ
    '.', //  U+01F2   ǲ  &#498;   Latin Capital Letter D with Small Letter Z
    '.', //  U+01F3   ǳ  &#499;   Latin Small Letter DZ
    '.', //  U+01F4   Ǵ  &#500;   Latin Capital Letter G with acute
    '.', //  U+01F5   ǵ  &#501;   Latin Small Letter G with acute
    '.', //  U+01F6   Ƕ  &#502;   Latin Capital Letter Hwair
    '.', //  U+01F7   Ƿ  &#503;   Latin Capital Letter Wynn
    '.', //  U+01F8   Ǹ  &#504;   Latin Capital Letter N with grave
    '.', //  U+01F9   ǹ  &#505;   Latin Small Letter N with grave
    '.', //  U+01FA   Ǻ  &#506;   Latin Capital Letter A with ring above and acute
    '.', //  U+01FB   ǻ  &#507;   Latin Small Letter A with ring above and acute
    '.', //  U+01FC   Ǽ  &#508;   Latin Capital Letter Æ with acute
    '.', //  U+01FD   ǽ  &#509;   Latin Small Letter Æ with acute
    '.', //  U+01FE   Ǿ  &#510;   Latin Capital Letter O with stroke and acute
    '.' //   U+01FF   ǿ  &#511;   Latin Small Letter O with stroke and acute
  };

  /*
    Older PDF security passwords use PDFDocEncoding, which is a modified iso-8859-1.
    Newer PDF security (version 5) passwords use unicode.
    This function is just for older PDF security passwords, since pdftk doesn't yet
    handle version 5 security.
    In practice, Acrobat/Reader accepts password characters outside of PDFDocEncoding.

    Returns -1 if input ss uses invalid characters
    otherwise returns the number of bytes copied to bb.
    If bb is null, then returns the number of bytes needed to copy ss.

    If we are encrypting, allow only the most conservative set of input characters
    in order to maximize platform independence.
    If we are decrypting, allow as many input characters as possible.

    TRANSLATOR NOTES:
    Returns a byte array; null if input uses invalid characters.
  */
  static byte[] utf8_password_to_pdfdoc(String ss_, boolean encrypt_b) {

    byte[] ss = ss_.getBytes(StandardCharsets.UTF_8);
    int ss_size = ss.length;
    ArrayList<Byte> bb = new ArrayList<Byte>(ss_size);

    for (int ii = 0; ii < ss_size; ) {

      // decode UTF-8
      int data = 0;
      //
      // our greatest valid point is 0x2122, so testing out to three bytes is enough
      if ((ss[ii] & 0x80) == 0) { // single byte
        data = ss[ii];
        ii++;
      } else if ((ss[ii] & 0xE0) == 0xC0
          && ii + 1 < ss_size
          && (ss[ii + 1] & 0xC0) == 0x80) { // two-byte
        data = (ss[ii] & 0x1f);
        data = data << 6;
        data = data + (ss[ii + 1] & 0x3f);
        ii += 2;
      } else if ((ss[ii] & 0xF0) == 0xE0
          && ii + 1 < ss_size
          && (ss[ii + 1] & 0xC0) == 0x80
          && ii + 2 < ss_size
          && (ss[ii + 2] & 0xC0) == 0x80) { // three-byte
        data = (ss[ii] & 0x0f);
        data = data << 6;
        data = data + (ss[ii + 1] & 0x3f);
        data = data << 6;
        data = data + (ss[ii + 2] & 0x3f);
        ii += 3;
      } else { // something else: out of range
        return null;
      }

      if (0x20 <= data && data < 0x7f || 0xa0 <= data && data <= 0xff) {
        // data point good
      } else { // convert some unicode points to PDFDocEncoding

        switch (data) {
          case 0x0152: // OE ligature
            data = 0226;
            break;
          case 0x0153: // oe ligature
            data = 0234;
            break;
          case 0x0160: // Scaron
            data = 0227;
            break;
          case 0x017E: // zcaron
            data = 0236;
            break;
          case 0x0178: // Ydieresis
            data = 0230;
            break;
          case 0x017D: // Zcaron
            data = 0231;
            break;
          case 0x0192: // florin
            data = 0206;
            break;
          case 0x0161: // scaron
            data = 0235;
            break;
          default:
            if (encrypt_b) { // data point out of bounds
              return null;
            } else {
              // password is being used to decrypt, so test for more mappings to PDFDocEncoding;

              if (0x100 <= data && data <= 0x1FF) {
                // use our latin extended windows map; these points are accepted by Acrobat/Reader
                // but are mapped to single-byte alternatives in a platform-dependent way
                data = unicode_latin_extended_windows_map[data - 0x100];
              } else {
                // in practice, Acorbat/Reader won't allow these characters to be input for V4
                // security and earlier;
                switch (data) {
                  case 0x20AC: // Euro
                    data = 0240;
                    break;
                  case 0x2022: // bullet
                    data = 0200;
                    break;
                  case 0x2020: // dagger
                    data = 0201;
                    break;
                  case 0x2021: // daggerdbl
                    data = 0202;
                    break;
                  case 0x2026: // ellipsis
                    data = 0203;
                    break;
                  case 0x02C6: // circumflex
                    data = 0032;
                    break;
                  case 0x2014: // emdash
                    data = 0204;
                    break;
                  case 0x2013: // endash
                    data = 0205;
                    break;
                  case 0x2039: // guilsinglleft
                    data = 0210;
                    break;
                  case 0x203A: // guilsinglright
                    data = 0211;
                    break;
                  case 0x2030: // perthousand
                    data = 0213;
                    break;
                  case 0x201E: // quotedblbase
                    data = 0214;
                    break;
                  case 0x201C: // quotedblleft
                    data = 0215;
                    break;
                  case 0x201D: // quotedblright
                    data = 0216;
                    break;
                  case 0x2018: // quoteleft
                    data = 0217;
                    break;
                  case 0x2019: // quoteright
                    data = 0220;
                    break;
                  case 0x201A: // quotesinglebase
                    data = 0221;
                    break;
                  case 0x02DC: // tilde
                    data = 0037;
                    break;
                  case 0x2122: // trademark
                    data = 0222;
                    break;
                  default:
                    return null; // data point is still out of bounds
                }
              }
            }
        }
      }

      bb.add((byte) data);
    }

    byte[] out = new byte[bb.size()];
    for (int i = 0; i < out.length; ++i) out[i] = bb.get(i);
    return out;
  }
};
