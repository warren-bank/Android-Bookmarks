package com.github.warren_bank.bookmarks.ui;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.Constants;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.database.model.DbIntent;
import com.github.warren_bank.bookmarks.ui.Bookmarks;
import com.github.warren_bank.bookmarks.ui.dialogs.DbFolderPicker;
import com.github.warren_bank.bookmarks.ui.listeners.IntentExtraValueTokenSeparatorOnClickListener;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;
import com.github.warren_bank.bookmarks.ui.widgets.ExpandablePanel;
import com.github.warren_bank.bookmarks.ui.widgets.ListViewInScrollView;
import com.github.warren_bank.bookmarks.ui.widgets.TextViewArrayAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveBookmark extends Activity {

  private DbGateway db;
  private DbIntent dbIntent;
  private InputMethodManager keyboard;

  // -----------------------------------
  // misc state
  // -----------------------------------
  private int intentId;
  private int folderId;

  // -----------------------------------
  // lists to hold the state of fields that accept multiple values
  // -----------------------------------
  private List<String>         categoriesList;
  private ArrayAdapter<String> categoriesListAdapter;
  // -----------------------------------
  private List<DbIntent.Extra>         extrasList;
  private ArrayAdapter<DbIntent.Extra> extrasListAdapter;

  // -----------------------------------
  // expandable panel
  // -----------------------------------
  private ExpandablePanel intent_attribute_flags_expandable_panel;

  // -----------------------------------
  // lists to display the state of fields that accept multiple values
  // -----------------------------------
  private ListView intent_attribute_categories_list;
  private ListView intent_attribute_extras_list;

  // -----------------------------------
  // spinners to provide easy access to common values
  // -----------------------------------
  private Spinner intent_attribute_action_spinner;
  private Spinner intent_attribute_data_type_spinner;
  private Spinner intent_attribute_categories_spinner;

  // -----------------------------------
  // input fields
  // -----------------------------------
  private EditText intent_attribute_name;
  private EditText intent_attribute_folder;
  private EditText intent_attribute_action;
  private EditText intent_attribute_package_name;
  private EditText intent_attribute_class_name;
  private EditText intent_attribute_data_uri;
  private EditText intent_attribute_data_type;
  private EditText intent_attribute_categories_text;

  private CheckBox flag_grant_read_uri_permission;
  private CheckBox flag_grant_write_uri_permission;
  private CheckBox flag_from_background;
  private CheckBox flag_debug_log_resolution;
  private CheckBox flag_exclude_stopped_packages;
  private CheckBox flag_include_stopped_packages;
  private CheckBox flag_grant_persistable_uri_permission;
  private CheckBox flag_grant_prefix_uri_permission;
  private CheckBox flag_direct_boot_auto;
  private CheckBox flag_debug_triaged_missing;
  private CheckBox flag_ignore_ephemeral;
  private CheckBox flag_activity_require_default;
  private CheckBox flag_activity_require_non_browser;
  private CheckBox flag_activity_match_external;
  private CheckBox flag_activity_launch_adjacent;
  private CheckBox flag_activity_retain_in_recents;
  private CheckBox flag_activity_task_on_home;
  private CheckBox flag_activity_clear_task;
  private CheckBox flag_activity_no_animation;
  private CheckBox flag_activity_reorder_to_front;
  private CheckBox flag_activity_no_user_action;
  private CheckBox flag_activity_clear_when_task_reset;
  private CheckBox flag_activity_new_document;
  private CheckBox flag_activity_launched_from_history;
  private CheckBox flag_activity_reset_task_if_needed;
  private CheckBox flag_activity_brought_to_front;
  private CheckBox flag_activity_exclude_from_recents;
  private CheckBox flag_activity_previous_is_top;
  private CheckBox flag_activity_forward_result;
  private CheckBox flag_activity_clear_top;
  private CheckBox flag_activity_multiple_task;
  private CheckBox flag_activity_new_task;
  private CheckBox flag_activity_single_top;
  private CheckBox flag_activity_no_history;
  private CheckBox flag_receiver_visible_to_instant_apps;
  private CheckBox flag_receiver_boot_upgrade;
  private CheckBox flag_receiver_from_shell;
  private CheckBox flag_receiver_exclude_background;
  private CheckBox flag_receiver_include_background;
  private CheckBox flag_receiver_registered_only_before_boot;
  private CheckBox flag_receiver_no_abort;
  private CheckBox flag_receiver_foreground;
  private CheckBox flag_receiver_replace_pending;
  private CheckBox flag_receiver_registered_only;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_save_bookmark);

    db = DbGateway.getInstance(SaveBookmark.this);

    // ---------------------------------
    // expandable panel
    // ---------------------------------
    intent_attribute_flags_expandable_panel   = (ExpandablePanel) findViewById(R.id.intent_attribute_flags_expandable_panel);

    // ---------------------------------
    // lists to display the state of fields that accept multiple values
    // ---------------------------------
    intent_attribute_categories_list          = (ListView) findViewById(R.id.intent_attribute_categories_list);
    intent_attribute_extras_list              = (ListView) findViewById(R.id.intent_attribute_extras_list);

    // ---------------------------------
    // spinners to provide easy access to common values
    // ---------------------------------
    intent_attribute_action_spinner           = (Spinner) findViewById(R.id.intent_attribute_action_spinner);
    intent_attribute_data_type_spinner        = (Spinner) findViewById(R.id.intent_attribute_data_type_spinner);
    intent_attribute_categories_spinner       = (Spinner) findViewById(R.id.intent_attribute_categories_spinner);

    // ---------------------------------
    // input fields
    // ---------------------------------
    intent_attribute_name                     = (EditText) findViewById(R.id.intent_attribute_name);
    intent_attribute_folder                   = (EditText) findViewById(R.id.intent_attribute_folder);
    intent_attribute_action                   = (EditText) findViewById(R.id.intent_attribute_action);
    intent_attribute_package_name             = (EditText) findViewById(R.id.intent_attribute_package_name);
    intent_attribute_class_name               = (EditText) findViewById(R.id.intent_attribute_class_name);
    intent_attribute_data_uri                 = (EditText) findViewById(R.id.intent_attribute_data_uri);
    intent_attribute_data_type                = (EditText) findViewById(R.id.intent_attribute_data_type);
    intent_attribute_categories_text          = (EditText) findViewById(R.id.intent_attribute_categories_text);

    flag_grant_read_uri_permission            = (CheckBox) findViewById(R.id.flag_grant_read_uri_permission);
    flag_grant_write_uri_permission           = (CheckBox) findViewById(R.id.flag_grant_write_uri_permission);
    flag_from_background                      = (CheckBox) findViewById(R.id.flag_from_background);
    flag_debug_log_resolution                 = (CheckBox) findViewById(R.id.flag_debug_log_resolution);
    flag_exclude_stopped_packages             = (CheckBox) findViewById(R.id.flag_exclude_stopped_packages);
    flag_include_stopped_packages             = (CheckBox) findViewById(R.id.flag_include_stopped_packages);
    flag_grant_persistable_uri_permission     = (CheckBox) findViewById(R.id.flag_grant_persistable_uri_permission);
    flag_grant_prefix_uri_permission          = (CheckBox) findViewById(R.id.flag_grant_prefix_uri_permission);
    flag_direct_boot_auto                     = (CheckBox) findViewById(R.id.flag_direct_boot_auto);
    flag_debug_triaged_missing                = (CheckBox) findViewById(R.id.flag_debug_triaged_missing);
    flag_ignore_ephemeral                     = (CheckBox) findViewById(R.id.flag_ignore_ephemeral);
    flag_activity_require_default             = (CheckBox) findViewById(R.id.flag_activity_require_default);
    flag_activity_require_non_browser         = (CheckBox) findViewById(R.id.flag_activity_require_non_browser);
    flag_activity_match_external              = (CheckBox) findViewById(R.id.flag_activity_match_external);
    flag_activity_launch_adjacent             = (CheckBox) findViewById(R.id.flag_activity_launch_adjacent);
    flag_activity_retain_in_recents           = (CheckBox) findViewById(R.id.flag_activity_retain_in_recents);
    flag_activity_task_on_home                = (CheckBox) findViewById(R.id.flag_activity_task_on_home);
    flag_activity_clear_task                  = (CheckBox) findViewById(R.id.flag_activity_clear_task);
    flag_activity_no_animation                = (CheckBox) findViewById(R.id.flag_activity_no_animation);
    flag_activity_reorder_to_front            = (CheckBox) findViewById(R.id.flag_activity_reorder_to_front);
    flag_activity_no_user_action              = (CheckBox) findViewById(R.id.flag_activity_no_user_action);
    flag_activity_clear_when_task_reset       = (CheckBox) findViewById(R.id.flag_activity_clear_when_task_reset);
    flag_activity_new_document                = (CheckBox) findViewById(R.id.flag_activity_new_document);
    flag_activity_launched_from_history       = (CheckBox) findViewById(R.id.flag_activity_launched_from_history);
    flag_activity_reset_task_if_needed        = (CheckBox) findViewById(R.id.flag_activity_reset_task_if_needed);
    flag_activity_brought_to_front            = (CheckBox) findViewById(R.id.flag_activity_brought_to_front);
    flag_activity_exclude_from_recents        = (CheckBox) findViewById(R.id.flag_activity_exclude_from_recents);
    flag_activity_previous_is_top             = (CheckBox) findViewById(R.id.flag_activity_previous_is_top);
    flag_activity_forward_result              = (CheckBox) findViewById(R.id.flag_activity_forward_result);
    flag_activity_clear_top                   = (CheckBox) findViewById(R.id.flag_activity_clear_top);
    flag_activity_multiple_task               = (CheckBox) findViewById(R.id.flag_activity_multiple_task);
    flag_activity_new_task                    = (CheckBox) findViewById(R.id.flag_activity_new_task);
    flag_activity_single_top                  = (CheckBox) findViewById(R.id.flag_activity_single_top);
    flag_activity_no_history                  = (CheckBox) findViewById(R.id.flag_activity_no_history);
    flag_receiver_visible_to_instant_apps     = (CheckBox) findViewById(R.id.flag_receiver_visible_to_instant_apps);
    flag_receiver_boot_upgrade                = (CheckBox) findViewById(R.id.flag_receiver_boot_upgrade);
    flag_receiver_from_shell                  = (CheckBox) findViewById(R.id.flag_receiver_from_shell);
    flag_receiver_exclude_background          = (CheckBox) findViewById(R.id.flag_receiver_exclude_background);
    flag_receiver_include_background          = (CheckBox) findViewById(R.id.flag_receiver_include_background);
    flag_receiver_registered_only_before_boot = (CheckBox) findViewById(R.id.flag_receiver_registered_only_before_boot);
    flag_receiver_no_abort                    = (CheckBox) findViewById(R.id.flag_receiver_no_abort);
    flag_receiver_foreground                  = (CheckBox) findViewById(R.id.flag_receiver_foreground);
    flag_receiver_replace_pending             = (CheckBox) findViewById(R.id.flag_receiver_replace_pending);
    flag_receiver_registered_only             = (CheckBox) findViewById(R.id.flag_receiver_registered_only);

    keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    initExpandablePanel();
    initListView();
    initSpinner();

    onNewIntent(getIntent());
  }

  private void initExpandablePanel() {
    intent_attribute_flags_expandable_panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
      public void onCollapse(View handle, View content) {
        ImageView icon = (ImageView) handle;
        icon.setImageResource(android.R.drawable.ic_menu_add);
      }
      public void onExpand(View handle, View content) {
        ImageView icon = (ImageView) handle;
        icon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
      }
    });
  }

  private void initListView() {
    // ---------------------------------
    // categories
    // ---------------------------------
    categoriesList = new ArrayList<String>();
    categoriesListAdapter = new TextViewArrayAdapter<String>(
      getApplicationContext(),
      android.R.layout.simple_list_item_1,
      categoriesList
    );
    intent_attribute_categories_list.setAdapter(categoriesListAdapter);
    intent_attribute_categories_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        updateCategory(position);
      }
    });
    ((TextViewArrayAdapter) categoriesListAdapter).setTextAppearance(android.R.style.TextAppearance_Inverse);
    ((TextViewArrayAdapter) categoriesListAdapter).setTextColor(R.color.textview_in_listview_in_scrollview);

    // ---------------------------------
    // extras
    // ---------------------------------
    extrasList = new ArrayList<DbIntent.Extra>();
    extrasListAdapter = new TextViewArrayAdapter<DbIntent.Extra>(
      getApplicationContext(),
      android.R.layout.simple_list_item_1,
      extrasList
    );
    intent_attribute_extras_list.setAdapter(extrasListAdapter);
    intent_attribute_extras_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
        updateExtra(position);
      }
    });
    ((TextViewArrayAdapter) categoriesListAdapter).setTextAppearance(android.R.style.TextAppearance_Inverse);
    ((TextViewArrayAdapter) extrasListAdapter).setTextColor(R.color.textview_in_listview_in_scrollview);
  }

  private void initSpinner() {
    // ---------------------------------
    // action
    // ---------------------------------
    ArrayAdapter<CharSequence> intent_attribute_action_adapter = ArrayAdapter.createFromResource(
      SaveBookmark.this,
      R.array.intent_actions,
      android.R.layout.simple_spinner_item
    );

    intent_attribute_action_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    intent_attribute_action_spinner.setAdapter(intent_attribute_action_adapter);

    intent_attribute_action_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // ignore first value in array
        if (pos > 0) {
          String action = intent_attribute_action_spinner.getSelectedItem().toString();
          intent_attribute_action.setText(action, TextView.BufferType.EDITABLE);

          // reset to first value
          intent_attribute_action_spinner.setSelection(0);
        }
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // ---------------------------------
    // data type
    // ---------------------------------
    ArrayAdapter<CharSequence> intent_attribute_data_type_adapter = ArrayAdapter.createFromResource(
      SaveBookmark.this,
      R.array.intent_data_types,
      android.R.layout.simple_spinner_item
    );

    intent_attribute_data_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    intent_attribute_data_type_spinner.setAdapter(intent_attribute_data_type_adapter);

    intent_attribute_data_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // ignore first value in array
        if (pos > 0) {
          String data_type = intent_attribute_data_type_spinner.getSelectedItem().toString();
          intent_attribute_data_type.setText(data_type, TextView.BufferType.EDITABLE);

          // reset to first value
          intent_attribute_data_type_spinner.setSelection(0);
        }
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // ---------------------------------
    // category
    // ---------------------------------
    ArrayAdapter<CharSequence> intent_attribute_categories_adapter = ArrayAdapter.createFromResource(
      SaveBookmark.this,
      R.array.intent_categories,
      android.R.layout.simple_spinner_item
    );

    intent_attribute_categories_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    intent_attribute_categories_spinner.setAdapter(intent_attribute_categories_adapter);

    intent_attribute_categories_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // ignore first value in array
        if (pos > 0) {
          String category = intent_attribute_categories_spinner.getSelectedItem().toString();
          categoriesList.add(category);
          categoriesListAdapter.notifyDataSetChanged();

          // reset to first value
          intent_attribute_categories_spinner.setSelection(0);
        }
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  @Override
  protected void onNewIntent(Intent intent) {
    try {
      if (intent == null) throw new Exception();

      intentId = intent.getIntExtra(Constants.EXTRA_INTENT_ID, -1);
      folderId = intent.getIntExtra(Constants.EXTRA_FOLDER_ID,  0);

      // remove extras used only internally
      intent.removeExtra(Constants.EXTRA_INTENT_ID);
      intent.removeExtra(Constants.EXTRA_FOLDER_ID);

      // remove the explicit reference to "SaveBookmark.class"
      intent.setComponent(null);

      dbIntent = (intentId >= 0)
        ? db.getDbIntent(intentId)
        : DbIntent.getInstance(intentId, folderId, /* name */ "", intent);

      if ((intentId >= 0) && (dbIntent != null))
        folderId = dbIntent.folder_id;
    }
    catch(Exception e) {
      intentId = -1;
      folderId = 0;
      dbIntent = null;
    }

    updateView();
  }

  private void updateView() {
    int titleResId = ((dbIntent != null) && (dbIntent.id >= 0))
      ? R.string.intent_edit_title
      : R.string.intent_add_title;

    setTitle(titleResId);

    if (Build.VERSION.SDK_INT >= 11) {
      ActionBar actionbar = getActionBar();

      if (actionbar != null)
        actionbar.setTitle(titleResId);
    }

    updateFolderPath();
    setInputFields();
  }

  private void hideKeyboard() {
    View v = getCurrentFocus();
    if (v == null)
      v = (View) intent_attribute_name;

    keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
  }

  public void changeFolder(View v) {
    hideKeyboard();

    DbFolderPicker.pickFolder(
      /* context  */ SaveBookmark.this,
      /* listener */ new DbFolderPicker.Listener() {
        @Override
        public boolean isValidFolderToPick(FolderContentItem folder) {
          return true;
        }

        @Override
        public void onFolderPick(FolderContentItem folder) {
          folderId = folder.id;
          updateFolderPath();
        }
      },
      /* initialFolderId */ folderId
    );
  }

  private void updateFolderPath() {
    String folderPath = db.getFolderPath(folderId);
    intent_attribute_folder.setText(folderPath, TextView.BufferType.NORMAL);
  }

  public void addCategory(View v) {
    String category = intent_attribute_categories_text.getText().toString().trim();

    if (!TextUtils.isEmpty(category)) {
      categoriesList.add(category);
      categoriesListAdapter.notifyDataSetChanged();

      // clear text input field
      intent_attribute_categories_text.setText("", TextView.BufferType.EDITABLE);

      // hide keyboard
      hideKeyboard();
    }
  }

  private void updateCategory(int position) {
    hideKeyboard();

    if ((position < 0) || (position >= categoriesList.size())) return;

    String oldCategory = categoriesList.get(position);

    View dialogView                                       = View.inflate(SaveBookmark.this, R.layout.dialog_update_intent_attribute_category, null);
    EditText dialog_update_intent_attribute_category_name = (EditText) dialogView.findViewById(R.id.dialog_update_intent_attribute_category_name);

    // ---------------------------------
    // initialize input fields
    // ---------------------------------
    dialog_update_intent_attribute_category_name.setText(oldCategory, TextView.BufferType.EDITABLE);

    // ---------------------------------
    // initialize AlertDialog
    // ---------------------------------
    new AlertDialog.Builder(SaveBookmark.this)
      .setView(dialogView)
      .setTitle(R.string.dialog_update_intent_attribute_category_title)
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          String newCategory = dialog_update_intent_attribute_category_name.getText().toString().trim();

          if (!TextUtils.isEmpty(newCategory) && !newCategory.equals(oldCategory)) {
            categoriesList.set(position, newCategory);
            categoriesListAdapter.notifyDataSetChanged();
          }
          hideKeyboard();
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          hideKeyboard();
        }
      })
      .setNeutralButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          if ((position >= 0) && (position < categoriesList.size())) {
            categoriesList.remove(position);
            categoriesListAdapter.notifyDataSetChanged();
          }
          hideKeyboard();
        }
      })
      .show();
  }

  public void addExtra(View v) {
    updateExtra(/* position */ -1);
  }

  private void updateExtra(int position) {
    hideKeyboard();

    DbIntent.Extra oldExtra = ((position >= 0) && (position < extrasList.size()))
      ? extrasList.get(position)
      : null;

    View     dialogView                                              = View.inflate(SaveBookmark.this, R.layout.dialog_update_intent_attribute_extra, null);
    EditText dialog_update_intent_attribute_extra_name               = (EditText) dialogView.findViewById(R.id.dialog_update_intent_attribute_extra_name);
    EditText dialog_update_intent_attribute_extra_value              = (EditText) dialogView.findViewById(R.id.dialog_update_intent_attribute_extra_value);
    Spinner  dialog_update_intent_attribute_extra_value_type_spinner = (Spinner)  dialogView.findViewById(R.id.dialog_update_intent_attribute_extra_value_type_spinner);
    Button   dialog_update_intent_attribute_extra_value_separator    = (Button)   dialogView.findViewById(R.id.dialog_update_intent_attribute_extra_value_separator);

    // ---------------------------------
    // initialize Spinner
    // ---------------------------------
    String[] valueTypeNames = db.getAllIntentExtraValueTypeNames();
    Arrays.sort(valueTypeNames);

    ArrayAdapter<String> dialog_update_intent_attribute_extra_value_type_adapter = new ArrayAdapter<String>(
      SaveBookmark.this,
      android.R.layout.simple_list_item_1,
      valueTypeNames
    );

    dialog_update_intent_attribute_extra_value_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    dialog_update_intent_attribute_extra_value_type_spinner.setAdapter(dialog_update_intent_attribute_extra_value_type_adapter);

    dialog_update_intent_attribute_extra_value_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String newValueType = dialog_update_intent_attribute_extra_value_type_spinner.getSelectedItem().toString();

        boolean allow_list = newValueType.endsWith("[]") || newValueType.startsWith("ArrayList");

        dialog_update_intent_attribute_extra_value_separator.setEnabled(allow_list);
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // ---------------------------------
    // initialize input fields
    // ---------------------------------
    if (oldExtra != null) {
      dialog_update_intent_attribute_extra_name.setText( oldExtra.name,  TextView.BufferType.EDITABLE);
      dialog_update_intent_attribute_extra_value.setText(oldExtra.value, TextView.BufferType.SPANNABLE);

      int spinner_index = Arrays.binarySearch(valueTypeNames, oldExtra.value_type);
      dialog_update_intent_attribute_extra_value_type_spinner.setSelection(
        (spinner_index > 0) ? spinner_index : 0
      );
    }

    // ---------------------------------
    // initialize Button
    // ---------------------------------
    IntentExtraValueTokenSeparatorOnClickListener dialog_update_intent_attribute_extra_value_separator_onclicklistener = new IntentExtraValueTokenSeparatorOnClickListener(
      /* context  */ SaveBookmark.this,
      /* editText */ dialog_update_intent_attribute_extra_value,
      /* token    */ DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN
    );

    if (oldExtra != null) {
      dialog_update_intent_attribute_extra_value_separator_onclicklistener.formatTokens(/* insertNewToken */ false, /* retainCursorPosition */ false);
    }

    dialog_update_intent_attribute_extra_value_separator.setOnClickListener(dialog_update_intent_attribute_extra_value_separator_onclicklistener);

    // ---------------------------------
    // initialize AlertDialog
    // ---------------------------------
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SaveBookmark.this);

    alertDialogBuilder
      .setView(dialogView)
      .setTitle(
        (oldExtra == null)
          ? R.string.dialog_update_intent_attribute_extra_title_add
          : R.string.dialog_update_intent_attribute_extra_title_edit
      )
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          String newName      = dialog_update_intent_attribute_extra_name.getText().toString().trim();
          String newValue     = dialog_update_intent_attribute_extra_value.getText().toString().trim();
          String newValueType = dialog_update_intent_attribute_extra_value_type_spinner.getSelectedItem().toString();

          if ((oldExtra != null) && oldExtra.name.equals(newName) && oldExtra.value.equals(newValue) && oldExtra.value_type.equals(newValueType)) {
            return;
          }

          DbIntent.Extra newExtra = new DbIntent.Extra(newName, newValueType, newValue);

          if ((position >= 0) && (position < extrasList.size()))
            extrasList.set(position, newExtra);
          else
            extrasList.add(newExtra);

          extrasListAdapter.notifyDataSetChanged();
          hideKeyboard();
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          hideKeyboard();
        }
      });

    if (oldExtra != null) {
      alertDialogBuilder
        .setNeutralButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            if ((position >= 0) && (position < extrasList.size())) {
              extrasList.remove(position);
              extrasListAdapter.notifyDataSetChanged();
            }
            hideKeyboard();
          }
        });
    }

    alertDialogBuilder.show();
  }

  public void saveIntent(View v) {
    boolean ok = true;

    ok &= getInputFields();
    if (!ok) return;

    String name   = intent_attribute_name.getText().toString().trim();
    Intent intent = dbIntent.getIntent();

    ok &= !TextUtils.isEmpty(name);
    ok &= (intent != null);
    if (!ok) return;

    if (intentId < 0)
      ok &= db.addIntent(folderId, name, intent);
    else
      ok &= db.updateIntent(intentId, folderId, name, intent);

    if (ok)
      showBookmarks();
  }

  // ---------------------------------------------------------------------------
  // ActionBar Menu
  // ---------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, Menu.FIRST, Menu.FIRST, R.string.app_name_long);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == Menu.FIRST) {
      showBookmarks();
    }
    return super.onOptionsItemSelected(item);
  }

  // ---------------------------------------------------------------------------
  // finish Activity and display list of all Bookmarks
  // ---------------------------------------------------------------------------

  private void showBookmarks() {
    Intent intent = new Intent(SaveBookmark.this, Bookmarks.class);
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
      Resources resources = getResources();

      String name, action, package_name, class_name, data_uri, data_type;
      int flags;

      if (dbIntent == null) {
        name         = "";
        action       = "";
        package_name = "";
        class_name   = "";
        data_uri     = "";
        data_type    = "";
        flags        = 0;

        categoriesList.clear();
        categoriesListAdapter.notifyDataSetChanged();

        extrasList.clear();
        extrasListAdapter.notifyDataSetChanged();
      }
      else {
        name         = dbIntent.name;
        action       = dbIntent.action;
        package_name = dbIntent.package_name;
        class_name   = dbIntent.class_name;
        data_uri     = dbIntent.data_uri;
        data_type    = dbIntent.data_type;
        flags        = dbIntent.flags;

        categoriesList.clear();
        categoriesList.addAll(
          Arrays.asList(dbIntent.categories)
        );
        categoriesListAdapter.notifyDataSetChanged();

        extrasList.clear();
        extrasList.addAll(
          Arrays.asList(dbIntent.extras)
        );
        extrasListAdapter.notifyDataSetChanged();
      }

      intent_attribute_name.setText        (name,         TextView.BufferType.EDITABLE);
      intent_attribute_action.setText      (action,       TextView.BufferType.EDITABLE);
      intent_attribute_package_name.setText(package_name, TextView.BufferType.EDITABLE);
      intent_attribute_class_name.setText  (class_name,   TextView.BufferType.EDITABLE);
      intent_attribute_data_uri.setText    (data_uri,     TextView.BufferType.EDITABLE);
      intent_attribute_data_type.setText   (data_type,    TextView.BufferType.EDITABLE);

      flag_grant_read_uri_permission.setChecked(
        (flags & resources.getInteger(R.integer.flag_grant_read_uri_permission)) > 0
      );
      flag_grant_write_uri_permission.setChecked(
        (flags & resources.getInteger(R.integer.flag_grant_write_uri_permission)) > 0
      );
      flag_from_background.setChecked(
        (flags & resources.getInteger(R.integer.flag_from_background)) > 0
      );
      flag_debug_log_resolution.setChecked(
        (flags & resources.getInteger(R.integer.flag_debug_log_resolution)) > 0
      );
      flag_exclude_stopped_packages.setChecked(
        (flags & resources.getInteger(R.integer.flag_exclude_stopped_packages)) > 0
      );
      flag_include_stopped_packages.setChecked(
        (flags & resources.getInteger(R.integer.flag_include_stopped_packages)) > 0
      );
      flag_grant_persistable_uri_permission.setChecked(
        (flags & resources.getInteger(R.integer.flag_grant_persistable_uri_permission)) > 0
      );
      flag_grant_prefix_uri_permission.setChecked(
        (flags & resources.getInteger(R.integer.flag_grant_prefix_uri_permission)) > 0
      );
      flag_direct_boot_auto.setChecked(
        (flags & resources.getInteger(R.integer.flag_direct_boot_auto)) > 0
      );
      flag_debug_triaged_missing.setChecked(
        (flags & resources.getInteger(R.integer.flag_debug_triaged_missing)) > 0
      );
      flag_ignore_ephemeral.setChecked(
        (flags & resources.getInteger(R.integer.flag_ignore_ephemeral)) > 0
      );
      flag_activity_require_default.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_require_default)) > 0
      );
      flag_activity_require_non_browser.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_require_non_browser)) > 0
      );
      flag_activity_match_external.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_match_external)) > 0
      );
      flag_activity_launch_adjacent.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_launch_adjacent)) > 0
      );
      flag_activity_retain_in_recents.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_retain_in_recents)) > 0
      );
      flag_activity_task_on_home.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_task_on_home)) > 0
      );
      flag_activity_clear_task.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_clear_task)) > 0
      );
      flag_activity_no_animation.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_no_animation)) > 0
      );
      flag_activity_reorder_to_front.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_reorder_to_front)) > 0
      );
      flag_activity_no_user_action.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_no_user_action)) > 0
      );
      flag_activity_clear_when_task_reset.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_clear_when_task_reset)) > 0
      );
      flag_activity_new_document.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_new_document)) > 0
      );
      flag_activity_launched_from_history.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_launched_from_history)) > 0
      );
      flag_activity_reset_task_if_needed.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_reset_task_if_needed)) > 0
      );
      flag_activity_brought_to_front.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_brought_to_front)) > 0
      );
      flag_activity_exclude_from_recents.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_exclude_from_recents)) > 0
      );
      flag_activity_previous_is_top.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_previous_is_top)) > 0
      );
      flag_activity_forward_result.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_forward_result)) > 0
      );
      flag_activity_clear_top.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_clear_top)) > 0
      );
      flag_activity_multiple_task.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_multiple_task)) > 0
      );
      flag_activity_new_task.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_new_task)) > 0
      );
      flag_activity_single_top.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_single_top)) > 0
      );
      flag_activity_no_history.setChecked(
        (flags & resources.getInteger(R.integer.flag_activity_no_history)) > 0
      );
      flag_receiver_visible_to_instant_apps.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_visible_to_instant_apps)) > 0
      );
      flag_receiver_boot_upgrade.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_boot_upgrade)) > 0
      );
      flag_receiver_from_shell.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_from_shell)) > 0
      );
      flag_receiver_exclude_background.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_exclude_background)) > 0
      );
      flag_receiver_include_background.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_include_background)) > 0
      );
      flag_receiver_registered_only_before_boot.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_registered_only_before_boot)) > 0
      );
      flag_receiver_no_abort.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_no_abort)) > 0
      );
      flag_receiver_foreground.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_foreground)) > 0
      );
      flag_receiver_replace_pending.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_replace_pending)) > 0
      );
      flag_receiver_registered_only.setChecked(
        (flags & resources.getInteger(R.integer.flag_receiver_registered_only)) > 0
      );
    }
    catch(Exception e) {}
  }

  // ---------------------------------------------------------------------------
  // update dbIntent with values from input fields
  // ---------------------------------------------------------------------------

  private boolean getInputFields() {
    try {
      Resources resources = getResources();

      String name, action, package_name, class_name, data_uri, data_type;
      int flags;

      name         = intent_attribute_name.getText().toString().trim();
      action       = intent_attribute_action.getText().toString().trim();
      package_name = intent_attribute_package_name.getText().toString().trim();
      class_name   = intent_attribute_class_name.getText().toString().trim();
      data_uri     = intent_attribute_data_uri.getText().toString().trim();
      data_type    = intent_attribute_data_type.getText().toString().trim();

      flags = 0;
      if (flag_grant_read_uri_permission.isChecked())
        flags |= resources.getInteger(R.integer.flag_grant_read_uri_permission);
      if (flag_grant_write_uri_permission.isChecked())
        flags |= resources.getInteger(R.integer.flag_grant_write_uri_permission);
      if (flag_from_background.isChecked())
        flags |= resources.getInteger(R.integer.flag_from_background);
      if (flag_debug_log_resolution.isChecked())
        flags |= resources.getInteger(R.integer.flag_debug_log_resolution);
      if (flag_exclude_stopped_packages.isChecked())
        flags |= resources.getInteger(R.integer.flag_exclude_stopped_packages);
      if (flag_include_stopped_packages.isChecked())
        flags |= resources.getInteger(R.integer.flag_include_stopped_packages);
      if (flag_grant_persistable_uri_permission.isChecked())
        flags |= resources.getInteger(R.integer.flag_grant_persistable_uri_permission);
      if (flag_grant_prefix_uri_permission.isChecked())
        flags |= resources.getInteger(R.integer.flag_grant_prefix_uri_permission);
      if (flag_direct_boot_auto.isChecked())
        flags |= resources.getInteger(R.integer.flag_direct_boot_auto);
      if (flag_debug_triaged_missing.isChecked())
        flags |= resources.getInteger(R.integer.flag_debug_triaged_missing);
      if (flag_ignore_ephemeral.isChecked())
        flags |= resources.getInteger(R.integer.flag_ignore_ephemeral);
      if (flag_activity_require_default.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_require_default);
      if (flag_activity_require_non_browser.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_require_non_browser);
      if (flag_activity_match_external.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_match_external);
      if (flag_activity_launch_adjacent.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_launch_adjacent);
      if (flag_activity_retain_in_recents.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_retain_in_recents);
      if (flag_activity_task_on_home.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_task_on_home);
      if (flag_activity_clear_task.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_clear_task);
      if (flag_activity_no_animation.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_no_animation);
      if (flag_activity_reorder_to_front.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_reorder_to_front);
      if (flag_activity_no_user_action.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_no_user_action);
      if (flag_activity_clear_when_task_reset.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_clear_when_task_reset);
      if (flag_activity_new_document.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_new_document);
      if (flag_activity_launched_from_history.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_launched_from_history);
      if (flag_activity_reset_task_if_needed.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_reset_task_if_needed);
      if (flag_activity_brought_to_front.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_brought_to_front);
      if (flag_activity_exclude_from_recents.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_exclude_from_recents);
      if (flag_activity_previous_is_top.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_previous_is_top);
      if (flag_activity_forward_result.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_forward_result);
      if (flag_activity_clear_top.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_clear_top);
      if (flag_activity_multiple_task.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_multiple_task);
      if (flag_activity_new_task.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_new_task);
      if (flag_activity_single_top.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_single_top);
      if (flag_activity_no_history.isChecked())
        flags |= resources.getInteger(R.integer.flag_activity_no_history);
      if (flag_receiver_visible_to_instant_apps.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_visible_to_instant_apps);
      if (flag_receiver_boot_upgrade.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_boot_upgrade);
      if (flag_receiver_from_shell.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_from_shell);
      if (flag_receiver_exclude_background.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_exclude_background);
      if (flag_receiver_include_background.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_include_background);
      if (flag_receiver_registered_only_before_boot.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_registered_only_before_boot);
      if (flag_receiver_no_abort.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_no_abort);
      if (flag_receiver_foreground.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_foreground);
      if (flag_receiver_replace_pending.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_replace_pending);
      if (flag_receiver_registered_only.isChecked())
        flags |= resources.getInteger(R.integer.flag_receiver_registered_only);

      dbIntent = DbIntent.getInstance(intentId, folderId, name, flags, action, package_name, class_name, data_uri, data_type, categoriesList, extrasList);

      return true;
    }
    catch(Exception e) {
      return false;
    }
  }

}
