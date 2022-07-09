package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.ui.dialogs.FolderContentsPicker;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;

public class FolderContentsPickerSimpleListener implements FolderContentsPicker.Listener {
  public FolderContentsPickerSimpleListener() {}

  @Override
  public boolean isValidFolderToPick(FolderContentItem folder) {
    return true;
  }

  @Override
  public boolean isValidFileToPick(FolderContentItem file) {
    return true;
  }

  @Override
  public void onFolderChange(FolderContentItem folder) {}

  @Override
  public void onFolderPick(FolderContentItem folder) {}

  @Override
  public void onFilePick(FolderContentItem file) {}
}
