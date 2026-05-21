package com.rplbo.app.manajemen_perpustakaan;

public class Book {
    private int id;
    private String title;
    private String author;
    private String publisher;
    private String genre;

    public Book(int id, String title, String author, String publisher, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.genre = genre;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public String getGenre() { return genre; }
}