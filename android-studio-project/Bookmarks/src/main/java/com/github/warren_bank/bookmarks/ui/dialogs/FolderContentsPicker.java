package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;
import com.github.warren_bank.bookmarks.ui.widgets.FolderContentsAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

public class FolderContentsPicker {

  public interface Implementation {
    public FolderContentItem getParentFolderContentItem(FolderContentItem folder);
    public List<FolderContentItem> getFolderContentItems(FolderContentItem folder, boolean showFiles);
  }

  public interface Listener {
    public boolean isValidFolderToPick(FolderContentItem folder);
    public boolean isValidFileToPick(FolderContentItem file);
    public void onFolderChange(FolderContentItem folder);
    public void onFolderPick(FolderContentItem folder);
    public void onFilePick(FolderContentItem file);
  }

  public static void pickFolder(Context context, FolderContentsPicker.Implementation implementation, FolderContentsPicker.Listener listener, FolderContentItem initialFolder) {
    FolderContentsPicker.showFilePicker(context, implementation, listener, initialFolder, /* showFiles */ false);
  }

  public static void pickFolder(Context context, FolderContentsPicker.Implementation implementation, FolderContentsPicker.Listener listener, FolderContentItem initialFolder, int resId_positiveButton) {
    FolderContentsPicker.showFilePicker(context, implementation, listener, initialFolder, /* showFiles */ false, resId_positiveButton);
  }

  public static void pickFile(Context context, FolderContentsPicker.Implementation implementation, FolderContentsPicker.Listener listener, FolderContentItem initialFolder) {
    FolderContentsPicker.showFilePicker(context, implementation, listener, initialFolder, /* showFiles */ true);
  }

  public static void showFilePicker(Context context, FolderContentsPicker.Implementation implementation, FolderContentsPicker.Listener listener, FolderContentItem initialFolder, boolean showFiles) {
    int resId_pickFolderPositiveButton = R.string.dialog_ok;
    FolderContentsPicker.showFilePicker(context, implementation, listener, initialFolder, showFiles, resId_pickFolderPositiveButton);
  }

  public static void showFilePicker(Context context, FolderContentsPicker.Implementation implementation, FolderContentsPicker.Listener listener, FolderContentItem folder, boolean showFiles, int resId_pickFolderPositiveButton) {
    FolderContentItem parentFolder = implementation.getParentFolderContentItem(folder);
    List<FolderContentItem> items = implementation.getFolderContentItems(folder, showFiles);
    if (parentFolder != null) {
      items.add(0, new FolderContentItem(
        /* id       */ parentFolder.id,
        /* name     */ "..",
        /* isFolder */ true,
        /* isHidden */ true
      ));
    }

    FolderContentsAdapter adapter = new FolderContentsAdapter(context, items, /* showHidden */ true);

    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
      .setTitle(folder.name)
      .setAdapter(adapter, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          FolderContentItem item = adapter.getItem(which);

          if (item.isFolder) {
            if (item.isHidden && item.name.equals(".."))
              item = parentFolder;

            listener.onFolderChange(item);
            FolderContentsPicker.showFilePicker(context, implementation, listener, /* folder */ item, showFiles, resId_pickFolderPositiveButton);
          }
          else {
            listener.onFilePick(item);
          }
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });

    if (!showFiles) {
      dialogBuilder.setPositiveButton(resId_pickFolderPositiveButton, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          listener.onFolderPick(folder);
        }
      });
    }

    AlertDialog filePicker = dialogBuilder.show();

    if (!showFiles && !listener.isValidFolderToPick(folder)) {
      filePicker.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }
  }
}
