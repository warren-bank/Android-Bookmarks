package com.github.warren_bank.bookmarks.utils;

// -----------------------------------------------------------------------------
// references:
//   https://android.googlesource.com/platform/frameworks/base.git/+/master/graphics/java/android/graphics/Bitmap.java
// -----------------------------------------------------------------------------

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;

import java.lang.reflect.Method;

public class BitmapUtils {

  public static Parcelable decodeFileToParcelable(String uriString) {
    if (TextUtils.isEmpty(uriString)) return null;

    Uri uri = Uri.parse(uriString);
    String scheme = uri.getScheme();
    if ((scheme != null) && !scheme.toLowerCase().equals("file")) return null;

    String filePath = uri.getPath();
    Bitmap value = BitmapFactory.decodeFile(filePath);

    if (value != null) {
      try {
        // embed file path into an optional array of private data
        byte[] chunk = filePath.getBytes();
        setNinePatchChunk(value, chunk);
      }
      catch(Exception e) {}

    // TODO: update build tools so this can compile
    /*
      if (Build.VERSION.SDK_INT >= 31)
        value = value.asShared();
    */
    }

    return value;
  }

  private static void setNinePatchChunk(Bitmap value, byte[] chunk) throws Exception {
    try {
      Method method = Bitmap.class.getDeclaredMethod("setNinePatchChunk", byte[].class);
      try {
        method.setAccessible(true);
      }
      catch(Exception e) {}
      method.invoke(value, chunk);
      return;
    }
    catch(Exception e) {}

    try {
      HiddenApiBypass.invoke(Bitmap.class, value, "setNinePatchChunk", chunk);
      return;
    }
    catch(Exception e) {}

    throw new Exception("unable to setNinePatchChunk");
  }

  public static String extractFilePath(Object valueObj) {
    Bitmap value    = (Bitmap) valueObj;
    String filePath = null;

    if (value != null) {
      try {
        // extract embedded file path from an optional array of private data
        byte[] chunk = value.getNinePatchChunk();
        if ((chunk != null) && (chunk.length > 0)) {
          String valueStr = new String(chunk);

          if (valueStr.charAt(0) == '/')
            filePath = valueStr;
        }
      }
      catch(Exception e) {}
    }

    return filePath;
  }

  public static boolean isBitmap(Object valueObj) {
    return (valueObj instanceof Bitmap);
  }
}
