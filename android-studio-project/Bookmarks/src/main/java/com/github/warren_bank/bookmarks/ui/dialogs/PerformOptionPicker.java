package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

public class PerformOptionPicker {

  public interface Listener {
    public void onPerformOptionPick(int which);
  }

  public static void showPerformOptionPicker(Context context, PerformOptionPicker.Listener listener) {
    showPerformOptionPicker(context, listener, /* remove_last_option= */ false);
  }

  public static void showPerformOptionPicker(Context context, PerformOptionPicker.Listener listener, boolean remove_last_option) {
    String[] perform_options = context.getResources().getStringArray(R.array.perform_options);

    if (Build.VERSION.SDK_INT < 26) {
      // remove: perform_start_foreground_service

      String[] all_options = perform_options;
      perform_options = new String[all_options.length - 1];

      for (int i=0; i < all_options.length; i++) {
        if (i < 2) {
          perform_options[i] = all_options[i];
        }
        else if (i > 2) {
          perform_options[i-1] = all_options[i];
        }
      }
    }

    if (remove_last_option) {
      // remove: perform_add_shortcut

      String[] all_options = perform_options;
      perform_options = new String[all_options.length - 1];

      for (int i=0; i < perform_options.length; i++) {
        perform_options[i] = all_options[i];
      }
    }

    new AlertDialog.Builder(context)
      .setTitle(R.string.perform_title)
      .setItems(perform_options, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          if ((Build.VERSION.SDK_INT < 26) && (which >= 2))
            which++;

          listener.onPerformOptionPick(which);
        }
      })
      .show();
  }
}
