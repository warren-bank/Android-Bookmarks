package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.ui.dialogs.FilesystemDirectoryPicker;

import java.io.File;

public class FilesystemDirectoryPickerSimpleListener implements FilesystemDirectoryPicker.Listener {
  public FilesystemDirectoryPickerSimpleListener() {}

  @Override
  public boolean isValidDirectoryToPick(File dir) {
    return true;
  }

  @Override
  public boolean isValidFileToPick(File file) {
    return true;
  }

  @Override
  public void onDirectoryPick(File dir) {}

  @Override
  public void onFilePick(File file) {}
}
