package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.ui.dialogs.FolderContentsPicker;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;
import com.github.warren_bank.bookmarks.utils.HashUtils;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FilesystemDirectoryPicker {

  public interface Listener {
    public boolean isValidDirectoryToPick(File dir);
    public boolean isValidFileToPick(File file);
    public void onDirectoryPick(File dir);
    public void onFilePick(File file);
  }

  public static void pickDirectory(Context context, FilesystemDirectoryPicker.Listener listener, String initialDirectoryPath) {
    FilesystemDirectoryPicker.showFilePicker(context, listener, initialDirectoryPath, /* showFiles */ false);
  }

  public static void pickDirectory(Context context, FilesystemDirectoryPicker.Listener listener, String initialDirectoryPath, int resId_positiveButton) {
    FilesystemDirectoryPicker.showFilePicker(context, listener, initialDirectoryPath, /* showFiles */ false, resId_positiveButton);
  }

  public static void pickFile(Context context, FilesystemDirectoryPicker.Listener listener, String initialDirectoryPath) {
    FilesystemDirectoryPicker.showFilePicker(context, listener, initialDirectoryPath, /* showFiles */ true);
  }

  public static void showFilePicker(Context context, FilesystemDirectoryPicker.Listener listener, String dirPath, boolean showFiles) {
    int resId_pickDirectoryPositiveButton = R.string.dialog_ok;
    FilesystemDirectoryPicker.showFilePicker(context, listener, dirPath, showFiles, resId_pickDirectoryPositiveButton);
  }

  public static void showFilePicker(Context context, FilesystemDirectoryPicker.Listener listener, String dirPath, boolean showFiles, int resId_pickDirectoryPositiveButton) {
    FolderContentItem currentFolder = FilesystemDirectoryPicker.getFolderContentItem(dirPath);
    if ((currentFolder == null) || !currentFolder.isFolder) return;

    FolderContentsPicker.Implementation fcpImplementation = new FolderContentsPicker.Implementation() {
      @Override
      public FolderContentItem getParentFolderContentItem(FolderContentItem folder) {
        if ((folder == null) || (folder.data_uri == null)) return null;

        return FilesystemDirectoryPicker.getFolderContentItem(folder.data_uri);
      }

      @Override
      public List<FolderContentItem> getFolderContentItems(FolderContentItem folder, boolean showFiles) {
        List<FolderContentItem> items = new ArrayList<FolderContentItem>();
        if (folder == null) return items;

        File dir = FilesystemDirectoryPicker.convertFolderContentItemToFile(folder);
        File[] list = FilesystemDirectoryPicker.dirContents(listener, dir, showFiles);
        for (File file : list) {
          items.add(FilesystemDirectoryPicker.convertFileToFolderContentItem(file));
        }
        return items;
      }
    };

    FolderContentsPicker.Listener fcpListener = new FolderContentsPicker.Listener() {
      @Override
      public boolean isValidFolderToPick(FolderContentItem item) {
        File dir = FilesystemDirectoryPicker.convertFolderContentItemToFile(item);
        return (dir != null)
          ? listener.isValidDirectoryToPick(dir)
          : false;
      }

      @Override
      public boolean isValidFileToPick(FolderContentItem item) {
        File file = FilesystemDirectoryPicker.convertFolderContentItemToFile(item);
        return (file != null)
          ? listener.isValidFileToPick(file)
          : false;
      }

      @Override
      public void onFolderChange(FolderContentItem item) {
      }

      @Override
      public void onFolderPick(FolderContentItem item) {
        File dir = FilesystemDirectoryPicker.convertFolderContentItemToFile(item);
        if ((dir != null) && dir.exists() && dir.isDirectory())
          listener.onDirectoryPick(dir);
      }

      @Override
      public void onFilePick(FolderContentItem item) {
        File file = FilesystemDirectoryPicker.convertFolderContentItemToFile(item);
        if ((file != null) && file.exists() && !file.isDirectory())
          listener.onFilePick(file);
      }
    };

    FolderContentsPicker.showFilePicker(context, fcpImplementation, fcpListener, currentFolder, showFiles, resId_pickDirectoryPositiveButton);
  }

  private static FolderContentItem getFolderContentItem(String filePath) {
    File file = new File(filePath);
    return FilesystemDirectoryPicker.convertFileToFolderContentItem(file);
  }

  private static FolderContentItem convertFileToFolderContentItem(File file) {
    if ((file == null) || !file.exists()) return null;

    int id = -1;
    try {
      id = HashUtils.convertToInt(
        HashUtils.SHA1(file.getAbsolutePath())
      );
    }
    catch(Exception e) {}

    return new FolderContentItem(
      /* id           */ id,
      /* name         */ file.getName(),
      /* isFolder     */ file.isDirectory(),
      /* isHidden     */ false,
      /* data_uri     */ file.getParent(),
      /* lastModified */ (file.isFile() ? file.lastModified() : 0)
    );
  }

  private static File convertFolderContentItemToFile(FolderContentItem item) {
    return (item != null)
      ? new File(item.data_uri, item.name)
      : null;
  }

  private static File[] dirContents(FilesystemDirectoryPicker.Listener listener, File dir, final boolean showFiles)  {
    if ((dir != null) && dir.exists() && dir.isDirectory()) {
      FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
          File file = new File(dir.getAbsolutePath() + File.separator + filename);
          if (showFiles)
            return file.isDirectory() || listener.isValidFileToPick(file);
          else
            return file.isDirectory();
        }
      };
      File[] list = dir.listFiles(filter);
      if (list != null)
        Arrays.sort(list, FilesystemDirectoryPicker.filesComperator);
      return list == null ? new File[0] : list;
    }
    else {
      return new File[0];
    }
  }

  private static Comparator<File> filesComperator = new Comparator<File>() {
    public int compare(File f1, File f2) {
      if (f1.isDirectory() && !f2.isDirectory())
        return -1;
      if (f2.isDirectory() && !f1.isDirectory())
        return 1;
      return f1.getName().compareToIgnoreCase(f2.getName());
    }
  };

}
