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
