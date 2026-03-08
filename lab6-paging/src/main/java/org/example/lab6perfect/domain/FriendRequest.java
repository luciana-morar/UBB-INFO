package org.example.lab6perfect.domain;

import java.time.LocalDateTime;

public class FriendRequest {


    private Long id;
    private User sender;
    private User receiver;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

   // public enum Status{ PENDING, APPROVED, REJECTED }
   public enum Status {
       PENDING("PENDING"),
       APPROVED("APPROVED"),
       REJECTED("REJECTED");

       private final String dbValue;

       Status(String dbValue) {
           this.dbValue = dbValue;
       }

       public String getDbValue() {
           return dbValue;
       }


   }
    public FriendRequest(Long id, User sender, User receiver, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public FriendRequest(User sender, User receiver) {
        this(null, sender, receiver, Status.PENDING, LocalDateTime.now(), LocalDateTime.now());
    }

    public String toString(){
        return sender.getUsername() + " -> " + receiver.getUsername() + "  (" + status + ")";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
