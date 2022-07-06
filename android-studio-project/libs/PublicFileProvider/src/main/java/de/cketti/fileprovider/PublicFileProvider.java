/*
 * Copyright (C) 2016 cketti
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cketti.fileprovider;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import org.xmlpull.v1.XmlPullParserException;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;


/**
 * PublicFileProvider is a special subclass of {@link ContentProvider} that facilitates exposing files associated with
 * an app by creating a {@code content://} {@link Uri} for a file instead of a {@code file:/// Uri}.
 * <p>
 * <strong>WARNING:</strong> Most of the time this is NOT what you want. Use
 * <a href="https://developer.android.com/reference/android/support/v4/content/FileProvider.html">FileProvider</a>
 * to only grant temporary access to files, e.g. when 
 * <a href="https://developer.android.com/training/secure-file-sharing/index.html">sharing</a> content to other apps.
 * </p><p>
 * PublicFileProvider is a modified version of FileProvider with the specific goal to expose files without using
 * Android's URI permission mechanism. This can come in handy when you have to provide a {@code content://} URI but
 * can't easily grant read access to whatever app ends up accessing the content.
 * One use case is a custom ringtone in a notification. Check out the blog post
 * <a href="https://commonsware.com/blog/2016/09/07/notifications-sounds-android-7p0-aggravation.html">Notifications,
 * Sounds, Android 7.0, and Aggravation</a> for more details.
 * </p><p>
 * This overview of PublicFileProvider includes the following topics:
 * </p>
 * <ol>
 *     <li><a href="#ProviderDefinition">Defining a PublicFileProvider</a></li>
 *     <li><a href="#SpecifyFiles">Specifying Available Files</a></li>
 *     <li><a href="#GetUri">Retrieving the Content URI for a File</a></li>
 * </ol>
 * <h3 id="ProviderDefinition">Defining a PublicFileProvider</h3>
 * <p>
 * Since the default functionality of PublicFileProvider includes content URI generation for files, you
 * don't need to define a subclass in code. Instead, you can include a PublicFileProvider in your app
 * by specifying it entirely in XML. To specify the PublicFileProvider component itself, add a
 * <code><a href="{@docRoot}guide/topics/manifest/provider-element.html">&lt;provider&gt;</a></code>
 * element to your app manifest. Set the <code>android:name</code> attribute to
 * <code>de.cketti.fileprovider.PublicFileProvider</code>. Set the <code>android:authorities</code>
 * attribute to a URI authority based on a domain you control; for example, if you control the
 * domain <code>mydomain.com</code> you should use the authority
 * <code>com.mydomain.publicfileprovider</code>. Set the <code>android:exported</code> attribute to
 * <code>true</code>. For example:
 * <pre class="prettyprint">
 *&lt;manifest&gt;
 *    ...
 *    &lt;application&gt;
 *        ...
 *        &lt;provider
 *            android:name="de.cketti.fileprovider.PublicFileProvider"
 *            android:authorities="com.mydomain.publicfileprovider"
 *            android:exported="true"&gt;
 *            ...
 *        &lt;/provider&gt;
 *        ...
 *    &lt;/application&gt;
 *&lt;/manifest&gt;</pre>
 * <p>
 * You can override some of the default behavior of PublicFileProvider by extending the PublicFileProvider class. If you
 * do so, use the fully-qualified class name in the <code>android:name</code>
 * attribute of the <code>&lt;provider&gt;</code> element.
 * <h3 id="SpecifyFiles">Specifying Available Files</h3>
 * A PublicFileProvider can only generate a content URI for files in directories that you specify
 * beforehand. To specify a directory, specify its storage area and path in XML, using child
 * elements of the <code>&lt;paths&gt;</code> element.
 * For example, the following <code>paths</code> element tells PublicFileProvider that you intend to
 * request content URIs for the <code>images/</code> subdirectory of your private file area.
 * <pre class="prettyprint">
 *&lt;paths xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 *    &lt;files-path name="my_notification_sounds" path="notification_sounds/"/&gt;
 *    ...
 *&lt;/paths&gt;
 *</pre>
 * <p>
 * The <code>&lt;paths&gt;</code> element must contain one or more of the following child elements:
 * </p>
 * <div>
 *     <div class="dt">
 * <pre class="prettyprint">
 *&lt;files-path name="<i>name</i>" path="<i>path</i>" /&gt;
 *</pre>
 *     </div>
 *     <div class="dd">
 *     Represents files in the <code>files/</code> subdirectory of your app's internal storage
 *     area. This subdirectory is the same as the value returned by {@link Context#getFilesDir()
 *     Context.getFilesDir()}.
 *     </div>
 *     <div class="dt">
 * <pre class="prettyprint">
 *&lt;cache-path name="<i>name</i>" path="<i>path</i>" /&gt;
 *</pre>
 *     </div>
 *     <div class="dd">
 *     Represents files in the cache subdirectory of your app's internal storage area. The root path
 *     of this subdirectory is the same as the value returned by {@link Context#getCacheDir()
 *     getCacheDir()}.
 *     </div>
 *     <div class="dt">
 * <pre class="prettyprint">
 *&lt;external-path name="<i>name</i>" path="<i>path</i>" /&gt;
 *</pre>
 *     </div>
 *     <div class="dd">
 *     Represents files in the root of the external storage area. The root path of this subdirectory
 *     is the same as the value returned by
 *     {@link Environment#getExternalStorageDirectory() Environment.getExternalStorageDirectory()}.
 *     </div>
 *     <div class="dt">
 * <pre class="prettyprint">
 *&lt;external-files-path name="<i>name</i>" path="<i>path</i>" /&gt;
 *</pre>
 *     </div>
 *     <div class="dd">
 *     Represents files in the root of your app's external storage area. The root path of this
 *     subdirectory is the same as the value returned by
 *     {@code Context#getExternalFilesDir(String) Context.getExternalFilesDir(null)}.
 *     </div>
 *     <div class="dt">
 * <pre class="prettyprint">
 *&lt;external-cache-path name="<i>name</i>" path="<i>path</i>" /&gt;
 *</pre>
 *     </div>
 *     <div class="dd">
 *     Represents files in the root of your app's external cache area. The root path of this
 *     subdirectory is the same as the value returned by
 *     {@link Context#getExternalCacheDir() Context.getExternalCacheDir()}.
 *     </div>
 * </div>
 * <p>
 *     These child elements all use the same attributes:
 * </p>
 * <div>
 *     <div class="dt">
 *         <code>name="<i>name</i>"</code>
 *     </div>
 *     <div class="dd">
 *         A URI path segment. To enforce security, this value hides the name of the subdirectory
 *         you're sharing. The subdirectory name for this value is contained in the
 *         <code>path</code> attribute.
 *     </div>
 *     <div class="dt">
 *         <code>path="<i>path</i>"</code>
 *     </div>
 *     <div class="dd">
 *         The subdirectory you're sharing. While the <code>name</code> attribute is a URI path
 *         segment, the <code>path</code> value is an actual subdirectory name. Notice that the
 *         value refers to a <b>subdirectory</b>, not an individual file or files. You can't
 *         share a single file by its file name, nor can you specify a subset of files using
 *         wildcards.
 *     </div>
 * </div>
 * <p>
 * You must specify a child element of <code>&lt;paths&gt;</code> for each directory that contains
 * files for which you want content URIs. For example, these XML elements specify two directories:
 * <pre class="prettyprint">
 *&lt;paths xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 *    &lt;files-path name="my_images" path="images/"/&gt;
 *    &lt;files-path name="my_docs" path="docs/"/&gt;
 *&lt;/paths&gt;
 *</pre>
 * <p>
 * Put the <code>&lt;paths&gt;</code> element and its children in an XML file in your project.
 * For example, you can add them to a new file called <code>res/xml/publicfileprovider_paths.xml</code>.
 * To link this file to the PublicFileProvider, add a
 * <a href="{@docRoot}guide/topics/manifest/meta-data-element.html">&lt;meta-data&gt;</a> element
 * as a child of the <code>&lt;provider&gt;</code> element that defines the PublicFileProvider. Set the
 * <code>&lt;meta-data&gt;</code> element's "android:name" attribute to
 * <code>de.cketti.fileprovider.PUBLIC_FILE_PROVIDER_PATHS</code>. Set the element's "android:resource" attribute
 * to <code>&#64;xml/publicfileprovider_paths</code> (notice that you don't specify the <code>.xml</code>
 * extension). For example:
 * <pre class="prettyprint">
 *&lt;provider
 *    android:name="de.cketti.fileprovider.PublicFileProvider"
 *    android:authorities="com.mydomain.publicfileprovider"
 *    android:exported="true"&gt;
 *    &lt;meta-data
 *        android:name="de.cketti.fileprovider.PUBLIC_FILE_PROVIDER_PATHS"
 *        android:resource="&#64;xml/publicfileprovider_paths" /&gt;
 *&lt;/provider&gt;
 *</pre>
 * <h3 id="GetUri">Generating the Content URI for a File</h3>
 * <p>
 * To share a file with another app using a content URI, your app has to generate the content URI.
 * To generate the content URI, create a new {@link File} for the file, then pass the {@link File}
 * to {@link #getUriForFile(Context, String, File) getUriForFile()}. You can send the content URI
 * returned by {@link #getUriForFile(Context, String, File) getUriForFile()} to another app in an
 * {@link android.content.Intent}. The client app that receives the content URI can open the file
 * and access its contents by calling
 * {@link android.content.ContentResolver#openFileDescriptor(Uri, String)
 * ContentResolver.openFileDescriptor} to get a {@link ParcelFileDescriptor}.
 * <p>
 * For example, suppose your app is offering files to other apps with a PublicFileProvider that has the
 * authority <code>com.mydomain.publicfileprovider</code>. To get a content URI for the file
 * <code>default_image.jpg</code> in the <code>images/</code> subdirectory of your internal storage
 * add the following code:
 * <pre class="prettyprint">
 *File imagePath = new File(Context.getFilesDir(), "images");
 *File newFile = new File(imagePath, "default_image.jpg");
 *Uri contentUri = getUriForFile(getContext(), "com.mydomain.publicfileprovider", newFile);
 *</pre>
 * As a result of the previous snippet,
 * {@link #getUriForFile(Context, String, File) getUriForFile()} returns the content URI
 * <code>content://com.mydomain.publicfileprovider/my_images/default_image.jpg</code>.
 */
public class PublicFileProvider extends ContentProvider {
    private static final String[] COLUMNS = { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };

    private static final String META_DATA_FILE_PROVIDER_PATHS = "de.cketti.fileprovider.PUBLIC_FILE_PROVIDER_PATHS";

    private static final String TAG_ROOT_PATH = "root-path";
    private static final String TAG_FILES_PATH = "files-path";
    private static final String TAG_CACHE_PATH = "cache-path";
    private static final String TAG_EXTERNAL = "external-path";
    private static final String TAG_EXTERNAL_FILES = "external-files-path";
    private static final String TAG_EXTERNAL_CACHE = "external-cache-path";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";

    private static final File DEVICE_ROOT = new File("/");

    // @GuardedBy("cache")
    private static final HashMap<String, PathStrategy> cache = new HashMap<>();

    private PathStrategy strategy;

    /**
     * The default PublicFileProvider implementation does not need to be initialized. If you want to
     * override this method, you must provide your own subclass of PublicFileProvider.
     */
    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * After the PublicFileProvider is instantiated, this method is called to provide the system with
     * information about the provider.
     *
     * @param context A {@link Context} for the current component.
     * @param info A {@link ProviderInfo} for the new provider.
     */
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);

        if (!info.exported) {
            throw new AssertionError("Provider must be exported");
        }

        strategy = getPathStrategy(context, info.authority);
    }

    /**
     * Return a content URI for a given {@link File}. A PublicFileProvider can only return a
     * <code>content</code> {@link Uri} for file paths defined in their <code>&lt;paths&gt;</code>
     * meta-data element. See the Class Overview for more information.
     *
     * @param context A {@link Context} for the current component.
     * @param authority The authority of a {@link PublicFileProvider} defined in a
     *            {@code <provider>} element in your app's manifest.
     * @param file A {@link File} pointing to the filename for which you want a
     * <code>content</code> {@link Uri}.
     * @return A content URI for the file.
     * @throws IllegalArgumentException When the given {@link File} is outside
     * the paths supported by the provider.
     */
    public static Uri getUriForFile(Context context, String authority, File file) {
        PathStrategy strategy = getPathStrategy(context, authority);
        return strategy.getUriForFile(file);
    }

    /**
     * Use a content URI returned by
     * {@link #getUriForFile(Context, String, File) getUriForFile()} to get information about a file
     * managed by the PublicFileProvider.
     * PublicFileProvider reports the column names defined in {@link android.provider.OpenableColumns}:
     * <ul>
     * <li>{@link android.provider.OpenableColumns#DISPLAY_NAME}</li>
     * <li>{@link android.provider.OpenableColumns#SIZE}</li>
     * </ul>
     * For more information, see
     * {@link ContentProvider#query(Uri, String[], String, String[], String)
     * ContentProvider.query()}.
     *
     * @param uri A content URI returned by {@link #getUriForFile}.
     * @param projection The list of columns to put into the {@link Cursor}. If null all columns are
     * included.
     * @param selection Selection criteria to apply. If null then all data that matches the content
     * URI is returned.
     * @param selectionArgs An array of {@link java.lang.String}, containing arguments to bind to
     * the <i>selection</i> parameter. The <i>query</i> method scans <i>selection</i> from left to
     * right and iterates through <i>selectionArgs</i>, replacing the current "?" character in
     * <i>selection</i> with the value at the current position in <i>selectionArgs</i>. The
     * values are bound to <i>selection</i> as {@link java.lang.String} values.
     * @param sortOrder A {@link java.lang.String} containing the column name(s) on which to sort
     * the resulting {@link Cursor}.
     * @return A {@link Cursor} containing the results of the query.
     *
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        File file = strategy.getFileForUri(uri);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] columns = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String column : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(column)) {
                columns[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = file.getName();
            } else if (OpenableColumns.SIZE.equals(column)) {
                columns[i] = OpenableColumns.SIZE;
                values[i++] = file.length();
            }
        }

        columns = copyOf(columns, i);
        values = copyOf(values, i);

        MatrixCursor cursor = new MatrixCursor(columns, 1);
        cursor.addRow(values);
        return cursor;
    }

    /**
     * Returns the MIME type of a content URI returned by
     * {@link #getUriForFile(Context, String, File) getUriForFile()}.
     *
     * @param uri A content URI returned by
     * {@link #getUriForFile(Context, String, File) getUriForFile()}.
     * @return If the associated file has an extension, the MIME type associated with that
     * extension; otherwise <code>application/octet-stream</code>.
     */
    @Override
    public String getType(Uri uri) {
        File file = strategy.getFileForUri(uri);

        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    @Override
    public final Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("This is a read-only provider");
    }

    @Override
    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("This is a read-only provider");
    }

    @Override
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("This is a read-only provider");
    }

    @Override
    public final ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new IllegalArgumentException("mode must be \"r\"");
        }
        
        File file = strategy.getFileForUri(uri);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public final ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) 
            throws FileNotFoundException {
        return super.openFile(uri, mode, signal);
    }

    @Override
    public final AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        return super.openAssetFile(uri, mode);
    }

    @Override
    public final AssetFileDescriptor openAssetFile(Uri uri, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        return super.openAssetFile(uri, mode, signal);
    }

    /**
     * Return {@link PathStrategy} for given authority, either by parsing or
     * returning from cache.
     */
    private static PathStrategy getPathStrategy(Context context, String authority) {
        PathStrategy pathStrategy;
        synchronized (cache) {
            pathStrategy = cache.get(authority);
            if (pathStrategy == null) {
                try {
                    pathStrategy = parsePathStrategy(context, authority);
                } catch (IOException | XmlPullParserException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                }
                cache.put(authority, pathStrategy);
            }
        }
        return pathStrategy;
    }

    /**
     * Parse and return {@link PathStrategy} for given authority as defined in
     * {@link #META_DATA_FILE_PROVIDER_PATHS} {@code <meta-data>}.
     *
     * @see #getPathStrategy(Context, String)
     */
    private static PathStrategy parsePathStrategy(Context context, String authority) 
            throws IOException, XmlPullParserException {
        SimplePathStrategy pathStrategy = new SimplePathStrategy(authority);

        PackageManager packageManager = context.getPackageManager();
        ProviderInfo info = packageManager.resolveContentProvider(authority, PackageManager.GET_META_DATA);
        XmlResourceParser parser = info.loadXmlMetaData(packageManager, META_DATA_FILE_PROVIDER_PATHS);
        if (parser == null) {
            throw new IllegalArgumentException("Missing " + META_DATA_FILE_PROVIDER_PATHS + " meta-data");
        }

        int type;
        while ((type = parser.next()) != END_DOCUMENT) {
            if (type == START_TAG) {
                String tag = parser.getName();

                String name = parser.getAttributeValue(null, ATTR_NAME);
                String path = parser.getAttributeValue(null, ATTR_PATH);

                File target = null;
                if (TAG_ROOT_PATH.equals(tag)) {
                    target = DEVICE_ROOT;
                } else if (TAG_FILES_PATH.equals(tag)) {
                    target = context.getFilesDir();
                } else if (TAG_CACHE_PATH.equals(tag)) {
                    target = context.getCacheDir();
                } else if (TAG_EXTERNAL.equals(tag)) {
                    target = Environment.getExternalStorageDirectory();
                } else if (TAG_EXTERNAL_FILES.equals(tag)) {
                    File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);
                    if (externalFilesDirs.length > 0) {
                        target = externalFilesDirs[0];
                    }
                } else if (TAG_EXTERNAL_CACHE.equals(tag)) {
                    File[] externalCacheDirs = ContextCompat.getExternalCacheDirs(context);
                    if (externalCacheDirs.length > 0) {
                        target = externalCacheDirs[0];
                    }
                }

                if (target != null) {
                    pathStrategy.addRoot(name, buildPath(target, path));
                }
            }
        }

        return pathStrategy;
    }

    /**
     * Strategy for mapping between {@link File} and {@link Uri}.
     * <p>
     * Strategies must be symmetric so that mapping a {@link File} to a
     * {@link Uri} and then back to a {@link File} points at the original
     * target.
     * <p>
     * Strategies must remain consistent across app launches, and not rely on
     * dynamic state. This ensures that any generated {@link Uri} can still be
     * resolved if your process is killed and later restarted.
     *
     * @see SimplePathStrategy
     */
    interface PathStrategy {
        /**
         * Return a {@link Uri} that represents the given {@link File}.
         */
        Uri getUriForFile(File file);

        /**
         * Return a {@link File} that represents the given {@link Uri}.
         */
        File getFileForUri(Uri uri);
    }

    /**
     * Strategy that provides access to files living under a narrow whitelist of
     * filesystem roots. It will throw {@link SecurityException} if callers try
     * accessing files outside the configured roots.
     * <p>
     * For example, if configured with
     * {@code addRoot("myfiles", context.getFilesDir())}, then
     * {@code context.getFileStreamPath("foo.txt")} would map to
     * {@code content://myauthority/myfiles/foo.txt}.
     */
    static class SimplePathStrategy implements PathStrategy {
        private final String authority;
        private final HashMap<String, File> roots = new HashMap<>();

        SimplePathStrategy(String authority) {
            this.authority = authority;
        }

        /**
         * Add a mapping from a name to a filesystem root. The provider only offers
         * access to files that live under configured roots.
         */
        void addRoot(String name, File root) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Name must not be empty");
            }

            try {
                // Resolve to canonical path to keep path checking fast
                root = root.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + root, e);
            }

            roots.put(name, root);
        }

        @Override
        public Uri getUriForFile(File file) {
            String path;
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            // Find the most-specific root path
            Map.Entry<String, File> mostSpecific = null;
            for (Map.Entry<String, File> root : roots.entrySet()) {
                String rootPath = root.getValue().getPath();
                if (path.startsWith(rootPath) && (mostSpecific == null ||
                        rootPath.length() > mostSpecific.getValue().getPath().length())) {
                    mostSpecific = root;
                }
            }

            if (mostSpecific == null) {
                throw new IllegalArgumentException("Failed to find configured root that contains " + path);
            }

            // Start at first char of path under root
            String rootPath = mostSpecific.getValue().getPath();
            if (rootPath.endsWith("/")) {
                path = path.substring(rootPath.length());
            } else {
                path = path.substring(rootPath.length() + 1);
            }

            // Encode the tag and path separately
            path = Uri.encode(mostSpecific.getKey()) + '/' + Uri.encode(path, "/");
            return new Uri.Builder()
                    .scheme("content")
                    .authority(authority)
                    .encodedPath(path)
                    .build();
        }

        @Override
        public File getFileForUri(Uri uri) {
            String path = uri.getEncodedPath();

            int splitIndex = path.indexOf('/', 1);
            String tag = Uri.decode(path.substring(1, splitIndex));
            path = Uri.decode(path.substring(splitIndex + 1));

            File root = roots.get(tag);
            if (root == null) {
                throw new IllegalArgumentException("Unable to find configured root for " + uri);
            }

            File file = new File(root, path);
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            if (!file.getPath().startsWith(root.getPath())) {
                throw new SecurityException("Resolved path jumped beyond configured root");
            }

            return file;
        }
    }

    private static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (segment != null) {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

    private static String[] copyOf(String[] original, int newLength) {
        String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }
}
