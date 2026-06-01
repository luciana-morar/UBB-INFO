package com.example.demo.dto;

import com.example.demo.model.Rental;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RentalDTO {
    private Long id;
    private Long userId;
    private String readerName;
    private Long bookId;
    private String bookTitle;
    private String startDate;
    private String dueDate;
    private String returnedDate;
    private String status;
    private Integer durationWeeks;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public RentalDTO() {}

    public RentalDTO(Rental rental) {
        this.id = rental.getId();
        this.userId = rental.getUser().getId();
        this.readerName = rental.getUser().getFirstName() + " " + rental.getUser().getLastName();
        this.bookId = rental.getBook().getId();
        this.bookTitle = rental.getBook().getTitle();
        this.startDate = rental.getStartDate().format(FORMATTER);
        this.dueDate = rental.getDueDate().format(FORMATTER);
        this.returnedDate = rental.getReturnedDate() != null ?
                rental.getReturnedDate().format(FORMATTER) : null;
        this.status = rental.getStatus();
        this.durationWeeks = rental.getDurationWeeks();
    }

    // Getteri și Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getReturnedDate() { return returnedDate; }
    public void setReturnedDate(String returnedDate) { this.returnedDate = returnedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
}
