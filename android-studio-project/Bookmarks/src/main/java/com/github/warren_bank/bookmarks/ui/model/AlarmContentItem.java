package com.github.warren_bank.bookmarks.ui.model;

public class AlarmContentItem implements Comparable {
  public int    id;
  public String perform;
  public String intent;
  public String time;
  public String freq;

  public AlarmContentItem(int id, String perform, String intent, String time, String freq) {
    this.id      = id;
    this.perform = perform;
    this.intent  = intent;
    this.time    = time;
    this.freq    = freq;
  }

  public boolean equals (Object obj) {
    if (!(obj instanceof AlarmContentItem)) return false;

    AlarmContentItem that = (AlarmContentItem) obj;

    return (this.id == that.id);
  }

  @Override
  public int compareTo(Object obj) {
    if (!(obj instanceof AlarmContentItem)) return -1;

    AlarmContentItem that = (AlarmContentItem) obj;

    return this.toString().compareTo(that.toString());
  }

  public String toString() {
    return perform + ": " + intent;
  }
}
