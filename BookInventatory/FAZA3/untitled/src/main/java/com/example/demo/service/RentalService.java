package com.example.demo.service;

import com.example.demo.controller.WebSocketController;
import com.example.demo.model.Book;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private WebSocketController webSocketController;

    // UC-3: Book Rental
    @Transactional
    public List<Rental> rentBooks(Long userId, List<Long> bookIds, Integer durationWeeks) {
        if (durationWeeks == null || durationWeeks < 1 || durationWeeks > 8) {
            throw new RuntimeException("Duration must be between 1 and 8 weeks");
        }
        if (bookIds == null || bookIds.isEmpty()) {
            throw new RuntimeException("No books selected");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Rental> rentals = new ArrayList<>();
        LocalDate startDate = LocalDate.now();
        LocalDate dueDate = startDate.plusWeeks(durationWeeks);

        for (Long bookId : bookIds) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

            if (book.getAvailableCopies() <= 0) {
                throw new RuntimeException("Book '" + book.getTitle() + "' is not available");
            }

            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);


            Rental rental = new Rental(user, book, startDate, dueDate, durationWeeks);
            rentals.add(rentalRepository.save(rental));
            webSocketController.notifyBookUpdate(book);
            webSocketController.notifyRentalUpdate(rental);
        }

        return rentals;
    }

    // UC-4: Reader initiates return → PENDING_APPROVAL
    @Transactional
    public List<Rental> returnBooks(Long userId, List<Long> rentalIds) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Rental> updatedRentals = new ArrayList<>();

        for (Long rentalId : rentalIds) {
            Rental rental = rentalRepository.findById(rentalId)
                    .orElseThrow(() -> new RuntimeException("Rental not found: " + rentalId));

            if (!rental.getUser().getId().equals(userId)) {
                throw new RuntimeException("Rental does not belong to this user");
            }
            if (!"ACTIVE".equals(rental.getStatus())) {
                throw new RuntimeException("Rental is not active");
            }

            rental.setStatus("PENDING_APPROVAL");
            rental.setReturnedDate(LocalDate.now());
            Rental savedRental = rentalRepository.save(rental);
            updatedRentals.add(savedRental);

            webSocketController.notifyRentalUpdate(savedRental);

        }

        return updatedRentals;
    }

    // UC-4: Librarian approves return
    @Transactional
    public Rental approveReturn(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (!"PENDING_APPROVAL".equals(rental.getStatus())) {
            throw new RuntimeException("Rental is not pending approval");
        }

        Book book = rental.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        rental.setStatus("APPROVED");

        webSocketController.notifyBookUpdate(rental.getBook());
        webSocketController.notifyRentalUpdate(rental);

        return rentalRepository.save(rental);
    }


    // Librarian: returnări în așteptare
    @Transactional(readOnly = true)
    public List<Rental> getPendingReturns() {
        return rentalRepository.findPendingReturnsWithDetails();
    }

    // Librarian: toate închirierile
    @Transactional(readOnly = true)
    public List<Rental> getAllRentals() {
        return rentalRepository.findAllWithDetails();
    }


    // UC-5: Toate închirierile unui reader
    @Transactional(readOnly = true)
    public List<Rental> getRentalsByUser(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    // Închirierile active ale unui reader
    @Transactional(readOnly = true)
    public List<Rental> getActiveRentalsByUser(Long userId) {
        return rentalRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }
}
