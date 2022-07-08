package com.github.warren_bank.bookmarks.utils;

import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.database.model.DbIntent;
import com.github.warren_bank.bookmarks.gson.model.GsonFolder;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.Intent;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class JsonBookmarkUtils {

  // ---------------------------------------------------------------------------
  // import
  // ---------------------------------------------------------------------------

  public static boolean importJSON(DbGateway db, File inputFile, int folderId, boolean allow_existing_folders) {
    try {
      BufferedReader reader = new BufferedReader(
        new FileReader(inputFile)
      );
      GsonFolder gsonFolder = new Gson().fromJson(reader, GsonFolder.class);
      JsonBookmarkUtils.addFolder(db, gsonFolder, folderId, allow_existing_folders);
      return true;
    }
    catch(Exception e) {
      return false;
    }
  }

  private static void addFolder(DbGateway db, GsonFolder gsonFolder, int parentId, boolean allow_existing_folders) throws Exception {
    int folderId;

    if ((gsonFolder == null) || TextUtils.isEmpty(gsonFolder.folderName))
      return;

    if (!allow_existing_folders) {
      folderId = db.getFolderId(parentId, gsonFolder.folderName);

      if (folderId >= 0)
        return;
    }

    // -------------------------------------------------------------------------
    // add folder
    // -------------------------------------------------------------------------

    // ignore the error that may occur if a folder with the same name already exists
    db.addFolder(parentId, gsonFolder.folderName);

    folderId = db.getFolderId(parentId, gsonFolder.folderName);

    // throw error if folder was not successfully saved to DB
    if (folderId < 0)
      throw new Exception("folder was not successfully saved to DB: " + gsonFolder.folderName);

    // -------------------------------------------------------------------------
    // add bookmarks
    // -------------------------------------------------------------------------

    if ((gsonFolder.bookmarks != null) && !gsonFolder.bookmarks.isEmpty()) {
      for (DbIntent dbIntent : gsonFolder.bookmarks) {
        Intent intent = db.getIntent(dbIntent);

        db.addIntent(
          folderId,
          dbIntent.name,
          intent
        );
      }
    }

    // -------------------------------------------------------------------------
    // add subfolders
    // -------------------------------------------------------------------------

    if ((gsonFolder.subfolders != null) && !gsonFolder.subfolders.isEmpty()) {
      for (GsonFolder gsonSubFolder : gsonFolder.subfolders) {
        JsonBookmarkUtils.addFolder(db, gsonSubFolder, folderId, allow_existing_folders);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // export
  // ---------------------------------------------------------------------------

  public static boolean exportJSON(DbGateway db, File outputFile, int folderId, boolean include_hidden) {
    try {
      GsonFolder gsonFolder = JsonBookmarkUtils.getFolder(db, folderId, include_hidden);
      if (gsonFolder == null)
        throw new Exception("could not serialize folder");

      BufferedWriter writer = new BufferedWriter(
        new FileWriter(outputFile)
      );

      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      gson.toJson(gsonFolder, writer);
      writer.close();
      return true;
    }
    catch(Exception e) {
      return false;
    }
  }

  private static GsonFolder getFolder(DbGateway db, int folderId, boolean include_hidden) throws Exception {
    FolderContentItem item = db.getFolderContentItem(folderId);
    if ((item == null) || !item.isFolder) return null;
    if (!include_hidden && item.isHidden) return null;

    GsonFolder gsonFolder = new GsonFolder(item.name);

    List<FolderContentItem> items = db.getFolderContentItems(folderId);

    for (int i=0; i < items.size(); i++) {
      item = items.get(i);
      if (item == null) continue;

      if (item.isFolder) {
        gsonFolder.addSubfolder(
          JsonBookmarkUtils.getFolder(db, item.id, include_hidden)
        );
      }
      else {
        gsonFolder.addBookmark(
          db.getDbIntent(item.id)
        );
      }
    }

    return gsonFolder;
  }
}
