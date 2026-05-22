package com.example.demo.controller;

import com.example.demo.dto.ReaderRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/readers")

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ReaderRestController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> getAllReaders() {
        return userService.getAllReaders().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDTO getReaderById(@PathVariable Long id) {
        return new UserDTO(userService.getReaderById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO addReader(@Valid @RequestBody ReaderRequest request) {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());

        return new UserDTO(userService.addReader(newUser));
    }

    @PutMapping("/{id}")
    public UserDTO updateReader(@PathVariable Long id, @RequestBody ReaderRequest request) {
        User updatedUser = new User();
        updatedUser.setUsername(request.getUsername());
        updatedUser.setPassword(request.getPassword());
        updatedUser.setFirstName(request.getFirstName());
        updatedUser.setLastName(request.getLastName());
        updatedUser.setEmail(request.getEmail());

        return new UserDTO(userService.updateReader(id, updatedUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReader(@PathVariable Long id) {
        userService.deleteReader(id);
    }
}
