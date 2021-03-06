package com.mwiacek.com.booksapiexample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Class with part of info returned by Google Books API
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    public String id;
    public VolumeInfo volumeInfo;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ImageLinks {
    public String smallThumbnail;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class VolumeInfo {
    public String[] authors;
    public String description;
    public ImageLinks imageLinks;
    public String publisher;
    public String publishedDate;
    public String title;
}
