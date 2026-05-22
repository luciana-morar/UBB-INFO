package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Metode existente:
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookRepository.searchByKeyword(keyword);
    }

    public Book addBook(Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        return bookRepository.save(book);
    }

    // ========== UC-11: Delete a Book ==========
    public void deleteBook(Long id) {
        // Verificăm dacă cartea există
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Cartea cu ID-ul " + id + " nu există!");
        }

        // Ștergem cartea (Hibernate generează DELETE automat)
        bookRepository.deleteById(id);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cartea nu există!"));
    }

    public Book updateBook(Long id, Book updatedBook) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cartea nu există!"));

        // Actualizăm câmpurile
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setCategory(updatedBook.getCategory());
        existingBook.setPublisher(updatedBook.getPublisher());
        existingBook.setYear(updatedBook.getYear());
        existingBook.setTotalCopies(updatedBook.getTotalCopies());

        // Dacă s-a schimbat numărul total de copii, ajustăm și disponibilele
        int deltaCopies = updatedBook.getTotalCopies() - existingBook.getTotalCopies();
        existingBook.setAvailableCopies(existingBook.getAvailableCopies() + deltaCopies);

        return bookRepository.save(existingBook);
    }
}