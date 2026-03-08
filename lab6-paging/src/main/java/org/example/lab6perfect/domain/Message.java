package org.example.lab6perfect.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message {
    private Long id;
    private User sender;
    private List<User> receiver;
    private String content;
    public LocalDateTime timestamp;

    private static Long counter= 0L;

    public Message(User sender, List<User> receiver, String content) {
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;

        this.id= ++counter;
        this.timestamp = LocalDateTime.now();
    }

    public Message(Long id, User sender, List<User> receivers, String content, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receivers;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() {return id;}
    public User getSender() { return sender; }
    public List<User> getReceiver() { return receiver; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp;}
    public void setId(Long id) {
        this.id = id;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setReceiver(List<User> receiver) {
        this.receiver = receiver;
    }
    public void addReceiver(User r) {
        this.receiver.add(r);
    }

    @Override
    public String toString() {
        String time = timestamp != null ? timestamp.toLocalTime().toString().substring(0, 8) : "00:00:00";
        String senderName = sender != null ? sender.getUsername() : "System";
        return String.format("[%s] %s: %s", time, senderName, content);
    }

}
