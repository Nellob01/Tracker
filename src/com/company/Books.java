package com.company;

import java.util.ArrayList;

/**
 * Created by noelaniekan on 1/16/17.
 */
public class Books extends ArrayList<Books> {
    int bookId;
    int userId;
    String title;
    String writer;
    String releaseDate;
    boolean read;

    public Books() {
    }

    public Books(int bookId, int userId, String title, String writer, String releaseDate, boolean read) {
        this.bookId = bookId;
        this.userId = userId;
        this.title = title;
        this.writer = writer;
        this.releaseDate = releaseDate;
        this.read = read;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
