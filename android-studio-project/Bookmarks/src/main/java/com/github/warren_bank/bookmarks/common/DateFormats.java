package com.github.warren_bank.bookmarks.common;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

  public static String formatDuration(long durationMillis, String format, boolean padWithZeros) {
    return DurationFormatUtils.formatDuration(durationMillis, format, padWithZeros);
  }

  public static String getAlarmContentDuration(long durationMillis) {
    String format = "MM' months, 'dd' days, 'HH' hrs, 'mm' mins, 'ss' secs'";
    boolean padWithZeros = false;
    return formatDuration(durationMillis, format, padWithZeros);
  }
}
