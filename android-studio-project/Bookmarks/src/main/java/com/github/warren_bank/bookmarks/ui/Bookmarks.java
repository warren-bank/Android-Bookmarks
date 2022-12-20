package com.github.warren_bank.bookmarks.ui;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.database.Update;
import com.github.warren_bank.bookmarks.database.Update.DatabaseUpdateResult;
import com.github.warren_bank.bookmarks.ui.SaveBookmark;
import com.github.warren_bank.bookmarks.ui.dialogs.DbFolderPicker;
import com.github.warren_bank.bookmarks.ui.dialogs.FilesystemDirectoryPicker;
import com.github.warren_bank.bookmarks.ui.dialogs.FilesystemDirectoryPickerSimpleListener;
import com.github.warren_bank.bookmarks.ui.dialogs.FolderContentsPickerSimpleListener;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;
import com.github.warren_bank.bookmarks.ui.widgets.FolderBreadcrumbsLayout;
import com.github.warren_bank.bookmarks.ui.widgets.FolderContentsAdapter;
import com.github.warren_bank.bookmarks.utils.FileUtils;
import com.github.warren_bank.bookmarks.utils.HtmlBookmarkUtils;
import com.github.warren_bank.bookmarks.utils.JsonBookmarkUtils;
import com.github.warren_bank.bookmarks.utils.RuntimePermissionUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bookmarks extends ListActivity implements RuntimePermissionUtils.RuntimePermissionListener, Update.DatabaseUpdateListener {
  /* Formats for Export and Import */
  private static final int FORMAT_DB                        = 1;
  private static final int FORMAT_HTML                      = 2;
  private static final int FORMAT_JSON                      = 3;
  /* ActionBar Menu Items */
  private static final int MENU_ACTIONBAR_INTENT_SEARCH     = Menu.FIRST;
  private static final int MENU_ACTIONBAR_FOLDER_ADD        = MENU_ACTIONBAR_INTENT_SEARCH     + 1;
  private static final int MENU_ACTIONBAR_INTENT_ADD        = MENU_ACTIONBAR_FOLDER_ADD        + 1;
  private static final int MENU_ACTIONBAR_MOVE_COMPLETE     = MENU_ACTIONBAR_INTENT_ADD        + 1;
  private static final int MENU_ACTIONBAR_MOVE_CANCEL       = MENU_ACTIONBAR_MOVE_COMPLETE     + 1;
  private static final int MENU_ACTIONBAR_SETTINGS          = MENU_ACTIONBAR_MOVE_CANCEL       + 1;
  private static final int MENU_ACTIONBAR_DB_BACKUP         = MENU_ACTIONBAR_SETTINGS          + 1;
  private static final int MENU_ACTIONBAR_DB_RESTORE        = MENU_ACTIONBAR_DB_BACKUP         + 1;
  private static final int MENU_ACTIONBAR_DB_EXPORT         = MENU_ACTIONBAR_DB_RESTORE        + 1;
  private static final int MENU_ACTIONBAR_DB_IMPORT         = MENU_ACTIONBAR_DB_EXPORT         + 1;
  private static final int MENU_ACTIONBAR_EXIT              = MENU_ACTIONBAR_DB_IMPORT         + 1;
  /* Context Menu Items for Folders */
  private static final int MENU_CONTEXT_FOLDER_HIDE         = Menu.FIRST;
  private static final int MENU_CONTEXT_FOLDER_UNHIDE       = MENU_CONTEXT_FOLDER_HIDE         + 1;
  private static final int MENU_CONTEXT_FOLDER_RENAME       = MENU_CONTEXT_FOLDER_UNHIDE       + 1;
  private static final int MENU_CONTEXT_FOLDER_MOVE         = MENU_CONTEXT_FOLDER_RENAME       + 1;
  private static final int MENU_CONTEXT_FOLDER_DELETE       = MENU_CONTEXT_FOLDER_MOVE         + 1;
  /* Context Menu Items for Intents */
  private static final int MENU_CONTEXT_INTENT_PERFORM      = Menu.FIRST;
  private static final int MENU_CONTEXT_INTENT_EDIT         = MENU_CONTEXT_INTENT_PERFORM      + 1;
  private static final int MENU_CONTEXT_INTENT_MOVE         = MENU_CONTEXT_INTENT_EDIT         + 1;
  private static final int MENU_CONTEXT_INTENT_COPY         = MENU_CONTEXT_INTENT_MOVE         + 1;
  private static final int MENU_CONTEXT_INTENT_DELETE       = MENU_CONTEXT_INTENT_COPY         + 1;

  // Preferences

  private static final String PREF_NAME = "BookmarksPrefs";

  // ===============
  // Settings Dialog
  // ===============

  // Show hidden folders?
  private static final String SHOW_HIDDEN_PREF_NAME = "show_hidden";
  private static boolean showHidden;

  // Perform automatic daily backup?
  private static final String AUTO_BACKUP_PREF_NAME = "auto_backup";
  private static boolean autoBackup;

  // Keep the 3 most recent automatic daily backups?
  private static final String BACKUP_VERSIONING_PREF_NAME = "backup_versioning";
  private static boolean backupVersioning;

  // Change initial folder to display at startup..
  private static final String STARTUP_FOLDER_PREF_NAME = "startup_folder";
  private static int startupFolderId;
  private static String startupFolderPath;

  // Change output directory for backups and exports..
  private static final String OUTPUT_DIRECTORY_PREF_NAME = "output_directory";
  private static String outputDirectoryPath;

  // ====================================
  // Inner class: search query parameters
  // ====================================

  private static class SearchParameters {
    public String  query;
    public boolean currentFolderOnly;

    public SearchParameters(String query, boolean currentFolderOnly) {
      this.query             = query;
      this.currentFolderOnly = currentFolderOnly;
    }
  }

  // ==================
  // Instance variables
  // ==================

  private DbGateway db;
  private SharedPreferences sharedPrefs;
  private View mainView;
  private FolderBreadcrumbsLayout folder_breadcrumbs;
  private SearchParameters searchParams;

  private ListView listView;
  private List<Integer> moveFolderIds;
  private List<Integer> moveIntentIds;
  private FolderContentItem currentFolder;
  private List<FolderContentItem> currentFolderContentItems;
  private FolderContentsAdapter currentFolderContentsAdapter;
  private InputMethodManager keyboard;
  private AlertDialog alertDialog;

  // ---------------------------------------------------------------------------
  // State of instance variables
  // ---------------------------------------------------------------------------

  private boolean isSearchResult() {
    return (searchParams != null);
  }

  private void updateSearchParams(SearchParameters newParams) {
    if ((searchParams == null) && (newParams == null)) return;

    boolean isDifferentMode = (
      ((searchParams == null) && (newParams != null)) ||
      ((searchParams != null) && (newParams == null))
    );

    searchParams = newParams;

    if (isDifferentMode) {
      updateViewForEmptyList();

      if (Build.VERSION.SDK_INT >= 11)
        invalidateOptionsMenu();
    }
  }

  // ---------------------------------------------------------------------------
  // Update layout based on the state of instance variables
  // ---------------------------------------------------------------------------

  private void updateViewForEmptyList() {
    findViewById(R.id.no_intents_in_folder ).setVisibility(!isSearchResult() ? View.VISIBLE : View.GONE);
    findViewById(R.id.no_results_for_search).setVisibility( isSearchResult() ? View.VISIBLE : View.GONE);
  }

  // ======================
  // Lifecycle
  // ======================

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Prevent multiple instances: https://stackoverflow.com/a/11042163
    if (!isTaskRoot()) {
      final Intent intent = getIntent();
      if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
        finish();
        return;
      }
    }

    setContentView(R.layout.activity_bookmarks);

    db                  = DbGateway.getInstance(Bookmarks.this);
    sharedPrefs         = getSharedPreferences(PREF_NAME, 0);
    mainView            = findViewById(R.id.main);
    folder_breadcrumbs  = (FolderBreadcrumbsLayout) findViewById(R.id.folder_breadcrumbs);
    searchParams        = null;

    showHidden          = sharedPrefs.getBoolean(SHOW_HIDDEN_PREF_NAME, true);
    autoBackup          = sharedPrefs.getBoolean(AUTO_BACKUP_PREF_NAME, false);
    backupVersioning    = sharedPrefs.getBoolean(BACKUP_VERSIONING_PREF_NAME, true);
    startupFolderId     = sharedPrefs.getInt(STARTUP_FOLDER_PREF_NAME, 0);
    startupFolderPath   = db.getFolderPath(startupFolderId);
    outputDirectoryPath = sharedPrefs.getString(OUTPUT_DIRECTORY_PREF_NAME, (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.default_backups_directory_name)));

    listView = getListView();
    listView.setDivider(null);
    registerForContextMenu(listView);

    moveFolderIds = new ArrayList<Integer>();
    moveIntentIds = new ArrayList<Integer>();
    currentFolder = null;
    currentFolderContentItems    = new ArrayList<FolderContentItem>();
    currentFolderContentsAdapter = new FolderContentsAdapter(Bookmarks.this, currentFolderContentItems, showHidden);
    setListAdapter(currentFolderContentsAdapter);

    updateDatabase(Update.MODE_INSTALL);
    getFolderContentItems();

    keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    alertDialog = null;
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if ((intent != null) && intent.getBooleanExtra(Constants.EXTRA_RELOAD_LIST, false)) {
      if (isSearchResult())
        execSearchQuery();
      else
        getFolderContentItems();
    }
  }

  @Override
  public void onBackPressed() {
    if (isSearchResult()) {
      updateSearchParams(null);
      getFolderContentItems();
      return;
    }
    if ((currentFolder != null) && (currentFolder.id > 0)) {
      FolderContentItem parentFolder = db.getParentFolderContentItem(currentFolder.id);

      if ((parentFolder != null) && (parentFolder.id >= 0) && (parentFolder.id != currentFolder.id)) {
        currentFolder = parentFolder;
        getFolderContentItems();
        return;
      }
    }
    super.onBackPressed();
  }

  @Override
  public void onPause() {
    SharedPreferences.Editor ed = sharedPrefs.edit();
    ed.putBoolean(SHOW_HIDDEN_PREF_NAME, showHidden);
    ed.putBoolean(AUTO_BACKUP_PREF_NAME, autoBackup);
    ed.putBoolean(BACKUP_VERSIONING_PREF_NAME, backupVersioning);
    ed.putInt(STARTUP_FOLDER_PREF_NAME, startupFolderId);
    ed.putString(OUTPUT_DIRECTORY_PREF_NAME, outputDirectoryPath);
    ed.commit();

    super.onPause();
  }

  @Override
  protected void onStop() {
    if (autoBackup)
      backup(true);

    super.onStop();
  }

  // ---------------------------------------------------------------------------
  // Update Database
  // ---------------------------------------------------------------------------

  private void updateDatabase(int mode) {
    Object passthrough = null;
    updateDatabase(mode, passthrough);
  }

  private void updateDatabase(int mode, Object passthrough) {
    boolean skipPreDatabaseUpdateCallback = false;
    updateDatabase(mode, passthrough, skipPreDatabaseUpdateCallback);
  }

  private void updateDatabase(int mode, Object passthrough, boolean skipPreDatabaseUpdateCallback) {
    Update updateDS = new Update(Bookmarks.this);
    updateDS.updateDatabase(Bookmarks.this, mode, passthrough, skipPreDatabaseUpdateCallback);
  }

  @Override // Update.DatabaseUpdateListener
  public boolean preDatabaseUpdate(int mode, int oldVersion, boolean willUpdate) {
    if ((mode == Update.MODE_INSTALL) && willUpdate) {
      boolean auto          = false;
      boolean isPreUpdate   = true;
      String backupFileName = FileUtils.getDatabaseFileName(Bookmarks.this, auto, isPreUpdate, oldVersion);
      backupPermissionCheck(auto, outputDirectoryPath, backupFileName, isPreUpdate);
      return false;
    }

    return true;
  }

  @Override // Update.DatabaseUpdateListener
  public void postDatabaseUpdate(int mode, int oldVersion, DatabaseUpdateResult result, Object passthrough) {
    Update.handleDatabaseUpdateResultErrors(Bookmarks.this, oldVersion, result, outputDirectoryPath);

    switch (mode) {
      case Update.MODE_INSTALL: {
        if (result.didUpdateSucceed) {
          getFolderContentItemsOnUiThread();
        }
        break;
      }

      case Update.MODE_RESTORE: {
        if (!result.didUpdateFail) {
          String backupFile = (String) passthrough;
          String toastTxt   = getString(R.string.dialog_restore_done) + "\n(" + backupFile + ")";
          Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_LONG).show();

          FileUtils.cleanDatabaseDirectory(Bookmarks.this);

          getFolderContentItemsOnUiThread();
        }
        break;
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Refresh current directory
  // ---------------------------------------------------------------------------

  private void getFolderContentItemsOnUiThread() {
    runOnUiThread(new Runnable() {
      public void run() {
        getFolderContentItems();
      }
    });
  }

  private void getFolderContentItems() {
    if (currentFolder == null)
      currentFolder = db.getFolderContentItem(startupFolderId);

    if (currentFolder == null)
      return;

    updateSearchParams(null);

    // update breadcrumbs
    folder_breadcrumbs.populate(
      currentFolder.id,
      R.layout.folder_breadcrumbs_link,
      R.layout.folder_breadcrumbs_separator
    );

    // update list of contents
    currentFolderContentItems.clear();
    currentFolderContentItems.addAll(
      db.getFolderContentItems(currentFolder.id)
    );
    currentFolderContentsAdapter.notifyDataSetChanged();
  }

  // ---------------------------------------------------------------------------
  // ActionBar Menu
  // ---------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item;

    if (!isSearchResult()) {
      item = menu.add(Menu.NONE, MENU_ACTIONBAR_INTENT_SEARCH, MENU_ACTIONBAR_INTENT_SEARCH, R.string.menu_actionbar_intent_search);
      item.setIcon(android.R.drawable.ic_menu_search);
      if (Build.VERSION.SDK_INT >= 11)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

      menu.add(Menu.NONE, MENU_ACTIONBAR_FOLDER_ADD,     MENU_ACTIONBAR_FOLDER_ADD,     R.string.menu_actionbar_folder_add);
      menu.add(Menu.NONE, MENU_ACTIONBAR_INTENT_ADD,     MENU_ACTIONBAR_INTENT_ADD,     R.string.menu_actionbar_intent_add);
      menu.add(Menu.NONE, MENU_ACTIONBAR_MOVE_COMPLETE,  MENU_ACTIONBAR_MOVE_COMPLETE,  R.string.menu_actionbar_move_complete);
      menu.add(Menu.NONE, MENU_ACTIONBAR_MOVE_CANCEL,    MENU_ACTIONBAR_MOVE_CANCEL,    R.string.menu_actionbar_move_cancel);
      menu.add(Menu.NONE, MENU_ACTIONBAR_SETTINGS,       MENU_ACTIONBAR_SETTINGS,       R.string.menu_actionbar_settings);
      menu.add(Menu.NONE, MENU_ACTIONBAR_DB_BACKUP,      MENU_ACTIONBAR_DB_BACKUP,      R.string.menu_actionbar_db_backup);
      menu.add(Menu.NONE, MENU_ACTIONBAR_DB_RESTORE,     MENU_ACTIONBAR_DB_RESTORE,     R.string.menu_actionbar_db_restore);
      menu.add(Menu.NONE, MENU_ACTIONBAR_DB_EXPORT,      MENU_ACTIONBAR_DB_EXPORT,      R.string.menu_actionbar_db_export);
      menu.add(Menu.NONE, MENU_ACTIONBAR_DB_IMPORT,      MENU_ACTIONBAR_DB_IMPORT,      R.string.menu_actionbar_db_import);
      menu.add(Menu.NONE, MENU_ACTIONBAR_EXIT,           MENU_ACTIONBAR_EXIT,           R.string.menu_actionbar_exit);
    }
    else {
      item = menu.add(Menu.NONE, MENU_ACTIONBAR_INTENT_SEARCH, MENU_ACTIONBAR_INTENT_SEARCH, R.string.menu_actionbar_intent_search);
      item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
      if (Build.VERSION.SDK_INT >= 11)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

      menu.add(Menu.NONE, MENU_ACTIONBAR_EXIT,           MENU_ACTIONBAR_EXIT,           R.string.menu_actionbar_exit);
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (!isSearchResult()) {
      boolean hasItemsToMove = !moveFolderIds.isEmpty() || !moveIntentIds.isEmpty();

      menu.findItem(MENU_ACTIONBAR_MOVE_COMPLETE)
        .setVisible(hasItemsToMove);
      menu.findItem(MENU_ACTIONBAR_MOVE_CANCEL)
        .setVisible(hasItemsToMove);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  // ---------------------------------------------------------------------------
  // ActionBar Menu Items
  // ---------------------------------------------------------------------------

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ACTIONBAR_INTENT_SEARCH :
        if (isSearchResult()) {
          // clear the visible search results, and display the current folder
          updateSearchParams(null);
          getFolderContentItems();
        }
        else {
          // display a dialog to execute a search query
          searchBookmarks();
        }
        break;
      case MENU_ACTIONBAR_FOLDER_ADD :
        addFolder();
        break;
      case MENU_ACTIONBAR_INTENT_ADD :
        addBookmark();
        break;
      case MENU_ACTIONBAR_MOVE_COMPLETE :
        completeMove();
        break;
      case MENU_ACTIONBAR_MOVE_CANCEL :
        moveFolderIds.clear();
        moveIntentIds.clear();
        break;
      case MENU_ACTIONBAR_SETTINGS :
        showSettings();
        break;
      case MENU_ACTIONBAR_DB_BACKUP :
        backup(false);
        break;
      case MENU_ACTIONBAR_DB_RESTORE :
        restore();
        break;
      case MENU_ACTIONBAR_DB_EXPORT :
        exportBookmarks();
        break;
      case MENU_ACTIONBAR_DB_IMPORT :
        importBookmarks();
        break;
      case MENU_ACTIONBAR_EXIT :
        onPause();  // save options
        backup(true);
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
    FolderContentItem selectedItem = currentFolderContentsAdapter.getItem(info.position);

    menu.setHeaderTitle(selectedItem.name);

    if (selectedItem.isFolder) {
      if (selectedItem.isHidden)
        menu.add(Menu.NONE, MENU_CONTEXT_FOLDER_UNHIDE,     MENU_CONTEXT_FOLDER_UNHIDE,       R.string.menu_context_folder_unhide);
      else
        menu.add(Menu.NONE, MENU_CONTEXT_FOLDER_HIDE,       MENU_CONTEXT_FOLDER_HIDE,         R.string.menu_context_folder_hide);

      menu.add(Menu.NONE, MENU_CONTEXT_FOLDER_RENAME,       MENU_CONTEXT_FOLDER_RENAME,       R.string.menu_context_folder_rename);
      menu.add(Menu.NONE, MENU_CONTEXT_FOLDER_MOVE,         MENU_CONTEXT_FOLDER_MOVE,         R.string.menu_context_folder_move);
      menu.add(Menu.NONE, MENU_CONTEXT_FOLDER_DELETE,       MENU_CONTEXT_FOLDER_DELETE,       R.string.menu_context_folder_delete);
    }
    else {
      menu.add(Menu.NONE, MENU_CONTEXT_INTENT_PERFORM,      MENU_CONTEXT_INTENT_PERFORM,      R.string.menu_context_intent_perform);
      menu.add(Menu.NONE, MENU_CONTEXT_INTENT_EDIT,         MENU_CONTEXT_INTENT_EDIT,         R.string.menu_context_intent_edit);
      menu.add(Menu.NONE, MENU_CONTEXT_INTENT_MOVE,         MENU_CONTEXT_INTENT_MOVE,         R.string.menu_context_intent_move);
      menu.add(Menu.NONE, MENU_CONTEXT_INTENT_COPY,         MENU_CONTEXT_INTENT_COPY,         R.string.menu_context_intent_copy);
      menu.add(Menu.NONE, MENU_CONTEXT_INTENT_DELETE,       MENU_CONTEXT_INTENT_DELETE,       R.string.menu_context_intent_delete);
    }
  }

  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    FolderContentItem selectedItem = currentFolderContentsAdapter.getItem(info.position);

    if (selectedItem.isFolder) {
      switch(item.getItemId()) {
        case MENU_CONTEXT_FOLDER_UNHIDE :
          unhideFolder(selectedItem);
          return true;
        case MENU_CONTEXT_FOLDER_HIDE :
          hideFolder(selectedItem);
          return true;
        case MENU_CONTEXT_FOLDER_RENAME :
          renameFolder(selectedItem);
          return true;
        case MENU_CONTEXT_FOLDER_MOVE :
          moveFolder(selectedItem);
          return true;
        case MENU_CONTEXT_FOLDER_DELETE :
          deleteFolder(selectedItem);
          return true;
      }
    }
    else {
      switch(item.getItemId()) {
        case MENU_CONTEXT_INTENT_PERFORM :
          performBookmark(selectedItem);
          return true;
        case MENU_CONTEXT_INTENT_EDIT :
          editBookmark(selectedItem);
          return true;
        case MENU_CONTEXT_INTENT_MOVE :
          moveBookmark(selectedItem);
          return true;
        case MENU_CONTEXT_INTENT_COPY :
          copyBookmark(selectedItem);
          return true;
        case MENU_CONTEXT_INTENT_DELETE :
          deleteBookmark(selectedItem);
          return true;
      }
    }

    return super.onContextItemSelected(item);
  }

  // ---------------------------------------------------------------------------
  // ListView OnClickListener
  // ---------------------------------------------------------------------------

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    FolderContentItem selectedItem = currentFolderContentsAdapter.getItem(position);

    if (selectedItem.isFolder) {
      currentFolder = selectedItem;
      getFolderContentItems();
    }
    else {
      intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_ACTIVITY);
    }
  }

  // ---------------------------------------------------------------------------
  // layout View OnClickListener
  // ---------------------------------------------------------------------------

  public void addBookmark(View v) {
    addBookmark();
  }

  public void openBreadcrumb(View v) {
    if (v != null) {
      FolderContentItem breadcrumbFolder = (FolderContentItem) v.getTag();

      if ((breadcrumbFolder != null) && (breadcrumbFolder.id >= 0) && ((currentFolder == null) || (currentFolder.id != breadcrumbFolder.id))) {
        currentFolder = breadcrumbFolder;
        getFolderContentItems();
      }
    }
  }

  // ---------------------------------------------------------------------------
  // implementation: ActionBar Menu
  // ---------------------------------------------------------------------------

  private void searchBookmarks() {
    View intent_search = View.inflate(Bookmarks.this, R.layout.dialog_intent_search, null);

    new AlertDialog.Builder(Bookmarks.this)
      .setView(intent_search)
      .setTitle(R.string.dialog_intent_search)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          boolean wasSearchResult     = isSearchResult();
          String  query               = ((EditText) intent_search.findViewById(R.id.query)).getText().toString().trim();
          boolean only_current_folder = ((CheckBox) intent_search.findViewById(R.id.only_current_folder)).isChecked();

          SearchParameters newParams = (!TextUtils.isEmpty(query))
            ? new SearchParameters(query, only_current_folder)
            : null;

          updateSearchParams(newParams);

          if (isSearchResult())
            execSearchQuery();
          else if (wasSearchResult)
            getFolderContentItems();
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void addFolder() {
    updateFolder(null);
  }

  private void addBookmark() {
    updateBookmark(null);
  }

  private void completeMove() {
    if (!moveFolderIds.isEmpty()) {
      for (Integer folderId : moveFolderIds) {
        db.setFolderParentId(/* id */ folderId, /* parentId */ currentFolder.id);
      }
      moveFolderIds.clear();
    }

    if (!moveIntentIds.isEmpty()) {
      for (Integer intentId : moveIntentIds) {
        db.setIntentFolderId(/* id */ intentId, /* folderId */ currentFolder.id);
      }
      moveIntentIds.clear();
    }

    getFolderContentItems();
  }

  private void exportBookmarks() {
    chooseImportExportFormat(/* restoring */ false);
  }

  private void importBookmarks() {
    chooseImportExportFormat(/* restoring */ true);
  }

  private void chooseImportExportFormat(final boolean restoring) {
    String[] import_export_formats = getResources().getStringArray(R.array.import_export_formats);

    new AlertDialog.Builder(Bookmarks.this)
      .setTitle(R.string.dialog_import_export_format)
      .setItems(import_export_formats, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          int format = -1;

          if ((which >= 0) && (which < import_export_formats.length)) {
            switch(import_export_formats[which]) {
              case "HTML":
                format = FORMAT_HTML;
                break;
              case "JSON":
                format = FORMAT_JSON;
                break;
            }
          }

          if (format >= 0) {
            if (restoring)
              importBookmarks(format);
            else
              exportBookmarks(format);
          }
        }
      })
      .show();
  }

  // ---------------------------------------------------------------------------
  // implementation: ListView Context Menu (for Folder)
  // ---------------------------------------------------------------------------

  private void unhideFolder(FolderContentItem selectedItem) {
    if (selectedItem.isFolder && selectedItem.isHidden) {
      db.unhideFolder(selectedItem.id);

      selectedItem.isHidden = false;

      if (!showHidden)
        currentFolderContentsAdapter.notifyDataSetChanged();
    }
  }

  private void hideFolder(FolderContentItem selectedItem) {
    if (selectedItem.isFolder && !selectedItem.isHidden) {
      db.hideFolder(selectedItem.id);

      selectedItem.isHidden = true;

      if (!showHidden)
        currentFolderContentsAdapter.notifyDataSetChanged();
    }
  }

  private void renameFolder(FolderContentItem selectedItem) {
    if (selectedItem.isFolder) {
      updateFolder(selectedItem);
    }
  }

  private void moveFolder(FolderContentItem selectedItem) {
    if (selectedItem.isFolder) {
      moveFolderIds.add(selectedItem.id);
    }
  }

  private void deleteFolder(FolderContentItem selectedItem) {
    if (selectedItem.isFolder) {
      deleteFolderContentItem(selectedItem);
    }
  }

  // ---------------------------------------------------------------------------
  // implementation: ListView Context Menu (for Intent)
  // ---------------------------------------------------------------------------

  private void performBookmark(FolderContentItem selectedItem) {
    if (selectedItem.isFolder) return;

    String[] perform_options = getResources().getStringArray(R.array.perform_options);

    if (Build.VERSION.SDK_INT < 26) {
      // remove: perform_start_foreground_service

      String[] all_options = perform_options;
      perform_options = new String[all_options.length - 1];

      for (int i=0; i < all_options.length; i++) {
        if (i < 2) {
          perform_options[i] = all_options[i];
        }
        else if (i > 2) {
          perform_options[i-1] = all_options[i];
        }
      }
    }

    new AlertDialog.Builder(Bookmarks.this)
      .setTitle(R.string.perform_title)
      .setItems(perform_options, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          if ((Build.VERSION.SDK_INT < 26) && (which >= 2))
            which++;

          switch(which) {
            case 0: { // perform_send_broadcast
                intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_SEND_BROADCAST);
              }
              break;
            case 1: { // perform_start_activity
                intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_ACTIVITY);
              }
              break;
            case 2: { // perform_start_foreground_service
                intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_FOREGROUND_SERVICE);
              }
              break;
            case 3: { // perform_start_service
                intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_SERVICE);
              }
              break;
            case 4: { // perform_stop_service
                intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_STOP_SERVICE);
              }
              break;
            case 5: { // perform_add_shortcut
                Object passthrough = (Object) selectedItem;
                intentPermissionCheck(selectedItem.id, Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_ADD_SHORTCUT, passthrough);
              }
              break;
          }
        }
      })
      .show();
  }

  private void addShortcutForBookmark(FolderContentItem selectedItem) {
    Intent intent = db.getIntent(selectedItem.id);
    if (intent == null) return;

    try {
      if (Build.VERSION.SDK_INT >= 26) {
        ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(getApplicationContext(), Integer.toString(selectedItem.id))
          .setShortLabel(selectedItem.name)
          .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.icon_shortcut))
          .setIntent(intent)
          .build();

        ShortcutManager shortcutManager = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);
        shortcutManager.requestPinShortcut(shortcutInfo, null);
      }
      else {
        Intent addIntent = new Intent();
        addIntent.putExtra(
          Intent.EXTRA_SHORTCUT_INTENT,
          intent
        );
        addIntent.putExtra(
          Intent.EXTRA_SHORTCUT_NAME,
          selectedItem.name
        );
        addIntent.putExtra(
          Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
          Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon_shortcut)
        );
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false);
        getApplicationContext().sendBroadcast(addIntent);
      }
    }
    catch(Exception e) {}
  }

  private void editBookmark(FolderContentItem selectedItem) {
    if (!selectedItem.isFolder) {
      updateBookmark(selectedItem);
    }
  }

  private void moveBookmark(FolderContentItem selectedItem) {
    if (!selectedItem.isFolder) {
      moveIntentIds.add(selectedItem.id);
    }
  }

  private void copyBookmark(FolderContentItem selectedItem) {
    if (!selectedItem.isFolder) {
      db.copyIntent(selectedItem.id);

      if (isSearchResult())
        execSearchQuery();
      else
        getFolderContentItems();
    }
  }

  private void deleteBookmark(FolderContentItem selectedItem) {
    if (!selectedItem.isFolder) {
      deleteFolderContentItem(selectedItem);
    }
  }

  // ---------------------------------------------------------------------------
  // implementation: add/rename Folder
  // ---------------------------------------------------------------------------

  private void updateFolder(FolderContentItem folder) {
    EditText input = new EditText(Bookmarks.this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);

    new AlertDialog.Builder(Bookmarks.this)
      .setTitle((folder == null) ? R.string.folder_add_title : R.string.folder_rename_title)
      .setView(input)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
          String folderName = input.getText().toString().trim();

          if (!TextUtils.isEmpty(folderName)) {
            if (folder == null)
              db.addFolder(/* parentId */ currentFolder.id, /* name */ folderName);
            else
              db.renameFolder(/* id */ folder.id, /* name */ folderName);

            getFolderContentItems();
          }
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
          return;
        }
      })
      .show();

    input.setText((folder == null) ? "" : folder.name);
    input.requestFocus();
  }

  // ---------------------------------------------------------------------------
  // implementation: add/edit Intent
  // ---------------------------------------------------------------------------

  private void updateBookmark(FolderContentItem bookmark) {
    Intent intent = new Intent(Bookmarks.this, SaveBookmark.class);

    if ((bookmark != null) && !bookmark.isFolder)
      intent.putExtra(Constants.EXTRA_INTENT_ID, bookmark.id);
    else if (currentFolder != null)
      intent.putExtra(Constants.EXTRA_FOLDER_ID, currentFolder.id);

    startActivity(intent);
  }

  // ---------------------------------------------------------------------------
  // implementation: delete Intent or folder
  // ---------------------------------------------------------------------------

  private void deleteFolderContentItem(FolderContentItem selectedItem) {
    int    title   = selectedItem.isFolder ? R.string.dialog_delete_folder_title : R.string.dialog_delete_intent_title;
    String message = getString(R.string.dialog_delete_confirm, selectedItem.name);

    new AlertDialog.Builder(Bookmarks.this)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          if (selectedItem.isFolder)
            db.deleteFolder(selectedItem.id);
          else
            db.deleteIntent(selectedItem.id);

          if (currentFolderContentItems.remove(selectedItem))
            currentFolderContentsAdapter.notifyDataSetChanged();
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  // ---------------------------------------------------------------------------
  // implementation: execute a search query and display the results
  // ---------------------------------------------------------------------------

  private void execSearchQuery() {
    // handle an inconsistent state that should never be able to occur
    if (searchParams == null) {
      updateSearchParams(null);
      getFolderContentItems();
      return;
    }

    // update breadcrumbs
    folder_breadcrumbs.setText(
      (getString(R.string.layout_main_results_for_search_label) + " " + searchParams.query),
      R.layout.folder_breadcrumbs_separator
    );

    // update list of contents
    currentFolderContentItems.clear();
    currentFolderContentItems.addAll(
      db.searchIntents(
        /* searchTerm */ searchParams.query,
        /* folderId   */ ((searchParams.currentFolderOnly && (currentFolder != null)) ? currentFolder.id : -1),
        /* includeURL */ false
      )
    );
    currentFolderContentsAdapter.notifyDataSetChanged();
  }

  // ---------------------------------------------------------------------------
  // implementation: Settings dialog w/ runtime Permissions check
  // ---------------------------------------------------------------------------

  private void showSettings() {
    if (alertDialog != null) {
      alertDialog.dismiss();
      alertDialog = null;
    }

    View settings = View.inflate(Bookmarks.this, R.layout.dialog_settings, null);

    ((CheckBox) settings.findViewById(R.id.show_hidden)).setChecked(showHidden);
    ((CheckBox) settings.findViewById(R.id.auto_backup)).setChecked(autoBackup);
    ((CheckBox) settings.findViewById(R.id.backup_versioning)).setChecked(backupVersioning);
    ((TextView) settings.findViewById(R.id.startup_folder)).setText(startupFolderPath);
    ((TextView) settings.findViewById(R.id.output_directory)).setText(outputDirectoryPath);

    alertDialog = new AlertDialog.Builder(Bookmarks.this)
      .setView(settings)
      .setTitle(R.string.settings_title)
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          alertDialog.dismiss();
          alertDialog = null;
        }
      })
      .show();
  }

  public void updateSettings(View v) {
    switch(v.getId()) {
      case R.id.show_hidden:
        showHidden ^= true;
        currentFolderContentsAdapter.setShowHidden(showHidden);
        currentFolderContentsAdapter.notifyDataSetChanged();
        break;
      case R.id.auto_backup:
        autoBackup ^= true;
        break;
      case R.id.backup_versioning:
        backupVersioning ^= true;
        break;
      case R.id.change_startup_folder:
        DbFolderPicker.pickFolder(
          /* context  */ Bookmarks.this,
          /* listener */ new FolderContentsPickerSimpleListener() {
            @Override
            public boolean isValidFileToPick(FolderContentItem file) {
              return false;
            }

            @Override
            public void onFolderPick(FolderContentItem folder) {
              startupFolderId   = folder.id;
              startupFolderPath = db.getFolderPath(startupFolderId);

              try {
                ((TextView) alertDialog.findViewById(R.id.startup_folder)).setText(startupFolderPath);
              }
              catch(Exception e) {}
            }
          },
          /* initialFolderId */ startupFolderId
        );
        break;
      case R.id.change_output_directory:
        String[] allRequestedPermissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
        boolean hasAllPermissions = RuntimePermissionUtils.hasAllPermissions(Bookmarks.this, allRequestedPermissions);

        if (!hasAllPermissions) {
          int requestCode = Constants.PERMISSION_CHECK_REQUEST_CODE_CHANGE_DEFAULT_OUTPUT_DIRECTORY_FILEPICKER;
          RuntimePermissionUtils.requestPermissions(Bookmarks.this, Bookmarks.this, allRequestedPermissions, requestCode, /* passthrough */ null);
          break;
        }

        FilesystemDirectoryPicker.pickDirectory(
          /* context  */ Bookmarks.this,
          /* listener */ new FilesystemDirectoryPickerSimpleListener() {
            @Override
            public boolean isValidFileToPick(File file) {
              return false;
            }

            @Override
            public void onDirectoryPick(File dir) {
              outputDirectoryPath = dir.getPath();

              try {
                ((TextView) alertDialog.findViewById(R.id.output_directory)).setText(outputDirectoryPath);
              }
              catch(Exception e) {}
            }
          },
          /* initialDirectoryPath */ outputDirectoryPath
        );
        break;
    }
  }

  // ---------------------------------------------------------------------------
  // implementation: start Intent w/ runtime Permissions check
  // ---------------------------------------------------------------------------

  private void intentPermissionCheck(final int intentId, final int requestCode) {
    Intent intent = db.getIntent(intentId);
    if (intent == null) return;

    RuntimePermissionUtils.requestPermissions(Bookmarks.this, Bookmarks.this, intent, requestCode);
  }

  private void intentPermissionCheck(final int intentId, final int requestCode, final Object passthrough) {
    Intent intent = db.getIntent(intentId);
    if (intent == null) return;

    RuntimePermissionUtils.requestPermissions(Bookmarks.this, Bookmarks.this, intent, requestCode, passthrough);
  }

  // ---------------------------------------------------------------------------
  // implementation: backup/restore/export/import Database w/ runtime Permissions check
  // ---------------------------------------------------------------------------

  private void backup(boolean auto) {
    if (auto) {
      backupPermissionCheck(auto, outputDirectoryPath);
    }
    else {
      filePickerPermissionCheck(outputDirectoryPath, Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_FILEPICKER);
    }
  }

  private void restore() {
    filePickerPermissionCheck(outputDirectoryPath, Constants.PERMISSION_CHECK_REQUEST_CODE_RESTORE_DATABASE_FILEPICKER);
  }

  private void exportBookmarks(final int format) {
    int requestCode = -1;

    switch(format) {
      case FORMAT_HTML:
        requestCode = Constants.PERMISSION_CHECK_REQUEST_CODE_EXPORT_HTML_FILEPICKER;
        break;
      case FORMAT_JSON:
        requestCode = Constants.PERMISSION_CHECK_REQUEST_CODE_EXPORT_JSON_FILEPICKER;
        break;
    }

    if (requestCode >= 0)
      filePickerPermissionCheck(outputDirectoryPath, requestCode);
  }

  private void importBookmarks(final int format) {
    int requestCode = -1;

    switch(format) {
      case FORMAT_HTML:
        requestCode = Constants.PERMISSION_CHECK_REQUEST_CODE_IMPORT_HTML_FILEPICKER;
        break;
      case FORMAT_JSON:
        requestCode = Constants.PERMISSION_CHECK_REQUEST_CODE_IMPORT_JSON_FILEPICKER;
        break;
    }

    if (requestCode >= 0)
      filePickerPermissionCheck(outputDirectoryPath, requestCode);
  }

  private void filePickerPermissionCheck(final String dirPath, final int requestCode) {
    String[] allRequestedPermissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
    Object passthrough = (Object) dirPath;

    RuntimePermissionUtils.requestPermissions(Bookmarks.this, Bookmarks.this, allRequestedPermissions, requestCode, passthrough);
  }

  private class PassthroughBackup {
    protected boolean auto;
    protected String  outputDirectoryPath;
    protected String  backupFileName;

    protected PassthroughBackup(boolean auto, String outputDirectoryPath, String backupFileName) {
      this.auto                = auto;
      this.outputDirectoryPath = outputDirectoryPath;
      this.backupFileName      = backupFileName;
    }
  }

  private void backupPermissionCheck(boolean auto, String outputDirectoryPath) {
    String backupFileName = FileUtils.getDatabaseFileName(Bookmarks.this, auto);
    boolean isPreUpdate   = false;
    backupPermissionCheck(auto, outputDirectoryPath, backupFileName, isPreUpdate);
  }

  private void backupPermissionCheck(boolean auto, String outputDirectoryPath, String backupFileName, boolean isPreUpdate) {
    if (auto && !autoBackup) return;

    String[] allRequestedPermissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};

    int requestCode = isPreUpdate
      ? Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE
      : Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE;

    Object passthrough = (Object) new PassthroughBackup(auto, outputDirectoryPath, backupFileName);

    RuntimePermissionUtils.requestPermissions(Bookmarks.this, Bookmarks.this, allRequestedPermissions, requestCode, passthrough);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.onRequestPermissionsResult(Bookmarks.this, Bookmarks.this, requestCode, permissions, grantResults);
  }

  @Override // RuntimePermissionUtils.RuntimePermissionListener
  public void onRequestPermissionsGranted(int requestCode, Object passthrough) {
    switch(requestCode) {
      case Constants.PERMISSION_CHECK_REQUEST_CODE_CHANGE_DEFAULT_OUTPUT_DIRECTORY_FILEPICKER: {
          if (alertDialog == null) {
            showSettings();
          }
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_RESTORE_DATABASE_FILEPICKER: {
          String  dirPath   = (String) passthrough;
          boolean restoring = true;
          int format        = FORMAT_DB;
          filePicker(dirPath, restoring, format);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_IMPORT_HTML_FILEPICKER: {
          String  dirPath   = (String) passthrough;
          boolean restoring = true;
          int format        = FORMAT_HTML;
          filePicker(dirPath, restoring, format);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_IMPORT_JSON_FILEPICKER: {
          String  dirPath   = (String) passthrough;
          boolean restoring = true;
          int format        = FORMAT_JSON;
          filePicker(dirPath, restoring, format);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_FILEPICKER: {
          String  dirPath   = (String) passthrough;
          boolean restoring = false;
          int format        = FORMAT_DB;
          filePicker(dirPath, restoring, format);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_EXPORT_HTML_FILEPICKER: {
          String  dirPath   = (String) passthrough;
          boolean restoring = false;
          int format        = FORMAT_HTML;
          filePicker(dirPath, restoring, format);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_EXPORT_JSON_FILEPICKER: {
          String  dirPath   = (String) passthrough;
          boolean restoring = false;
          int format        = FORMAT_JSON;
          filePicker(dirPath, restoring, format);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE: {
          // the user has granted the permission required to save a backup of the DB.. before updating the version of the database schema

          PassthroughBackup pb = (PassthroughBackup) passthrough;
          backup(pb.auto, pb.outputDirectoryPath, pb.backupFileName);

          updateDatabase(/* mode */ Update.MODE_INSTALL, /* passthrough */ (Object) null, /* skipPreDatabaseUpdateCallback */ true);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE: {
          PassthroughBackup pb = (PassthroughBackup) passthrough;
          backup(pb.auto, pb.outputDirectoryPath, pb.backupFileName);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_SEND_BROADCAST: {
          Intent intent = (Intent) passthrough;
          sendBroadcast(intent);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_ACTIVITY: {
          Intent intent = (Intent) passthrough;
          try {
            startActivity(intent);
          }
          catch(Exception e) { // ActivityNotFoundException
            Toast.makeText(getApplicationContext(), R.string.messages_activity_not_found, Toast.LENGTH_LONG).show();
          }
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_FOREGROUND_SERVICE: {
          if (Build.VERSION.SDK_INT >= 26) {
            Intent intent = (Intent) passthrough;
            try {
              startForegroundService(intent);
            }
            catch(Exception e) { // SecurityException, ForegroundServiceStartNotAllowedException
              Toast.makeText(getApplicationContext(), R.string.messages_no_permission, Toast.LENGTH_LONG).show();
            }
          }
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_START_SERVICE: {
          Intent intent = (Intent) passthrough;
          try {
            startService(intent);
          }
          catch(Exception e) { // SecurityException, IllegalStateException, BackgroundServiceStartNotAllowedException
            Toast.makeText(getApplicationContext(), R.string.messages_no_permission, Toast.LENGTH_LONG).show();
          }
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_STOP_SERVICE: {
          Intent intent = (Intent) passthrough;
          try {
            stopService(intent);
          }
          catch(Exception e) { // SecurityException, IllegalStateException
            Toast.makeText(getApplicationContext(), R.string.messages_no_permission, Toast.LENGTH_LONG).show();
          }
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_INTENT_ADD_SHORTCUT: {
          FolderContentItem selectedItem = (FolderContentItem) passthrough;
          addShortcutForBookmark(selectedItem);
        }
        break;
    }
  }

  @Override // RuntimePermissionUtils.RuntimePermissionListener
  public void onRequestPermissionsDenied(int requestCode, Object passthrough, String[] missingPermissions) {
    switch(requestCode) {
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE: {
          // the user has denied the permission required to save a backup of the DB.. before updating the version of the database schema;
          // explain the implications and offer one final chance to do so.

          new AlertDialog.Builder(Bookmarks.this)
            .setTitle(R.string.dialog_backup_preupdate_title)
            .setMessage(R.string.dialog_backup_preupdate_message)
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // ask to grant the permissions again

                PassthroughBackup pb = (PassthroughBackup) passthrough;
                backupPermissionCheck(pb.auto, pb.outputDirectoryPath, pb.backupFileName, /* isPreUpdate */ true);
              }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // perform update without backup

                updateDatabase(/* mode */ Update.MODE_INSTALL, /* passthrough */ (Object) null, /* skipPreDatabaseUpdateCallback */ true);
              }
            })
            .show();
        }
        break;
      default: {
          Toast.makeText(getApplicationContext(), R.string.messages_no_permission, Toast.LENGTH_LONG).show();
        }
        break;
    }
  }

  private void filePicker(final String dirPath, final boolean restoring, final int format) {
    File dir = new File(dirPath);
    if (!dir.isDirectory())
      dir.mkdir();

    FilesystemDirectoryPicker.Listener listener = new FilesystemDirectoryPicker.Listener() {
      @Override
      public boolean isValidFileToPick(File file) {
        if (!restoring) return false;

        switch(format) {
          case FORMAT_DB:
            return FileUtils.isDatabaseFile(file);
          case FORMAT_HTML:
            return FileUtils.isHtmlBookmarksFile(file);
          case FORMAT_JSON:
            return FileUtils.isJsonBookmarksFile(file);
        }

        return false;
      }

      @Override
      public void onFilePick(File file) {
        if (!restoring) return;

        switch(format) {
          case FORMAT_DB:
            confirmRestore(file.getPath());
            break;
          case FORMAT_HTML:
          case FORMAT_JSON:
            importBookmarks(format, file.getPath());
            break;
        }
      }

      @Override
      public boolean isValidDirectoryToPick(File dir) {
        return true;
      }

      @Override
      public void onDirectoryPick(File dir) {
        if (restoring) return;

        switch(format) {
          case FORMAT_DB:
            backup(false, dir.getPath());
            break;
          case FORMAT_HTML:
          case FORMAT_JSON:
            exportBookmarks(format, dir.getPath());
            break;
        }
      }
    };

    int resId_pickDirectoryPositiveButton = R.string.dialog_ok;
    if (!restoring) {
      switch(format) {
        case FORMAT_DB:
          resId_pickDirectoryPositiveButton = R.string.dialog_backup;
          break;
        case FORMAT_HTML:
          resId_pickDirectoryPositiveButton = R.string.dialog_export_html;
          break;
        case FORMAT_JSON:
          resId_pickDirectoryPositiveButton = R.string.dialog_export_json;
          break;
      }
    }

    FilesystemDirectoryPicker.showFilePicker(
      /* context */ Bookmarks.this,
      listener,
      dirPath,
      /* showFiles */ restoring,
      resId_pickDirectoryPositiveButton
    );
  }

  private void backup(boolean auto, final String outputDirectoryPath) {
    String backupFileName = FileUtils.getDatabaseFileName(Bookmarks.this, auto);
    backup(auto, outputDirectoryPath, backupFileName);
  }

  private void backup(boolean auto, final String outputDirectoryPath, String backupFileName) {
    File source = new File(FileUtils.getDatabaseFilePath(Bookmarks.this));
    File destination = new File(outputDirectoryPath, backupFileName);
    if (
      auto &&
      (
        !autoBackup || (source.lastModified() == destination.lastModified())
      )
    ) {
      return;
    }
    if (backupVersioning && destination.exists()) {
      File previous0 = new File(outputDirectoryPath, FileUtils.getDatabaseFileNameForBackupVersion(backupFileName, 0));
      if (previous0.exists()) {
        File previous1 = new File(outputDirectoryPath, FileUtils.getDatabaseFileNameForBackupVersion(backupFileName, 1));
        if (previous1.exists())
          previous1.delete();
        previous0.renameTo(previous1);
      }
      destination.renameTo(previous0);
    }
    else {
      destination.delete();
    }
    File folder = new File(outputDirectoryPath);
    if (!folder.isDirectory())
      folder.mkdir();
    int toastTxt = R.string.dialog_backup_done;
    try {
      copy(source, destination);
    }
    catch (IOException e) {
      toastTxt = R.string.dialog_backup_failed;
      e.printStackTrace();
    }
    if (!auto && (toastTxt == R.string.dialog_backup_done)) {
      askToChangeDefaultOutputDirectoryPath(outputDirectoryPath, toastTxt);
    }
    if (!auto && (listView != null)) {
      Toast.makeText(getApplicationContext(), getString(toastTxt) + "\n("+ outputDirectoryPath +")", Toast.LENGTH_LONG).show();
    }
  }

  private void confirmRestore(final String backupFile) {
    new AlertDialog.Builder(Bookmarks.this)
      .setTitle(R.string.dialog_restore)
      .setMessage(R.string.dialog_restore_now)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          restore(backupFile);
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void restore(String backupFile) {
    File source      = new File(backupFile);
    File destination = new File(FileUtils.getDatabaseFilePath(Bookmarks.this));

    if (!source.exists()) {
      Toast.makeText(getApplicationContext(), getString(R.string.dialog_restore_notfound), Toast.LENGTH_LONG).show();
      return;
    }

    try {
      copy(source, destination);
    }
    catch (IOException e) {
      Toast.makeText(getApplicationContext(), getString(R.string.dialog_restore_failed), Toast.LENGTH_LONG).show();
      return;
    }

    updateDatabase(Update.MODE_RESTORE, (Object) backupFile);
  }

  private void copy(File source, File destination) throws IOException {
    if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
      throw new IOException("External Storage is not mounted.");

    db.getSQLiteStore().close();
    try {
      FileUtils.copyFile(source, destination);
    }
    finally {
      db.getSQLiteStore().openDataBase();
    }
  }

  private void importBookmarks(final int format, final String filePath) {
    View import_bookmarks = View.inflate(Bookmarks.this, R.layout.dialog_import_bookmarks, null);

    if (format != FORMAT_HTML) {
      import_bookmarks.findViewById(R.id.allow_duplicate_urls_global).setVisibility(View.GONE);
      import_bookmarks.findViewById(R.id.allow_duplicate_urls_folder).setVisibility(View.GONE);
    }

    int resId_title = R.string.menu_actionbar_db_import;
    switch(format) {
      case FORMAT_HTML:
        resId_title = R.string.dialog_import_html;
        break;
      case FORMAT_JSON:
        resId_title = R.string.dialog_import_json;
        break;
    }

    new AlertDialog.Builder(Bookmarks.this)
      .setView(import_bookmarks)
      .setTitle(resId_title)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          boolean allow_existing_folders      = ((CheckBox) import_bookmarks.findViewById(R.id.allow_existing_folders)     ).isChecked();
          boolean allow_duplicate_urls_global = ((CheckBox) import_bookmarks.findViewById(R.id.allow_duplicate_urls_global)).isChecked();
          boolean allow_duplicate_urls_folder = ((CheckBox) import_bookmarks.findViewById(R.id.allow_duplicate_urls_folder)).isChecked();

          importBookmarks(format, filePath, allow_existing_folders, allow_duplicate_urls_global, allow_duplicate_urls_folder);
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void importBookmarks(final int format, final String filePath, final boolean allow_existing_folders, final boolean allow_duplicate_urls_global, final boolean allow_duplicate_urls_folder) {
    File    inputFile = FileUtils.getFile(filePath);
    int     folderId  = currentFolder.id;
    int     toastId   = -1;
    boolean didImport = false;

    switch(format) {
      case FORMAT_HTML: {
          if (!FileUtils.isHtmlBookmarksFile(inputFile))
            toastId = R.string.dialog_import_notfound;
          else
            didImport = HtmlBookmarkUtils.importHTML(db, inputFile, folderId, allow_existing_folders, allow_duplicate_urls_global, allow_duplicate_urls_folder);
        }
        break;
      case FORMAT_JSON: {
          if (!FileUtils.isJsonBookmarksFile(inputFile))
            toastId = R.string.dialog_import_notfound;
          else
            didImport = JsonBookmarkUtils.importJSON(db, inputFile, folderId, allow_existing_folders);
        }
        break;
    }

    if (toastId == -1) {
      toastId = didImport
        ? R.string.dialog_import_done
        : R.string.dialog_import_failed;
    }

    if (toastId == R.string.dialog_import_done)
      getFolderContentItems();

    Toast.makeText(getApplicationContext(), toastId, Toast.LENGTH_LONG).show();
  }

  private void exportBookmarks(final int format, final String outputDirectoryPath) {
    View export_bookmarks = View.inflate(Bookmarks.this, R.layout.dialog_export_bookmarks, null);

    int resId_title = R.string.menu_actionbar_db_export;
    switch(format) {
      case FORMAT_HTML:
        resId_title = R.string.dialog_export_html;
        break;
      case FORMAT_JSON:
        resId_title = R.string.dialog_export_json;
        break;
    }

    new AlertDialog.Builder(Bookmarks.this)
      .setView(export_bookmarks)
      .setTitle(resId_title)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          boolean only_current_folder = ((CheckBox) export_bookmarks.findViewById(R.id.only_current_folder)).isChecked();
          boolean include_hidden      = ((CheckBox) export_bookmarks.findViewById(R.id.include_hidden)     ).isChecked();

          exportBookmarks(format, outputDirectoryPath, only_current_folder, include_hidden);
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void exportBookmarks(final int format, final String outputDirectoryPath, final boolean only_current_folder, final boolean include_hidden) {
    String outputFileName = null;
    switch(format) {
      case FORMAT_HTML: {
          outputFileName = FileUtils.getHtmlBookmarksFileName(Bookmarks.this);
        }
        break;
      case FORMAT_JSON: {
          outputFileName = FileUtils.getJsonBookmarksFileName(Bookmarks.this);
        }
        break;
    }
    if (outputFileName == null) return;

    File outputFile   = new File(outputDirectoryPath, outputFileName);
    int  folderId     = only_current_folder ? currentFolder.id : 0;
    boolean didExport = false;

    switch(format) {
      case FORMAT_HTML: {
          didExport = HtmlBookmarkUtils.exportHTML(db, outputFile, folderId, include_hidden);
        }
        break;
      case FORMAT_JSON: {
          didExport = JsonBookmarkUtils.exportJSON(db, outputFile, folderId, include_hidden);
        }
        break;
    }

    if (didExport) {
      Toast.makeText(getApplicationContext(), getString(R.string.dialog_export_done) + "\n("+ outputDirectoryPath +")", Toast.LENGTH_LONG).show();
      askToChangeDefaultOutputDirectoryPath(outputDirectoryPath, R.string.dialog_export_done);
    }
    else {
      Toast.makeText(getApplicationContext(), R.string.dialog_export_failed, Toast.LENGTH_LONG).show();
    }
  }

  private void askToChangeDefaultOutputDirectoryPath(String outputDirectoryPath, int resId_title) {
    if (!outputDirectoryPath.equals(Bookmarks.outputDirectoryPath)) {
      final CharSequence[] outputDirectoryPaths = {outputDirectoryPath, Bookmarks.outputDirectoryPath};
      new AlertDialog.Builder(Bookmarks.this)
        .setTitle(resId_title)
        .setSingleChoiceItems(outputDirectoryPaths, 1, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            Bookmarks.outputDirectoryPath = outputDirectoryPaths[which].toString();
          }
        })
        .setPositiveButton(R.string.dialog_backup_usefolder, null)
        .show();
    }
  }
}
