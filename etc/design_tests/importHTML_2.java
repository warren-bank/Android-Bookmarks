// https://replit.com/languages/java10

import java.util.regex.Pattern;
import java.util.regex.Matcher;

class Main {
  private static final Pattern FOLDER_NAME   = Pattern.compile("<\\s*[hH]3(?:\\s+[^>]*)?>\\s*([^<]+)\\s*</\\s*[hH]3\\s*>");
  private static final Pattern BOOKMARK_NAME = Pattern.compile("<\\s*[aA]\\s+[^>]*>\\s*([^<]+)\\s*</\\s*[aA]\\s*>");
  private static final Pattern BOOKMARK_URL  = Pattern.compile("<\\s*[A]\\s+[^>]*HREF\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

  public static void main(String args[]) {
    String  line1 = "<DT><H3 ADD_DATE=\"1358810166\" LAST_MODIFIED=\"1653708471\" PERSONAL_TOOLBAR_FOLDER=\"true\">Bookmarks bar</H3>";
    String  line2 = "<DT><A HREF=\"https://omertron.github.io/api-themoviedb/apidocs/index.html\" ADD_DATE=\"1653708471\">api-themoviedb</A>";
    String  line3 = line1 + line2 + line1 + line2 + line1 + line2;
    Matcher matcher;

    matcher = FOLDER_NAME.matcher(line3);
    matcher.reset();
    while (matcher.find()) {
      System.out.println("folder name:   " + matcher.group(1));
    }

    matcher = BOOKMARK_NAME.matcher(line3);
    matcher.reset();
    while (matcher.find()) {
      System.out.println("bookmark name: " + matcher.group(1));
    }

    matcher = BOOKMARK_URL.matcher(line3);
    matcher.reset();
    while (matcher.find()) {
      System.out.println("bookmark url:  " + matcher.group(1));
    }
  }
}

/*
 * -----------------------------------------------
 * output:
 * -----------------------------------------------
 * folder name:   Bookmarks bar
 * folder name:   Bookmarks bar
 * folder name:   Bookmarks bar
 * bookmark name: api-themoviedb
 * bookmark name: api-themoviedb
 * bookmark name: api-themoviedb
 * bookmark url:  https://omertron.github.io/api-themoviedb/apidocs/index.html
 * bookmark url:  https://omertron.github.io/api-themoviedb/apidocs/index.html
 * bookmark url:  https://omertron.github.io/api-themoviedb/apidocs/index.html
 * -----------------------------------------------
 */
