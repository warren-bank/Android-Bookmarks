package com.github.warren_bank.bookmarks.ui;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.ui.SaveAlarm;
import com.github.warren_bank.bookmarks.ui.model.AlarmContentItem;
import com.github.warren_bank.bookmarks.ui.widgets.AlarmContentsAdapter;
import com.github.warren_bank.bookmarks.utils.AlarmUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class Alarms extends ListActivity {
  /* ActionBar Menu Items */
  private static final int MENU_ACTIONBAR_BOOKMARKS  = Menu.FIRST;
  private static final int MENU_ACTIONBAR_CANCEL_ALL = MENU_ACTIONBAR_BOOKMARKS  + 1;
  private static final int MENU_ACTIONBAR_EXIT       = MENU_ACTIONBAR_CANCEL_ALL + 1;
  /* Context Menu Items */
  private static final int MENU_CONTEXT_ALARM_EDIT   = Menu.FIRST;
  private static final int MENU_CONTEXT_ALARM_CANCEL = MENU_CONTEXT_ALARM_EDIT   + 1;

  // ==================
  // Instance variables
  // ==================

  private DbGateway db;
  private View mainView;

  private ListView listView;
  private List<AlarmContentItem> currentAlarmContentItems;
  private AlarmContentsAdapter currentAlarmContentsAdapter;

  // ======================
  // Lifecycle
  // ======================

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_alarms);

    db       = DbGateway.getInstance(Alarms.this);
    mainView = findViewById(R.id.main);

    listView = getListView();
    registerForContextMenu(listView);

    currentAlarmContentItems    = new ArrayList<AlarmContentItem>();
    currentAlarmContentsAdapter = new AlarmContentsAdapter(Alarms.this, currentAlarmContentItems);
    setListAdapter(currentAlarmContentsAdapter);

    getAlarmContentItems();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if ((intent != null) && intent.getBooleanExtra(Constants.EXTRA_RELOAD_LIST, false)) {
      getAlarmContentItems();
    }
  }

  // ---------------------------------------------------------------------------
  // Refresh current directory
  // ---------------------------------------------------------------------------

  private void getAlarmContentItems() {
    // update list of contents
    currentAlarmContentItems.clear();
    currentAlarmContentItems.addAll(
      db.getAlarmContentItems()
    );
    currentAlarmContentsAdapter.notifyDataSetChanged();
  }

  // ---------------------------------------------------------------------------
  // ActionBar Menu
  // ---------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MENU_ACTIONBAR_BOOKMARKS,  MENU_ACTIONBAR_BOOKMARKS,  R.string.app_name_long);
    menu.add(Menu.NONE, MENU_ACTIONBAR_CANCEL_ALL, MENU_ACTIONBAR_CANCEL_ALL, R.string.menu_actionbar_cancel_all);
    menu.add(Menu.NONE, MENU_ACTIONBAR_EXIT,       MENU_ACTIONBAR_EXIT,       R.string.menu_actionbar_exit);

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
      case MENU_ACTIONBAR_CANCEL_ALL :
        cancelAllAlarms();
        break;
      case MENU_ACTIONBAR_EXIT :
        db.getSQLiteStore().close();
        this.finish();
        System.gc();
        System.exit(0);  // kill process
    }
    return super.onOptionsItemSelected(item);
  }

  // ---------------------------------------------------------------------------
  // ListView Context Menu
  // ---------------------------------------------------------------------------

  public void openContext(View v) {
    if (Build.VERSION.SDK_INT >= 24)
      listView.showContextMenuForChild(v, v.getX(), v.getY());
    else
      openContextMenu(v);
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    AlarmContentItem selectedItem = currentAlarmContentsAdapter.getItem(info.position);

    menu.setHeaderTitle(selectedItem.toString());

    menu.add(Menu.NONE, MENU_CONTEXT_ALARM_EDIT,   MENU_CONTEXT_ALARM_EDIT,   R.string.menu_context_alarm_edit);
    menu.add(Menu.NONE, MENU_CONTEXT_ALARM_CANCEL, MENU_CONTEXT_ALARM_CANCEL, R.string.menu_context_alarm_cancel);
  }

  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    AlarmContentItem selectedItem = currentAlarmContentsAdapter.getItem(info.position);

    switch(item.getItemId()) {
      case MENU_CONTEXT_ALARM_EDIT :
        editAlarm(selectedItem);
        return true;
      case MENU_CONTEXT_ALARM_CANCEL :
        cancelAlarm(selectedItem);
        return true;
    }

    return super.onContextItemSelected(item);
  }

  // ---------------------------------------------------------------------------
  // ListView OnClickListener
  // ---------------------------------------------------------------------------

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    AlarmContentItem selectedItem = currentAlarmContentsAdapter.getItem(position);
    editAlarm(selectedItem);
  }

  // ---------------------------------------------------------------------------
  // implementation: ActionBar Menu
  // ---------------------------------------------------------------------------

  private void showBookmarks() {
    Intent intent = new Intent(Alarms.this, Bookmarks.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(Constants.EXTRA_RELOAD_LIST, false);
    startActivity(intent);
    finish();
  }

  private void cancelAllAlarms() {
    cancelAllAlarmsWithConfirmation();
  }

  // ---------------------------------------------------------------------------
  // implementation: ListView Context Menu
  // ---------------------------------------------------------------------------

  private void editAlarm(AlarmContentItem selectedItem) {
    Intent intent = new Intent(Alarms.this, SaveAlarm.class);
    intent.putExtra(Constants.EXTRA_ALARM_ID, selectedItem.id);
    startActivity(intent);
  }

  private void cancelAlarm(AlarmContentItem selectedItem) {
    cancelAlarmWithConfirmation(selectedItem);
  }

  // ---------------------------------------------------------------------------
  // implementation: cancel one alarm
  // ---------------------------------------------------------------------------

  private void cancelAlarmWithConfirmation(AlarmContentItem selectedItem) {
    int    title   = R.string.dialog_cancel_alarm_title;
    String message = getString(R.string.dialog_delete_confirm, selectedItem.toString());

    new AlertDialog.Builder(Alarms.this)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          cancelAlarmWithoutConfirmation(selectedItem);
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void cancelAlarmWithoutConfirmation(AlarmContentItem selectedItem) {
    AlarmUtils.cancel(Alarms.this, selectedItem.id);

    db.deleteAlarm(selectedItem.id);

    if (currentAlarmContentItems.remove(selectedItem))
      currentAlarmContentsAdapter.notifyDataSetChanged();
  }

  // ---------------------------------------------------------------------------
  // implementation: cancel all alarms
  // ---------------------------------------------------------------------------

  private void cancelAllAlarmsWithConfirmation() {
    int    title   = R.string.dialog_cancel_alarms_title;
    String message = getString(R.string.dialog_delete_confirm, getString(R.string.dialog_delete_confirm_alarms));

    new AlertDialog.Builder(Alarms.this)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          cancelAllAlarmsWithoutConfirmation();
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void cancelAllAlarmsWithoutConfirmation() {
    for (AlarmContentItem item : currentAlarmContentItems) {
      AlarmUtils.cancel(Alarms.this, item.id);
    }

    db.deleteAllAlarms();

    currentAlarmContentItems.clear();
    currentAlarmContentsAdapter.notifyDataSetChanged();
  }
}
