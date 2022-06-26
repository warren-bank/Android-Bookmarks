package com.github.warren_bank.bookmarks.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormats {
  public static final DateFormat NORMALIZE_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  public static final DateFormat DISPLAY_FILE_PICKER = DateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT);

  public static String getNormalizedDateTime() {
    Date date = Calendar.getInstance().getTime();
    return DateFormats.getNormalizedDateTime(date);
  }

  public static String getNormalizedDateTime(Date date) {
    return DateFormats.NORMALIZE_DATE_TIME.format(date);
  }
}
