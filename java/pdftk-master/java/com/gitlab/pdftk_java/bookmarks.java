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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfDestination;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;
import pdftk.com.lowagie.text.pdf.PdfWriter;

class bookmarks {

  static int GetPageNumber(
      PdfDictionary dict_p, PdfReader reader_p, HashMap<PdfDictionary, Integer> cache) {
    // take a PdfPage dictionary and return its page location in the document;
    // recurse our way up the pages tree, counting pages as we go;
    // dict_p may be a page or a page tree object;
    // return value is zero-based;
    { // consult the cache
      Integer it = cache.get(dict_p);
      if (it != null) return it;
    }

    int ret_val = 0;

    if (dict_p != null && dict_p.contains(PdfName.PARENT)) {
      PdfObject parent_po = reader_p.getPdfObject(dict_p.get(PdfName.PARENT));
      if (parent_po != null && parent_po.isDictionary()) {
        PdfDictionary parent_p = (PdfDictionary) parent_po;
        // a parent is a page tree object and will have Kids

        // recurse up the page tree
        int sum_pages = GetPageNumber(parent_p, reader_p, cache);

        PdfObject parent_kids_p = reader_p.getPdfObject(parent_p.get(PdfName.KIDS));
        if (parent_kids_p != null && parent_kids_p.isArray()) {
          // Kids may be Pages or Page Tree Nodes

          // iterate over *dict_p's parent's kids until we run into *dict_p
          ArrayList<PdfObject> kids_p = ((PdfArray) parent_kids_p).getArrayList();
          if (kids_p != null) {
            for (PdfObject kids_ii : kids_p) {

              PdfObject kid_po = reader_p.getPdfObject(kids_ii);
              if (kid_po != null && kid_po.isDictionary()) {
                PdfDictionary kid_p = (PdfDictionary) kid_po;

                // Translator note: comparing references
                if (kid_p == dict_p) // we have what we were looking for
                  ret_val = sum_pages;

                // is kid a page, or is kid a page tree object? add count to sum;
                // PdfDictionary::isPage() and PdfDictionary::isPages()
                // are not reliable, here

                PdfObject kid_type_p = reader_p.getPdfObject(kid_p.get(PdfName.TYPE));
                if (kid_type_p != null && kid_type_p.isName()) {

                  if (kid_type_p.equals(PdfName.PAGE)) {
                    // *kid_p is a Page

                    // store page number in our cache
                    cache.put(kid_p, sum_pages);

                    //
                    sum_pages += 1;
                  } else if (kid_type_p.equals(PdfName.PAGES)) {
                    // *kid_p is a Page Tree Node

                    PdfObject count_p = reader_p.getPdfObject(kid_p.get(PdfName.COUNT));
                    if (count_p != null && count_p.isNumber()) {

                      //
                      sum_pages += ((PdfNumber) count_p).intValue();
                    } else { // error
                      System.err.println("pdftk Error in GetPageNumber(): invalid count;");
                    }
                  } else { // error
                    System.err.println("pdftk Error in GetPageNumber(): unexpected kid type;");
                  }
                } else { // error
                  System.err.println("pdftk Error in GetPageNumber(): invalid kid_type_p;");
                }
              } else { // error
                System.err.println("pdftk Error in GetPageNumber(): invalid kid_p;");
              }
            } // done iterating over kids

          } else { // error
            System.err.println("pdftk Error in GetPageNumber(): invalid kids_p;");
          }
        } else { // error
          System.err.println("pdftk Error in GetPageNumber(): invalid kids array;");
        }
      } else { // error
        System.err.println("pdftk Error in GetPageNumber(): invalid parent;");
      }
    } else {
      // *dict_p has no parent; end recursion
      ret_val = 0;
      cache.put(dict_p, ret_val);
    }

    return ret_val;
  }

  static int ReadOutlines(
      ArrayList<PdfBookmark> bookmark_data,
      PdfDictionary outline_p,
      int level,
      PdfReader reader_p,
      boolean utf8_b) {

    int ret_val = 0;
    HashMap<PdfDictionary, Integer> cache = new HashMap<PdfDictionary, Integer>();

    while (outline_p != null) {

      // load this with collected data, then add to vector
      PdfBookmark bookmark = new PdfBookmark();

      // the title
      PdfObject title_p = reader_p.getPdfObject(outline_p.get(PdfName.TITLE));
      if (title_p != null && title_p.isString()) {
        bookmark.m_title = report.OutputPdfString((PdfString) title_p, utf8_b);
      } else { // error
        ret_val = 1;
      }

      // the level; 1-based to jive with HTML heading level concept
      bookmark.m_level = level + 1;

      // page number, 1-based;
      // a zero value indicates no page destination or an error
      {
        boolean fail_b = false;

        // the destination object may take be in a couple different places
        // and may take a couple, different forms

        PdfObject destination_p = null;
        {
          if (outline_p.contains(PdfName.DEST)) {
            destination_p = reader_p.getPdfObject(outline_p.get(PdfName.DEST));
          } else if (outline_p.contains(PdfName.A)) {

            PdfObject action_po = reader_p.getPdfObject(outline_p.get(PdfName.A));
            if (action_po != null && action_po.isDictionary()) {
              PdfDictionary action_p = (PdfDictionary) action_po;

              PdfObject s_p = reader_p.getPdfObject(action_p.get(PdfName.S));
              if (s_p != null && s_p.isName()) {

                if (s_p.equals(PdfName.GOTO)) {
                  destination_p = reader_p.getPdfObject(action_p.get(PdfName.D));
                } else { // immediate action is not a link in this document;
                  // not an error
                  // fail_b= true;
                }
              } else { // error
                fail_b = true;
              }
            } else { // error
              fail_b = true;
            }
          } else { // unexpected
            fail_b = true;
          }
        }

        // destination is an array
        if (destination_p != null && destination_p.isArray()) {

          ArrayList<PdfObject> array_list_p = ((PdfArray) destination_p).getArrayList();
          if (array_list_p != null && !array_list_p.isEmpty()) {

            PdfObject page_p = reader_p.getPdfObject(array_list_p.get(0));
            if (page_p != null && page_p.isDictionary()) {
              bookmark.m_page_num = GetPageNumber((PdfDictionary) page_p, reader_p, cache) + 1;
            } else { // error
              fail_b = true;
            }
          } else { // error
            fail_b = true;
          }
        } // TODO: named destinations handling
        else { // error
          fail_b = true;
        }

        if (fail_b) { // output our 'null page reference' code
          bookmark.m_page_num = 0;
        }
      }

      // add bookmark to collected data
      if (0 < bookmark.m_level) bookmark_data.add(bookmark);

      // recurse into any children
      if (outline_p.contains(PdfName.FIRST)) {

        PdfObject child_p = reader_p.getPdfObject(outline_p.get(PdfName.FIRST));
        if (child_p != null && child_p.isDictionary()) {

          ret_val += ReadOutlines(bookmark_data, (PdfDictionary) child_p, level + 1, reader_p, utf8_b);
        }
      }

      // iterate over siblings
      if (outline_p.contains(PdfName.NEXT)) {

        PdfObject sibling_p = reader_p.getPdfObject(outline_p.get(PdfName.NEXT));
        if (sibling_p != null && sibling_p.isDictionary()) {
          outline_p = (PdfDictionary) sibling_p;
        } else // break out of loop
        outline_p = null;
      } else // break out of loop
      outline_p = null;
    }

    return ret_val;
  }

  static void RemoveBookmarks(PdfReader reader_p, PdfDictionary bookmark_p)
        // call reader_p->removeUnusedObjects() afterward
      {
    if (bookmark_p.contains(PdfName.FIRST)) { // recurse
      PdfDictionary first_p = (PdfDictionary) reader_p.getPdfObject(bookmark_p.get(PdfName.FIRST));
      RemoveBookmarks(reader_p, first_p);

      bookmark_p.remove(PdfName.FIRST);
    }

    if (bookmark_p.contains(PdfName.NEXT)) { // recurse
      PdfDictionary next_p = (PdfDictionary) reader_p.getPdfObject(bookmark_p.get(PdfName.NEXT));
      RemoveBookmarks(reader_p, next_p);

      bookmark_p.remove(PdfName.NEXT);
    }

    bookmark_p.remove(PdfName.PARENT);
    bookmark_p.remove(PdfName.PREV);
    bookmark_p.remove(PdfName.LAST);
  }

  static class BuildBookmarksState {
    PdfDictionary final_child_p;
    PdfIndirectReference final_child_ref_p;
    int num_bookmarks_total;
  };

  // Translator note: returns num_bookmarks_total
  static int BuildBookmarks(
      PdfReader reader_p,
      ListIterator<PdfBookmark> it,
      PdfDictionary parent_p,
      PRIndirectReference parent_ref_p,
      int parent_level,
      boolean utf8_b) {
    int num_bookmarks_total = 0;

    PdfDictionary bookmark_prev_p = null;
    PRIndirectReference bookmark_first_ref_p = null;
    PRIndirectReference bookmark_prev_ref_p = null;
    int num_bookmarks = 0;

    PdfBookmark it_content = it.next();
    it.previous();
    if (parent_level + 1 < it_content.m_level) { // first child jumping levels

      ////
      // add missing level

      ++num_bookmarks;
      ++num_bookmarks_total;
      PdfDictionary bookmark_p = new PdfDictionary();
      PRIndirectReference bookmark_ref_p = reader_p.getPRIndirectReference(bookmark_p);
      bookmark_first_ref_p = bookmark_ref_p;

      bookmark_p.put(PdfName.PARENT, (PdfObject) parent_ref_p);

      PdfString title_p = new PdfString("");
      bookmark_p.put(PdfName.TITLE, title_p);

      bookmark_prev_p = bookmark_p;
      bookmark_prev_ref_p = bookmark_ref_p;

      // recurse in loop
    }

    for (; it.hasNext(); it.next()) {
      it_content = it.next();
      it.previous();

      if (parent_level + 1 < it_content.m_level) { // encountered child; recurse
        num_bookmarks_total +=
            BuildBookmarks(
                reader_p,
                it,
                bookmark_prev_p, // parent
                bookmark_prev_ref_p,
                parent_level + 1,
                utf8_b);
        it.previous();
        continue;
      } else if (it_content.m_level < parent_level + 1) {
        break; // no more children; add children to parent and return
      }

      ////
      // create child

      ++num_bookmarks;
      ++num_bookmarks_total;
      PdfDictionary bookmark_p = new PdfDictionary();
      PRIndirectReference bookmark_ref_p = reader_p.getPRIndirectReference(bookmark_p);
      if (bookmark_first_ref_p == null) bookmark_first_ref_p = bookmark_ref_p;

      bookmark_p.put(PdfName.PARENT, (PdfObject) parent_ref_p);

      if (bookmark_prev_ref_p != null) {
        bookmark_p.put(PdfName.PREV, (PdfObject) bookmark_prev_ref_p);
        bookmark_prev_p.put(PdfName.NEXT, (PdfObject) bookmark_ref_p);
      }

      if (utf8_b) { // UTF-8 encoded input
        bookmark_p.put(PdfName.TITLE, new PdfString(it_content.m_title /*,
                       itext::PdfObject::TEXT_UNICODE*/));
      } else { // XML entities input
        String jvs = data_import.XmlStringToJcharArray(it_content.m_title);

        bookmark_p.put(PdfName.TITLE, new PdfString(jvs /*,
                       itext::PdfObject::TEXT_UNICODE*/));
      }

      if (0 < it_content.m_page_num) { // destination
        PdfDestination dest_p = new PdfDestination(PdfDestination.FIT);
        PRIndirectReference page_ref_p = reader_p.getPageOrigRef(it_content.m_page_num);
        if (page_ref_p != null) {
          dest_p.addPage((PdfIndirectReference) page_ref_p);
        }
        bookmark_p.put(PdfName.DEST, dest_p);
      }

      bookmark_prev_p = bookmark_p;
      bookmark_prev_ref_p = bookmark_ref_p;
    }

    if (bookmark_first_ref_p != null && bookmark_prev_ref_p != null) {
      // pack these children into parent before returning
      parent_p.put(PdfName.FIRST, (PdfObject) bookmark_first_ref_p);
      parent_p.put(PdfName.LAST, (PdfObject) bookmark_prev_ref_p);
      if (parent_level == 0) {
        parent_p.put(PdfName.COUNT, new PdfNumber(num_bookmarks_total));
      } else {
        parent_p.put(PdfName.COUNT, new PdfNumber(num_bookmarks));
      }
    }

    return num_bookmarks_total;
  }

  // for use with writers, e.g. PdfCopy (esp. PdfCopy.setOutlines())
  static void BuildBookmarks(
      PdfWriter writer_p,
      ListIterator<PdfBookmark> it,
      PdfDictionary parent_p,
      PdfIndirectReference parent_ref_p,
      PdfDictionary after_child_p,
      PdfIndirectReference after_child_ref_p,
      int parent_level,
      int page_num_offset,
      int level_offset,
      boolean utf8_b,
      BuildBookmarksState state)
      throws IOException {
    // when using after_child, caller must
    // call writer_p->addToBody( after_child_p, after_child_ref_p ) upon return
    PdfDictionary bookmark_prev_p = after_child_p;
    PdfIndirectReference bookmark_prev_ref_p = after_child_ref_p;

    PdfIndirectReference bookmark_first_ref_p = null;
    int num_bookmarks = 0;

    PdfBookmark it_content = it.next();
    it.previous();
    if (parent_level + 1 < it_content.m_level) { // first child jumping levels

      ////
      // add missing level

      ++num_bookmarks;
      ++state.num_bookmarks_total;
      PdfDictionary bookmark_p = new PdfDictionary();
      PdfIndirectReference bookmark_ref_p = writer_p.getPdfIndirectReference();
      bookmark_first_ref_p = bookmark_ref_p;

      bookmark_p.put(PdfName.PARENT, (PdfObject) parent_ref_p);

      PdfString title_p = new PdfString("");
      bookmark_p.put(PdfName.TITLE, title_p);

      bookmark_prev_p = bookmark_p;
      bookmark_prev_ref_p = bookmark_ref_p;

      // recurse in loop
    }

    for (; it.hasNext(); it.next()) {
      it_content = it.next();
      it.previous();

      if (parent_level + 1 < it_content.m_level) { // encountered child; recurse
        BuildBookmarks(
            writer_p,
            it,
            bookmark_prev_p, // parent
            bookmark_prev_ref_p,
            null,
            null,
            parent_level + 1,
            page_num_offset,
            level_offset,
            utf8_b,
            state);
        it.previous();
        continue;
      } else if (it_content.m_level < parent_level + 1) {
        break; // no more children; add children to parent and return
      }

      ////
      // create child

      ++num_bookmarks;
      ++state.num_bookmarks_total;
      PdfDictionary bookmark_p = new PdfDictionary();
      PdfIndirectReference bookmark_ref_p = writer_p.getPdfIndirectReference();
      if (bookmark_first_ref_p == null) bookmark_first_ref_p = bookmark_ref_p;

      bookmark_p.put(PdfName.PARENT, (PdfObject) parent_ref_p);

      if (bookmark_prev_ref_p != null) {
        bookmark_p.put(PdfName.PREV, (PdfObject) bookmark_prev_ref_p);
        bookmark_prev_p.put(PdfName.NEXT, (PdfObject) bookmark_ref_p);
      }

      if (utf8_b) { // UTF-8 encoded input
        bookmark_p.put(PdfName.TITLE, new PdfString(it_content.m_title /*,
                       itext::PdfObject::TEXT_UNICODE*/));
      } else { // XML entities input
        String jvs = data_import.XmlStringToJcharArray(it_content.m_title);

        bookmark_p.put(PdfName.TITLE, new PdfString(jvs /*,
                       itext::PdfObject::TEXT_UNICODE*/));
      }

      if (0 < it_content.m_page_num) { // destination
        PdfDestination dest_p = new PdfDestination(PdfDestination.FIT);
        PdfIndirectReference page_ref_p =
            writer_p.getPageReference(it_content.m_page_num + page_num_offset);
        if (page_ref_p != null) {
          dest_p.addPage((PdfIndirectReference) page_ref_p);
        }
        bookmark_p.put(PdfName.DEST, dest_p);
      }

      // finished with prev; add to body
      if (bookmark_prev_p != null) writer_p.addToBody(bookmark_prev_p, bookmark_prev_ref_p);

      bookmark_prev_p = bookmark_p;
      bookmark_prev_ref_p = bookmark_ref_p;
    }

    // finished with prev; add to body (unless we're appending)
    if (bookmark_prev_p != null && after_child_p == null)
      writer_p.addToBody(bookmark_prev_p, bookmark_prev_ref_p);

    if (bookmark_first_ref_p != null && bookmark_prev_ref_p != null) {
      // pack these children into parent before returning
      if (!parent_p.contains(PdfName.FIRST)) // in case we're appending
      parent_p.put(PdfName.FIRST, (PdfObject) bookmark_first_ref_p);
      parent_p.put(PdfName.LAST, (PdfObject) bookmark_prev_ref_p);
      if (parent_level == 0) { // only for top-level "outlines" dict
        parent_p.put(PdfName.COUNT, new PdfNumber(state.num_bookmarks_total));
      } else {
        parent_p.put(PdfName.COUNT, new PdfNumber(num_bookmarks));
      }
    }

    // pass back to calling function so it can call BuildBookmarks serially
    state.final_child_p = bookmark_prev_p;
    state.final_child_ref_p = bookmark_prev_ref_p;
  }
};
