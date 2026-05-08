package com.example.demo.config;


import com.example.demo.model.Book;
import com.example.demo.model.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("reader1")) {
            User reader = new User("reader1", passwordEncoder.encode("password"),
                    "John", "Doe", "john.doe@email.com", "READER");
            userRepository.save(reader);
        }
        if (!userRepository.existsByUsername("reader2")) {
            User reader = new User("reader2", passwordEncoder.encode("password"),
                    "Ana", "Pop", "pop@email.com", "READER");
            userRepository.save(reader);
        }

        if (!userRepository.existsByUsername("alice")) {
            User librarian = new User("alice", passwordEncoder.encode("admin123"),
                    "Alice", "Smith", "alice@library.com", "LIBRARIAN");
            userRepository.save(librarian);
        }
        if (!userRepository.existsByUsername("admin")) {
            User librarian = new User("admin", passwordEncoder.encode("admin"),
                    "Ioana", "Ene", "ene@library.com", "LIBRARIAN");
            userRepository.save(librarian);
        }

        if (bookRepository.count() == 0) {
            bookRepository.save(new Book("1984", "George Orwell", "Fiction", "Secker & Warburg", 1949, 5, 3));
            bookRepository.save(new Book("To Kill a Mockingbird", "Harper Lee", "Fiction", "J.B. Lippincott & Co.", 1960, 4, 2));
            bookRepository.save(new Book("The Great Gatsby", "F. Scott Fitzgerald", "Fiction", "Charles Scribner's Sons", 1925, 6, 4));
            bookRepository.save(new Book("Clean Code", "Robert C. Martin", "Technology", "Prentice Hall", 2008, 5, 5));
            bookRepository.save(new Book("The Pragmatic Programmer", "Andrew Hunt", "Technology", "Addison-Wesley", 1999, 4, 3));
            bookRepository.save(new Book("Sapiens", "Yuval Noah Harari", "History", "Harper", 2011, 3, 2));
            bookRepository.save(new Book("Educated", "Tara Westover", "Biography", "Random House", 2018, 5, 5));
            bookRepository.save(new Book("Atomic Habits", "James Clear", "Self Help", "Avery", 2018, 8, 6));
        }
    }
}
