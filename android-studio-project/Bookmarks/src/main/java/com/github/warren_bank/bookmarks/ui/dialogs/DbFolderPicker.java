package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

public class DbFolderPicker {

  public interface Listener {
    public boolean isValidFolderToPick(FolderContentItem folder);
    public void onFolderPick(FolderContentItem folder);
  }

  public static void pickFolder(Context context, DbFolderPicker.Listener listener, int initialFolderId) {
    DbFolderPicker.showFilePicker(context, listener, initialFolderId);
  }

  public static void pickFolder(Context context, DbFolderPicker.Listener listener, int initialFolderId, int resId_positiveButton) {
    DbFolderPicker.showFilePicker(context, listener, initialFolderId, resId_positiveButton);
  }

  public static void showFilePicker(Context context, DbFolderPicker.Listener listener, int folderId) {
    int resId_pickFolderPositiveButton = R.string.dialog_ok;
    DbFolderPicker.showFilePicker(context, listener, folderId, resId_pickFolderPositiveButton);
  }

  public static void showFilePicker(Context context, DbFolderPicker.Listener listener, int folderId, int resId_pickFolderPositiveButton) {
    DbGateway db = DbGateway.getInstance(context);

    FolderContentItem currentFolder = db.getFolderContentItem(folderId);
    List<FolderContentItem> currentFolderContentItems = db.getFoldersInFolder(folderId);

    if (folderId > 0) {
      FolderContentItem parentFolderContentItem = db.getParentFolderContentItem(folderId);
      parentFolderContentItem.name = "..";
      currentFolderContentItems.add(0, parentFolderContentItem);
    }

    String[] folderNames = new String[currentFolderContentItems.size()];
    for (int i=0; i < currentFolderContentItems.size(); i++) {
      folderNames[i] = currentFolderContentItems.get(i).name;
    }

    AlertDialog.Builder filePickerBuilder = new AlertDialog.Builder(context)
      .setTitle(currentFolder.name)
      .setItems(folderNames, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          FolderContentItem chosenFolder = currentFolderContentItems.get(which);
          DbFolderPicker.showFilePicker(context, listener, /* folderId */ chosenFolder.id, resId_pickFolderPositiveButton);
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });

    filePickerBuilder.setPositiveButton(resId_pickFolderPositiveButton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        listener.onFolderPick(currentFolder);
      }
    });

    AlertDialog filePicker = filePickerBuilder.show();

    if (!listener.isValidFolderToPick(currentFolder)) {
      filePicker.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }
  }

}
