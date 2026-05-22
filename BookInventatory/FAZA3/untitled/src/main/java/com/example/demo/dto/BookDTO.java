package com.example.demo.dto;

import com.example.demo.model.Book;

public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private Integer year;
    private Integer totalCopies;
    private Integer availableCopies;
    private String availabilityDisplay;

    public BookDTO() {}

    public BookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.category = book.getCategory();
        this.publisher = book.getPublisher();
        this.year = book.getYear();
        this.totalCopies = book.getTotalCopies();
        this.availableCopies = book.getAvailableCopies();
        this.availabilityDisplay = book.getAvailableCopies() + " / " + book.getTotalCopies();
    }

    // Getteri și Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    public String getAvailabilityDisplay() { return availabilityDisplay; }
    public void setAvailabilityDisplay(String availabilityDisplay) { this.availabilityDisplay = availabilityDisplay; }
}
