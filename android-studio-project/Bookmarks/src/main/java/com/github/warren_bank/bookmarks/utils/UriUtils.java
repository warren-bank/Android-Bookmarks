package com.github.warren_bank.bookmarks.utils;

import com.github.warren_bank.bookmarks.common.Constants;

import de.cketti.fileprovider.PublicFileProvider;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;

public class UriUtils {

  public static Uri usePublicFileProvider(Context context, String uriString) {
    uriString = UriUtils.normalizeUriString(uriString);
    if (uriString == null) return null;

    Uri uri = Uri.parse(uriString);

    return UriUtils.usePublicFileProvider(context, uri);
  }

  public static String normalizeUriString(String uriString) {
    if (TextUtils.isEmpty(uriString)) return null;

    if (uriString.charAt(0) == '/') {
      // file path
      Uri uri = Uri.fromFile(new File(uriString));
      uriString = uri.toString();
    }

    return uriString;
  }

  public static Uri usePublicFileProvider(Context context, Uri uri) {
    if ((Build.VERSION.SDK_INT >= 24) && ("file").equals(uri.getScheme())) {
      // file path
      File file = new File(uri.getPath());
      uri = PublicFileProvider.getUriForFile(context, Constants.PUBLIC_FILE_PROVIDER_AUTHORITY, file);
    }

    return uri;
  }

  public static boolean isUri(Object valueObj) {
    return (valueObj instanceof Uri);
  }

}
