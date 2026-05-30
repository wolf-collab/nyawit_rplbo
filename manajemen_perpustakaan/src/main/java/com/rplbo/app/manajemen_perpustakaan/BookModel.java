package com.rplbo.app.manajemen_perpustakaan;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BookModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty author;
    private final SimpleStringProperty publisher;
    private final SimpleStringProperty genre;
    private final SimpleStringProperty status;
    private final SimpleStringProperty bookmarked;
    private String imagePath;
    private int loanId;

    public BookModel(int id, String title, String author, String publisher, String genre, String status, boolean isBookmarked, String imagePath) {
        this.id         = new SimpleIntegerProperty(id);
        this.title      = new SimpleStringProperty(title);
        this.author     = new SimpleStringProperty(author);
        this.publisher  = new SimpleStringProperty(publisher != null ? publisher : "-");
        this.genre      = new SimpleStringProperty(genre != null ? genre : "-");
        this.status     = new SimpleStringProperty(status != null ? status : "Tersedia");
        this.bookmarked = new SimpleStringProperty(isBookmarked ? "★" : "☆");
        this.imagePath  = imagePath != null ? imagePath : "";
        this.loanId     = 0;
    }

    // Legacy constructor (backward compatibility)
    public BookModel(int id, String title, String author, String publisher, String genre, String status, boolean isBookmarked) {
        this(id, title, author, publisher, genre, status, isBookmarked, null);
    }

    public int getId()              { return id.get(); }
    public String getTitle()        { return title.get(); }
    public String getAuthor()       { return author.get(); }
    public String getPublisher()    { return publisher.get(); }
    public String getGenre()        { return genre.get(); }
    public String getStatus()       { return status.get(); }
    public String getBookmarked()   { return bookmarked.get(); }
    public String getImagePath()    { return imagePath; }
    public int getLoanId()          { return loanId; }

    public SimpleIntegerProperty idProperty()       { return id; }
    public SimpleStringProperty titleProperty()     { return title; }
    public SimpleStringProperty authorProperty()    { return author; }
    public SimpleStringProperty publisherProperty() { return publisher; }
    public SimpleStringProperty genreProperty()     { return genre; }
    public SimpleStringProperty statusProperty()    { return status; }
    public SimpleStringProperty bookmarkedProperty(){ return bookmarked; }

    public void setBookmarked(boolean isBookmarked) { this.bookmarked.set(isBookmarked ? "★" : "☆"); }
    public void setImagePath(String imagePath)      { this.imagePath = imagePath != null ? imagePath : ""; }
    public void setLoanId(int loanId)               { this.loanId = loanId; }
}
