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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class pdftk {

  /* TODO: should read from compiler */
  static final String PDFTK_VER = "3.0.6";
  static final boolean ASK_ABOUT_WARNINGS = false;

  static String prompt_for_password(String pass_name, String pass_app) {
    System.out.println("Please enter the " + pass_name + " password to use on " + pass_app + ".");
    System.out.println("   It can be empty, or have a maximum of 32 characters:");
    Scanner s = new Scanner(System.in);
    String password = s.nextLine();
    if (32 < password.length()) { // too long; trim
      System.out.println("The password you entered was over 32 characters long,");
      System.out.println("   so I am dropping: \"" + password.substring(32) + "\"");
      password = password.substring(0, 32);
    }
    return password;
  }

  static String prompt_for_filename(String message) {
    // input could be multibyte, so try working
    // with bytes instead of formatted input features

    System.out.println(message);

    Scanner s = new Scanner(System.in);
    return s.nextLine();
  }

  static boolean confirm_overwrite(String filename) {
    System.out.println(
        "Warning: the output file: " + filename + " already exists.  Overwrite? (y/n)");
    Scanner s = new Scanner(System.in);
    String buff = s.nextLine();
    return buff.startsWith("y") || buff.startsWith("Y");
  }

  static boolean file_exists(String filename) {
    try {
      FileInputStream fp = new FileInputStream(filename);
      return true;
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  static OutputStream get_output_stream(String output_filename, boolean ask_about_warnings_b) {
    OutputStream os_p = null;

    if (output_filename.isEmpty() || output_filename.equals("PROMPT")) {
      output_filename = prompt_for_filename("Please enter a name for the output:");
      // recurse; try again
      return get_output_stream(output_filename, ask_about_warnings_b);
    }
    if (output_filename.equals("-")) { // stdout
      os_p = System.out;
    } else {
      if (ask_about_warnings_b) {
        // test for existing file by this name
        boolean output_exists_b = false;
        if (file_exists(output_filename)) {
          if (!confirm_overwrite(output_filename)) {
            // recurse; try again
            return get_output_stream("PROMPT", ask_about_warnings_b);
          }
        }
      }

      // attempt to open the stream
      try {
        os_p = new FileOutputStream(output_filename);
      } catch (IOException ioe_p) { // file open error
        System.err.println("Error: Failed to open output file: ");
        System.err.println("   " + output_filename);
        System.err.println("   No output created.");
        os_p = null;
      }
    }

    return os_p;
  }

  static PrintStream get_print_stream(String m_output_filename, boolean m_output_utf8_b)
      throws IOException {
    Charset encoding = (m_output_utf8_b ? StandardCharsets.UTF_8 : StandardCharsets.US_ASCII);
    if (m_output_filename.isEmpty() || m_output_filename.equals("-")) {
      return new PrintStream(System.out, true, encoding.name());
    } else {
      return new PrintStream(m_output_filename, encoding.name());
    }
  }

  public static void main(String[] args) {
    System.exit(main_noexit(args));
  }

  public static int main_noexit(String[] args) {
    boolean help_b = false;
    boolean version_b = false;
    boolean synopsis_b = (args.length == 0);
    ErrorCode ret_val = ErrorCode.NO_ERROR; // default: no error

    for (String argv : args) {
      version_b = version_b || (argv.equals("--version")) || (argv.equals("-version"));
      help_b = help_b || (argv.equals("--help")) || (argv.equals("-help")) || (argv.equals("-h"));
    }

    if (help_b) {
      describe_full();
    } else if (version_b) {
      describe_header();
    } else if (synopsis_b) {
      describe_synopsis();
    } else {
      try {
        TK_Session tk_session = new TK_Session(args);

        tk_session.dump_session_data();

        if (tk_session.is_valid()) {
          // create_output() prints necessary error messages
          ret_val = tk_session.create_output();
        } else { // error
          System.err.println("Done.  Input errors, so no output created.");
          ret_val = ErrorCode.ERROR;
        }
      }
      // per https://bugs.launchpad.net/ubuntu/+source/pdftk/+bug/544636
      catch (java.lang.ClassCastException c_p) {
        String message = c_p.getMessage();
        if (message.indexOf("com.lowagie.text.pdf.PdfDictionary") >= 0
            && message.indexOf("com.lowagie.text.pdf.PRIndirectReference") >= 0) {
          System.err.println("Error: One input PDF seems to not conform to the PDF standard.");
          System.err.println("Perhaps the document information dictionary is a direct object");
          System.err.println("   instead of an indirect reference.");
          System.err.println("Please report this bug to the program which produced the PDF.");
          System.err.println();
        }
        System.err.println("Java Exception:");
        c_p.printStackTrace();
        ret_val = ErrorCode.ERROR;
      } catch (java.lang.Throwable t_p) {
        System.err.println("Unhandled Java Exception in main():");
        t_p.printStackTrace();
        ret_val = ErrorCode.BUG;
      }
    }
    if (ret_val == ErrorCode.BUG) {
      describe_bug_report();
    }
    return ret_val.code;
  }

  static void describe_header() {
    System.out.println(
        "pdftk port to java " + PDFTK_VER + " a Handy Tool for Manipulating PDF Documents");
    System.out.println(
        "Copyright (c) 2017-2018 Marc Vinyals - https://gitlab.com/pdftk-java/pdftk");
    System.out.println("Copyright (c) 2003-2013 Steward and Lee, LLC.");
    System.out.println("pdftk includes a modified version of the iText library.");
    System.out.println("Copyright (c) 1999-2009 Bruno Lowagie, Paulo Soares, et al.");
    System.out.println(
        "This is free software; see the source code for copying conditions. There is");
    System.out.println(
        "NO warranty, not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
  }

  static void describe_synopsis() {
    System.out.println(
        "SYNOPSIS\n"
            + "       pdftk <input PDF files | - | PROMPT>\n"
            + "      [ input_pw <input PDF owner passwords | PROMPT> ]\n"
            + "      [ <operation> <operation arguments> ]\n"
            + "      [ output <output filename | - | PROMPT> ]\n"
            + "      [ encrypt_40bit | encrypt_128bit ]\n"
            + "      [ allow <permissions> ]\n"
            + "      [ owner_pw <owner password | PROMPT> ]\n"
            + "      [ user_pw <user password | PROMPT> ]\n"
            + "      [ flatten ] [ need_appearances ]\n"
            + "      [ compress | uncompress ]\n"
            + "      [ keep_first_id | keep_final_id ] [ drop_xfa ] [ drop_xmp ]\n"
            + "      [ verbose ] [ dont_ask | do_ask ]\n"
            + "       Where:\n"
            + "      <operation> may be empty, or:\n"
            + "      [ cat | shuffle | burst | rotate |\n"
            + "        generate_fdf | fill_form |\n"
            + "        background | multibackground |\n"
            + "        stamp | multistamp |\n"
            + "        dump_data | dump_data_utf8 |\n"
            + "        dump_data_fields | dump_data_fields_utf8 |\n"
            + "        dump_data_annots |\n"
            + "        update_info | update_info_utf8 |\n"
            + "        attach_files | unpack_files ]\n"
            + "\n"
            + "       For Complete Help: pdftk --help\n");
  }

  static void describe_full() {
    describe_header();
    System.out.println();

    describe_synopsis();
    System.out.println();

    System.out.println(
        "DESCRIPTION\n"
            + "       If PDF is electronic paper, then pdftk is an electronic staple-remover,\n"
            + "       hole-punch, binder, secret-decoder-ring, and X-Ray-glasses.  Pdftk is a\n"
            + "       simple tool for doing everyday things with PDF documents.  Use it to:\n"
            + "\n"
            + "       * Merge PDF Documents or Collate PDF Page Scans\n"
            + "       * Split PDF Pages into a New Document\n"
            + "       * Rotate PDF Documents or Pages\n"
            + "       * Decrypt Input as Necessary (Password Required)\n"
            + "       * Encrypt Output as Desired\n"
            + "       * Fill PDF Forms with X/FDF Data and/or Flatten Forms\n"
            + "       * Generate FDF Data Stencils from PDF Forms\n"
            + "       * Apply a Background Watermark or a Foreground Stamp\n"
            + "       * Report PDF Metrics, Bookmarks and Metadata\n"
            + "       * Add/Update PDF Bookmarks or Metadata\n"
            + "       * Attach Files to PDF Pages or the PDF Document\n"
            + "       * Unpack PDF Attachments\n"
            + "       * Burst a PDF Document into Single Pages\n"
            + "       * Uncompress and Re-Compress Page Streams\n"
            + "       * Repair Corrupted PDF (Where Possible)\n"
            + "\n"
            + "OPTIONS\n"
            + "       A summary of options is included below.\n"
            + "\n"
            + "       --help, -h\n"
            + "        Show this summary of options.\n"
            + "\n"
            + "       <input PDF files | - | PROMPT>\n"
            + "        A list of the input PDF files. If you plan to combine these PDFs\n"
            + "        (without using handles) then list files in the order you want\n"
            + "        them combined.  Use - to pass a single PDF into pdftk via stdin.\n"
            + "        Input files can be associated with handles, where a handle is\n"
            + "        one or more upper-case letters:\n"
            + "\n"
            + "        <input PDF handle>=<input PDF filename>\n"
            + "\n"
            + "        Handles are often omitted.  They are useful when specifying PDF\n"
            + "        passwords or page ranges, later.\n"
            + "\n"
            + "        For example: A=input1.pdf QT=input2.pdf M=input3.pdf\n"
            + "\n"
            + "       [input_pw <input PDF owner passwords | PROMPT>]\n"
            + "        Input PDF owner passwords, if necessary, are associated with\n"
            + "        files by using their handles:\n"
            + "\n"
            + "        <input PDF handle>=<input PDF file owner password>\n"
            + "\n"
            + "        If handles are not given, then passwords are associated with\n"
            + "        input files by order.\n"
            + "\n"
            + "        Most pdftk features require that encrypted input PDF are accom-\n"
            + "        panied by the ~owner~ password. If the input PDF has no owner\n"
            + "        password, then the user password must be given, instead.  If the\n"
            + "        input PDF has no passwords, then no password should be given.\n"
            + "\n"
            + "        When running in do_ask mode, pdftk will prompt you for a pass-\n"
            + "        word if the supplied password is incorrect or none was given.\n"
            + "\n"
            + "       [<operation> <operation arguments>]\n"
            + "        Available operations are: cat, shuffle, burst, rotate, gener-\n"
            + "        ate_fdf, fill_form, background, multibackground, stamp, multi-\n"
            + "        stamp, dump_data, dump_data_utf8, dump_data_fields,\n"
            + "        dump_data_fields_utf8, dump_data_annots, update_info,\n"
            + "        update_info_utf8, attach_files, unpack_files. Some operations\n"
            + "        takes additional arguments, described below.\n"
            + "\n"
            + "        If this optional argument is omitted, then pdftk runs in 'fil-\n"
            + "        ter' mode.  Filter mode takes only one PDF input and creates a\n"
            + "        new PDF after applying all of the output options, like encryp-\n"
            + "        tion and compression.\n"
            + "\n"
            + "    cat [<page ranges>]\n"
            + "     Assembles (catenates) pages from input PDFs to create a new\n"
            + "     PDF. Use cat to merge PDF pages or to split PDF pages from\n"
            + "     documents. You can also use it to rotate PDF pages. Page\n"
            + "     order in the new PDF is specified by the order of the given\n"
            + "     page ranges. Page ranges are described like this:\n"
            + "\n"
            + "     <input PDF handle>[<begin page number>[-<end page num-\n"
            + "     ber>[<qualifier>]]][<page rotation>]\n"
            + "\n"
            + "     Where the handle identifies one of the input PDF files, and\n"
            + "     the beginning and ending page numbers are one-based refer-\n"
            + "     ences to pages in the PDF file.  The qualifier can be even or\n"
            + "     odd, and the page rotation can be north, south, east, west,\n"
            + "     left, right, or down.\n"
            + "\n"
            + "     If a PDF handle is given but no pages are specified, then the\n"
            + "     entire PDF is used. If no pages are specified for any of the\n"
            + "     input PDFs, then the input PDFs' bookmarks are also merged\n"
            + "     and included in the output.\n"
            + "\n"
            + "     If the handle is omitted from the page range, then the pages\n"
            + "     are taken from the first input PDF.\n"
            + "\n"
            + "     The even qualifier causes pdftk to use only the even-numbered\n"
            + "     PDF pages, so 1-6even yields pages 2, 4 and 6 in that order.\n"
            + "     6-1even yields pages 6, 4 and 2 in that order.\n"
            + "\n"
            + "     The odd qualifier works similarly to the even.\n"
            + "\n"
            + "     The page rotation setting can cause pdftk to rotate pages and\n"
            + "     documents.  Each option sets the page rotation as follows (in\n"
            + "     degrees): north: 0, east: 90, south: 180, west: 270, left:\n"
            + "     -90, right: +90, down: +180. left, right, and down make rela-\n"
            + "     tive adjustments to a page's rotation.\n"
            + "\n"
            + "     If no arguments are passed to cat, then pdftk combines all\n"
            + "     input PDFs in the order they were given to create the output.\n"
            + "\n"
            + "     NOTES:\n"
            + "     * <end page number> may be less than <begin page number>.\n"
            + "     * The keyword end may be used to reference the final page of\n"
            + "     a document instead of a page number.\n"
            + "     * Reference a single page by omitting the ending page number.\n"
            + "     * The handle may be used alone to represent the entire PDF\n"
            + "     document, e.g., B1-end is the same as B.\n"
            + "     * You can reference page numbers in reverse order by prefix-\n"
            + "     ing them with the letter r. For example, page r1 is the last\n"
            + "     page of the document, r2 is the next-to-last page of the doc-\n"
            + "     ument, and rend is the first page of the document. You can\n"
            + "     use this prefix in ranges, too, for example r3-r1 is the last\n"
            + "     three pages of a PDF.\n"
            + "\n"
            + "     Page Range Examples without Handles:\n"
            + "     1-endeast - rotate entire document 90 degrees\n"
            + "     5 11 20 - take single pages from input PDF\n"
            + "     5-25oddwest - take odd pages in range, rotate 90 degrees\n"
            + "     6-1 - reverse pages in range from input PDF\n"
            + "\n"
            + "     Page Range Examples Using Handles:\n"
            + "     Say A=in1.pdf B=in2.pdf, then:\n"
            + "     A1-21 - take range from in1.pdf\n"
            + "     Bend-1odd - take all odd pages from in2.pdf in reverse order\n"
            + "     A72 - take a single page from in1.pdf\n"
            + "     A1-21 Beven A72 - assemble pages from both in1.pdf and\n"
            + "     in2.pdf\n"
            + "     Awest - rotate entire in1.pdf document 90 degrees\n"
            + "     B - use all of in2.pdf\n"
            + "     A2-30evenleft - take the even pages from the range, remove 90\n"
            + "     degrees from each page's rotation\n"
            + "     A A - catenate in1.pdf with in1.pdf\n"
            + "     Aevenwest Aoddeast - apply rotations to even pages, odd pages\n"
            + "     from in1.pdf\n"
            + "     Awest Bwest Bdown - catenate rotated documents\n"
            + "\n"
            + "    shuffle [<page ranges>]\n"
            + "     Collates pages from input PDFs to create a new PDF.  Works\n"
            + "     like the cat operation except that it takes one page at a\n"
            + "     time from each page range to assemble the output PDF.  If one\n"
            + "     range runs out of pages, it continues with the remaining\n"
            + "     ranges.  Ranges can use all of the features described above\n"
            + "     for cat, like reverse page ranges, multiple ranges from a\n"
            + "     single PDF, and page rotation.  This feature was designed to\n"
            + "     help collate PDF pages after scanning paper documents.\n"
            + "\n"
            + "    burst  Splits a single input PDF document into individual pages.\n"
            + "     Also creates a report named doc_data.txt which is the same as\n"
            + "     the output from dump_data.  If the output section is omitted,\n"
            + "     then PDF pages are named: pg_%04d.pdf, e.g.: pg_0001.pdf,\n"
            + "     pg_0002.pdf, etc.  To name these pages yourself, supply a\n"
            + "     printf-styled format string via the output section.  For\n"
            + "     example, if you want pages named: page_01.pdf, page_02.pdf,\n"
            + "     etc., pass output page_%02d.pdf to pdftk.  Encryption can be\n"
            + "     applied to the output by appending output options such as\n"
            + "     owner_pw, e.g.:\n"
            + "\n"
            + "     pdftk in.pdf burst owner_pw foopass\n"
            + "\n"
            + "    rotate [<page ranges>]\n"
            + "     Takes a single input PDF and rotates just the specified\n"
            + "     pages.  All other pages remain unchanged.  The page order\n"
            + "     remains unchaged.  Specify the pages to rotate using the same\n"
            + "     notation as you would with cat, except you omit the pages\n"
            + "     that you aren't rotating:\n"
            + "\n"
            + "     [<begin page number>[-<end page number>[<qualifier>]]][<page\n"
            + "     rotation>]\n"
            + "\n"
            + "     The qualifier can be even or odd, and the page rotation can\n"
            + "     be north, south, east, west, left, right, or down.\n"
            + "\n"
            + "     Each option sets the page rotation as follows (in degrees):\n"
            + "     north: 0, east: 90, south: 180, west: 270, left: -90, right:\n"
            + "     +90, down: +180. left, right, and down make relative adjust-\n"
            + "     ments to a page's rotation.\n"
            + "\n"
            + "     The given order of the pages doesn't change the page order in\n"
            + "     the output.\n"
            + "\n"
            + "    generate_fdf\n"
            + "     Reads a single input PDF file and generates an FDF file suit-\n"
            + "     able for fill_form out of it to the given output filename or\n"
            + "     (if no output is given) to stdout.  Does not create a new\n"
            + "     PDF.\n"
            + "\n"
            + "    fill_form <FDF data filename | XFDF data filename | - | PROMPT>\n"
            + "     Fills the single input PDF's form fields with the data from\n"
            + "     an FDF file, XFDF file or stdin. Enter the data filename\n"
            + "     after fill_form, or use - to pass the data via stdin, like\n"
            + "     so:\n"
            + "\n"
            + "     pdftk form.pdf fill_form data.fdf output form.filled.pdf\n"
            + "\n"
            + "     If the input FDF file includes Rich Text formatted data in\n"
            + "     addition to plain text, then the Rich Text data is packed\n"
            + "     into the form fields as well as the plain text.  Pdftk also\n"
            + "     sets a flag that cues Reader/Acrobat to generate new field\n"
            + "     appearances based on the Rich Text data.  So when the user\n"
            + "     opens the PDF, the viewer will create the Rich Text appear-\n"
            + "     ance on the spot.  If the user's PDF viewer does not support\n"
            + "     Rich Text, then the user will see the plain text data\n"
            + "     instead.  If you flatten this form before Acrobat has a\n"
            + "     chance to create (and save) new field appearances, then the\n"
            + "     plain text field data is what you'll see.\n"
            + "\n"
            + "     Also see the flatten and need_appearances options.\n"
            + "\n"
            + "    background <background PDF filename | - | PROMPT>\n"
            + "     Applies a PDF watermark to the background of a single input\n"
            + "     PDF.  Pass the background PDF's filename after background\n"
            + "     like so:\n"
            + "\n"
            + "     pdftk in.pdf background back.pdf output out.pdf\n"
            + "\n"
            + "     Pdftk uses only the first page from the background PDF and\n"
            + "     applies it to every page of the input PDF.  This page is\n"
            + "     scaled and rotated as needed to fit the input page.  You can\n"
            + "     use - to pass a background PDF into pdftk via stdin.\n"
            + "\n"
            + "     If the input PDF does not have a transparent background (such\n"
            + "     as a PDF created from page scans) then the resulting back-\n"
            + "     ground won't be visible -- use the stamp operation instead.\n"
            + "\n"
            + "    multibackground <background PDF filename | - | PROMPT>\n"
            + "     Same as the background operation, but applies each page of\n"
            + "     the background PDF to the corresponding page of the input\n"
            + "     PDF.  If the input PDF has more pages than the stamp PDF,\n"
            + "     then the final stamp page is repeated across these remaining\n"
            + "     pages in the input PDF.\n"
            + "\n"
            + "    stamp <stamp PDF filename | - | PROMPT>\n"
            + "     This behaves just like the background operation except it\n"
            + "     overlays the stamp PDF page on top of the input PDF docu-\n"
            + "     ment's pages.  This works best if the stamp PDF page has a\n"
            + "     transparent background.\n"
            + "\n"
            + "    multistamp <stamp PDF filename | - | PROMPT>\n"
            + "     Same as the stamp operation, but applies each page of the\n"
            + "     background PDF to the corresponding page of the input PDF.\n"
            + "     If the input PDF has more pages than the stamp PDF, then the\n"
            + "     final stamp page is repeated across these remaining pages in\n"
            + "     the input PDF.\n"
            + "\n"
            + "    dump_data\n"
            + "     Reads a single input PDF file and reports its metadata, book-\n"
            + "     marks (a/k/a outlines), page metrics (media, rotation and\n"
            + "     labels), data embedded by STAMPtk (see STAMPtk's embed\n"
            + "     option) and other data to the given output filename or (if no\n"
            + "     output is given) to stdout.  Non-ASCII characters are encoded\n"
            + "     as XML numerical entities.  Does not create a new PDF.\n"
            + "\n"
            + "    dump_data_utf8\n"
            + "     Same as dump_data excepct that the output is encoded as\n"
            + "     UTF-8.\n"
            + "\n"
            + "    dump_data_fields\n"
            + "     Reads a single input PDF file and reports form field statis-\n"
            + "     tics to the given output filename or (if no output is given)\n"
            + "     to stdout. Non-ASCII characters are encoded as XML numerical\n"
            + "     entities. Does not create a new PDF.\n"
            + "\n"
            + "    dump_data_fields_utf8\n"
            + "     Same as dump_data_fields excepct that the output is encoded\n"
            + "     as UTF-8.\n"
            + "\n"
            + "    dump_data_annots\n"
            + "     This operation currently reports only link annotations.\n"
            + "     Reads a single input PDF file and reports annotation informa-\n"
            + "     tion to the given output filename or (if no output is given)\n"
            + "     to stdout. Non-ASCII characters are encoded as XML numerical\n"
            + "     entities. Does not create a new PDF.\n"
            + "\n"
            + "    update_info <info data filename | - | PROMPT>\n"
            + "     Changes the bookmarks and metadata in a single PDF's Info\n"
            + "     dictionary to match the input data file. The input data file\n"
            + "     uses the same syntax as the output from dump_data. Non-ASCII\n"
            + "     characters should be encoded as XML numerical entities.\n"
            + "\n"
            + "     This operation does not change the metadata stored in the\n"
            + "     PDF's XMP stream, if it has one. (For this reason you should\n"
            + "     include a ModDate entry in your updated info with a current\n"
            + "     date/timestamp, format: D:YYYYMMDDHHmmSS, e.g. D:201307241346\n"
            + "     -- omitted data after YYYY revert to default values.)\n"
            + "\n"
            + "     For example:\n"
            + "\n"
            + "     pdftk in.pdf update_info in.info output out.pdf\n"
            + "\n"
            + "    update_info_utf8 <info data filename | - | PROMPT>\n"
            + "     Same as update_info except that the input is encoded as\n"
            + "     UTF-8.\n"
            + "\n"
            + "    attach_files <attachment filenames | PROMPT> [to_page <page number |\n"
            + "    PROMPT>]\n"
            + "     Packs arbitrary files into a PDF using PDF's file attachment\n"
            + "     features. More than one attachment may be listed after\n"
            + "     attach_files. Attachments are added at the document level\n"
            + "     unless the optional to_page option is given, in which case\n"
            + "     the files are attached to the given page number (the first\n"
            + "     page is 1, the final page is end). For example:\n"
            + "\n"
            + "     pdftk in.pdf attach_files table1.html table2.html to_page 6\n"
            + "     output out.pdf\n"
            + "\n"
            + "    unpack_files\n"
            + "     Copies all of the attachments from the input PDF into the\n"
            + "     current folder or to an output directory given after output.\n"
            + "     For example:\n"
            + "\n"
            + "     pdftk report.pdf unpack_files output ~/atts/\n"
            + "\n"
            + "     or, interactively:\n"
            + "\n"
            + "     pdftk report.pdf unpack_files output PROMPT\n"
            + "\n"
            + "       [output <output filename | - | PROMPT>]\n"
            + "        The output PDF filename may not be set to the name of an input\n"
            + "        filename. Use - to output to stdout.  When using the dump_data\n"
            + "        operation, use output to set the name of the output data file.\n"
            + "        When using the unpack_files operation, use output to set the\n"
            + "        name of an output directory.  When using the burst operation,\n"
            + "        you can use output to control the resulting PDF page filenames\n"
            + "        (described above).\n"
            + "\n"
            + "       [encrypt_40bit | encrypt_128bit]\n"
            + "        If an output PDF user or owner password is given, output PDF\n"
            + "        encryption strength defaults to 128 bits.  This can be overrid-\n"
            + "        den by specifying encrypt_40bit.\n"
            + "\n"
            + "       [allow <permissions>]\n"
            + "        Permissions are applied to the output PDF only if an encryption\n"
            + "        strength is specified or an owner or user password is given.  If\n"
            + "        permissions are not specified, they default to 'none,' which\n"
            + "        means all of the following features are disabled.\n"
            + "\n"
            + "        The permissions section may include one or more of the following\n"
            + "        features:\n"
            + "\n"
            + "        Printing\n"
            + "         Top Quality Printing\n"
            + "\n"
            + "        DegradedPrinting\n"
            + "         Lower Quality Printing\n"
            + "\n"
            + "        ModifyContents\n"
            + "         Also allows Assembly\n"
            + "\n"
            + "        Assembly\n"
            + "\n"
            + "        CopyContents\n"
            + "         Also allows ScreenReaders\n"
            + "\n"
            + "        ScreenReaders\n"
            + "\n"
            + "        ModifyAnnotations\n"
            + "         Also allows FillIn\n"
            + "\n"
            + "        FillIn\n"
            + "\n"
            + "        AllFeatures\n"
            + "         Allows the user to perform all of the above, and top\n"
            + "         quality printing.\n"
            + "\n"
            + "       [owner_pw <owner password | PROMPT>]\n"
            + "\n"
            + "       [user_pw <user password | PROMPT>]\n"
            + "        If an encryption strength is given but no passwords are sup-\n"
            + "        plied, then the owner and user passwords remain empty, which\n"
            + "        means that the resulting PDF may be opened and its security\n"
            + "        parameters altered by anybody.\n"
            + "\n"
            + "       [compress | uncompress]\n"
            + "        These are only useful when you want to edit PDF code in a text\n"
            + "        editor like vim or emacs.  Remove PDF page stream compression by\n"
            + "        applying the uncompress filter. Use the compress filter to\n"
            + "        restore compression.\n"
            + "\n"
            + "       [flatten]\n"
            + "        Use this option to merge an input PDF's interactive form fields\n"
            + "        (and their data) with the PDF's pages. Only one input PDF may be\n"
            + "        given. Sometimes used with the fill_form operation.\n"
            + "\n"
            + "       [need_appearances]\n"
            + "        Sets a flag that cues Reader/Acrobat to generate new field\n"
            + "        appearances based on the form field values.  Use this when fill-\n"
            + "        ing a form with non-ASCII text to ensure the best presentation\n"
            + "        in Adobe Reader or Acrobat.  It won't work when combined with\n"
            + "        the flatten option.\n"
            + "\n"
            + "       [keep_first_id | keep_final_id]\n"
            + "        When combining pages from multiple PDFs, use one of these\n"
            + "        options to copy the document ID from either the first or final\n"
            + "        input document into the new output PDF. Otherwise pdftk creates\n"
            + "        a new document ID for the output PDF. When no operation is\n"
            + "        given, pdftk always uses the ID from the (single) input PDF.\n"
            + "\n"
            + "       [drop_xfa]\n"
            + "        If your input PDF is a form created using Acrobat 7 or Adobe\n"
            + "        Designer, then it probably has XFA data.  Filling such a form\n"
            + "        using pdftk yields a PDF with data that fails to display in\n"
            + "        Acrobat 7 (and 6?).  The workaround solution is to remove the\n"
            + "        form's XFA data, either before you fill the form using pdftk or\n"
            + "        at the time you fill the form. Using this option causes pdftk to\n"
            + "        omit the XFA data from the output PDF form.\n"
            + "\n"
            + "        This option is only useful when running pdftk on a single input\n"
            + "        PDF.  When assembling a PDF from multiple inputs using pdftk,\n"
            + "        any XFA data in the input is automatically omitted.\n"
            + "\n"
            + "       [drop_xmp]\n"
            + "        Many PDFs store document metadata using both an Info dictionary\n"
            + "        (old school) and an XMP stream (new school).  Pdftk's\n"
            + "        update_info operation can update the Info dictionary, but not\n"
            + "        the XMP stream.  The proper remedy for this is to include a\n"
            + "        ModDate entry in your updated info with a current date/time-\n"
            + "        stamp. The date/timestamp format is: D:YYYYMMDDHHmmSS, e.g.\n"
            + "        D:201307241346 -- omitted data after YYYY revert to default val-\n"
            + "        ues. This newer ModDate should cue PDF viewers that the Info\n"
            + "        metadata is more current than the XMP data.\n"
            + "\n"
            + "        Alternatively, you might prefer to remove the XMP stream from\n"
            + "        the PDF altogether -- that's what this option does.  Note that\n"
            + "        objects inside the PDF might have their own, separate XMP meta-\n"
            + "        data streams, and that drop_xmp does not remove those.  It only\n"
            + "        removes the PDF's document-level XMP stream.\n"
            + "\n"
            + "       [verbose]\n"
            + "        By default, pdftk runs quietly. Append verbose to the end and it\n"
            + "        will speak up.\n"
            + "\n"
            + "       [dont_ask | do_ask]\n"
            + "        Depending on the compile-time settings (see ASK_ABOUT_WARNINGS),\n"
            + "        pdftk might prompt you for further input when it encounters a\n"
            + "        problem, such as a bad password. Override this default behavior\n"
            + "        by adding dont_ask (so pdftk won't ask you what to do) or do_ask\n"
            + "        (so pdftk will ask you what to do).\n"
            + "\n"
            + "        When running in dont_ask mode, pdftk will over-write files with\n"
            + "        its output without notice.\n"
            + "\n"
            + "EXAMPLES\n"
            + "       Collate scanned pages\n"
            + "   pdftk A=even.pdf B=odd.pdf shuffle A B output collated.pdf\n"
            + "   or if odd.pdf is in reverse order:\n"
            + "   pdftk A=even.pdf B=odd.pdf shuffle A Bend-1 output collated.pdf\n"
            + "\n"
            + "       Decrypt a PDF\n"
            + "   pdftk secured.pdf input_pw foopass output unsecured.pdf\n"
            + "\n"
            + "       Encrypt a PDF using 128-bit strength (the default), withhold all per-\n"
            + "       missions (the default)\n"
            + "   pdftk 1.pdf output 1.128.pdf owner_pw foopass\n"
            + "\n"
            + "       Same as above, except password 'baz' must also be used to open output\n"
            + "       PDF\n"
            + "   pdftk 1.pdf output 1.128.pdf owner_pw foo user_pw baz\n"
            + "\n"
            + "       Same as above, except printing is allowed (once the PDF is open)\n"
            + "   pdftk 1.pdf output 1.128.pdf owner_pw foo user_pw baz allow printing\n"
            + "\n"
            + "       Join in1.pdf and in2.pdf into a new PDF, out1.pdf\n"
            + "   pdftk in1.pdf in2.pdf cat output out1.pdf\n"
            + "   or (using handles):\n"
            + "   pdftk A=in1.pdf B=in2.pdf cat A B output out1.pdf\n"
            + "   or (using wildcards):\n"
            + "   pdftk *.pdf cat output combined.pdf\n"
            + "\n"
            + "       Remove page 13 from in1.pdf to create out1.pdf\n"
            + "   pdftk in.pdf cat 1-12 14-end output out1.pdf\n"
            + "   or:\n"
            + "   pdftk A=in1.pdf cat A1-12 A14-end output out1.pdf\n"
            + "\n"
            + "       Apply 40-bit encryption to output, revoking all permissions (the\n"
            + "       default). Set the owner PW to 'foopass'.\n"
            + "   pdftk 1.pdf 2.pdf cat output 3.pdf encrypt_40bit owner_pw foopass\n"
            + "\n"
            + "       Join two files, one of which requires the password 'foopass'. The out-\n"
            + "       put is not encrypted.\n"
            + "   pdftk A=secured.pdf 2.pdf input_pw A=foopass cat output 3.pdf\n"
            + "\n"
            + "       Uncompress PDF page streams for editing the PDF in a text editor (e.g.,\n"
            + "       vim, emacs)\n"
            + "   pdftk doc.pdf output doc.unc.pdf uncompress\n"
            + "\n"
            + "       Repair a PDF's corrupted XREF table and stream lengths, if possible\n"
            + "   pdftk broken.pdf output fixed.pdf\n"
            + "\n"
            + "       Burst a single PDF document into pages and dump its data to\n"
            + "       doc_data.txt\n"
            + "   pdftk in.pdf burst\n"
            + "\n"
            + "       Burst a single PDF document into encrypted pages. Allow low-quality\n"
            + "       printing\n"
            + "   pdftk in.pdf burst owner_pw foopass allow DegradedPrinting\n"
            + "\n"
            + "       Write a report on PDF document metadata and bookmarks to report.txt\n"
            + "   pdftk in.pdf dump_data output report.txt\n"
            + "\n"
            + "       Rotate the first PDF page to 90 degrees clockwise\n"
            + "   pdftk in.pdf cat 1east 2-end output out.pdf\n"
            + "\n"
            + "       Rotate an entire PDF document to 180 degrees\n"
            + "   pdftk in.pdf cat 1-endsouth output out.pdf\n"
            + "\n"
            + "NOTES\n"
            + "       This is a port of pdftk to java. See https://gitlab.com/pdftk-java/pdftk\n"
            + "       The original program can be found at www.pdftk.com\n"
            + "\n"
            + "AUTHOR\n"
            + "       Original author of pdftk is Sid Steward (sid.steward at pdflabs dot com).");
  }

  static void describe_bug_report() {
    System.err.println("There was a problem with pdftk-java. Please report it at");
    System.err.println("https://gitlab.com/pdftk-java/pdftk/issues");
    System.err.println(
        "including the message above, the version of pdftk-java ("
            + PDFTK_VER
            + "), and if possible steps to reproduce the error.");
  }
};
