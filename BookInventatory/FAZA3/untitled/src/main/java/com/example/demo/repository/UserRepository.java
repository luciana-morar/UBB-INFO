package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Metode furnizate automat de Spring Data JPA:
    // - save(User) - salvează sau actualizează
    // - findById(Long) - caută după ID
    // - findAll() - lista toți utilizatorii
    // - delete(User) - șterge un utilizator
    // - deleteById(Long) - șterge după ID

    // Metode custom (Spring le implementează automat pe baza numelui):
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // Pentru UC-9 (View Readers) - va fi folosit în Iterația 3
    List<User> findByRole(String role);  // "READER" sau "LIBRARIAN"
}
