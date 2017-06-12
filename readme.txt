Application is simple example showing how to:

1. get list of books from Google Books
2. display it in the Android ListView with paging, memory and disk caching
3. display book details

It's using:

1. Jackson libraries released under Apache (Software) License, version 2.0 ("the License")
2. some ideas from http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
3. some ideas from Android SDK

To run it:

1. login into https://console.developers.google.com
2. create project for Google Books API and generate API key
3. put API key into searchURLPrefix in the BooksListListViewAdapter.java
4. import project into Android Studio
   * "Import Project (Eclipse ADT, Gradle, etc.)"
   * select build.gradle in top
   * answer Yes for using Gradle wrapper

Project is provided without any warranty or responsibility.
