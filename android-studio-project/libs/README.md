* [PublicFileProvider](https://github.com/cketti/PublicFileProvider)
  - tag: [v1.0.0](https://github.com/cketti/PublicFileProvider/releases/tag/v1.0.0)
  - commit: [61da03adf70a7d6bd6f69fe1099675d81bfaf832](https://github.com/cketti/PublicFileProvider/tree/61da03adf70a7d6bd6f69fe1099675d81bfaf832)
  - date: 2017-04-02
  - author: [Christian Ketterer](https://github.com/cketti)
  - license: [Apache-2.0](https://github.com/cketti/PublicFileProvider/blob/61da03adf70a7d6bd6f69fe1099675d81bfaf832/LICENSE)
  - background:
    * Android 7.0 (API level 24) added [FileUriExposedException](https://developer.android.com/reference/android/os/FileUriExposedException)
      - which is thrown when a `file://` URI is used in an Intent to share file(s) between apps
    * this leaves two options:
      1. never raise the target SDK of the app higher than 23
      2. always use `content://` URIs in Intents
  - summary of library:
    * a special subclass of [ContentProvider](https://developer.android.com/reference/android/content/ContentProvider.html)
    * a modified version of [FileProvider](https://developer.android.com/reference/android/support/v4/content/FileProvider.html)
    * facilitates exposing files by creating `content://` URIs without using Android's URI permission mechanism
  - features of library:
    * minimal, and tiny
    * standalone, and a drop-in replacement for [FileProvider](https://developer.android.com/reference/android/support/v4/content/FileProvider.html)
  - more info about library:
    * [blog post by author](http://cketti.de/2017/04/03/when-uri-permissions-are-in-the-way/)
    * [maven repo](https://mvnrepository.com/artifact/de.cketti.fileprovider/public-fileprovider)

* [HiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass)
  - tag: [v4.3](https://github.com/LSPosed/AndroidHiddenApiBypass/releases/tag/v4.3)
  - commit: [a50636430d46032ba4b0717321e4ceb5cf6466cf](https://github.com/LSPosed/AndroidHiddenApiBypass/tree/a50636430d46032ba4b0717321e4ceb5cf6466cf)
  - date: 2022-02-25
  - author: [LSPosed](https://github.com/LSPosed)
  - license: [Apache-2.0](https://github.com/LSPosed/AndroidHiddenApiBypass/blob/a50636430d46032ba4b0717321e4ceb5cf6466cf/LICENSE)
  - background:
    * [Restrictions on non-SDK interfaces](https://developer.android.com/guide/app-compatibility/restrictions-non-sdk-interfaces)
      - Starting in Android 9 (API level 28), the platform restricts which non-SDK interfaces your app can use. These restrictions apply whenever an app references a non-SDK interface or attempts to obtain its handle using reflection or JNI.
  - summary of library:
    * Bypass restrictions on non-SDK interfaces
  - features of library:
    * minimal, and tiny
    * implemented purely in Java
  - more info about library:
    * [blog post by author](https://lovesykun.cn/archives/android-hidden-api-bypass.html)
    * [maven repo](https://mvnrepository.com/artifact/org.lsposed.hiddenapibypass/hiddenapibypass)
  - more info pertaining to this topic:
    * [Stack Overflow](https://stackoverflow.com/questions/55970137/bypass-androids-hidden-api-restrictions)

* [TimeDurationPicker](https://github.com/svenwiegand/time-duration-picker)
  - tag: [v1.1.3](https://github.com/svenwiegand/time-duration-picker/releases/tag/1.1.3)
  - commit: [d3fbbba6a613feaa170aaaa7bfc489e4245f20b1](https://github.com/svenwiegand/time-duration-picker/commit/d3fbbba6a613feaa170aaaa7bfc489e4245f20b1)
  - date: 2017-10-22
  - author: [Sven Wiegand](https://github.com/svenwiegand)
  - license: [MIT](https://github.com/svenwiegand/time-duration-picker/blob/d3fbbba6a613feaa170aaaa7bfc489e4245f20b1/LICENSE.md)
  - summary of library:
    * UI widget: `TimeDurationPicker`
    * UI dialog: `TimeDurationPickerDialog`
  - features of library:
    * minimal, and tiny
    * implemented purely in Java
