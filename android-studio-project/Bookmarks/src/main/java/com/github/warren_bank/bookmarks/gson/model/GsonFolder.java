package com.github.warren_bank.bookmarks.gson.model;

import com.github.warren_bank.bookmarks.database.model.DbIntent;

import java.util.ArrayList;
import java.util.List;

public class GsonFolder {
  public String folderName;
  public List<DbIntent> bookmarks;
  public List<GsonFolder> subfolders;

  public GsonFolder(String folderName) {
    this.folderName = folderName;

    bookmarks  = new ArrayList<DbIntent>();
    subfolders = new ArrayList<GsonFolder>();
  }

  public void addBookmark(DbIntent dbIntent) {
    if (dbIntent != null)
      bookmarks.add(dbIntent);
  }

  public void addSubfolder(GsonFolder gsonFolder) {
    if (gsonFolder != null)
      subfolders.add(gsonFolder);
  }
}
