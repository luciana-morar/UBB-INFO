package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rental_seq")
    @SequenceGenerator(name = "rental_seq", sequenceName = "rental_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnedDate;

    @Column(nullable = false)
    private Integer durationWeeks;

    @Column(nullable = false)
    private String status; // ACTIVE, PENDING_APPROVAL, APPROVED

    public Rental() {}

    public Rental(User user, Book book, LocalDate startDate,
                  LocalDate dueDate, Integer durationWeeks) {
        this.user = user;
        this.book = book;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.durationWeeks = durationWeeks;
        this.status = "ACTIVE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }

    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
