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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A time picker dialog with upto 6 number pickers left to right:
 *  day, hour, minute, second, milli, AM/PM.
 *
 * If is24hourFormat is true then AM/PM picker is not displayed and
 * hour range is 0..23. Otherwise hour range is 1..12.
 * The milli picker is not displayed if step >= SECOND_IN_MILLIS
 * The second picker is not displayed if step >= MINUTE_IN_MILLIS.
 */
public class TimeDurationPicker extends AlertDialog implements DialogInterface.OnClickListener, NumberPicker.OnValueChangeListener {

  private final NumberPicker mSignSpinner;
  private final NumberPicker mDaySpinner;
  private final NumberPicker mHourSpinner;
  private final NumberPicker mMinuteSpinner;
  private final NumberPicker mSecSpinner;
  private final NumberPicker mMilliSpinner;
  private final NumberPicker mAmPmSpinner;
  private final long mStep;
  private final long mBaseMilli;
  private final boolean mIs24hourFormat;
  private final boolean mIsSigned;
  private final boolean mIsValueChangeListener;

  private OnDurationSetListener mListener;

  /**
   * Adds an onDurationSet() method.
   */
  public interface OnDurationSetListener {
    void onDurationSet(boolean isNegative, int day, int hourOfDay, int minute, int second, int milli);
  }

  private static final long SECOND_IN_MILLIS = 1000l;
  private static final long MINUTE_IN_MILLIS = 60l * SECOND_IN_MILLIS;
  private static final long HOUR_IN_MILLIS   = 60l * MINUTE_IN_MILLIS;
  private static final long DAY_IN_MILLIS    = 24l * HOUR_IN_MILLIS;

  public TimeDurationPicker(
      Context context,
      int theme,
      boolean isNegative,
      int day, int hour, int minute, int second, int milli,
      long min, long max, long step, boolean is24hourFormat, boolean isSigned, boolean isValueChangeListener,
      OnDurationSetListener listener) {
    super(context, theme);
    mListener = listener;
    mStep = step;
    mIs24hourFormat = is24hourFormat;
    mIsSigned = isSigned;
    mIsValueChangeListener = isValueChangeListener;

    if (min >= max) {
      min = 0l;
      max = 365l * DAY_IN_MILLIS - 1l;
    }
    if (step < 0l || step >= 365l * DAY_IN_MILLIS) {
      step = MINUTE_IN_MILLIS;
    }

    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.dialog_pick_time_duration, null);
    setView(view);

    mSignSpinner   = (NumberPicker) view.findViewById(R.id.sign);
    mDaySpinner    = (NumberPicker) view.findViewById(R.id.day);
    mHourSpinner   = (NumberPicker) view.findViewById(R.id.hour);
    mMinuteSpinner = (NumberPicker) view.findViewById(R.id.minute);
    mSecSpinner    = (NumberPicker) view.findViewById(R.id.second);
    mMilliSpinner  = (NumberPicker) view.findViewById(R.id.milli);
    mAmPmSpinner   = (NumberPicker) view.findViewById(R.id.ampm);

    if (isValueChangeListener) {
      mSignSpinner.setOnValueChangedListener(this);
      mDaySpinner.setOnValueChangedListener(this);
      mHourSpinner.setOnValueChangedListener(this);
      mMinuteSpinner.setOnValueChangedListener(this);
      mSecSpinner.setOnValueChangedListener(this);
      mMilliSpinner.setOnValueChangedListener(this);
      mAmPmSpinner.setOnValueChangedListener(this);
    }

    int minDay = (int) (min / DAY_IN_MILLIS);
    int maxDay = (int) (max / DAY_IN_MILLIS);
    min -= minDay * DAY_IN_MILLIS;
    max -= maxDay * DAY_IN_MILLIS;

    if (minDay == maxDay) {
      mDaySpinner.setEnabled(false);
      day = minDay;
    } else {
      mDaySpinner.setMinValue(minDay);
      mDaySpinner.setMaxValue(maxDay);
    }
    mDaySpinner.setValue(day);

    int minHour = (int) (min / HOUR_IN_MILLIS);
    int maxHour = (int) (max / HOUR_IN_MILLIS);
    min -= minHour * HOUR_IN_MILLIS;
    max -= maxHour * HOUR_IN_MILLIS;

    if ((minDay == maxDay) && (minHour == maxHour)) {
      mHourSpinner.setEnabled(false);
      hour = minHour;
    }

    if (!isSigned) {
      mSignSpinner.setVisibility(View.GONE);
      view.findViewById(R.id.sign_day_sep).setVisibility(View.GONE);
    } else {
      int minSign = 0; // + positive
      int maxSign = 1; // - negative
      int iniSign = isNegative ? maxSign : minSign;
      mSignSpinner.setMinValue(minSign);
      mSignSpinner.setMaxValue(maxSign);
      mSignSpinner.setDisplayedValues(new String[] { "+", "-" });
      mSignSpinner.setValue(iniSign);
    }

    if (is24hourFormat) {
      view.findViewById(R.id.milli_ampm_sep).setVisibility(View.GONE);
      mAmPmSpinner.setVisibility(View.GONE);
    } else {
      int minAmPm = minHour / 12;
      int maxAmPm = maxHour / 12;
      int amPm = hour / 12;
      mAmPmSpinner.setMinValue(minAmPm);
      mAmPmSpinner.setMaxValue(maxAmPm);
      mAmPmSpinner.setDisplayedValues(new String[] {
          context.getString(R.string.time_picker_dialog_am),
          context.getString(R.string.time_picker_dialog_pm)
      });

      hour %= 12;
      if (hour == 0) {
        hour = 12;
      }
      if (minAmPm == maxAmPm) {
        mAmPmSpinner.setEnabled(false);
        amPm = minAmPm;

        minHour %= 12;
        maxHour %= 12;
        if (minHour == 0 && maxHour == 0) {
          minHour = 12;
          maxHour = 12;
        } else if (minHour == 0) {
          minHour = maxHour;
          maxHour = 12;
        } else if (maxHour == 0) {
          maxHour = 12;
        }
      } else {
        minHour = 1;
        maxHour = 12;
      }
      mAmPmSpinner.setValue(amPm);
    }

    if ((minDay == maxDay) && (minHour == maxHour)) {
      mHourSpinner.setEnabled(false);
    } else {
      if (minDay != maxDay) {
        minHour = 0;
        maxHour = is24hourFormat ? 23 : 11;
      }
      mHourSpinner.setMinValue(minHour);
      mHourSpinner.setMaxValue(maxHour);
      mHourSpinner.setValue(hour);
    }

    NumberFormatter twoDigitPaddingFormatter = new NumberFormatter("%02d");

    int minMinute = (int) (min / MINUTE_IN_MILLIS);
    int maxMinute = (int) (max / MINUTE_IN_MILLIS);
    min -= minMinute * MINUTE_IN_MILLIS;
    max -= maxMinute * MINUTE_IN_MILLIS;

    if ((minDay == maxDay) && (minHour == maxHour)) {
      mMinuteSpinner.setMinValue(minMinute);
      mMinuteSpinner.setMaxValue(maxMinute);
      if (minMinute == maxMinute) {
        // Set this otherwise the box is empty until you stroke it.
        mMinuteSpinner.setDisplayedValues(
          new String[] { twoDigitPaddingFormatter.format(minMinute) }
        );
        mMinuteSpinner.setEnabled(false);
        minute = minMinute;
      }
    } else {
      mMinuteSpinner.setMinValue(0);
      mMinuteSpinner.setMaxValue(59);
    }

    mMinuteSpinner.setValue(minute);
    if (step % HOUR_IN_MILLIS == 0l) {
      mMinuteSpinner.setEnabled(false);
      // TODO(tkent): We should set minutes value of
      // WebDateTimeChooserParams::stepBase.
      mMinuteSpinner.setValue(minMinute);
    }

    mMinuteSpinner.setFormatter(twoDigitPaddingFormatter);

    if (step >= MINUTE_IN_MILLIS) {
      // Remove the ':' in front of the second spinner as well.
      view.findViewById(R.id.minute_second_sep).setVisibility(View.GONE);
      mSecSpinner.setVisibility(View.GONE);
      view.findViewById(R.id.second_label).setVisibility(View.GONE);
    }

    int minSecond = (int) (min / SECOND_IN_MILLIS);
    int maxSecond = (int) (max / SECOND_IN_MILLIS);
    min -= minSecond * SECOND_IN_MILLIS;
    max -= maxSecond * SECOND_IN_MILLIS;

    if (minHour == maxHour && minMinute == maxMinute) {
      mSecSpinner.setMinValue(minSecond);
      mSecSpinner.setMaxValue(maxSecond);
      if (minSecond == maxSecond) {
        // Set this otherwise the box is empty until you stroke it.
        mSecSpinner.setDisplayedValues(
          new String[] { twoDigitPaddingFormatter.format(minSecond) }
        );
        mSecSpinner.setEnabled(false);
        second = minSecond;
      }
    } else {
      mSecSpinner.setMinValue(0);
      mSecSpinner.setMaxValue(59);
    }

    mSecSpinner.setValue(second);
    mSecSpinner.setFormatter(twoDigitPaddingFormatter);

    if (step >= SECOND_IN_MILLIS) {
      // Remove the '.' in front of the milli spinner as well.
      view.findViewById(R.id.second_milli_sep).setVisibility(View.GONE);
      mMilliSpinner.setVisibility(View.GONE);
      view.findViewById(R.id.milli_label).setVisibility(View.GONE);
    }

    // Round to the nearest step.
    milli = (int) (((milli + step / 2) / step) * step);
    if (step == 1l || step == 10l || step == 100l) {
      if ((minHour == maxHour) && (minMinute == maxMinute) && (minSecond == maxSecond)) {
        mMilliSpinner.setMinValue((int) (min / step));
        mMilliSpinner.setMaxValue((int) (max / step));

        if (min == max) {
          mMilliSpinner.setEnabled(false);
          milli = (int) min;
        }
      } else {
        mMilliSpinner.setMinValue(0);
        mMilliSpinner.setMaxValue((int) (999 / step));
      }

      if (step == 1l) {
        mMilliSpinner.setFormatter(new NumberFormatter("%03d"));
      } else if (step == 10l) {
        mMilliSpinner.setFormatter(new NumberFormatter("%02d"));
      } else if (step == 100l) {
        mMilliSpinner.setFormatter(new NumberFormatter("%d"));
      }
      mMilliSpinner.setValue((int) (milli / step));
      mBaseMilli = 0l;
    } else if (step < SECOND_IN_MILLIS) {
      // Non-decimal step value.
      ArrayList<String> strValue = new ArrayList<String>();
      for (long i = min; i < max; i += step) {
        strValue.add(String.format(Locale.getDefault(), "%03d", i));
      }
      mMilliSpinner.setMinValue(0);
      mMilliSpinner.setMaxValue(strValue.size() - 1);
      mMilliSpinner.setValue((int) (((long)milli - min) / step));
      mMilliSpinner.setDisplayedValues(strValue.toArray(new String[strValue.size()]));
      mBaseMilli = min;
    } else {
      mBaseMilli = 0l;
    }
  }

  public void updateListener(OnDurationSetListener listener) {
    mListener = listener;
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    notifyDateSet();
  }

  @Override
  public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    if (mIsValueChangeListener)
      notifyDateSet();
  }

  private void notifyDateSet() {
    int day    = getPickerValue(mDaySpinner);
    int hour   = getPickerValue(mHourSpinner);
    int minute = getPickerValue(mMinuteSpinner);
    int sec    = getPickerValue(mSecSpinner);
    int milli  = (int) (getPickerValue(mMilliSpinner) * mStep + mBaseMilli);
    if (!mIs24hourFormat) {
      int ampm = getPickerValue(mAmPmSpinner);
      if (hour == 12) {
        hour = 0;
      }
      hour += ampm * 12;
    }
    boolean isNegative = false;
    if (mIsSigned) {
      int sign = getPickerValue(mSignSpinner);
      if (sign == 1)
      isNegative = true;
    }
    mListener.onDurationSet(isNegative, day, hour, minute, sec, milli);
  }

  /**
   * Clear focus before retrieving so that values inserted with
   * keyboard are taken into account.
  */
  private int getPickerValue(NumberPicker picker) {
    picker.clearFocus();
    return picker.getValue();
  }

  private static class NumberFormatter implements NumberPicker.Formatter {
    private final String mFormat;

    NumberFormatter(String format) {
      mFormat = format;
    }

    @Override
    public String format(int value) {
      return String.format(Locale.getDefault(), mFormat, value);
    }
  }
}
