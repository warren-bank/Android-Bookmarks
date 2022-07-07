package com.github.warren_bank.bookmarks.database.model;

import com.github.warren_bank.bookmarks.utils.BitmapUtils;
import com.github.warren_bank.bookmarks.utils.UriUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DbIntent {

  public static class Extra {
    public String name;
    public String value_type;
    public String value;

    public Extra(String name, String value_type, String value) {
      this.name       = name;
      this.value_type = value_type;
      this.value      = value;
    }

    // for display in ArrayAdapter
    public String toString() {
      return String.format("(%s) %s", value_type, name);
    }
  }

  public int    id;
  public int    folder_id;
  public String name;
  public int    flags;
  public String action;
  public String package_name;
  public String class_name;
  public String data_uri;
  public String data_type;

  public String[] categories;
  public Extra[]  extras;

  private DbIntent(int id, int folder_id, String name, int flags, String action, String package_name, String class_name, String data_uri, String data_type, String[] categories, Extra[] extras) {
    this.id           = id;
    this.folder_id    = folder_id;
    this.name         = name;
    this.flags        = flags;
    this.action       = action;
    this.package_name = package_name;
    this.class_name   = class_name;
    this.data_uri     = UriUtils.normalizeUriString(data_uri);
    this.data_type    = data_type;
    this.categories   = categories;
    this.extras       = extras;
  }

  public static DbIntent getInstance(int id, int folder_id, String name, int flags, String action, String package_name, String class_name, String data_uri, String data_type, String[] categories, Extra[] extras) {
    return new DbIntent(id, folder_id, name, flags, action, package_name, class_name, data_uri, data_type, categories, extras);
  }

  public static DbIntent getInstance(int id, int folder_id, String name, int flags, String action, String package_name, String class_name, String data_uri, String data_type, List<String> categoriesList, List<Extra> extrasList) {
    String[] categories = ((categoriesList == null) || categoriesList.isEmpty())
      ? new String[0]
      : categoriesList.toArray(new String[categoriesList.size()]);

    Extra[] extras = ((extrasList == null) || extrasList.isEmpty())
      ? new Extra[0]
      : extrasList.toArray(new Extra[extrasList.size()]);

    return new DbIntent(id, folder_id, name, flags, action, package_name, class_name, data_uri, data_type, categories, extras);
  }

  // -----------------------------------
  // conventions used by serialization
  // -----------------------------------

  /* -----------------------------------
   * EXTRA_ARRAY_SEPARATOR_TOKEN
   *   - used when an extra contains a list of values
   *   - the serialized String value that is written to the DB will contain instances of this token
   *   - token is removed when the serialized String value is read from the DB and converted back into a list of values
   *   - token is only used internally, and is never exposed
   *   - value of token must be highly unlikely to occur in the actual value of elements in the list..
   *     to prevent accidentally splitting those elements
   *
   * EXTRA_ARRAY_SEPARATOR_TOKEN_PATTERN
   *   - regex pattern:
   *       \s*\{\{\|,\|\}\}\s*
   * -----------------------------------
   */
  public static final String  EXTRA_ARRAY_SEPARATOR_TOKEN         = "{{|,|}}";
  public static final Pattern EXTRA_ARRAY_SEPARATOR_TOKEN_PATTERN = Pattern.compile("\\s*" + DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN.replaceAll("([\\{\\|\\}])", "\\\\$1") + "\\s*");

  // -----------------------------------
  // Intent -to- model
  // -----------------------------------

  public static DbIntent getInstance(int id, int folder_id, String name, Intent intent) throws Exception {
    if (intent == null) throw new Exception("Intent is null");

    int      flags          = intent.getFlags();
    String   action         = intent.getAction();
    String   package_name   = null;
    String   class_name     = null;
    String   data_uri       = intent.getDataString();
    String   data_type      = intent.getType();
    String[] categories     = null;
    Extra[]  extras         = null;

    // -------------

    ComponentName component = intent.getComponent();
    if (component != null) {
      package_name = component.getPackageName();
      class_name   = component.getClassName();
    }

    if (TextUtils.isEmpty(package_name) && (Build.VERSION.SDK_INT >= 4)) {
      package_name = intent.getPackage();
    }

    // -------------

    Set<String> categorySet = intent.getCategories();

    categories = ((categorySet != null) && !categorySet.isEmpty())
      ? categorySet.toArray(new String[categorySet.size()])
      : new String[0];

    // -------------

    Bundle bundle = intent.getExtras();
    List<Extra> extrasList = new ArrayList<Extra>();
    if (bundle != null) {
      Set<String> keySet = bundle.keySet();
      if ((keySet != null) && !keySet.isEmpty()) {
        Object value;
        Extra  extra;
        for(String key : keySet) {
          if (!TextUtils.isEmpty(key)) {
            value = bundle.get(key);
            if (value != null) {
              extra = DbIntent.getExtra(key, value);
              if (extra != null) {
                extrasList.add(extra);
              }
            }
          }
        }
      }
    }

    extras = ((extrasList != null) && !extrasList.isEmpty())
      ? extrasList.toArray(new Extra[extrasList.size()])
      : new Extra[0];

    // -------------

    return new DbIntent(id, folder_id, name, flags, action, package_name, class_name, data_uri, data_type, categories, extras);
  }

  private static Extra getExtra(String name, Object valueObj) {
    String valueType = null;
    return DbIntent.getExtra(name, valueObj, valueType);
  }

  private static Extra getExtra(String name, Object valueObj, String valueType) {
    try {
      if (TextUtils.isEmpty(valueType))
        valueType = valueObj.getClass().getName();

      if (TextUtils.isEmpty(valueType))
        return null;

      // normalize class name
      {
        int indexSuffix = valueType.indexOf('$');

        if (indexSuffix == 0)
          return null;
        if (indexSuffix > 0)
          valueType = valueType.substring(0, indexSuffix);
      }

      Extra extra = new Extra(name, /* value_type */ null, /* value */ null);

      switch(valueType) {
        case "java.lang.Boolean" : {
            Boolean value    = (Boolean) valueObj;
            String  valueStr = value.toString();

            extra.value_type = "boolean";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Boolean;" : {
            Boolean[] values = (Boolean[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Boolean value = values[i];
                valuesStr[i]  = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "boolean[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Byte" : {
            Byte   value    = (Byte) valueObj;
            String valueStr = value.toString();

            extra.value_type = "byte";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Byte;" : {
            Byte[] values = (Byte[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Byte value   = values[i];
                valuesStr[i] = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "byte[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Character" : {
            Character value = (Character) valueObj;
            String valueStr = value.toString();

            extra.value_type = "char";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Character;" : {
            Character[] values = (Character[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Character value = values[i];
                valuesStr[i]    = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "char[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Double" : {
            Double value    = (Double) valueObj;
            String valueStr = value.toString();

            extra.value_type = "double";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Double;" : {
            Double[] values = (Double[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Double value = values[i];
                valuesStr[i] = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "double[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Float" : {
            Float  value    = (Float) valueObj;
            String valueStr = value.toString();

            extra.value_type = "float";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Float;" : {
            Float[] values = (Float[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Float value  = values[i];
                valuesStr[i] = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "float[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Integer" : {
            Integer value    = (Integer) valueObj;
            String  valueStr = value.toString();

            extra.value_type = "int";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Integer;" : {
            Integer[] values = (Integer[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Integer value = values[i];
                valuesStr[i]  = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "int[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Long" : {
            Long   value    = (Long) valueObj;
            String valueStr = value.toString();

            extra.value_type = "long";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Long;" : {
            Long[] values = (Long[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Long value   = values[i];
                valuesStr[i] = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "long[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.Short" : {
            Short  value    = (Short) valueObj;
            String valueStr = value.toString();

            extra.value_type = "short";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.Short;" : {
            Short[] values = (Short[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Short value  = values[i];
                valuesStr[i] = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "short[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "java.lang.String" : {
            String valueStr = (String) valueObj;

            extra.value_type = "String";
            extra.value      = valueStr;
          }
          break;
        case "[Ljava.lang.String;" : {
            String[] valuesStr = (String[]) valueObj;
            if (valuesStr.length > 0) {
              String valueStr  = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "String[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "android.graphics.Bitmap" : {
            String filePath = BitmapUtils.extractFilePath(valueObj);

            if (!TextUtils.isEmpty(filePath)) {
              extra.value_type = "Bitmap";
              extra.value      = filePath;
            }
          }
          break;
        case "[Landroid.graphics.Bitmap;" : {
            Object[] values = (Object[]) valueObj;
            if (values.length > 0) {
              ArrayList<String> filePaths = new ArrayList<String>(values.length);
              String filePath;

              for (int i=0; i < values.length; i++) {
                filePath = BitmapUtils.extractFilePath(values[i]);

                if (!TextUtils.isEmpty(filePath))
                  filePaths.add(filePath);
              }

              if (!filePaths.isEmpty()) {
                String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, filePaths.toArray(new String[filePaths.size()]));

                extra.value_type = "Bitmap[]";
                extra.value      = valueStr;
              }
            }
          }
          break;
        case "android.net.Uri" :
        case "android.net.Uri$StringUri" : {
            Uri    value    = (Uri) valueObj;
            String valueStr = value.toString();

            extra.value_type = "Uri";
            extra.value      = valueStr;
          }
          break;
        case "[Landroid.net.Uri;" : {
            Uri[] values = (Uri[]) valueObj;
            if (values.length > 0) {
              String[] valuesStr = new String[values.length];

              for (int i=0; i < values.length; i++) {
                Uri value  = values[i];
                valuesStr[i] = value.toString();
              }
              String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

              extra.value_type = "Uri[]";
              extra.value      = valueStr;
            }
          }
          break;
        case "android.os.Parcelable" : {
            Parcelable value = (Parcelable) valueObj;

            if (BitmapUtils.isBitmap((Object)value))
              extra = DbIntent.getExtra(name, valueObj, "android.graphics.Bitmap");
            else if (UriUtils.isUri((Object)value))
              extra = DbIntent.getExtra(name, valueObj, "android.net.Uri");
          }
          break;
        case "[Landroid.os.Parcelable;" : {
            Parcelable[] values = (Parcelable[]) valueObj;
            if (values.length > 0) {
              Parcelable value = values[0];

              if (BitmapUtils.isBitmap((Object)value)) {
                String   newvalueType = "[Landroid.graphics.Bitmap;";
                Bitmap[] newValueObj  = new Bitmap[values.length];
                for (int i=0; i < values.length; i++) {
                  newValueObj[i] = (Bitmap) values[i];
                }
                extra = DbIntent.getExtra(name, newValueObj, newvalueType);
              }
              else if (UriUtils.isUri((Object)value)) {
                String newvalueType = "[Landroid.net.Uri;";
                Uri[]  newValueObj  = new Uri[values.length];
                for (int i=0; i < values.length; i++) {
                  newValueObj[i] = (Uri) values[i];
                }
                extra = DbIntent.getExtra(name, newValueObj, newvalueType);
              }
            }
          }
          break;
        case "java.util.ArrayList" : {
            ArrayList<Object> values = (ArrayList<Object>) valueObj;

            if ((values != null) && !values.isEmpty()) {
              Object listValueObj = values.get(0);
              Extra  listExtra    = DbIntent.getExtra(null, listValueObj);

              if (listExtra != null) {
                switch(listExtra.value_type) {
                  case "int" : {
                      String[] valuesStr = new String[values.size()];

                      for (int i=0; i < values.size(); i++) {
                        Integer value = (Integer) values.get(i);
                        valuesStr[i]  = value.toString();
                      }
                      String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

                      extra.value_type = "ArrayList<Integer>";
                      extra.value      = valueStr;
                    }
                    break;
                  case "String" : {
                      String[] valuesStr = values.toArray(new String[values.size()]);
                      String   valueStr  = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

                      extra.value_type = "ArrayList<String>";
                      extra.value      = valueStr;
                    }
                    break;
                  case "Bitmap" : {
                      ArrayList<String> filePaths = new ArrayList<String>(values.size());
                      String filePath;

                      for (int i=0; i < values.size(); i++) {
                        filePath = BitmapUtils.extractFilePath(values.get(i));

                        if (!TextUtils.isEmpty(filePath))
                          filePaths.add(filePath);
                      }

                      if (!filePaths.isEmpty()) {
                        String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, filePaths.toArray(new String[filePaths.size()]));

                        extra.value_type = "ArrayList<Bitmap>";
                        extra.value      = valueStr;
                      }
                    }
                    break;
                  case "Uri" : {
                      String[] valuesStr = new String[values.size()];

                      for (int i=0; i < values.size(); i++) {
                        Uri value    = (Uri) values.get(i);
                        valuesStr[i] = value.toString();
                      }
                      String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

                      extra.value_type = "ArrayList<Uri>";
                      extra.value      = valueStr;
                    }
                    break;
                }
              }
            }
          }
          break;
      }

      if ((extra != null) && (TextUtils.isEmpty(extra.value_type) || TextUtils.isEmpty(extra.value)))
        extra = null;

      return extra;
    }
    catch(Exception e) {
      return null;
    }
  }

  // -----------------------------------
  // model -to- Intent
  // -----------------------------------

  public Intent getIntent(Context context) {
    Intent intent = new Intent();

    if (flags >= 0) {
      intent.setFlags(flags);
    }

    if (!TextUtils.isEmpty(action)) {
      intent.setAction(action);
    }

    if (!TextUtils.isEmpty(package_name)) {
      if (!TextUtils.isEmpty(class_name)) {
        intent.setClassName(package_name, class_name);
      }
      else if (Build.VERSION.SDK_INT >= 4) {
        intent.setPackage(package_name);
      }
    }

    if (!TextUtils.isEmpty(data_uri)) {
      try {
        Uri formatted_data_uri = Uri.parse(data_uri);

        if (Build.VERSION.SDK_INT >= 16) {
          formatted_data_uri.normalizeScheme();
        }

        formatted_data_uri = UriUtils.usePublicFileProvider(context, formatted_data_uri);

        if (!TextUtils.isEmpty(data_type)) {
          String formatted_data_type = (Build.VERSION.SDK_INT >= 16)
            ? Intent.normalizeMimeType(data_type)
            : data_type.trim().toLowerCase();

          intent.setDataAndType(formatted_data_uri, formatted_data_type);
        }
        else {
          intent.setData(formatted_data_uri);
        }
      }
      catch(Exception e) {}
    }
    else if (!TextUtils.isEmpty(data_type)) {
      String formatted_data_type = (Build.VERSION.SDK_INT >= 16)
        ? Intent.normalizeMimeType(data_type)
        : data_type.trim().toLowerCase();

      intent.setType(formatted_data_type);
    }

    if ((categories != null) && (categories.length > 0)) {
      for (String category : categories) {
        if (!TextUtils.isEmpty(category)) {
          intent.addCategory(category);
        }
      }
    }

    if ((extras != null) && (extras.length > 0)) {
      for (Extra extra : extras) {
        if ((extra != null) && !TextUtils.isEmpty(extra.name) && !TextUtils.isEmpty(extra.value_type) && !TextUtils.isEmpty(extra.value)) {
          addExtra(context, intent, extra);
        }
      }
    }

    return intent;
  }

  private void addExtra(Context context, Intent intent, Extra extra) {
    switch(extra.value_type) {
      case "boolean" : {
          boolean value = Boolean.getBoolean(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "boolean[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            boolean[] values = new boolean[parts.length];
            boolean   value;

            for (int i=0; i < parts.length; i++) {
              value     = Boolean.getBoolean(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "byte" : {
          byte value = Byte.parseByte(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "byte[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            byte[] values = new byte[parts.length];
            byte   value;

            for (int i=0; i < parts.length; i++) {
              value     = Byte.parseByte(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "char" : {
          char value = extra.value.charAt(0);

          intent.putExtra(extra.name, value);
        }
        break;
      case "char[]" : {
          char[] values = extra.value.toCharArray();

          intent.putExtra(extra.name, values);
        }
        break;
      case "double" : {
          double value = Double.parseDouble(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "double[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            double[] values = new double[parts.length];
            double   value;

            for (int i=0; i < parts.length; i++) {
              value     = Double.parseDouble(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "float" : {
          float value = Float.parseFloat(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "float[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            float[] values = new float[parts.length];
            float   value;

            for (int i=0; i < parts.length; i++) {
              value     = Float.parseFloat(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "int" : {
          int value = Integer.parseInt(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "int[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            int[] values = new int[parts.length];
            int   value;

            for (int i=0; i < parts.length; i++) {
              value     = Integer.parseInt(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "ArrayList<Integer>" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<Integer> values = new ArrayList<Integer>(parts.length);
            Integer value;

            for (int i=0; i < parts.length; i++) {
              value = Integer.valueOf(parts[i]);
              values.add(value);
            }

            intent.putIntegerArrayListExtra(extra.name, values);
          }
        }
        break;
      case "long" : {
          long value = Long.parseLong(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "long[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            long[] values = new long[parts.length];
            long   value;

            for (int i=0; i < parts.length; i++) {
              value     = Long.parseLong(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "short" : {
          short value = Short.parseShort(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "short[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            short[] values = new short[parts.length];
            short   value;

            for (int i=0; i < parts.length; i++) {
              value     = Short.parseShort(parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, values);
          }
        }
        break;
      case "String" : {
          intent.putExtra(extra.name, extra.value);
        }
        break;
      case "String[]" : {
          String[] values = getExtraValueArray(extra);

          intent.putExtra(extra.name, values);
        }
        break;
      case "ArrayList<String>" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<String> values = new ArrayList<String>(
              Arrays.asList(parts)
            );

            intent.putStringArrayListExtra(extra.name, values);
          }
        }
        break;
      case "Bitmap" : {
          Parcelable value = BitmapUtils.decodeFileToParcelable(extra.value);

          if (value != null)
            intent.putExtra(extra.name, value);
        }
        break;
      case "Bitmap[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<Parcelable> values = new ArrayList<Parcelable>(parts.length);
            Parcelable value;

            for (int i=0; i < parts.length; i++) {
              value = BitmapUtils.decodeFileToParcelable(parts[i]);

              if (value != null)
                values.add(value);
            }

            if (!values.isEmpty())
              intent.putExtra(extra.name, values.toArray(new Parcelable[values.size()]));
          }
        }
        break;
      case "ArrayList<Bitmap>" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<Parcelable> values = new ArrayList<Parcelable>(parts.length);
            Parcelable value;

            for (int i=0; i < parts.length; i++) {
              value = BitmapUtils.decodeFileToParcelable(parts[i]);

              if (value != null)
                values.add(value);
            }

            if (!values.isEmpty())
              intent.putParcelableArrayListExtra(extra.name, values);
          }
        }
        break;
      case "Uri" : {
          Uri value = UriUtils.usePublicFileProvider(context, extra.value);

          if (value != null)
            intent.putExtra(extra.name, (Parcelable) value);
        }
        break;
      case "Uri[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            Uri[] values = new Uri[parts.length];
            Uri   value;

            for (int i=0; i < parts.length; i++) {
              value     = UriUtils.usePublicFileProvider(context, parts[i]);
              values[i] = value;
            }

            intent.putExtra(extra.name, (Parcelable[]) values);
          }
        }
        break;
      case "ArrayList<Uri>" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<Uri> values = new ArrayList<Uri>(parts.length);
            Uri value;

            for (int i=0; i < parts.length; i++) {
              value = UriUtils.usePublicFileProvider(context, parts[i]);
              values.add(value);
            }

            intent.putParcelableArrayListExtra(extra.name, values);
          }
        }
        break;
    }
  }

  public String[] getExtraValueArray(Extra extra) {
    try {
      return DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN_PATTERN.split(extra.value);
    }
    catch(Exception e) {
      return new String[0];
    }
  }
}
