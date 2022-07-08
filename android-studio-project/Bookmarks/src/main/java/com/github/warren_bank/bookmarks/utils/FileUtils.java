package com.github.warren_bank.bookmarks.utils;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.DateFormats;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtils {

  // --------------------------------------------------------------------------- common:

  public static File getFile(String path) {
    if (TextUtils.isEmpty(path)) return null;

    File file = new File(path);
    return file;
  }

  public static String getFileName(String path) {
    String fileName;
    File file = FileUtils.getFile(path);
    fileName = (file != null) ? file.getName() : "";
    fileName = !TextUtils.isEmpty(fileName) ? fileName : "";
    fileName = fileName.replace("/", "");
    return fileName;
  }

  public static String getFileName(URL url) {
    String path     = (url == null) ? null : url.getPath();
    String fileName = FileUtils.getFileName(path);
    return fileName;
  }

  public static String getFileName(String fileName, String fileExtension, boolean auto, boolean isPreUpdate, boolean isPostUpdate, int oldVersion) {
    StringBuilder sb = new StringBuilder(64);
    sb.append(fileName);
    if (!auto) {
      sb.append("." + DateFormats.getNormalizedDateTime());
    }
    if (isPreUpdate && !isPostUpdate) {
      sb.append(".preupdate");
      if (oldVersion > 0) {
        sb.append("-v" + oldVersion);
      }
    }
    if (!isPreUpdate && isPostUpdate) {
      sb.append(".update");
      if (oldVersion > 0) {
        sb.append("-v" + oldVersion);
      }
    }
    sb.append("." + fileExtension);
    return sb.toString();
  }

  public static boolean exists(String path) {
    File file = FileUtils.getFile(path);
    return ((file != null) && file.exists());
  }

  // --------------------------------------------------------------------------- test paths: for HTML Bookmarks

  private static Pattern htmlFileNameRegex = null;

  public static boolean isHtmlBookmarksFile(File file) {
    boolean result = false;
    try {
      if (FileUtils.htmlFileNameRegex == null) {
        FileUtils.htmlFileNameRegex = Pattern.compile("^.*bookmarks.*\\.htm[l]?(?:[\\.\\-].*)?$", Pattern.CASE_INSENSITIVE);
      }

      result = (file != null) && file.exists() && file.isFile() && FileUtils.htmlFileNameRegex.matcher(file.getName()).matches();
    }
    catch(Exception e) {}
    return result;
  }

  // --------------------------------------------------------------------------- test paths: for JSON Bookmarks

  private static Pattern jsonFileNameRegex = null;

  public static boolean isJsonBookmarksFile(File file) {
    boolean result = false;
    try {
      if (FileUtils.jsonFileNameRegex == null) {
        FileUtils.jsonFileNameRegex = Pattern.compile("^.*\\.json(?:[\\.\\-].*)?$", Pattern.CASE_INSENSITIVE);
      }

      result = (file != null) && file.exists() && file.isFile() && FileUtils.jsonFileNameRegex.matcher(file.getName()).matches();
    }
    catch(Exception e) {}
    return result;
  }

  // --------------------------------------------------------------------------- test paths: for DB

  private static Pattern dbFileNameRegex = null;

  public static boolean isDatabaseFile(File file) {
    boolean result = false;
    try {
      if (FileUtils.dbFileNameRegex == null) {
        FileUtils.dbFileNameRegex = Pattern.compile("^.*bookmarks.*\\.db(?:[\\.\\-].*)?$", Pattern.CASE_INSENSITIVE);
      }

      result = (file != null) && file.exists() && file.isFile() && FileUtils.dbFileNameRegex.matcher(file.getName()).matches();
    }
    catch(Exception e) {}
    return result;
  }

  // --------------------------------------------------------------------------- get paths: for export of HTML Bookmarks

  public static String getHtmlBookmarksFileName(Context context) {
    context = context.getApplicationContext();
    String fileName = context.getString(R.string.netscape_html_export_file_name);
    return FileUtils.getFileName(fileName, /* fileExtension */ "html", /* auto */ false, /* isPreUpdate */ false, /* isPostUpdate */ false, /* oldVersion */ -1);
  }

  // --------------------------------------------------------------------------- get paths: for export of JSON

  public static String getJsonBookmarksFileName(Context context) {
    context = context.getApplicationContext();
    String fileName = context.getString(R.string.json_export_file_name);
    return FileUtils.getFileName(fileName, /* fileExtension */ "json", /* auto */ false, /* isPreUpdate */ false, /* isPostUpdate */ false, /* oldVersion */ -1);
  }

  // --------------------------------------------------------------------------- get paths: for DB

  public static String getDatabaseFileName(Context context) {
    boolean auto = true;
    return FileUtils.getDatabaseFileName(context, auto);
  }

  public static String getDatabaseFileName(Context context, boolean auto) {
    boolean isPreUpdate = false;
    int     oldVersion  = -1;
    return FileUtils.getDatabaseFileName(context, auto, isPreUpdate, oldVersion);
  }

  public static String getDatabaseFileName(Context context, boolean auto, boolean isPreUpdate, int oldVersion) {
    context = context.getApplicationContext();
    String fileName = context.getString(R.string.database_file_name);
    return FileUtils.getFileName(fileName, /* fileExtension */ "db", auto, isPreUpdate, /* isPostUpdate */ false, oldVersion);
  }

  public static String getDatabaseFileNameForBackupVersion(String fileName, int backupVersion) {
    return fileName + backupVersion; // .db0, .db1, etc
  }

  public static String getDatabaseDirectoryPath(Context context) {
    context = context.getApplicationContext();
    String dirPath = context.getApplicationInfo().dataDir + "/" + context.getString(R.string.databases_directory_name);
    return dirPath;
  }

  public static String getDatabaseFilePath(Context context) {
    String dirPath  = FileUtils.getDatabaseDirectoryPath(context);
    String fileName = FileUtils.getDatabaseFileName(context);

    return dirPath + "/" + fileName;
  }

  // --------------------------------------------------------------------------- copy: generic file

  public static void copyFile(File source, File destination) throws IOException {
    if (source == null)
      throw new IOException("Source file is null.");

    if (destination == null)
      throw new IOException("Destination file is null.");

    if (source.equals(destination))
      throw new IOException("Source and Destination files refer to the same path.");

    FileChannel sourceCh = null, destinationCh = null;
    try {
      sourceCh = new FileInputStream(source).getChannel();
      if (destination.exists()) destination.delete();
      destination.createNewFile();
      destinationCh = new FileOutputStream(destination).getChannel();
      destinationCh.transferFrom(sourceCh, 0, sourceCh.size());
      destination.setLastModified(source.lastModified());
    }
    finally {
      if (sourceCh != null) {
        sourceCh.close();
      }
      if (destinationCh != null) {
        destinationCh.close();
      }
    }
  }

  // --------------------------------------------------------------------------- deletion: generic files and directories

  public static boolean deleteFile(String filePath) {
    File file = FileUtils.getFile(filePath);
    return ((file != null) && file.exists() && file.delete());
  }

  public static boolean deleteDirectoryContents(String dirPath, boolean useRecursion) {
    List<String> keepFileNames = null;
    return FileUtils.deleteDirectoryContents(dirPath, useRecursion, keepFileNames);
  }

  public static boolean deleteDirectoryContents(String dirPath, boolean useRecursion, List<String> keepFileNames) {
    File dir = FileUtils.getFile(dirPath);
    return ((dir != null) && dir.exists())
      ? FileUtils.deleteDirectoryContents(dir, useRecursion, keepFileNames)
      : false;
  }

  // delete contents of a directory with optional recursion
  public static boolean deleteDirectoryContents(File dir, boolean useRecursion, List<String> keepFileNames) {
    if (!dir.exists() || !dir.isDirectory()) return false;

    File[] files    = dir.listFiles();
    boolean success = true;
    boolean keep, is_empty;

    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          keep = (keepFileNames != null) && keepFileNames.contains(file.getName());

          if (!keep)
            success &= file.delete();
        }
        else if (file.isDirectory() && useRecursion) {
          success &= FileUtils.deleteDirectoryContents(file, useRecursion, keepFileNames);

          File[] children = file.listFiles();
          is_empty = (children == null) || (children.length == 0);

          if (is_empty)
            success &= file.delete();
        }
      }
    }
    return success;
  }

  // --------------------------------------------------------------------------- deletion: for DB

  public static boolean cleanDatabaseDirectory(Context context) {
    String dirPath             = FileUtils.getDatabaseDirectoryPath(context);
    String fileName            = FileUtils.getDatabaseFileName(context);
    boolean useRecursion       = false;
    List<String> keepFileNames = new ArrayList<String>();
    keepFileNames.add(fileName);

    return FileUtils.deleteDirectoryContents(dirPath, useRecursion, keepFileNames);
  }

  // --------------------------------------------------------------------------- output: generic

  public static boolean writeToFile(String content, File file) {
    boolean append = false;
    return FileUtils.writeToFile(content, file, append);
  }

  public static boolean writeToFile(String content, File file, boolean append) {
    boolean result = true;

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, append);
      fos.write(content.getBytes());
    }
    catch(Exception e) {
      result = false;
    }

    try {
      if (fos != null) fos.close();
    }
    catch(Exception e) {}

    return result;
  }

  // ---------------------------------------------------------------------------

}
