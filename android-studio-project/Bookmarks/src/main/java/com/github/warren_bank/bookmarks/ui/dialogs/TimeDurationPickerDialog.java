/*
 * Based on:
 *   https://github.com/chromium/chromium/raw/80.0.3987.137/content/public/android/java/src/org/chromium/content/browser/picker/MultiFieldTimePickerDialog.java
 *   https://github.com/chromium/chromium/raw/80.0.3987.137/content/public/android/java/src/org/chromium/content/browser/picker/InputDialogContainer.java
 *   https://github.com/chromium/chromium/raw/80.0.3987.137/content/public/android/java/res/layout/multi_field_time_picker_dialog.xml
 */

// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.github.warren_bank.bookmarks.ui.dialogs;

import com.github.warren_bank.bookmarks.R;

import android.content.Context;
import android.content.DialogInterface;

import java.util.concurrent.TimeUnit;

public class TimeDurationPickerDialog {

  /**
   * Adds an onDurationSet() method.
   */
  public interface OnDurationSetListener {
    void onDurationSet(TimeDurationPicker view, long duration);
  }

  final TimeDurationPicker picker;

  public TimeDurationPickerDialog(
      final Context context,
      final OnDurationSetListener listener,
      long duration
  ) {

    if (duration < 0l)
      duration = 0l;

    int day       = (int)  TimeUnit.MILLISECONDS.toDays(duration);
    int hourOfDay = (int)  TimeUnit.MILLISECONDS.toHours(duration)   % 24;
    int minute    = (int) (TimeUnit.MILLISECONDS.toMinutes(duration) % 60);
    int second    = (int) (TimeUnit.MILLISECONDS.toSeconds(duration) % 60);
    int millis    = (int) (TimeUnit.MILLISECONDS.toMillis(duration)  % 1000);

    long min     = 0l;
    long max     = 0l;     // use default value: 365 days
    long step    = 60000l; // 1 minute = (1000 ms/sec)(60 sec/min)

    boolean is24hourFormat        = true;
    boolean isSigned              = false;
    boolean isValueChangeListener = true;

    picker = new TimeDurationPicker(
      context,
      /* theme= */ 0,
      /* isNegative= */ false,
      day, hourOfDay, minute, second, millis,
      min, max, step, is24hourFormat, isSigned, isValueChangeListener,
      /* listener= */ null
    );

    picker.updateListener(
      new TimeDurationPicker.OnDurationSetListener() {
        @Override
        public void onDurationSet(boolean isNegative, int day, int hourOfDay, int minute, int second, int milli) {
          long duration = ((long)milli) + ((long)second * 1000l) + ((long)minute * 60l * 1000l) + ((long)hourOfDay * 60l * 60l * 1000l) + ((long)day * 24l * 60l * 60l * 1000l);

          listener.onDurationSet(picker, duration);
        }
      }
    );

    picker.setButton(
      DialogInterface.BUTTON_POSITIVE,
      context.getString(android.R.string.ok),
      (DialogInterface.OnClickListener) picker
    );

    picker.setButton(
      DialogInterface.BUTTON_NEGATIVE,
      context.getString(R.string.time_picker_dialog_reset),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          listener.onDurationSet(picker, 0l);
        }
      }
    );
  }

  public void show() {
    picker.show();
  }
}
