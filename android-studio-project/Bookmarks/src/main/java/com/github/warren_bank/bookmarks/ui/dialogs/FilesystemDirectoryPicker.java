package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.DateFormats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

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
    File dir = new File(dirPath);

    File[] tempDirList = FilesystemDirectoryPicker.dirContents(listener, dir, showFiles);
    int showParent = (dirPath.equals(Environment.getExternalStorageDirectory().getPath()) ? 0 : 1);
    File[] dirList = new File[tempDirList.length + showParent];
    String[] dirNamesList = new String[tempDirList.length + showParent];
    if (showParent == 1) {
      dirList[0] = dir.getParentFile();
      dirNamesList[0] = "..";
    }
    for(int i = 0; i < tempDirList.length; i++) {
      dirList[i + showParent] = tempDirList[i];
      dirNamesList[i + showParent] = tempDirList[i].getName();
      if (showFiles && tempDirList[i].isFile())
        dirNamesList[i + showParent] += " ("+ DateFormats.DISPLAY_FILE_PICKER.format(tempDirList[i].lastModified()) +")";
    }

    AlertDialog.Builder filePickerBuilder = new AlertDialog.Builder(context)
      .setTitle(dir.toString())
      .setItems(dirNamesList, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          File chosenFile = dirList[which];
          if (chosenFile.isDirectory()) {
            FilesystemDirectoryPicker.showFilePicker(context, listener, /* dirPath */ chosenFile.toString(), showFiles, resId_pickDirectoryPositiveButton);
          }
          else if (showFiles) {
            listener.onFilePick(chosenFile);
          }
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });

    if (!showFiles) {
      filePickerBuilder.setPositiveButton(resId_pickDirectoryPositiveButton, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          listener.onDirectoryPick(dir);
        }
      });
    }

    AlertDialog filePicker = filePickerBuilder.show();

    if (!showFiles && !listener.isValidDirectoryToPick(dir)) {
      filePicker.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }
  }

  private static File[] dirContents(FilesystemDirectoryPicker.Listener listener, File dir, final boolean showFiles)  {
    if (dir.exists() && dir.isDirectory()) {
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
