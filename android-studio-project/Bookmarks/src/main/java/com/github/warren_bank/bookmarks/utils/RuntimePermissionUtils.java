package com.github.warren_bank.bookmarks.utils;

import com.github.warren_bank.bookmarks.BuildConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RuntimePermissionUtils {

  public interface RuntimePermissionListener {
    public void onRequestPermissionsGranted (int requestCode, Object passthrough);
    public void onRequestPermissionsDenied  (int requestCode, Object passthrough, String[] missingPermissions);
  }

  private static HashMap<Integer,Object> passthroughCache = new HashMap<Integer,Object>();

  public static boolean hasAllPermissions(Activity activity, Intent intent) {
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(activity, intent);

    return (missingPermissions == null);
  }

  public static boolean hasAllPermissions(Activity activity, String[] allRequestedPermissions) {
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(activity, allRequestedPermissions);

    return (missingPermissions == null);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, Intent intent, int requestCode) {
    Object passthrough = (Object) intent;
    RuntimePermissionUtils.requestPermissions(activity, listener, intent, requestCode, passthrough);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, Intent intent, int requestCode, Object passthrough) {
    if (intent == null) return;

    String[] allRequestedPermissions = RuntimePermissionUtils.getAllRequestedPermissions(activity, intent);
    RuntimePermissionUtils.requestPermissions(activity, listener, allRequestedPermissions, requestCode, passthrough);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, String[] allRequestedPermissions, int requestCode) {
    Object passthrough = null;
    RuntimePermissionUtils.requestPermissions(activity, listener, allRequestedPermissions, requestCode, passthrough);
  }

  public static void requestPermissions(Activity activity, RuntimePermissionListener listener, String[] allRequestedPermissions, int requestCode, Object passthrough) {
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(activity, allRequestedPermissions);

    if (missingPermissions == null) {
      listener.onRequestPermissionsGranted(requestCode, passthrough);
    }
    else {
      passthroughCache.put(requestCode, passthrough);

      activity.requestPermissions(missingPermissions, requestCode);
    }
  }

  public static void onRequestPermissionsResult(Activity activity, RuntimePermissionListener listener, int requestCode, String[] permissions, int[] grantResults) {
    Object passthrough          = passthroughCache.remove(requestCode);
    String[] missingPermissions = RuntimePermissionUtils.getMissingPermissions(permissions, grantResults);

    if (missingPermissions == null) {
      listener.onRequestPermissionsGranted(requestCode, passthrough);
    }
    else {
      listener.onRequestPermissionsDenied(requestCode, passthrough, missingPermissions);
    }
  }

  private static String[] getMissingPermissions(Activity activity, Intent intent) {
    String[] allRequestedPermissions = RuntimePermissionUtils.getAllRequestedPermissions(activity, intent);
    return RuntimePermissionUtils.getMissingPermissions(activity, allRequestedPermissions);
  }

  private static String[] getAllRequestedPermissions(Activity activity, Intent intent) {
    if (Build.VERSION.SDK_INT < 23)
      return null;

    if (intent == null)
      return null;

    String action = intent.getAction();
    if (TextUtils.isEmpty(action))
      return null;

    List<String> allRequestedPermissions = new ArrayList<String>();

    switch(action) {
      case "android.intent.action.CALL" : {
          if (BuildConfig.ALLOW_RUNTIME_PERMISSIONS_USER)
            allRequestedPermissions.add("android.permission.CALL_PHONE");
        }
        break;
    }

    if (allRequestedPermissions.isEmpty())
      return null;

    return allRequestedPermissions.toArray(new String[allRequestedPermissions.size()]);
  }

  private static String[] getMissingPermissions(Activity activity, String[] allRequestedPermissions) {
    if (Build.VERSION.SDK_INT < 23)
      return null;

    if ((allRequestedPermissions == null) || (allRequestedPermissions.length == 0))
      return null;

    List<String> missingPermissions = new ArrayList<String>();

    for (String permission : allRequestedPermissions) {
      if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        missingPermissions.add(permission);
      }
    }

    if (missingPermissions.isEmpty())
      return null;

    return missingPermissions.toArray(new String[missingPermissions.size()]);
  }

  private static String[] getMissingPermissions(String[] allRequestedPermissions, int[] allGrantResults) {
    if ((allRequestedPermissions == null) || (allRequestedPermissions.length == 0))
      return null;

    if ((allGrantResults == null) || (allGrantResults.length == 0))
      return allRequestedPermissions;

    List<String> missingPermissions = new ArrayList<String>();
    int index;

    for (index = 0; (index < allGrantResults.length) && (index < allRequestedPermissions.length); index++) {
      if (allGrantResults[index] != PackageManager.PERMISSION_GRANTED) {
        missingPermissions.add(allRequestedPermissions[index]);
      }
    }

    while (index < allRequestedPermissions.length) {
      missingPermissions.add(allRequestedPermissions[index]);
      index++;
    }

    if (missingPermissions.isEmpty())
      return null;

    return missingPermissions.toArray(new String[missingPermissions.size()]);
  }

  // ---------------------------------------------------------------------------
  // runtime permissions: specific to alarms

  public static void checkAlarmPermissions(Context context) {
    Uri uri = Uri.parse("package:" + context.getPackageName());

    if (!canScheduleExactAlarms(context)) {
      Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri);
      context.startActivity(intent);
    }

    if (!canStartActivityFromBackground() && !canDrawOverlays(context)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
      context.startActivity(intent);
    }
  }

  public static boolean canScheduleExactAlarms(Context context) {
    return (Build.VERSION.SDK_INT < 31)
      ? true
      : AlarmUtils.getAlarmManager(context).canScheduleExactAlarms();
  }

  public static boolean canStartActivityFromBackground() {
    return (Build.VERSION.SDK_INT < 29);
  }

  public static boolean canDrawOverlays(Context context) {
    return (Build.VERSION.SDK_INT < 23)
      ? true
      : Settings.canDrawOverlays(context);
  }

  // ---------------------------------------------------------------------------
  // runtime permissions: specific to the file system

  public static void checkFilePermissions(Context context) {
    Uri uri = Uri.parse("package:" + context.getPackageName());

    if (!canAccessAllFiles()) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
      context.startActivity(intent);
    }
  }

  public static boolean canAccessAllFiles() {
    return (Build.VERSION.SDK_INT < 30)
      ? true
      : Environment.isExternalStorageManager();
  }

  // ---------------------------------------------------------------------------
}
