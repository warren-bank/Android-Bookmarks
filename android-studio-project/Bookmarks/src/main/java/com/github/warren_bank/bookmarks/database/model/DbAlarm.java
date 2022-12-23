package com.github.warren_bank.bookmarks.database.model;

public class DbAlarm {

  public int  id;
  public int  intent_id;
  public long trigger_at;
  public long interval;
  public int  perform;
  public int  flags;

  private DbAlarm(int id, int intent_id, long trigger_at, long interval, int perform, int flags) {
    this.id         = id;
    this.intent_id  = intent_id;
    this.trigger_at = trigger_at;
    this.interval   = interval;
    this.perform    = perform;
    this.flags      = flags;
  }

  public static DbAlarm getInstance(int id, int intent_id, long trigger_at, long interval, int perform, int flags) {
    return new DbAlarm(id, intent_id, trigger_at, interval, perform, flags);
  }

  // -----------------------------------
  // convenience methods:
  // convert bit field flags to booleans
  // -----------------------------------

  public static boolean isFlagOn(DbAlarm dbAlarm, int flag) {
    return isFlagOn(dbAlarm.flags, flag);
  }

  public static boolean isFlagOn(int flags_bitfield, int flag) {
    int masked_bit = flags_bitfield & flag;
    return (masked_bit == flag);
  }
}
