// https://replit.com/languages/java10

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class Main {
  public static void main(String args[]) {
    HashMap<String,Object> map = getMap();
    printMap(map);
    printExtras(map);
  }

  // ---------------------------------------------------------------------------
  // initialize Java Objects

  private static HashMap<String,Object> getMap() {
    HashMap<String,Object> map = new HashMap<String,Object>();

    map.put(
      "boolean",
      Boolean.valueOf("true")
    );

    map.put(
      "boolean[]",
      new Boolean[] { (Boolean)map.get("boolean"), (Boolean)map.get("boolean"), (Boolean)map.get("boolean") }
    );

    map.put(
      "byte",
      Byte.valueOf("127")
    );

    map.put(
      "byte[]",
      new Byte[] { (Byte)map.get("byte"), (Byte)map.get("byte"), (Byte)map.get("byte") }
    );

    map.put(
      "char",
      Character.valueOf('A')
    );

    map.put(
      "char[]",
      new Character[] { (Character)map.get("char"), (Character)map.get("char"), (Character)map.get("char") }
    );

    map.put(
      "double",
      Double.valueOf("123.456")
    );

    map.put(
      "double[]",
      new Double[] { (Double)map.get("double"), (Double)map.get("double"), (Double)map.get("double") }
    );

    map.put(
      "float",
      Float.valueOf("123.456")
    );

    map.put(
      "float[]",
      new Float[] { (Float)map.get("float"), (Float)map.get("float"), (Float)map.get("float") }
    );

    map.put(
      "int",
      Integer.valueOf("256")
    );

    map.put(
      "int[]",
      new Integer[] { (Integer)map.get("int"), (Integer)map.get("int"), (Integer)map.get("int") }
    );

    map.put(
      "ArrayList<Integer>",
      new ArrayList<Integer>(
        Arrays.asList((Integer[])map.get("int[]"))
      )
    );

    map.put(
      "long",
      Long.valueOf("256")
    );

    map.put(
      "long[]",
      new Long[] { (Long)map.get("long"), (Long)map.get("long"), (Long)map.get("long") }
    );

    map.put(
      "short",
      Short.valueOf("256")
    );

    map.put(
      "short[]",
      new Short[] { (Short)map.get("short"), (Short)map.get("short"), (Short)map.get("short") }
    );

    map.put(
      "String",
      "Hello World"
    );

    map.put(
      "String[]",
      new String[] { (String)map.get("String"), (String)map.get("String"), (String)map.get("String") }
    );

    map.put(
      "ArrayList<String>",
      new ArrayList<String>(
        Arrays.asList((String[])map.get("String[]"))
      )
    );

    return map;
  }

  // ---------------------------------------------------------------------------
  // print Java Objects

  private static void printMap(HashMap<String,Object> map) {
    String name, valueType;
    Object valueObj;

    for (Map.Entry<String,Object> set : map.entrySet()) {
      name      = set.getKey();
      valueObj  = set.getValue();
      valueType = valueObj.getClass().getName();

      System.out.println(name + " = (" + valueType + ") " + valueObj.toString());
    }
  }

  // ---------------------------------------------------------------------------
  // DB model

  public static class Extra {
    public String name;
    public String value_type;
    public String value;

    public Extra(String name, String value_type, String value) {
      this.name       = name;
      this.value_type = value_type;
      this.value      = value;
    }
  }

  // ---------------------------------------------------------------------------
  // serialize Java Objects to DB model

  // mock: android.text.TextUtils
  private static class TextUtils {
    public static boolean isEmpty(String val) {
      return (val == null) || val.equals("");
    }
    public static String join(String glue, String[] vals) {
      String val = (vals.length == 0) ? "" : vals[0];
      for (int i=1; i < vals.length; i++) {
        val += glue + vals[i];
      }
      return val;
    }
  }

  private static final String  EXTRA_ARRAY_SEPARATOR_TOKEN         = "{{|,|}}";
  private static final Pattern EXTRA_ARRAY_SEPARATOR_TOKEN_PATTERN = Pattern.compile("\\s*" + Main.EXTRA_ARRAY_SEPARATOR_TOKEN.replaceAll("([\\{\\|\\}])", "\\\\$1") + "\\s*");

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

          for (var i=0; i < values.length; i++) {
            Boolean value = values[i];
            valuesStr[i]  = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Byte value   = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Character value = values[i];
            valuesStr[i]    = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Double value = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Float value  = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Integer value = values[i];
            valuesStr[i]  = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Long value   = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

          for (var i=0; i < values.length; i++) {
            Short value  = values[i];
            valuesStr[i] = value.toString();
          }
          String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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
          String   valueStr  = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

          extra.value_type = "String[]";
          extra.value      = valueStr;
        }
        break;
      case "java.util.ArrayList" : {
          ArrayList<Object> values = (ArrayList<Object>) valueObj;

          if ((values != null) && !values.isEmpty()) {
            Object listValueObj = values.get(0);
            Extra  listExtra    = getExtra(null, listValueObj);

            if (listExtra != null) {
              switch(listExtra.value_type) {
                case "int" : {
                    String[] valuesStr = new String[values.size()];

                    for (var i=0; i < values.size(); i++) {
                      Integer value = (Integer) values.get(i);
                      valuesStr[i]  = value.toString();
                    }
                    String valueStr = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

                    extra.value_type = "ArrayList<Integer>";
                    extra.value      = valueStr;
                  }
                  break;
                case "String" : {
                    String[] valuesStr = values.toArray(new String[values.size()]);
                    String   valueStr  = TextUtils.join(Main.EXTRA_ARRAY_SEPARATOR_TOKEN, valuesStr);

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

  // ---------------------------------------------------------------------------
  // print DB model

  private static void printExtras(HashMap<String,Object> map) {
    String name;
    Object valueObj;
    Extra extra;

    for (Map.Entry<String,Object> set : map.entrySet()) {
      name      = set.getKey();
      valueObj  = set.getValue();
      extra     = getExtra(name, valueObj);

      if (extra != null)
        System.out.println(extra.name + " = (" + extra.value_type + ") " + extra.value);
    }
  }

  // ---------------------------------------------------------------------------
}

/*
 * -----------------------------------------------------------------------------
 * output: Java Objects
 * -----------------------------------------------------------------------------
 * boolean = (java.lang.Boolean) true
 * boolean[] = ([Ljava.lang.Boolean;) [Ljava.lang.Boolean;@5f184fc6
 * byte = (java.lang.Byte) 127
 * byte[] = ([Ljava.lang.Byte;) [Ljava.lang.Byte;@7b23ec81
 * char = (java.lang.Character) A
 * char[] = ([Ljava.lang.Character;) [Ljava.lang.Character;@4f3f5b24
 * double = (java.lang.Double) 123.456
 * double[] = ([Ljava.lang.Double;) [Ljava.lang.Double;@6acbcfc0
 * float = (java.lang.Float) 123.456
 * float[] = ([Ljava.lang.Float;) [Ljava.lang.Float;@15aeb7ab
 * int = (java.lang.Integer) 256
 * int[] = ([Ljava.lang.Integer;) [Ljava.lang.Integer;@7ef20235
 * ArrayList<Integer> = (java.util.ArrayList) []
 * long = (java.lang.Long) 256
 * long[] = ([Ljava.lang.Long;) [Ljava.lang.Long;@5fd0d5ae
 * short = (java.lang.Short) 256
 * short[] = ([Ljava.lang.Short;) [Ljava.lang.Short;@16b98e56
 * String = (java.lang.String) Hello World
 * String[] = ([Ljava.lang.String;) [Ljava.lang.String;@27d6c5e0
 * ArrayList<String> = (java.util.ArrayList) []
 * -----------------------------------------------------------------------------
 */

/*
 * -----------------------------------------------------------------------------
 * output: DB model
 * -----------------------------------------------------------------------------
 * boolean = (boolean) true
 * boolean[] = (boolean[]) true{{|,|}}true{{|,|}}true
 * byte = (byte) 127
 * byte[] = (byte[]) 127{{|,|}}127{{|,|}}127
 * char = (char) A
 * char[] = (char[]) A{{|,|}}A{{|,|}}A
 * double = (double) 123.456
 * double[] = (double[]) 123.456{{|,|}}123.456{{|,|}}123.456
 * float = (float) 123.456
 * float[] = (float[]) 123.456{{|,|}}123.456{{|,|}}123.456
 * int = (int) 256
 * int[] = (int[]) 256{{|,|}}256{{|,|}}256
 * ArrayList<Integer> = (ArrayList<Integer>) 256{{|,|}}256{{|,|}}256
 * long = (long) 256
 * long[] = (long[]) 256{{|,|}}256{{|,|}}256
 * short = (short) 256
 * short[] = (short[]) 256{{|,|}}256{{|,|}}256
 * String = (String) Hello World
 * String[] = (String[]) Hello World{{|,|}}Hello World{{|,|}}Hello World
 * ArrayList<String> = (ArrayList<String>) Hello World{{|,|}}Hello World{{|,|}}Hello World
 * -----------------------------------------------------------------------------
 */
