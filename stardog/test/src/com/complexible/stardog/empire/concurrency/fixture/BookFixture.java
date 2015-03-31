package com.complexible.stardog.empire.concurrency.fixture;

import com.complexible.stardog.empire.concurrency.entity.Book;

import java.net.URI;
import java.util.Date;

/**
 * Created by anand on 31/03/15.
 */
public class BookFixture {
    public static Book create() {
        Book aNewBook = new Book();
        aNewBook.setIssued(new Date());
        aNewBook.setTitle("How to use Empire");
        aNewBook.setPublisher("Clark & Parsia");
        aNewBook.setPrimarySubjectOf(URI.create("http://github.com/clarkparsia/Empire"));
        return aNewBook;
    }
}
