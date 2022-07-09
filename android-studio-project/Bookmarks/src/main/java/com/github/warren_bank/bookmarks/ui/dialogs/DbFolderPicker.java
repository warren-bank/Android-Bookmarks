package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.ui.dialogs.FolderContentsPicker;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;

import android.content.Context;

import java.util.List;

public class DbFolderPicker {

  public interface Listener extends FolderContentsPicker.Listener {
  }

  public static void pickFolder(Context context, FolderContentsPicker.Listener listener, int initialFolderId) {
    DbFolderPicker.showFilePicker(context, listener, initialFolderId, /* showFiles */ false);
  }

  public static void pickFolder(Context context, FolderContentsPicker.Listener listener, int initialFolderId, int resId_positiveButton) {
    DbFolderPicker.showFilePicker(context, listener, initialFolderId, /* showFiles */ false, resId_positiveButton);
  }

  public static void pickFile(Context context, FolderContentsPicker.Listener listener, int initialFolderId) {
    DbFolderPicker.showFilePicker(context, listener, initialFolderId, /* showFiles */ true);
  }

  public static void showFilePicker(Context context, FolderContentsPicker.Listener listener, int initialFolderId, boolean showFiles) {
    int resId_pickFolderPositiveButton = R.string.dialog_ok;
    DbFolderPicker.showFilePicker(context, listener, initialFolderId, showFiles, resId_pickFolderPositiveButton);
  }

  public static void showFilePicker(Context context, FolderContentsPicker.Listener listener, int folderId, boolean showFiles, int resId_pickFolderPositiveButton) {
    DbGateway db = DbGateway.getInstance(context);

    FolderContentItem currentFolder = db.getFolderContentItem(folderId);
    if ((currentFolder == null) || !currentFolder.isFolder) return;

    FolderContentsPicker.Implementation fcpImplementation = new FolderContentsPicker.Implementation() {
      @Override
      public FolderContentItem getParentFolderContentItem(FolderContentItem folder) {
        if ((folder == null) || (folder.id <= 0)) return null;

        return db.getParentFolderContentItem(folder.id);
      }

      @Override
      public List<FolderContentItem> getFolderContentItems(FolderContentItem folder, boolean showFiles) {
        return showFiles
          ? db.getFolderContentItems(folder.id)
          : db.getFoldersInFolder(folder.id);
      }
    };

    FolderContentsPicker.showFilePicker(context, fcpImplementation, listener, currentFolder, showFiles, resId_pickFolderPositiveButton);
  }

}
