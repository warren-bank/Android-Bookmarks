package com.github.warren_bank.bookmarks.ui;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.common.DateFormats;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.database.model.DbAlarm;
import com.github.warren_bank.bookmarks.ui.Alarms;
import com.github.warren_bank.bookmarks.ui.Bookmarks;
import com.github.warren_bank.bookmarks.ui.dialogs.PerformOptionPicker;
import com.github.warren_bank.bookmarks.ui.dialogs.TimeDurationPicker;
import com.github.warren_bank.bookmarks.ui.dialogs.TimeDurationPickerDialog;
import com.github.warren_bank.bookmarks.ui.model.AlarmContentItem;
import com.github.warren_bank.bookmarks.utils.AlarmUtils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class SaveAlarm extends Activity {
  /* ActionBar Menu Items */
  private static final int MENU_ACTIONBAR_BOOKMARKS = Menu.FIRST;
  private static final int MENU_ACTIONBAR_ALARMS    = MENU_ACTIONBAR_BOOKMARKS + 1;

  private final Calendar calendar = Calendar.getInstance();

  private DbGateway db;
  private DbAlarm dbAlarm;
  private boolean time_set;

  // -----------------------------------
  // input fields
  // -----------------------------------
  private EditText alarm_attribute_intent;
  private EditText alarm_attribute_perform;
  private EditText alarm_attribute_date;
  private EditText alarm_attribute_time;
  private EditText alarm_attribute_interval;

  private CheckBox flag_alarm_is_exact;
  private CheckBox flag_alarm_run_when_idle;
  private CheckBox flag_alarm_wake_when_idle;
  private CheckBox flag_alarm_run_when_missed;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_save_alarm);

    db       = DbGateway.getInstance(SaveAlarm.this);
    dbAlarm  = null;
    time_set = false;

    // ---------------------------------
    // input fields
    // ---------------------------------
    alarm_attribute_intent     = (EditText) findViewById(R.id.alarm_attribute_intent);
    alarm_attribute_perform    = (EditText) findViewById(R.id.alarm_attribute_perform);
    alarm_attribute_date       = (EditText) findViewById(R.id.alarm_attribute_date);
    alarm_attribute_time       = (EditText) findViewById(R.id.alarm_attribute_time);
    alarm_attribute_interval   = (EditText) findViewById(R.id.alarm_attribute_interval);

    flag_alarm_is_exact        = (CheckBox) findViewById(R.id.flag_alarm_is_exact);
    flag_alarm_run_when_idle   = (CheckBox) findViewById(R.id.flag_alarm_run_when_idle);
    flag_alarm_wake_when_idle  = (CheckBox) findViewById(R.id.flag_alarm_wake_when_idle);
    flag_alarm_run_when_missed = (CheckBox) findViewById(R.id.flag_alarm_run_when_missed);

    onNewIntent(getIntent());
    AlarmUtils.checkPermissions(SaveAlarm.this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    try {
      if (intent == null) throw new Exception();

      int alarmId  = intent.getIntExtra(Constants.EXTRA_ALARM_ID,  -1);
      int intentId = intent.getIntExtra(Constants.EXTRA_INTENT_ID, -1);

      if (alarmId >= 0) {
        // edit existing alarm
        dbAlarm = db.getDbAlarm(alarmId);
      }
      if (intentId >= 0) {
        if (dbAlarm != null) {
          // should never happen, but.. update?
          dbAlarm.intent_id = intentId;
        }
        else {
          // add new alarm
          dbAlarm = DbAlarm.getInstance(/* id= */ -1, /* intent_id= */ intentId, /* trigger_at= */ -1l, /* interval= */ 0l, /* perform= */ -1, /* flags= */ 0);
        }
      }
    }
    catch(Exception e) {
    }
    finally {
      if ((dbAlarm == null) || (dbAlarm.intent_id < 0)) {
        finish();
        return;
      }

      if (!time_set && (dbAlarm.trigger_at >= 0l)) {
        calendar.setTimeInMillis(dbAlarm.trigger_at);
      }

      setInputFields();
    }
  }

  // ---------------------------------------------------------------------------
  // ActionBar Menu
  // ---------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MENU_ACTIONBAR_BOOKMARKS, MENU_ACTIONBAR_BOOKMARKS, R.string.app_name_long);
    menu.add(Menu.NONE, MENU_ACTIONBAR_ALARMS,    MENU_ACTIONBAR_ALARMS,    R.string.menu_actionbar_scheduled);

    return super.onCreateOptionsMenu(menu);
  }

  // ---------------------------------------------------------------------------
  // ActionBar Menu Items
  // ---------------------------------------------------------------------------

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ACTIONBAR_BOOKMARKS :
        showBookmarks();
        break;
      case MENU_ACTIONBAR_ALARMS :
        showAlarms();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  // ---------------------------------------------------------------------------
  // finish Activity and display list of all Bookmarks
  // ---------------------------------------------------------------------------

  private void showBookmarks() {
    Intent intent = new Intent(SaveAlarm.this, Bookmarks.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(Constants.EXTRA_RELOAD_LIST, false);
    startActivity(intent);
    finish();
  }

  // ---------------------------------------------------------------------------
  // finish Activity and display list of all Alarms
  // ---------------------------------------------------------------------------

  private void showAlarms() {
    Intent intent = new Intent(SaveAlarm.this, Alarms.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(Constants.EXTRA_RELOAD_LIST, true);
    startActivity(intent);
    finish();
  }

  // ---------------------------------------------------------------------------
  // initialize input fields
  // ---------------------------------------------------------------------------

  private void setInputFields() {
    try {
      Resources resources      = getResources();
      AlarmContentItem uiAlarm = db.getAlarmContentItem(dbAlarm);

      String intent, perform, date, time, interval;

      intent   = uiAlarm.intent;
      perform  = uiAlarm.perform;
      date     = DateFormats.getAlarmDate(calendar.getTimeInMillis());
      time     = DateFormats.getAlarmTime(calendar.getTimeInMillis());
      interval = uiAlarm.freq;

      alarm_attribute_intent.setText  (intent,   TextView.BufferType.EDITABLE);
      alarm_attribute_perform.setText (perform,  TextView.BufferType.EDITABLE);
      alarm_attribute_date.setText    (date,     TextView.BufferType.EDITABLE);
      alarm_attribute_time.setText    (time,     TextView.BufferType.EDITABLE);
      alarm_attribute_interval.setText(interval, TextView.BufferType.EDITABLE);

      flag_alarm_is_exact.setChecked(
        DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_is_exact))
      );
      flag_alarm_run_when_idle.setChecked(
        DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_run_when_idle))
      );
      flag_alarm_wake_when_idle.setChecked(
        DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_wake_when_idle))
      );
      flag_alarm_run_when_missed.setChecked(
        DbAlarm.isFlagOn(dbAlarm, resources.getInteger(R.integer.flag_alarm_run_when_missed))
      );
    }
    catch(Exception e) {}
  }

  // ---------------------------------------------------------------------------
  // update text fields
  // ---------------------------------------------------------------------------

  private void updateTextPerformOption() {
    DbAlarm          dbDummy = DbAlarm.getInstance(/* id= */ -1, /* intent_id= */ -1, /* trigger_at= */ -1l, /* interval */ 0l, dbAlarm.perform, /* flags= */ 0);
    AlarmContentItem uiDummy = db.getAlarmContentItem(dbDummy);

    alarm_attribute_perform.setText(uiDummy.perform, TextView.BufferType.EDITABLE);
  }

  private void updateTextDate() {
    String date = DateFormats.getAlarmDate(calendar.getTimeInMillis());

    alarm_attribute_date.setText(date, TextView.BufferType.EDITABLE);
  }

  private void updateTextTime() {
    String time = DateFormats.getAlarmTime(calendar.getTimeInMillis());

    alarm_attribute_time.setText(time, TextView.BufferType.EDITABLE);
  }

  private void updateTextInterval() {
    DbAlarm          dbDummy = DbAlarm.getInstance(/* id= */ -1, /* intent_id= */ -1, /* trigger_at= */ -1l, dbAlarm.interval, /* perform= */ -1, /* flags= */ 0);
    AlarmContentItem uiDummy = db.getAlarmContentItem(dbDummy);

    alarm_attribute_interval.setText(uiDummy.freq, TextView.BufferType.EDITABLE);
  }

  // ---------------------------------------------------------------------------
  // update dbAlarm with values from input fields
  // ---------------------------------------------------------------------------

  private boolean getInputFields() {
    try {
      Resources resources = getResources();
      int flags;

      flags = 0;
      if (flag_alarm_is_exact.isChecked())
        flags |= resources.getInteger(R.integer.flag_alarm_is_exact);
      if (flag_alarm_run_when_idle.isChecked())
        flags |= resources.getInteger(R.integer.flag_alarm_run_when_idle);
      if (flag_alarm_wake_when_idle.isChecked())
        flags |= resources.getInteger(R.integer.flag_alarm_wake_when_idle);
      if (flag_alarm_run_when_missed.isChecked())
        flags |= resources.getInteger(R.integer.flag_alarm_run_when_missed);

      dbAlarm.flags = flags;

      if (time_set || (dbAlarm.trigger_at < 0l))
        dbAlarm.trigger_at = calendar.getTimeInMillis();

      if ((dbAlarm.trigger_at < 0l) || (dbAlarm.perform < 0))
        throw new Exception();

      if (dbAlarm.id < 0) {
        // initial trigger for new alarm must be set in future
        long now = System.currentTimeMillis() + 5000l;
        if (dbAlarm.trigger_at < now)
          dbAlarm.trigger_at = now;
      }

      return true;
    }
    catch(Exception e) {
      return false;
    }
  }

  // ---------------------------------------------------------------------------
  // layout View OnClickListener
  // ---------------------------------------------------------------------------

  public void openPerformOptionPicker(View v) {
    PerformOptionPicker.showPerformOptionPicker(
      SaveAlarm.this,
      new PerformOptionPicker.Listener() {
        @Override
        public void onPerformOptionPick(int which) {
          dbAlarm.perform = which;
          updateTextPerformOption();
        }
      },
      /* remove_last_option= */ true
    );
  }

  public void openDatePicker(View v) {
    new DatePickerDialog(
      SaveAlarm.this,
      new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
          calendar.set(Calendar.YEAR,         year);
          calendar.set(Calendar.MONTH,        month);
          calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

          time_set = true;
          updateTextDate();
        }
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH)
    )
    .show();
  }

  public void openTimePicker(View v) {
    new TimePickerDialog(
      SaveAlarm.this,
      new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
          calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
          calendar.set(Calendar.MINUTE,      minute);
          calendar.set(Calendar.SECOND,      0);

          time_set = true;
          updateTextTime();
        }
      },
      calendar.get(Calendar.HOUR_OF_DAY),
      calendar.get(Calendar.MINUTE),
      /* is24HourView= */ false
    )
    .show();
  }

  public void openIntervalPicker(View v) {
    new TimeDurationPickerDialog(
      SaveAlarm.this,
      new TimeDurationPickerDialog.OnDurationSetListener() {
        @Override
        public void onDurationSet(TimeDurationPicker view, long duration) {
          dbAlarm.interval = duration;

          updateTextInterval();
        }
      },
      /* duration= */ dbAlarm.interval
    )
    .show();
  }

  public void saveAlarm(View v) {
    boolean ok = true;

    ok &= getInputFields();
    if (!ok) return;

    if (dbAlarm.id < 0)
      ok &= db.addAlarm(dbAlarm);
    else
      ok &= db.updateAlarm(dbAlarm);

    if (ok) {
      AlarmUtils.schedule(SaveAlarm.this, dbAlarm);
      showAlarms();
    }
  }

}
