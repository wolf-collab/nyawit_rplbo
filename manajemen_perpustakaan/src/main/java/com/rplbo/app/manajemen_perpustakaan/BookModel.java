package com.rplbo.app.manajemen_perpustakaan;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Model data buku untuk ditampilkan di TableView.
 * Menggunakan JavaFX Property agar TableView bisa auto-update.
 */
public class BookModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty author;
    private final SimpleStringProperty genre;
    private final SimpleStringProperty status;
    private final SimpleStringProperty bookmarked; // "★" atau "☆"

    public BookModel(int id, String title, String author, String genre, String status, boolean isBookmarked) {
        this.id         = new SimpleIntegerProperty(id);
        this.title      = new SimpleStringProperty(title);
        this.author     = new SimpleStringProperty(author);
        this.genre      = new SimpleStringProperty(genre != null ? genre : "-");
        this.status     = new SimpleStringProperty(status != null ? status : "Tersedia");
        this.bookmarked = new SimpleStringProperty(isBookmarked ? "★" : "☆");
    }

    // Getters untuk TableView
    public int getId()              { return id.get(); }
    public String getTitle()        { return title.get(); }
    public String getAuthor()       { return author.get(); }
    public String getGenre()        { return genre.get(); }
    public String getStatus()       { return status.get(); }
    public String getBookmarked()   { return bookmarked.get(); }

    // Property getters (wajib ada agar TableView binding bekerja)
    public SimpleIntegerProperty idProperty()       { return id; }
    public SimpleStringProperty titleProperty()     { return title; }
    public SimpleStringProperty authorProperty()    { return author; }
    public SimpleStringProperty genreProperty()     { return genre; }
    public SimpleStringProperty statusProperty()    { return status; }
    public SimpleStringProperty bookmarkedProperty(){ return bookmarked; }

    public void setBookmarked(boolean isBookmarked) {
        this.bookmarked.set(isBookmarked ? "★" : "☆");
    }
}