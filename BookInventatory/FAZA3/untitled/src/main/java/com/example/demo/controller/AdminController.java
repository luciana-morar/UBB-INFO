package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.model.User;
import com.example.demo.service.BookService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    // ========== Book Management (existente) ==========

    @GetMapping("/books")
    public String manageBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "admin/books";
    }

    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/add-book";
    }

    @PostMapping("/books/add")
    public String addBook(@ModelAttribute Book book) {
        bookService.addBook(book);
        return "redirect:/admin/books";
    }

    // ========== UC-11: Delete a Book ==========
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return "redirect:/admin/books?success=Cartea a fost ștearsă cu succes";
        } catch (RuntimeException e) {
            return "redirect:/admin/books?error=" + e.getMessage();
        }
    }

    // ========== Reader Management (ITERATION 2) ==========

    // UC-9: View Readers (afișează lista - va fi folosit în Iterația 3)
    @GetMapping("/readers")
    public String manageReaders(Model model) {
        model.addAttribute("readers", userService.getAllReaders());
        return "admin/readers";
    }

    // UC-6: Show Add Reader Form
    @GetMapping("/readers/add")
    public String showAddReaderForm(Model model) {
        model.addAttribute("reader", new User());
        return "admin/add-reader";
    }

    // UC-6: Add a Reader
    @PostMapping("/readers/add")
    public String addReader(@ModelAttribute User reader, Model model) {
        try {
            userService.addReader(reader);
            return "redirect:/admin/readers?success=Cititor adăugat cu succes";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reader", reader);
            return "admin/add-reader";
        }
    }

    // UC-8: Show Update Reader Form
    @GetMapping("/readers/edit/{id}")
    public String showUpdateReaderForm(@PathVariable Long id, Model model) {
        try {
            User reader = userService.getReaderById(id);
            model.addAttribute("reader", reader);
            return "admin/edit-reader";
        } catch (RuntimeException e) {
            return "redirect:/admin/readers?error=" + e.getMessage();
        }
    }

    // UC-8: Update Reader Information
    @PostMapping("/readers/edit/{id}")
    public String updateReader(@PathVariable Long id, @ModelAttribute User reader, Model model) {
        try {
            userService.updateReader(id, reader);
            return "redirect:/admin/readers?success=Cititor actualizat cu succes";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reader", reader);
            return "admin/edit-reader";
        }
    }

    // UC-7: Delete a Reader
    @PostMapping("/readers/delete/{id}")
    public String deleteReader(@PathVariable Long id) {
        try {
            userService.deleteReader(id);
            return "redirect:/admin/readers?success=Cititor șters cu succes";
        } catch (RuntimeException e) {
            return "redirect:/admin/readers?error=" + e.getMessage();
        }
    }
}
