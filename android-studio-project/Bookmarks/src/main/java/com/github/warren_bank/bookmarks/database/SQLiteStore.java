package com.github.warren_bank.bookmarks.database;

import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.utils.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class SQLiteStore extends SQLiteOpenHelper {
  private static SQLiteStore instance = null;
  private static String      DB_PATH  = null;
  private static String      DB_NAME  = null;

  private SQLiteDatabase db;

  public static SQLiteStore getInstance(Context context) {
    if (instance == null) {
      DB_PATH = FileUtils.getDatabaseDirectoryPath(context) + "/";
      DB_NAME = FileUtils.getDatabaseFileName(context);

      instance = new SQLiteStore(context.getApplicationContext());
    }
    return instance;
  }

  private SQLiteStore(Context context) {
    super(context, DB_NAME, null, 1);

    try {
      openDataBase();
    }
    catch (SQLException sqle) {
      try {
        createDataBase();
        close();
        try {
          openDataBase();
        }
        catch (SQLException sqle2) {
          Log.e(Constants.LOG_TAG, sqle2.getMessage());
        }
      }
      catch (IOException e) {
        Log.e(Constants.LOG_TAG, "Unable to create database");
      }
    }
  }

  public void createDataBase() throws IOException {
    boolean dbExist = checkDataBase();
    if (!dbExist) {
      this.getWritableDatabase();
    }
  }

  private boolean checkDataBase() {
    SQLiteDatabase checkDB = null;
    try {
      String myPath = DB_PATH + DB_NAME;
      if (!FileUtils.exists(myPath)) throw new Exception("");

      checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
    catch (Exception e) {
      Log.d(Constants.LOG_TAG, "Database does't exist yet.");
    }
    if (checkDB != null) {
      checkDB.close();
    }
    return checkDB != null ? true : false;
  }

  public void openDataBase() throws SQLException {
    String myPath = DB_PATH + DB_NAME;
    db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    if (Build.VERSION.SDK_INT >= 16) {
      db.disableWriteAheadLogging();
    }
  }

  public Cursor query(String query) {
    boolean skipVersionCheck = false;
    return query(query, skipVersionCheck);
  }

  public Cursor query(String query, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return null;

    Cursor c = null;

    try {
      c = db.rawQuery(query, null);
    }
    catch (SQLiteException e) {
      return null;
    }
    return c;
  }

  public boolean execQuery(String query) {
    boolean skipVersionCheck = false;
    return execQuery(query, skipVersionCheck);
  }

  public boolean execQuery(String query, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return false;

    try {
      db.execSQL(query);
    }
    catch (SQLiteException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean execTransaction(List<String> queries) {
    boolean skipVersionCheck = false;
    return execTransaction(queries, skipVersionCheck);
  }

  public boolean execTransaction(List<String> queries, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return false;

    // validate input
    if ((queries == null) || queries.isEmpty()) return false;

    boolean result = true;
    try {
      db.beginTransaction();
      for (String query : queries) {
        db.execSQL(query);
      }
      db.setTransactionSuccessful();
    }
    catch (SQLiteException e) {
      e.printStackTrace();
      result = false;
    }
    finally {
      db.endTransaction();
    }
    return result;
  }

  @Override
  public synchronized void close() {
    if (db != null) db.close();
    super.close();
  }

  @Override
  public void onCreate(SQLiteDatabase dbase) {
    if (dbase == null)
      dbase = db;
    if (dbase == null)
      return;

    try {
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS application ("
        + "  version INTEGER NOT NULL PRIMARY KEY"
        + ");"
      );
      dbase.execSQL(
          "INSERT INTO application (version) VALUES (" + Update.VERSION_CURRENT + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS folders ("
        + "  id                   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
        + "  parent_id            INTEGER NOT NULL,"
        + "  hidden               INTEGER NOT NULL DEFAULT 0,"
        + "  name                 VARCHAR NOT NULL,"

        + "  UNIQUE (parent_id, name)"
        + ");"
      );
      dbase.execSQL(
          "INSERT INTO folders (id, parent_id, name) VALUES (0, -1, 'Bookmarks');"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS intents ("
        + "  id                   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
        + "  folder_id            INTEGER NOT NULL DEFAULT 0,"
        + "  name                 VARCHAR NOT NULL,"

        + "  flags                INTEGER NOT NULL DEFAULT 0,"
        + "  action               VARCHAR,"
        + "  package_name         VARCHAR,"
        + "  class_name           VARCHAR,"
        + "  data_uri             VARCHAR,"
        + "  data_type            VARCHAR,"

        + "  FOREIGN KEY (folder_id) REFERENCES folders (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_intents_folder_id ON intents (folder_id);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS intent_extra_value_types ("
        + "  id                   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
        + "  name                 VARCHAR NOT NULL"
        + ");"
      );
      dbase.execSQL(
          "INSERT INTO intent_extra_value_types (name) VALUES"
        + "  ('boolean'),"
        + "  ('boolean[]'),"
        + "  ('byte'),"
        + "  ('byte[]'),"
        + "  ('char'),"
        + "  ('char[]'),"
        + "  ('double'),"
        + "  ('double[]'),"
        + "  ('float'),"
        + "  ('float[]'),"
        + "  ('int'),"
        + "  ('int[]'),"
        + "  ('ArrayList<Integer>'),"
        + "  ('long'),"
        + "  ('long[]'),"
        + "  ('short'),"
        + "  ('short[]'),"
        + "  ('String'),"
        + "  ('String[]'),"
        + "  ('ArrayList<String>'),"
        + "  ('Bitmap'),"
        + "  ('Bitmap[]'),"
        + "  ('ArrayList<Bitmap>'),"
        + "  ('Uri'),"
        + "  ('Uri[]'),"
        + "  ('ArrayList<Uri>');"

      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS intent_extras ("
        + "  intent_id            INTEGER NOT NULL,"
        + "  value_type_id        INTEGER NOT NULL,"
        + "  name                 VARCHAR NOT NULL,"
        + "  value                VARCHAR NOT NULL,"

        + "  FOREIGN KEY (intent_id)     REFERENCES intents (id),"
        + "  FOREIGN KEY (value_type_id) REFERENCES intent_extra_value_types (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_intent_extras_intent_id ON intent_extras (intent_id);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS intent_categories ("
        + "  intent_id            INTEGER NOT NULL,"
        + "  category             VARCHAR NOT NULL,"

        + "  FOREIGN KEY (intent_id) REFERENCES intents (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_intent_categories_intent_id ON intent_categories (intent_id);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS intent_alarms ("
        + "  id                   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
        + "  intent_id            INTEGER NOT NULL,"
        + "  trigger_at           INTEGER NOT NULL,"           // milliseconds:    timestamp for next alarm
        + "  interval             INTEGER NOT NULL,"           // milliseconds:    for an alarm that repeats indefinitely until cancelled by user
        + "  perform              INTEGER NOT NULL,"           // array index:     R.array.perform_options
        + "  flags                INTEGER NOT NULL DEFAULT 0," // bit field:       R.integer.flag_alarm_is_exact (1) | R.integer.flag_alarm_run_when_idle (2) | R.integer.flag_alarm_wake_when_idle (4) | R.integer.flag_alarm_run_when_missed (8)

        + "  FOREIGN KEY (intent_id) REFERENCES intents (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_intent_alarms_intent_id ON intent_alarms (intent_id);"
      );
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    Update.resetVersionCache();
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Update.resetVersionCache();
  }
}
