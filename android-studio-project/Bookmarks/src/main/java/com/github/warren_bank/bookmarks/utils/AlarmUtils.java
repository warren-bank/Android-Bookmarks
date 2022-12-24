package com.github.warren_bank.bookmarks.utils;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.database.model.DbAlarm;
import com.github.warren_bank.bookmarks.receiver.AlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import java.util.List;

public class AlarmUtils {

  public static void schedule(Context context, DbAlarm dbAlarm) {
    AlarmManager am     = getAlarmManager(context);
    Resources resources = context.getResources();

    schedule(context, dbAlarm, am, resources, /* db= */ null, /* force_run_when_missed= */ false);
  }

  public static void rescheduleAll(Context context) {
    AlarmManager am        = getAlarmManager(context);
    Resources resources    = context.getResources();
    DbGateway db           = DbGateway.getInstance(context);
    List<DbAlarm> dbAlarms = db.getDbAlarms();

    for (DbAlarm dbAlarm : dbAlarms) {
      schedule(context, dbAlarm, am, resources, db, /* force_run_when_missed= */ false);
    }
  }

  public static void execute(Context context, int alarmId) {
    DbGateway db    = DbGateway.getInstance(context);
    DbAlarm dbAlarm = db.getDbAlarm(alarmId);

    if (dbAlarm == null) return;

    /*
     * this method is only called by the broadcast receiver,
     * to trigger the execution of the Intent for an alarm that has already been executed.
     * 
     * rather than calling directly:
     *   execute(context, dbAlarm, db);
     *
     * delegate execution of the alarm:
     *   - to the internal scheduler,
     *     so the alarm is rescheduled in the same way
     *     as would occur at reboot
     *   - the advantage is that
     *     the rescheduling of repeating alarms
     *     is handled in one place
     *
     * the parameter: 'force_run_when_missed'
     *   - has the same immediate effect as would:
     *       dbAlarm.flags |= resources.getInteger(R.integer.flag_alarm_run_when_missed);
     *   - but does not permanently change the flags
     *     belonging to a repeating alarm,
     *     which would override the option chosen by the user
     *
     * 'trigger_at' should always be < now.
     * if, however, this is ever not the case,
     * then the alarm is simply rescheduled
     * to trigger at a time in the future.
     */

    AlarmManager am     = getAlarmManager(context);
    Resources resources = context.getResources();

    schedule(context, dbAlarm, am, resources, db, /* force_run_when_missed= */ true);
  }

  public static void cancel(Context context, int alarmId) {
    AlarmManager am = getAlarmManager(context);

    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.putExtra(Constants.EXTRA_ALARM_ID, alarmId);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, /* requestCode= */ alarmId, intent, PendingIntent.FLAG_IMMUTABLE);

    am.cancel(pendingIntent);
  }

  protected static AlarmManager getAlarmManager(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    return am;
  }

  private static void schedule(Context context, DbAlarm dbAlarm, AlarmManager am, Resources resources, DbGateway db, boolean force_run_when_missed) {
    if (am == null)
      am = getAlarmManager(context);
    if (resources == null)
      resources = context.getResources();

    boolean is_repeating    = (dbAlarm.interval > 0l);
    boolean is_exact        = DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_is_exact));
    boolean run_when_idle   = DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_run_when_idle));
    boolean wake_when_idle  = DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_wake_when_idle));
    boolean run_when_missed = DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_run_when_missed)) || force_run_when_missed;

    long now             = System.currentTimeMillis();
    long triggerAtMillis = dbAlarm.trigger_at;

    if (triggerAtMillis < now) {
      if (db == null) {
        db = DbGateway.getInstance(context);
      }
      if (run_when_missed) {
        execute(context, dbAlarm, db);
      }
      if (is_repeating) {
        while (triggerAtMillis < now) {
          triggerAtMillis += dbAlarm.interval;
        }
        dbAlarm.trigger_at = triggerAtMillis;
        db.updateAlarm(dbAlarm);
      }
      else {
        db.deleteAlarm(dbAlarm.id);
        return;
      }
    }

    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.putExtra(Constants.EXTRA_ALARM_ID, dbAlarm.id);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, /* requestCode= */ dbAlarm.id, intent, PendingIntent.FLAG_IMMUTABLE);
    int type                    = wake_when_idle ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC;
    boolean is_set              = false;

    if (!is_set && (Build.VERSION.SDK_INT >= 23)) {
      if (run_when_idle) {
        if (is_exact)
          am.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent);
        else
          am.setAndAllowWhileIdle(type, triggerAtMillis, pendingIntent);

        is_set = true;
      }
    }
    if (!is_set && (Build.VERSION.SDK_INT >= 19)) {
      if (is_exact)
        am.setExact(type, triggerAtMillis, pendingIntent);
      else
        am.setWindow(type, triggerAtMillis, /* windowLengthMillis= */ 600000l, pendingIntent);

      is_set = true;
    }
    if (!is_set) {
      am.set(type, triggerAtMillis, pendingIntent);
      is_set = true;
    }
  }

  private static void execute(Context context, DbAlarm dbAlarm, DbGateway db) {
    if (db == null) {
      db = DbGateway.getInstance(context);
    }

    Intent intent = db.getIntent(dbAlarm.intent_id);

    try {
      switch(dbAlarm.perform) {
        case 0 : {
          // perform_send_broadcast
          context.sendBroadcast(intent);
          break;
        }
        case 1 : {
          // perform_start_activity
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent);
          break;
        }
        case 2 : {
          // perform_start_foreground_service
          if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
            break;
          }
        }
        case 3 : {
          // perform_start_service
          context.startService(intent);
          break;
        }
        case 4 : {
          // perform_stop_service
          context.stopService(intent);
          break;
        }
      }
    }
    catch(Exception e) {}
  }

}
