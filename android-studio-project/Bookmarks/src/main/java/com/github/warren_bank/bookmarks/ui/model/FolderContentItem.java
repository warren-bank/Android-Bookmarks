package com.github.warren_bank.bookmarks.ui.model;

public class FolderContentItem implements Comparable {
  public int     id;
  public String  name;
  public boolean isFolder;
  public boolean isHidden;
  public String  data_uri;
  public long    lastModified;

  public FolderContentItem(int id, String name, boolean isFolder, boolean isHidden) {
    this(id, name, isFolder, isHidden, /* data_uri */ null);
  }

  public FolderContentItem(int id, String name, boolean isFolder, boolean isHidden, String data_uri) {
    this(id, name, isFolder, isHidden, data_uri, /* lastModified */ 0);
  }

  public FolderContentItem(int id, String name, boolean isFolder, boolean isHidden, String data_uri, long lastModified) {
    this.id           = id;
    this.name         = name;
    this.isFolder     = isFolder;
    this.isHidden     = isHidden;
    this.data_uri     = data_uri;
    this.lastModified = lastModified;
  }

  public boolean equals (Object obj) {
    if (!(obj instanceof FolderContentItem)) return false;

    FolderContentItem that = (FolderContentItem) obj;

    return (this.id == that.id) && (this.isFolder == that.isFolder);
  }

  @Override
  public int compareTo(Object obj) {
    if (!(obj instanceof FolderContentItem)) return -1;

    FolderContentItem that = (FolderContentItem) obj;

    if (this.isFolder && !that.isFolder) return -1;
    if (that.isFolder && !this.isFolder) return  1;

    return this.name.compareTo(that.name);
  }

  // for serialization in db.getFolderPath()
  public String toString() {
    return name;
  }
}
