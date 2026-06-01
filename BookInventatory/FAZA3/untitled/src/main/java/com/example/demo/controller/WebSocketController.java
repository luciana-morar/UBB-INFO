package com.example.demo.controller;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.RentalDTO;
import com.example.demo.model.Book;
import com.example.demo.model.Rental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyBookUpdate(Book book) {
        messagingTemplate.convertAndSend("/topic/books", new BookDTO(book));
    }

    public void notifyRentalUpdate(Rental rental) {
        messagingTemplate.convertAndSend("/topic/rentals", new RentalDTO(rental));
    }
}
