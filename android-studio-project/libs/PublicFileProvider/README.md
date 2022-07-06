# PublicFileProvider

PublicFileProvider is a special subclass of [`ContentProvider`](https://developer.android.com/reference/android/content/ContentProvider.html) that facilitates exposing files associated with an app by creating a `content://` URI for a file instead of a `file:///` URI.

**WARNING:** Most of the time this is NOT what you want. Use [FileProvider](https://developer.android.com/reference/android/support/v4/content/FileProvider.html) to only grant temporary access to files, e.g. when [sharing](https://developer.android.com/training/secure-file-sharing/index.html) content to other apps.
 
PublicFileProvider is a modified version of FileProvider with the specific goal to expose files without using Android's URI permission mechanism. This can come in handy when you have to provide a `content://` URI but can't easily grant read access to whatever app ends up accessing the content.
One use case is a custom ringtone in a notification. Check out the blog post [Notifications, Sounds, Android 7.0, and Aggravation](https://commonsware.com/blog/2016/09/07/notifications-sounds-android-7p0-aggravation.html) for more details.
I also wrote a bit about how this library came to be: [When URI permissions are in the way](http://cketti.de/2017/04/03/when-uri-permissions-are-in-the-way/)


## Usage

Add a provider element to your Manifest:

```xml
<manifest>
    ...
    <application>
        ...
        <provider
            android:name="de.cketti.fileprovider.PublicFileProvider"
            android:authorities="com.mydomain.publicfileprovider"
            android:exported="true">
            
            <meta-data
                android:name="de.cketti.fileprovider.PUBLIC_FILE_PROVIDER_PATHS"
                android:resource="@xml/publicfileprovider_paths" />
        
        </provider>
        ...
    </application>
</manifest>
```

Create a file `res/xml/publicfileprovider_paths.xml` with the configuration, e.g.

```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path name="my_notification_sounds" path="notification_sounds/"/>
</paths>
```
The format of this file is identical to that of [FileProvider](https://developer.android.com/reference/android/support/v4/content/FileProvider.html).

To get the `content://` URI for a file you want to expose to all apps on the device use the following code:

```java
File notificationSoundsPath = new File(Context.getFilesDir(), "notification_sounds");
File myNotificationSoundFile = new File(imagePath, "ding.ogg");
Uri contentUri = getUriForFile(getContext(), "com.mydomain.publicfileprovider", myNotificationSoundFile);
```


## Include the library

The library is available on Maven Central. Add this to your `dependencies` block in `build.gradle`:

```groovy
compile 'de.cketti.fileprovider:public-fileprovider:1.0.0'
```


## License

    Copyright 2016 cketti

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
