package com.github.warren_bank.bookmarks.receiver;

import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.utils.AlarmUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null) return;

    String action = intent.getAction();
    if (action != null) {
      switch(action) {
        case "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" :
        case "android.intent.action.BOOT_COMPLETED" :
        case "android.intent.action.QUICKBOOT_POWERON" : {
          AlarmUtils.rescheduleAll(context);
          notifyRefreshReceiver(context);
          return;
        }
      }
    }

    int alarmId  = intent.getIntExtra(Constants.EXTRA_ALARM_ID, -1);
    if (alarmId >= 0) {
      AlarmUtils.execute(context, alarmId);
      notifyRefreshReceiver(context);
    }
  }

  private void notifyRefreshReceiver(Context context) {
    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.EXTRA_RELOAD_LIST));
  }
}
