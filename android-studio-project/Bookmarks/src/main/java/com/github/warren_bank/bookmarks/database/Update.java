package com.github.warren_bank.bookmarks.database;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Update {

  // ---------------------------------------------------------------------------
  // static
  // ---------------------------------------------------------------------------

  public static final int MODE_INSTALL = 1;
  public static final int MODE_RESTORE = 2;

  public static final int VERSION_CURRENT = 2;
  public static       int VERSION_ACTUAL  = -1;

  // ---------------------------------------------------------------------------

  private static Cursor query(SQLiteStore db, String query) {
    boolean skipVersionCheck = true;
    return db.query(query, skipVersionCheck);
  }

  private static boolean execQuery(SQLiteStore db, String query) {
    boolean skipVersionCheck = true;
    return db.execQuery(query, skipVersionCheck);
  }

  private static boolean execTransaction(SQLiteStore db, List<String> queries) {
    boolean skipVersionCheck = true;
    return db.execTransaction(queries, skipVersionCheck);
  }

  // ---------------------------------------------------------------------------

  protected static void resetVersionCache() {
    Update.VERSION_ACTUAL = -1;
  }

  protected static boolean needsUpdate(SQLiteStore db) {
    return (Update.getVersion(db, /* resetCache */ false) != Update.VERSION_CURRENT);
  }

  private static int getVersion(SQLiteStore db, boolean resetCache) {
    if (resetCache)
      Update.resetVersionCache();

    if (Update.VERSION_ACTUAL <= 0) {
      try {
        Cursor c = Update.query(db, "SELECT version FROM application");
        if (c != null && c.moveToFirst()) {
          Update.VERSION_ACTUAL = c.getInt(0);
          Log.d(Constants.LOG_TAG, "Current database version: " + Update.VERSION_ACTUAL);
        }
        c.close();
      } catch (SQLiteException e) {
        Log.e(Constants.LOG_TAG, e.getMessage());
      }
    }

    if (Update.VERSION_ACTUAL <= 0) {
      Update.VERSION_ACTUAL = 1;
      Update.execQuery(db, "INSERT INTO application (version) VALUES (" + Update.VERSION_ACTUAL + ")");
      Log.d(Constants.LOG_TAG, "DB version blank. All updates will be run; please ignore errors.");
    }

    return Update.VERSION_ACTUAL;
  }

  // ---------------------------------------------------------------------------
  // inner classes and interfaces
  // ---------------------------------------------------------------------------

  public class DatabaseUpdateResult {
    public boolean didUpdateFail;
    public boolean didUpdateSucceed;
    public boolean didUpdateAllSeries;

    public DatabaseUpdateResult() {
      this.didUpdateFail          = false;
      this.didUpdateSucceed       = false;
      this.didUpdateAllSeries     = false;
    }
  }

  public interface DatabaseUpdateListener {
    public boolean preDatabaseUpdate (int mode, int oldVersion, boolean willUpdate);
    public void    postDatabaseUpdate(int mode, int oldVersion, DatabaseUpdateResult result, Object passthrough);
  }

  // ---------------------------------------------------------------------------
  // instance
  // ---------------------------------------------------------------------------

  private Context context;
  private SQLiteStore db;
  private DatabaseUpdateResult databaseUpdateResult;

  public Update(Activity context) {
    // IMPORTANT: Do NOT use "context.getApplicationContext()". Context must be for an Activity (NOT an Application) to create a ProgressDialog or AlertDialog (if needed).
    this.context = (Context) context;
    this.db      = SQLiteStore.getInstance(context);
  }

  private Cursor query(String query) {
    return Update.query(db, query);
  }

  private boolean execQuery(String query) {
    return Update.execQuery(db, query);
  }

  private boolean execTransaction(List<String> queries) {
    return Update.execTransaction(db, queries);
  }

  public void updateDatabase(DatabaseUpdateListener listener, int mode) {
    Object passthrough = null;
    updateDatabase(listener, mode, passthrough);
  }

  public void updateDatabase(DatabaseUpdateListener listener, int mode, Object passthrough) {
    boolean skipPreDatabaseUpdateCallback = false;
    updateDatabase(listener, mode, passthrough, skipPreDatabaseUpdateCallback);
  }

  public void updateDatabase(DatabaseUpdateListener listener, int mode, Object passthrough, boolean skipPreDatabaseUpdateCallback) {
    Runnable updateDatabase = new Runnable() {
      public void run() {
        Looper.prepare();
        Log.d(Constants.LOG_TAG, "Database update routine");

        databaseUpdateResult = new DatabaseUpdateResult();

        int     oldVersion = getVersionNumber();
        boolean willUpdate = Update.needsUpdate(db);

        if (!skipPreDatabaseUpdateCallback) {
          if (listener != null) {
            boolean proceed = listener.preDatabaseUpdate(mode, oldVersion, willUpdate);
            if (!proceed) {
              Looper.loop();
              return;
            }
          }
        }

        if (willUpdate) {
          Log.d(Constants.LOG_TAG, "Database needs update");

          updateDatabaseVersion();
        }

        if (databaseUpdateResult.didUpdateFail)
          Log.e(Constants.LOG_TAG, "Attempt to update version of database schema has failed");
        else if (databaseUpdateResult.didUpdateSucceed)
          Log.d(Constants.LOG_TAG, "Version of database schema has been updated");
        else
          Log.d(Constants.LOG_TAG, "Version of database schema did not require an update");

        if (listener != null)
          listener.postDatabaseUpdate(mode, oldVersion, databaseUpdateResult, passthrough);

        Looper.loop();
      }
    };
    Thread updateDatabaseTh = new Thread(updateDatabase);
    updateDatabaseTh.start();
  }

  private int getVersionNumber() {
    return Update.getVersion(db, /* resetCache */ true);
  }

  private void updateDatabaseVersion() {
    boolean didUpdate, result;
    int version;

    databaseUpdateResult.didUpdateSucceed = true;
    didUpdate                             = false;
    version                               = getVersionNumber();

    if (!databaseUpdateResult.didUpdateFail && (version == 1)) {
      didUpdate                                = true;
      result                                   = update_version_001();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }

    databaseUpdateResult.didUpdateSucceed &= didUpdate;
  }

  private boolean update_version_001() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 2");
    try {
      List<String> queries = new ArrayList<String>();
      queries.add("UPDATE application SET version=2");
      queries.add(
          "INSERT INTO intent_extra_value_types (name) VALUES"
        + "  ('Bitmap'),"
        + "  ('Bitmap[]'),"
        + "  ('ArrayList<Bitmap>'),"
        + "  ('Uri'),"
        + "  ('Uri[]'),"
        + "  ('ArrayList<Uri>');"
      );
      return execTransaction(queries);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  // ---------------------------------------------------------------------------

  public static void handleDatabaseUpdateResultErrors(Activity context, int oldVersion, DatabaseUpdateResult result, String logFolder) {
    if (result.didUpdateFail) {
      String error = context.getString(R.string.messages_db_error_update);
      Toast.makeText(context.getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }
  }

}
