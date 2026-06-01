package com.example.demo.dto;

import java.util.List;

public class RentalRequestDTO {
    private List<Long> bookIds;
    private Integer durationWeeks;

    // Getteri și Setteri
    public List<Long> getBookIds() { return bookIds; }
    public void setBookIds(List<Long> bookIds) { this.bookIds = bookIds; }
    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
}