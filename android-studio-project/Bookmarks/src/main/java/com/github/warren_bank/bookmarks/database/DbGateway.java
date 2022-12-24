package com.github.warren_bank.bookmarks.database;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.common.DateFormats;
import com.github.warren_bank.bookmarks.database.model.DbAlarm;
import com.github.warren_bank.bookmarks.database.model.DbIntent;
import com.github.warren_bank.bookmarks.ui.model.AlarmContentItem;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DbGateway {
  private static DbGateway instance = null;

  private Context     context;
  private SQLiteStore db;

  public static DbGateway getInstance(Context context) {
    if (instance == null) {
      instance = new DbGateway(context.getApplicationContext());
    }
    return instance;
  }

  private DbGateway(Context context) {
    this.context = context;
    this.db      = SQLiteStore.getInstance(context);
  }

  public SQLiteStore getSQLiteStore() {
    return this.db;
  }

  // ---------------------------------------------------------------------------
  // helpers:
  // ---------------------------------------------------------------------------

  private String sqlEscapeString(String value) {
    boolean notNull = false;
    return sqlEscapeString(value, notNull);
  }

  private String sqlEscapeString(String value, boolean notNull) throws NullPointerException {
    if (value == null) {
      if (notNull)
        throw new NullPointerException("illegal call to: DatabaseUtils.sqlEscapeString(null)");
      else
        return "NULL";
    }
    return DatabaseUtils.sqlEscapeString(value);
  }

  private String normalizeEmptyString(String value) {
    String defaultValue = "";
    return normalizeEmptyString(value, defaultValue);
  }

  private String normalizeEmptyString(String value, String defaultValue) {
    List<String> blacklist = null;
    return normalizeEmptyString(value, defaultValue, blacklist);
  }

  private String normalizeEmptyString(String value, String defaultValue, List<String> blacklist) {
    return ((value == null) || value.isEmpty() || value.toLowerCase().equals("null"))
      ? defaultValue
      : ((blacklist != null) && blacklist.contains(value))
        ? defaultValue
        : value;
  }

  private String getColumnString(Cursor c, String columnName) {
    String defaultValue = "";
    return getColumnString(c, columnName, defaultValue);
  }

  private String getColumnString(Cursor c, String columnName, String defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    return normalizeEmptyString(
      c.getString(columnIndex),
      defaultValue
    );
  }

  private int getColumnInteger(Cursor c, String columnName) {
    int defaultValue = -1;
    return getColumnInteger(c, columnName, defaultValue);
  }

  private int getColumnInteger(Cursor c, String columnName, int defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    int value = c.getInt(columnIndex);
    return value;
  }

  private long getColumnLong(Cursor c, String columnName) {
    long defaultValue = -1l;
    return getColumnLong(c, columnName, defaultValue);
  }

  private long getColumnLong(Cursor c, String columnName, long defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    long value = c.getLong(columnIndex);
    return value;
  }

  private float getColumnFloat(Cursor c, String columnName) {
    float defaultValue = -1.0f;
    return getColumnFloat(c, columnName, defaultValue);
  }

  private float getColumnFloat(Cursor c, String columnName, float defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    float value = c.getFloat(columnIndex);
    return value;
  }

  private boolean getColumnBoolean(Cursor c, String columnName) {
    boolean defaultValue = false;
    return getColumnBoolean(c, columnName, defaultValue);
  }

  private boolean getColumnBoolean(Cursor c, String columnName, boolean defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    int value = getColumnInteger(c, columnName, 0);
    return (value == 1);
  }

  // ---------------------------------------------------------------------------
  // read from DB:
  // ---------------------------------------------------------------------------

  public String getIntentName(int intentId) {
    String query = "SELECT name FROM intents WHERE id = " + intentId;
    String name  = null;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        name = getColumnString(c, "name", name);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return name;
  }

  public int getIntentFolderId(int intentId) {
    String query = "SELECT folder_id FROM intents WHERE id = " + intentId;
    int folderId = -1;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        folderId = getColumnInteger(c, "folder_id", folderId);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return folderId;
  }

  public int getFolderId(int parentId, String name) {
    String query  = "SELECT id FROM folders WHERE parent_id = " + parentId + " AND name = " + sqlEscapeString(name);
    int id = -1;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        id = getColumnInteger(c, "id", id);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return id;
  }

  public String getFolderPath(int folderId) {
    String sep = "/";
    return getFolderPath(folderId, sep);
  }

  public String getFolderPath(int folderId, String sep) {
    List<FolderContentItem> breadcrumbs = getFolderBreadcrumbs(folderId);

    String folderPath = ((breadcrumbs != null) && !breadcrumbs.isEmpty())
      ? TextUtils.join(sep, breadcrumbs)
      : "";
    return folderPath;
  }

  public List<FolderContentItem> getFolderBreadcrumbs(int folderId) {
    List<FolderContentItem> breadcrumbs = new ArrayList<FolderContentItem>();
    FolderContentItem item;

    item = getFolderContentItem(folderId);
    if (item == null) return breadcrumbs;
    breadcrumbs.add(0, item);

    while ((item = getParentFolderContentItem(folderId)) != null) {
      breadcrumbs.add(0, item);
      folderId = item.id;
    }
    return breadcrumbs;
  }

  // never used to resolve an Intent; only used to resolve a Folder.
  public FolderContentItem getFolderContentItem(int folderId) {
    String query = "SELECT name, hidden FROM folders WHERE id = " + folderId;
    int hidden = -1;
    String name = null;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        name   = getColumnString (c, "name");
        hidden = getColumnInteger(c, "hidden", hidden);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    return (!TextUtils.isEmpty(name) && (hidden >= 0))
      ? new FolderContentItem(folderId, name, /* isFolder */ true, /* isHidden */ (hidden >= 1))
      : null;
  }

  // never used to resolve an Intent; only used to resolve a Folder.
  public FolderContentItem getParentFolderContentItem(int folderId) {
    String query = "SELECT parent.id as id, parent.name as name, parent.hidden as hidden FROM folders"
      + " JOIN folders as parent"
      + " ON folders.parent_id = parent.id"
      + " WHERE folders.id = " + folderId;
    int id = -1, hidden = -1;
    String name = null;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        id     = getColumnInteger(c, "id", id);
        name   = getColumnString (c, "name");
        hidden = getColumnInteger(c, "hidden", hidden);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    return ((id >= 0) && !TextUtils.isEmpty(name) && (hidden >= 0))
      ? new FolderContentItem(id, name, /* isFolder */ true, /* isHidden */ (hidden >= 1))
      : null;
  }

  public List<FolderContentItem> getFolderContentItems(int folderId) {
    boolean includeURL = false;
    return getFolderContentItems(folderId, includeURL);
  }

  // used to resolve sorted list that contains both Intents and Folders
  public List<FolderContentItem> getFolderContentItems(int folderId, boolean includeURL) {
    List<FolderContentItem> items = new ArrayList<FolderContentItem>();

    items.addAll(getFoldersInFolder(folderId));
    items.addAll(getIntentsInFolder(folderId, includeURL));
    Collections.sort(items);
    return items;
  }

  public List<FolderContentItem> getFoldersInFolder(int parentId) {
    List<FolderContentItem> items = new ArrayList<FolderContentItem>();
    String query = "SELECT id, name, hidden FROM folders WHERE parent_id = " + parentId;
    int id, hidden;
    String name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          id     = getColumnInteger(c, "id", -1);
          name   = getColumnString (c, "name");
          hidden = getColumnInteger(c, "hidden", -1);

          if ((id >= 0) && !TextUtils.isEmpty(name) && (hidden >= 0)) {
            FolderContentItem item = new FolderContentItem(id, name, /* isFolder */ true, /* isHidden */ (hidden >= 1));
            items.add(item);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return items;
  }

  public List<FolderContentItem> getIntentsInFolder(int folderId) {
    boolean includeURL = false;
    return getIntentsInFolder(folderId, includeURL);
  }

  public List<FolderContentItem> getIntentsInFolder(int folderId, boolean includeURL) {
    List<FolderContentItem> items = new ArrayList<FolderContentItem>();

    String query = "SELECT id, name"
      + (includeURL
          ? ", data_uri"
          : ""
        )
      + " FROM intents"
      + " WHERE folder_id = " + folderId;

    int id;
    String name, data_uri;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          id       = getColumnInteger(c, "id", -1);
          name     = getColumnString (c, "name");
          data_uri = includeURL ? getColumnString(c, "data_uri") : null;

          if ((id >= 0) && !TextUtils.isEmpty(name)) {
            FolderContentItem item = new FolderContentItem(id, name, /* isFolder */ false, /* isHidden */ false, data_uri);
            items.add(item);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return items;
  }

  public List<FolderContentItem> searchIntents(String searchTerm, int folderId) {
    boolean includeURL = false;
    return searchIntents(searchTerm, folderId, includeURL);
  }

  public List<FolderContentItem> searchIntents(String searchTerm, int folderId, boolean includeURL) {
    List<FolderContentItem> items = new ArrayList<FolderContentItem>();

    if (TextUtils.isEmpty(searchTerm)) return items;

    String query = "SELECT id, name"
      + (includeURL
          ? ", data_uri"
          : ""
        )
      + " FROM intents"
      + " WHERE"
      + "   name LIKE " + sqlEscapeString("%" + searchTerm + "%")
      + ((folderId >= 0)
          ? " AND folder_id = " + folderId
          : ""
        );

    int id;
    String name, data_uri;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          id       = getColumnInteger(c, "id", -1);
          name     = getColumnString (c, "name");
          data_uri = includeURL ? getColumnString(c, "data_uri") : null;

          if ((id >= 0) && !TextUtils.isEmpty(name)) {
            FolderContentItem item = new FolderContentItem(id, name, /* isFolder */ false, /* isHidden */ false, data_uri);
            items.add(item);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return items;
  }

  public int getIntentCountByDataUri(String data_uri) {
    int folderId = -1;
    return getIntentCountByDataUri(data_uri, folderId);
  }

  public int getIntentCountByDataUri(String data_uri, int folderId) {
    String query  = "SELECT COUNT(*) as count FROM intents"
      + " WHERE data_uri = "     + sqlEscapeString(data_uri)
      + ((folderId >= 0)
          ? (" AND folder_id = " + folderId)
          : ""
        );

    int count = 0;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        count = getColumnInteger(c, "count", count);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return count;
  }

  public Intent getIntent(int intentId) {
    DbIntent dbIntent = getDbIntent(intentId);
    return getIntent(dbIntent);
  }

  public Intent getIntent(DbIntent dbIntent) {
    return (dbIntent != null)
      ? dbIntent.getIntent(context)
      : null;
  }

  private int getNextAvailableIntentId() {
    String query = "SELECT MAX(id) as max FROM intents";
    int id = 1;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        id = getColumnInteger(c, "max", (id - 1)) + 1;
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return id;
  }

  public String[] getAllIntentExtraValueTypeNames() {
    HashMap<String,Integer> map = getAllIntentExtraValueTypes();
    Set<String> names = map.keySet();

    return ((names != null) && !names.isEmpty())
      ? names.toArray(new String[names.size()])
      : new String[0];
  }

  private HashMap<String,Integer> getAllIntentExtraValueTypes() {
    HashMap<String,Integer> map = new HashMap<String,Integer>();
    String query = "SELECT id, name FROM intent_extra_value_types";
    int id;
    String name;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          id   = getColumnInteger(c, "id", -1);
          name = getColumnString (c, "name");

          if ((id >= 0) && !TextUtils.isEmpty(name)) {
            map.put(name, id);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return map;
  }

  // ---------------------------------------------------------------------------
  // write to DB:
  // ---------------------------------------------------------------------------

  public boolean addFolder(int parentId, String name) {
    String query = "INSERT INTO folders"
      + "   (parent_id, name)"
      + " VALUES"
      + "   ("
      +               parentId              + ", "
      +               sqlEscapeString(name)
      + "   )";

    return db.execQuery(query);
  }

  public boolean setFolderParentId(int folderId, int parentId) {
    String query = "UPDATE folders"
      + " SET"
      + "   parent_id = " + parentId
      + " WHERE"
      + "   id = " + folderId;

    return db.execQuery(query);
  }

  public boolean renameFolder(int folderId, String name) {
    String query = "UPDATE folders"
      + " SET"
      + "   name = " + sqlEscapeString(name)
      + " WHERE"
      + "   id = "   + folderId;

    return db.execQuery(query);
  }

  public boolean hideFolder(int folderId) {
    return setFolderHidden(folderId, /* hidden */ true);
  }

  public boolean unhideFolder(int folderId) {
    return setFolderHidden(folderId, /* hidden */ false);
  }

  private boolean setFolderHidden(int folderId, boolean hidden) {
    String query = "UPDATE folders"
      + " SET"
      + "   hidden = " + (hidden ? "1" : "0")
      + " WHERE"
      + "   id = " + folderId;

    return db.execQuery(query);
  }

  public boolean deleteFolder(int folderId) {
    boolean result = true;
    List<FolderContentItem> items;

    // recursively delete all subfolders
    items = getFoldersInFolder(folderId);
    for (FolderContentItem folder : items) {
      result &= deleteFolder(folder.id);
      if (!result) return false;
    }

    // delete all bookmarks in folder
    items = getIntentsInFolder(folderId);
    for (FolderContentItem intent : items) {
      result &= deleteIntent(intent.id);
      if (!result) return false;
    }

    // delete empty folder
    String query = "DELETE"
      + " FROM"
      + "   folders"
      + " WHERE"
      + "   id = " + folderId;

    result &= db.execQuery(query);

    return result;
  }

  public boolean addIntent(int folderId, String name, Intent intent) {
    int intentId = -1;
    return updateIntent(intentId, folderId, name, intent);
  }

  public boolean updateIntent(int intentId, int folderId, String name, Intent intent) {
    try {
      if (intentId < 0) {
        // add new Intent
        intentId = getNextAvailableIntentId();
      }
      else {
        // delete the existing Intent, then write fresh data
        deleteIntent(intentId);
      }

      if (intentId < 0) return false;

      DbIntent    dbIntent = DbIntent.getInstance(intentId, folderId, name, intent);
      List<String> queries = new ArrayList<String>();

      queries.add(
          "INSERT INTO intents"
        + "   (id, folder_id, name, flags, action, package_name, class_name, data_uri, data_type)"
        + " VALUES"
        + "   ("
        +         intentId                                             + ", "
        +         folderId                                             + ", "
        +         sqlEscapeString(name)                                + ", "
        +         dbIntent.flags                                       + ", "
        +         sqlEscapeString(dbIntent.action)                     + ", "
        +         sqlEscapeString(dbIntent.package_name)               + ", "
        +         sqlEscapeString(dbIntent.class_name)                 + ", "
        +         sqlEscapeString(dbIntent.data_uri)                   + ", "
        +         sqlEscapeString(dbIntent.data_type)
        + "   )"
      );

      if ((dbIntent.extras != null) && (dbIntent.extras.length > 0)) {
        HashMap<String,Integer> valueTypeMap = getAllIntentExtraValueTypes();

        for (DbIntent.Extra extra : dbIntent.extras) {
          if ((extra != null) && !TextUtils.isEmpty(extra.name) && !TextUtils.isEmpty(extra.value_type) && !TextUtils.isEmpty(extra.value) && valueTypeMap.containsKey(extra.value_type)) {
            queries.add(
                "INSERT INTO intent_extras"
              + "   (intent_id, value_type_id, name, value)"
              + " VALUES"
              + "   ("
              +         intentId                                       + ", "
              +         valueTypeMap.get(extra.value_type).toString()  + ", "
              +         sqlEscapeString(extra.name)                    + ", "
              +         sqlEscapeString(extra.value)
              + "   )"
            );
          }
        }
      }

      if ((dbIntent.categories != null) && (dbIntent.categories.length > 0)) {
        for (String category : dbIntent.categories) {
          if (!TextUtils.isEmpty(category)) {
            queries.add(
                "INSERT INTO intent_categories"
              + "   (intent_id, category)"
              + " VALUES"
              + "   ("
              +         intentId                                       + ", "
              +         sqlEscapeString(category)
              + "   )"
            );
          }
        }
      }

      return db.execTransaction(queries);
    }
    catch(Exception e) {
      return false;
    }
  }

  public boolean copyIntent(int intentId) {
    int newIntentId = getNextAvailableIntentId();
    if (newIntentId < 0) return false;

    List<String> queries = new ArrayList<String>();

    queries.add(
        "INSERT INTO intents"
      + "   (id, folder_id, name, flags, action, package_name, class_name, data_uri, data_type)"
      + " SELECT"
      + "    " + newIntentId + " as id, folder_id, name, flags, action, package_name, class_name, data_uri, data_type"
      + " FROM"
      + "   intents"
      + " WHERE"
      + "   id = " + intentId
    );

    queries.add(
        "INSERT INTO intent_extras"
      + "   (intent_id, value_type_id, name, value)"
      + " SELECT"
      + "    " + newIntentId + " as intent_id, value_type_id, name, value"
      + " FROM"
      + "   intent_extras"
      + " WHERE"
      + "   intent_id = " + intentId
    );

    queries.add(
        "INSERT INTO intent_categories"
      + "   (intent_id, category)"
      + " SELECT"
      + "    " + newIntentId + " as intent_id, category"
      + " FROM"
      + "   intent_categories"
      + " WHERE"
      + "   intent_id = " + intentId
    );

    return db.execTransaction(queries);
  }

  public boolean setIntentFolderId(int intentId, int folderId) {
    String query = "UPDATE intents"
      + " SET"
      + "   folder_id = " + folderId
      + " WHERE"
      + "   id = " + intentId;

    return db.execQuery(query);
  }

  public boolean deleteIntent(int intentId) {
    if (intentId < 0) return false;

    List<String> queries = new ArrayList<String>();

    queries.add(
        "DELETE"
      + " FROM"
      + "   intent_categories"
      + " WHERE"
      + "   intent_id = " + intentId
    );

    queries.add(
        "DELETE"
      + " FROM"
      + "   intent_extras"
      + " WHERE"
      + "   intent_id = " + intentId
    );

    queries.add(
        "DELETE"
      + " FROM"
      + "   intents"
      + " WHERE"
      + "   id = " + intentId
    );

    return db.execTransaction(queries);
  }

  public boolean addAlarm(DbAlarm dbAlarm) {
    if ((dbAlarm.intent_id < 0) || (dbAlarm.perform < 0) || (dbAlarm.trigger_at < 0)) return false;

    dbAlarm.id = -1;

    ContentValues values = new ContentValues();
    values.put("intent_id",  dbAlarm.intent_id);
    values.put("trigger_at", dbAlarm.trigger_at);
    values.put("interval",   dbAlarm.interval);
    values.put("perform",    dbAlarm.perform);
    values.put("flags",      dbAlarm.flags);

    long id = db.insert("intent_alarms", null, values);
    boolean ok = (id >= 0l);

    if (ok)
      dbAlarm.id = (int) id;

    return ok;
  }

  public boolean updateAlarm(DbAlarm dbAlarm) {
    if ((dbAlarm.intent_id < 0) || (dbAlarm.perform < 0) || (dbAlarm.trigger_at < 0)) return false;

    if (dbAlarm.id < 0)
      return addAlarm(dbAlarm);

    if (!deleteAlarm(dbAlarm.id))
      return false;

    try {
      String query = "INSERT INTO intent_alarms"
        + "   (id, intent_id, trigger_at, interval, perform, flags)"
        + " VALUES"
        + "   ("
        +         dbAlarm.id                                           + ", "
        +         dbAlarm.intent_id                                    + ", "
        +         dbAlarm.trigger_at                                   + ", "
        +         dbAlarm.interval                                     + ", "
        +         dbAlarm.perform                                      + ", "
        +         dbAlarm.flags
        + "   )";

      return db.execQuery(query);
    }
    catch(Exception e) {
      return false;
    }
  }

  public boolean deleteAlarm(int alarmId) {
    if (alarmId < 0) return false;

    String query = "DELETE"
      + " FROM"
      + "   intent_alarms"
      + " WHERE"
      + "   id = " + alarmId;

    return db.execQuery(query);
  }

  public boolean deleteAllAlarms() {
    String query = "DELETE FROM intent_alarms";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------
  // read DB model:
  // ---------------------------------------------------------------------------

  public DbIntent getDbIntent(int intentId) {
    int folder_id = 0, flags = 0;
    String name = null, action = null, package_name = null, class_name = null, data_uri = null, data_type = null;
    String category;
    String extra_name, extra_type, extra_value;

    List<String> categories     = new ArrayList<String>();
    List<DbIntent.Extra> extras = new ArrayList<DbIntent.Extra>();

    String query;
    Cursor c;

    // ---------------------------------

    query = "SELECT folder_id, name, flags, action, package_name, class_name, data_uri, data_type FROM intents WHERE id = " + intentId;
    c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        folder_id    = getColumnInteger(c, "folder_id", folder_id);
        name         = getColumnString (c, "name");
        flags        = getColumnInteger(c, "flags", flags);
        action       = getColumnString (c, "action");
        package_name = getColumnString (c, "package_name");
        class_name   = getColumnString (c, "class_name");
        data_uri     = getColumnString (c, "data_uri");
        data_type    = getColumnString (c, "data_type");
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    if (TextUtils.isEmpty(name)) return null;

    // ---------------------------------

    query = "SELECT category FROM intent_categories WHERE intent_id = " + intentId;
    c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          category = getColumnString (c, "category");

          if (!TextUtils.isEmpty(category)) {
            categories.add(category);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    // ---------------------------------

    query = "SELECT extras.name as name, types.name as value_type, extras.value as value"
            + " FROM intent_extras as extras"
            + " JOIN intent_extra_value_types as types"
            + " ON extras.value_type_id = types.id"
            + " WHERE extras.intent_id = " + intentId;
    c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          extra_name   = getColumnString (c, "name");
          extra_type   = getColumnString (c, "value_type");
          extra_value  = getColumnString (c, "value");

          if (!TextUtils.isEmpty(extra_name) && !TextUtils.isEmpty(extra_type) && !TextUtils.isEmpty(extra_value)) {
            DbIntent.Extra extra = new DbIntent.Extra(extra_name, extra_type, extra_value);
            extras.add(extra);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    // ---------------------------------

    DbIntent dbIntent = DbIntent.getInstance(intentId, folder_id, name, flags, action, package_name, class_name, data_uri, data_type, categories, extras);
    return dbIntent;
  }

  public DbAlarm getDbAlarm(int alarmId) {
    List<DbAlarm> dbAlarms = getDbAlarms(alarmId);

    return ((dbAlarms == null) || dbAlarms.isEmpty())
      ? null
      : dbAlarms.get(0);
  }

  public List<DbAlarm> getDbAlarms() {
    return getDbAlarms(-1);
  }

  private List<DbAlarm> getDbAlarms(int alarmId) {
    List<DbAlarm> dbAlarms = new ArrayList<DbAlarm>();

    String query = "SELECT id, intent_id, trigger_at, interval, perform, flags FROM intent_alarms"
      + ((alarmId >= 0) ? (" WHERE id = " + alarmId) : "");

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          DbAlarm dbAlarm = getDbAlarm(c);

          if (dbAlarm != null) {
            dbAlarms.add(dbAlarm);
          }
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return dbAlarms;
  }

  private DbAlarm getDbAlarm(Cursor c) {
    int  id = -1, intent_id = -1, perform = -1, flags = 0;
    long trigger_at = -1, interval = 0;

    try {
      id         = getColumnInteger(c, "id",         id);
      intent_id  = getColumnInteger(c, "intent_id",  intent_id);
      perform    = getColumnInteger(c, "perform",    perform);
      flags      = getColumnInteger(c, "flags",      flags);
      trigger_at = getColumnLong(   c, "trigger_at", trigger_at);
      interval   = getColumnLong(   c, "interval",   interval);
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if ((id < 0) || (intent_id < 0) || (perform < 0) || (trigger_at < 0)) return null;

    DbAlarm dbAlarm = DbAlarm.getInstance(id, intent_id, trigger_at, interval, perform, flags);
    return dbAlarm;
  }

  public AlarmContentItem getAlarmContentItem(DbAlarm dbAlarm) {
    return convertDbAlarmToAlarmContentItem(dbAlarm);
  }

  public AlarmContentItem getAlarmContentItem(int alarmId) {
    List<AlarmContentItem> items = getAlarmContentItems(alarmId);

    return ((items == null) || items.isEmpty())
      ? null
      : items.get(0);
  }

  public List<AlarmContentItem> getAlarmContentItems() {
    return getAlarmContentItems(-1);
  }

  private List<AlarmContentItem> getAlarmContentItems(int alarmId) {
    List<AlarmContentItem> items = new ArrayList<AlarmContentItem>();
    List<DbAlarm> dbAlarms = getDbAlarms(alarmId);

    for (DbAlarm dbAlarm : dbAlarms) {
      items.add(
        convertDbAlarmToAlarmContentItem(dbAlarm)
      );
    }

    Collections.sort(items);
    return items;
  }

  private static String[] perform_options = null;

  private AlarmContentItem convertDbAlarmToAlarmContentItem(DbAlarm dbAlarm) {
    if (perform_options == null)
      perform_options = context.getResources().getStringArray(R.array.perform_options);

    int id = dbAlarm.id;

    String perform = "", intent = "", time = "", freq = "";

    if (dbAlarm.perform >= 0) {
      // ex: "Start Activity"
      perform = perform_options[dbAlarm.perform];
    }

    if (dbAlarm.intent_id >= 0) {
      // ex: "/folder/path/to/Intent_name"
      int    folderId   = getIntentFolderId(dbAlarm.intent_id);
      String folderPath = getFolderPath(folderId, "/");
      String intentName = getIntentName(dbAlarm.intent_id);

      intent = folderPath + "/" + intentName;
    }

    if (dbAlarm.trigger_at >= 0l) {
      // ex: "01/01/1970 1:00pm + 0-10 minutes"
      int     flag     = context.getResources().getInteger(R.integer.flag_alarm_is_exact);
      boolean is_exact = DbAlarm.isFlagOn(dbAlarm, flag);

      time = DateFormats.getFileContentDateTime(dbAlarm.trigger_at) + (!is_exact ? " + 0-10 minutes" : "");
    }

    if (dbAlarm.interval > 0l) {
      // ex: "30 minutes"
      freq = DateFormats.getAlarmContentDuration(dbAlarm.interval);
    }

    return new AlarmContentItem(id, perform, intent, time, freq);
  }

}
