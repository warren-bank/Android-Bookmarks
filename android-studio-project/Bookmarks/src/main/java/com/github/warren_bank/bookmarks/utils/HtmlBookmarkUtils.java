package com.github.warren_bank.bookmarks.utils;

import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.database.model.DbIntent;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;
import com.github.warren_bank.bookmarks.utils.FileUtils;

import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Stack;

public class HtmlBookmarkUtils {

  // ---------------------------------------------------------------------------
  // import
  // ---------------------------------------------------------------------------

  private static final Pattern FOLDER_NAME   = Pattern.compile("<\\s*[hH]3(?:\\s+[^>]*)?>\\s*([^<]+)\\s*</\\s*[hH]3\\s*>");
  private static final Pattern BOOKMARK_NAME = Pattern.compile("<\\s*[aA]\\s+[^>]*>\\s*([^<]+)\\s*</\\s*[aA]\\s*>");
  private static final Pattern BOOKMARK_URL  = Pattern.compile("<\\s*[A]\\s+[^>]*HREF\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
  private static final Pattern FOLDER_END    = Pattern.compile("</\\s*[dD][lL]\\s*>");

  public static boolean importHTML(DbGateway db, File inputFile, int folderId, boolean allow_existing_folders, boolean allow_duplicate_urls_global, boolean allow_duplicate_urls_folder) {
    try {
      Matcher matcher;
      String line, folderName, bookmarkName, bookmarkUrl;
      boolean is_existing_folder = false;

      Stack<Integer> folderIds = new Stack<Integer>();
      folderIds.push(folderId);

      BufferedReader reader = new BufferedReader(
        new FileReader(inputFile)
      );

      while ((line = reader.readLine()) != null) {

        // ---------------------------------------------------------------------
        // descend into subfolder
        // ---------------------------------------------------------------------

        matcher = FOLDER_NAME.matcher(line);
        if (matcher.find()) {
          folderName = matcher.group(1);
          folderName = Html.fromHtml(folderName).toString();

          if (!allow_existing_folders && !is_existing_folder) {
            folderId = db.getFolderId(folderIds.peek(), folderName);
            if (folderId >= 0) {
              is_existing_folder = true;
            }
          }

          if (is_existing_folder) {
            // use placeholder folderId to track
            folderIds.push(-1);
          }
          else {
            // ignore the error that may occur if a folder with the same name already exists
            db.addFolder(folderIds.peek(), folderName);

            folderId = db.getFolderId(folderIds.peek(), folderName);
            if (folderId < 0) return false;

            folderIds.push(folderId);
          }

          continue;
        }

        // ---------------------------------------------------------------------
        // add bookmark
        // ---------------------------------------------------------------------

        if (!is_existing_folder) {
          matcher = BOOKMARK_NAME.matcher(line);
          if (matcher.find()) {
            bookmarkName = matcher.group(1);
            bookmarkName = Html.fromHtml(bookmarkName).toString();

            matcher = BOOKMARK_URL.matcher(line);
            if (matcher.find()) {
              bookmarkUrl = matcher.group(1);

              if (!allow_duplicate_urls_global && (db.getIntentCountByDataUri(bookmarkUrl) > 0))
                continue;

              if (!allow_duplicate_urls_folder && (db.getIntentCountByDataUri(bookmarkUrl, folderIds.peek()) > 0))
                continue;

              DbIntent dbIntent = DbIntent.getInstance(
                /* id           */ -1,
                /* folder_id    */ folderIds.peek(),
                /* name         */ bookmarkName,
                /* flags        */ Intent.FLAG_ACTIVITY_NEW_TASK,
                /* action       */ Intent.ACTION_VIEW,
                /* package_name */ "",
                /* class_name   */ "",
                /* data_uri     */ bookmarkUrl,
                /* data_type    */ "text/html",
                /* categories   */ new String[0],
                /* extras       */ new DbIntent.Extra[0]
              );

              Intent intent = dbIntent.getIntent();

              db.addIntent(
                /* folderId     */ folderIds.peek(),
                /* name         */ bookmarkName,
                intent
              );

              continue;
            }
          }
        }

        // ---------------------------------------------------------------------
        // ascend to parent folder
        // ---------------------------------------------------------------------

        matcher = FOLDER_END.matcher(line);
        if (matcher.find()) {
          folderIds.pop();

          if (is_existing_folder && (folderIds.peek() >= 0)) {
            is_existing_folder = false;
          }

          continue;
        }

      }
      reader.close();
    }
    catch(Exception e) {
      return false;
    }
    return true;
  }

  // ---------------------------------------------------------------------------
  // export
  // ---------------------------------------------------------------------------

  public static boolean exportHTML(DbGateway db, File outputFile, int folderId, boolean include_hidden) {
    try {
      FolderContentItem item = db.getFolderContentItem(folderId);
      if ((item == null) || !item.isFolder) return false;
      if (!include_hidden && item.isHidden) return false;

      BufferedWriter writer = new BufferedWriter(
        new FileWriter(outputFile)
      );

      writer.write("<!DOCTYPE NETSCAPE-Bookmark-file-1>");
      writer.newLine();
      writer.write("<!-- This is an automatically generated file.");
      writer.newLine();
      writer.write("     It will be read and overwritten.");
      writer.newLine();
      writer.write("     DO NOT EDIT! -->");
      writer.newLine();
      writer.write("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">");
      writer.newLine();
      writer.write("<TITLE>Bookmarks</TITLE>");
      writer.newLine();
      writer.write("<H1>Bookmarks</H1>");
      writer.newLine();
      writer.write("<DL><p>");
      writer.newLine();
      writer.flush();

      exportHTMLFolder(db, writer, /* indent_level */ 1, folderId, /* folderName */ item.name, include_hidden);

      writer.write("</DL><p>");
      writer.newLine();
      writer.flush();
      writer.close();
    }
    catch(Exception e) {
      return false;
    }
    return true;
  }

  private static final String indent = "    ";

  private static boolean exportHTMLFolder(DbGateway db, BufferedWriter writer, int indent_level, int folderId, String folderName, boolean include_hidden) {
    try {
      String indent_folder  = new String(new char[indent_level]).replace("\0", indent);
      String indent_content = indent_folder + indent;

      List<FolderContentItem> items = db.getFolderContentItems(folderId, /* includeURL */ true);

      writer.write(indent_folder + "<DT><H3>" + TextUtils.htmlEncode(folderName) + "</H3>");
      writer.newLine();
      writer.write(indent_folder + "<DL><p>");
      writer.newLine();

      for (FolderContentItem item : items) {
        if (item.isFolder) {
          if (!include_hidden && item.isHidden)
            continue;
          else
            exportHTMLFolder(db, writer, (indent_level + 1), /* folderId */ item.id, /* folderName */ item.name, include_hidden);
        }
        else { // bookmark
          writer.write(indent_content + "<DT><A HREF=\"" + item.data_uri + "\">" + TextUtils.htmlEncode(item.name) + "</A>");
          writer.newLine();
        }
      }

      writer.write(indent_folder + "</DL><p>");
      writer.newLine();
    }
    catch(Exception e) {
      return false;
    }
    return true;
  }

}
