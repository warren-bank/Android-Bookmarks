package com.github.warren_bank.bookmarks.database.model;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
    this.data_uri     = data_uri;
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
    Extra  extra     = new Extra(name, /* value_type */ null, /* value */ null);
    String valueType = valueObj.getClass().getName();

    switch(valueType) {
      case "java.lang.Boolean" : {
          Boolean value    = (Boolean) valueObj;
          String  valueStr = value.toString();

          extra.value_type = "boolean";
          extra.value      = valueStr;
        }
        break;
      case "[Ljava.lang.Boolean;" : {
          Boolean[] values    = (Boolean[]) valueObj;
          String[]  valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Boolean value = values[i];
            valuesStr[i]  = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "boolean[]";
          extra.value      = valueStr;
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
          Byte[]   values    = (Byte[]) valueObj;
          String[] valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Byte value   = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "byte[]";
          extra.value      = valueStr;
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
          Character[] values    = (Character[]) valueObj;
          String[]    valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Character value = values[i];
            valuesStr[i]    = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "char[]";
          extra.value      = valueStr;
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
          Double[] values    = (Double[]) valueObj;
          String[] valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Double value = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "double[]";
          extra.value      = valueStr;
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
          Float[]  values    = (Float[]) valueObj;
          String[] valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Float value  = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "float[]";
          extra.value      = valueStr;
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
          Integer[] values    = (Integer[]) valueObj;
          String[]  valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Integer value = values[i];
            valuesStr[i]  = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "int[]";
          extra.value      = valueStr;
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
          Long[]   values    = (Long[]) valueObj;
          String[] valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Long value   = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "long[]";
          extra.value      = valueStr;
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
          Short[]  values    = (Short[]) valueObj;
          String[] valuesStr = new String[values.length];

          for (int i=0; i < values.length; i++) {
            Short value  = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "short[]";
          extra.value      = valueStr;
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
          String   valueStr  = TextUtils.join(DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "String[]";
          extra.value      = valueStr;
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
              }
            }
          }

          if (TextUtils.isEmpty(extra.value_type))
            extra = null;
        }
        break;
      default : {
          extra = null;
        }
        break;
    }

    return extra;
  }

  // -----------------------------------
  // model -to- Intent
  // -----------------------------------

  public Intent getIntent() {
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
          addExtra(intent, extra);
        }
      }
    }

    return intent;
  }

  private void addExtra(Intent intent, Extra extra) {
    switch(extra.value_type) {
      case "boolean" : {
          boolean value = Boolean.getBoolean(extra.value);

          intent.putExtra(extra.name, value);
        }
        break;
      case "boolean[]" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            boolean[] value = new boolean[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Boolean.getBoolean(parts[i]);
            }

            intent.putExtra(extra.name, value);
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
            byte[] value = new byte[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Byte.parseByte(parts[i]);
            }

            intent.putExtra(extra.name, value);
          }
        }
        break;
      case "char" : {
          char value = extra.value.charAt(0);

          intent.putExtra(extra.name, value);
        }
        break;
      case "char[]" : {
          char[] value = extra.value.toCharArray();

          intent.putExtra(extra.name, value);
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
            double[] value = new double[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Double.parseDouble(parts[i]);
            }

            intent.putExtra(extra.name, value);
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
            float[] value = new float[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Float.parseFloat(parts[i]);
            }

            intent.putExtra(extra.name, value);
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
            int[] value = new int[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Integer.parseInt(parts[i]);
            }

            intent.putExtra(extra.name, value);
          }
        }
        break;
      case "ArrayList<Integer>" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<Integer> value = new ArrayList<Integer>(parts.length);

            for (int i=0; i < parts.length; i++) {
              value.add(Integer.valueOf(parts[i]));
            }

            intent.putExtra(extra.name, value);
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
            long[] value = new long[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Long.parseLong(parts[i]);
            }

            intent.putExtra(extra.name, value);
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
            short[] value = new short[parts.length];

            for (int i=0; i < parts.length; i++) {
              value[i] = Short.parseShort(parts[i]);
            }

            intent.putExtra(extra.name, value);
          }
        }
        break;
      case "String" : {
          intent.putExtra(extra.name, extra.value);
        }
        break;
      case "String[]" : {
          String[] value = getExtraValueArray(extra);

          intent.putExtra(extra.name, value);
        }
        break;
      case "ArrayList<String>" : {
          String[] parts = getExtraValueArray(extra);

          if (parts.length > 0) {
            ArrayList<String> value = new ArrayList<String>(
              Arrays.asList(parts)
            );

            intent.putExtra(extra.name, value);
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
