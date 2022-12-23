package com.github.warren_bank.bookmarks.common;

public class Constants {
  public static final String PACKAGE_NAME = "com.github.warren_bank.bookmarks";
  public static final String LOG_TAG      = "Bookmarks";

  /* Runtime Permissions */
  public static final int PERMISSION_CHECK_REQUEST_CODE_CHANGE_DEFAULT_OUTPUT_DIRECTORY_FILEPICKER = 1;
  public static final int PERMISSION_CHECK_REQUEST_CODE_RESTORE_DATABASE_FILEPICKER                = 2;
  public static final int PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_FILEPICKER                 = 3;
  public static final int PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE                  = 4;
  public static final int PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE                            = 5;
  public static final int PERMISSION_CHECK_REQUEST_CODE_IMPORT_HTML_FILEPICKER                     = 6;
  public static final int PERMISSION_CHECK_REQUEST_CODE_EXPORT_HTML_FILEPICKER                     = 7;
  public static final int PERMISSION_CHECK_REQUEST_CODE_IMPORT_JSON_FILEPICKER                     = 8;
  public static final int PERMISSION_CHECK_REQUEST_CODE_EXPORT_JSON_FILEPICKER                     = 9;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_SEND_BROADCAST                      = 10;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_START_ACTIVITY                      = 11;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_START_FOREGROUND_SERVICE            = 12;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_START_SERVICE                       = 13;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_STOP_SERVICE                        = 14;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_ADD_SHORTCUT                        = 15;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_EXTRA_WITH_FILE_SCHEME_URI          = 16;
  public static final int PERMISSION_CHECK_REQUEST_CODE_INTENT_OPEN_DATA_URI_FILEPICKER            = 17;

  /* ContentProvider Authorities */
  public static final String PUBLIC_FILE_PROVIDER_AUTHORITY = Constants.PACKAGE_NAME + ".publicfileprovider";

  /* Intent Extras */
  public static final String EXTRA_INTENT_ID                = Constants.PACKAGE_NAME + ".BookmarkId";
  public static final String EXTRA_FOLDER_ID                = Constants.PACKAGE_NAME + ".FolderId";
  public static final String EXTRA_ALARM_ID                 = Constants.PACKAGE_NAME + ".AlarmId";
  public static final String EXTRA_RELOAD_LIST              = Constants.PACKAGE_NAME + ".ReloadList";
}
