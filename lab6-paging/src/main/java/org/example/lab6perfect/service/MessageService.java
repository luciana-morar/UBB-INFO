package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.ReplyMessage;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.obs.Observable;
import org.example.lab6perfect.repository.MessageRepoDB;
import org.example.lab6perfect.validator.MessageValidator;

import java.util.List;

public class MessageService {
    public final MessageRepoDB messageRepo;
    public final MessageValidator validator;

    public MessageService(MessageRepoDB messageRepo, MessageValidator validator) {
        this.messageRepo = messageRepo;
        this.validator = validator;
    }

    public Message sendMessage(User sender, List<User> receiver, String content) {
        Message msg = new Message(sender, receiver, content);
        validator.validate(msg);
        Long messageId = messageRepo.addMessage(msg);

        if (messageId != null) {
            // Notifică observatorii pentru friend request (dacă e cazul)
            // Mesajele de chat sunt notificate prin override în StartApplication
            Observable.getInstance().notifyObservers(msg);
            System.out.println("Mesaj trimis de " + sender.getUsername());
        }
        return msg;
    }

    public ReplyMessage sendReply(User sender, List<User> receiver, String content, Long originalMessageId) {
        ReplyMessage msg = new ReplyMessage(sender, receiver, content, originalMessageId);
        validator.validate(msg);
        Long messageId = messageRepo.addMessage(msg);

        if (messageId != null) {
            Observable.getInstance().notifyObservers(msg);
            System.out.println("Reply trimis");
        }
        return msg;
    }

    public List<Message> getMessagesForUser(User user) {
        return messageRepo.getMessagesForUser(user.getId());
    }

    public List<Message> getConversation(User u1, User u2) {
        return messageRepo.getConversation(u1.getId(), u2.getId());
    }

    public Message getMessageById(Long messageId) {
        return messageRepo.getMessageById(messageId);
    }
}
