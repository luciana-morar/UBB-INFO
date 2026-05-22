package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Pentru criptarea parolei

    // ========== UC-6: Add a Reader ==========
    public User addReader(User user) {
        // Validare: username-ul trebuie să fie unic
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username-ul există deja!");
        }

        // Validare: câmpuri obligatorii
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username-ul este obligatoriu!");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Parola este obligatorie!");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("Prenumele este obligatoriu!");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Numele este obligatoriu!");
        }

        // Setăm rolul de READER (forțat, indiferent ce vine în request)
        user.setRole("READER");

        // Criptăm parola înainte de salvare (securitate!)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Salvăm în baza de date (Hibernate generează INSERT automat)
        return userRepository.save(user);
    }

    // ========== UC-7: Delete a Reader ==========
    public void deleteReader(Long id) {
        // Verificăm dacă utilizatorul există
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Cititorul cu ID-ul " + id + " nu există!");
        }

        // Ștergem utilizatorul (Hibernate generează DELETE automat)
        userRepository.deleteById(id);
    }

    // ========== UC-8: Update Reader Information ==========
    public User updateReader(Long id, User updatedUser) {
        // Găsim utilizatorul existent
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cititorul cu ID-ul " + id + " nu există!"));

        // Validare: username-ul nou nu trebuie să fie deja folosit de altcineva
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("Username-ul " + updatedUser.getUsername() + " este deja folosit!");
        }

        // Validare: câmpuri obligatorii
        if (updatedUser.getUsername() == null || updatedUser.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username-ul este obligatoriu!");
        }
        if (updatedUser.getFirstName() == null || updatedUser.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("Prenumele este obligatoriu!");
        }
        if (updatedUser.getLastName() == null || updatedUser.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Numele este obligatoriu!");
        }

        // Actualizăm câmpurile (păstrăm rolul existent)
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        // Dacă s-a trimis o parolă nouă, o criptăm și o actualizăm
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Salvăm modificările (Hibernate generează UPDATE automat)
        return userRepository.save(existingUser);
    }


    // Obține toți cititorii (pentru a-i afișa în tabel)
    public List<User> getAllReaders() {
        // Returnăm doar utilizatorii cu rol READER
        return userRepository.findByRole("READER");
    }

    // Obține un cititor după ID
    public User getReaderById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cititorul nu există!"));

        // Verificăm să fie READER, nu LIBRARIAN
        if (!"READER".equals(user.getRole())) {
            throw new RuntimeException("Utilizatorul nu este un cititor valid!");
        }

        return user;
    }
}