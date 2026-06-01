package com.example.demo.controller;

import com.example.demo.dto.RentalDTO;
import com.example.demo.dto.RentalRequestDTO;
import com.example.demo.model.Rental;
import com.example.demo.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class RentalRestController {

    @Autowired
    private RentalService rentalService;

    @PostMapping("/rent")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public List<RentalDTO> rentBooks(
            @RequestParam Long userId,
            @RequestBody RentalRequestDTO request) {
        List<Rental> rentals = rentalService.rentBooks(userId, request.getBookIds(), request.getDurationWeeks());
        return rentals.stream().map(RentalDTO::new).collect(Collectors.toList());
    }

    @PostMapping("/return")
    @Transactional
    public List<RentalDTO> returnBooks(
            @RequestParam Long userId,
            @RequestBody List<Long> rentalIds) {
        List<Rental> rentals = rentalService.returnBooks(userId, rentalIds);
        return rentals.stream().map(RentalDTO::new).collect(Collectors.toList());
    }

    @PostMapping("/{rentalId}/approve")
    @Transactional
    public RentalDTO approveReturn(@PathVariable Long rentalId) {
        Rental rental = rentalService.approveReturn(rentalId);
        return new RentalDTO(rental);
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public List<RentalDTO> getMyRentals(@RequestParam Long userId) {
        List<Rental> rentals = rentalService.getRentalsByUser(userId);
        return rentals.stream().map(RentalDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/my/active")
    @Transactional(readOnly = true)
    public List<RentalDTO> getMyActiveRentals(@RequestParam Long userId) {
        List<Rental> rentals = rentalService.getActiveRentalsByUser(userId);
        return rentals.stream().map(RentalDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/pending")
    @Transactional(readOnly = true)
    public List<RentalDTO> getPendingReturns() {
        List<Rental> rentals = rentalService.getPendingReturns();
        return rentals.stream().map(RentalDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/all")
    @Transactional(readOnly = true)
    public List<RentalDTO> getAllRentals() {
        List<Rental> rentals = rentalService.getAllRentals();
        return rentals.stream().map(RentalDTO::new).collect(Collectors.toList());
    }
}