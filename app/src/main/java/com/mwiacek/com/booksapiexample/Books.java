package com.mwiacek.com.booksapiexample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Books {
    // This is in every request. We could save some memory after saving it once
    public int totalItems;

    public Book[] items;
}
