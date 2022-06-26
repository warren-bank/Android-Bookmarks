// https://replit.com/languages/java10

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Main {
  public static void main(String args[]) {
    HashMap<String,Object> map = new HashMap<String,Object>();

    map.put(
      "boolean",
      Boolean.valueOf("true")
    );

    map.put(
      "boolean[]",
      new Boolean[] { (Boolean)map.get("boolean") }
    );

    map.put(
      "byte",
      Byte.valueOf("127")
    );

    map.put(
      "byte[]",
      new Byte[] { (Byte)map.get("byte") }
    );

    map.put(
      "char",
      Character.valueOf('A')
    );

    map.put(
      "char[]",
      new Character[] { (Character)map.get("char") }
    );

    map.put(
      "double",
      Double.valueOf("123.456")
    );

    map.put(
      "double[]",
      new Double[] { (Double)map.get("double") }
    );

    map.put(
      "float",
      Float.valueOf("123.456")
    );

    map.put(
      "float[]",
      new Float[] { (Float)map.get("float") }
    );

    map.put(
      "int",
      Integer.valueOf("256")
    );

    map.put(
      "int[]",
      new Integer[] { (Integer)map.get("int") }
    );

    map.put(
      "ArrayList<Integer>",
      new ArrayList<Integer>()
    );

    map.put(
      "long",
      Long.valueOf("256")
    );

    map.put(
      "long[]",
      new Long[] { (Long)map.get("long") }
    );

    map.put(
      "short",
      Short.valueOf("256")
    );

    map.put(
      "short[]",
      new Short[] { (Short)map.get("short") }
    );

    map.put(
      "String",
      "Hello World"
    );

    map.put(
      "String[]",
      new String[] { (String)map.get("String") }
    );

    map.put(
      "ArrayList<String>",
      new ArrayList<String>()
    );

    String name, valueType;
    Object valueObj;

    for (Map.Entry<String,Object> set : map.entrySet()) {
      name      = set.getKey();
      valueObj  = set.getValue();
      valueType = valueObj.getClass().getName();

      System.out.println(name + " = (" + valueType + ") " + valueObj.toString());
    }
  }
}

/*
 * -----------------------------------------------
 * output:
 * -----------------------------------------------
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
 * -----------------------------------------------
 */
