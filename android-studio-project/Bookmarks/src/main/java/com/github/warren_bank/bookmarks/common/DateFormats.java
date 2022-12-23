package com.github.warren_bank.bookmarks.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateFormats {
  public static final DateFormat NORMALIZE_DATE_TIME  = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  public static final DateFormat DISPLAY_FILE_CONTENT = DateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT);
  public static final DateFormat DISPLAY_ALARM_DATE   = DateFormat.getDateInstance(DateFormat.SHORT);
  public static final DateFormat DISPLAY_ALARM_TIME   = DateFormat.getTimeInstance(DateFormat.SHORT);

  public static String getNormalizedDateTime() {
    Date date = Calendar.getInstance().getTime();
    return DateFormats.getNormalizedDateTime(date);
  }

  public static String getNormalizedDateTime(Date date) {
    return DateFormats.NORMALIZE_DATE_TIME.format(date);
  }

  public static String getFileContentDateTime(long epoch) {
    Date date = new Date(epoch);
    return DateFormats.DISPLAY_FILE_CONTENT.format(date);
  }

  public static String getAlarmDate(long epoch) {
    Date date = new Date(epoch);
    return DateFormats.DISPLAY_ALARM_DATE.format(date);
  }

  public static String getAlarmTime(long epoch) {
    Date date = new Date(epoch);
    return DateFormats.DISPLAY_ALARM_TIME.format(date);
  }

  public static String getAlarmContentDuration(long durationMillis) {
    int day = (int)  TimeUnit.MILLISECONDS.toDays(durationMillis);
    int hrs = (int)  TimeUnit.MILLISECONDS.toHours(durationMillis)   % 24;
    int min = (int) (TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60);

    ArrayList<String> parts = new ArrayList<String>();

    if (day > 0)
      parts.add(day + " day" + ((day == 1) ? "" : "s"));
    if (hrs > 0)
      parts.add(hrs + " hr"  + ((hrs == 1) ? "" : "s"));
    if (min > 0)
      parts.add(min + " min" + ((min == 1) ? "" : "s"));

    return parts.isEmpty() ? "" : String.join(", ", parts);
  }
}
