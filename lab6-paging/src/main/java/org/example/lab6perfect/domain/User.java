package org.example.lab6perfect.domain;


import org.example.lab6perfect.domain.event.Event;
import org.example.lab6perfect.obs.Observable;
import org.example.lab6perfect.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class User {
    protected Long id;
    protected String username;
    protected String email;
    protected String password;


    public String getUsername() {
        return username;
    }
    public Long getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public void setPassword(String password) {
//        this.password = password;
//    }


    protected List<User> friends =new ArrayList<>();
    protected List<Event> events =new ArrayList<>();
    public List<Message> messages=new ArrayList<>();


    public void login(){
        System.out.println(username+" logged in.");
    }
    public void logout(){
        System.out.println(username+" logged out.");
    }


    public void receiveMessage(Message message){
        String senderName = (message.getSender() != null) ? message.getSender().getUsername() : "SYSTEM";
        System.out.println("Mesaj de la " + senderName + ": " + message.getContent());

        Observable.getInstance().notifyObservers(message);
    }

    // relatie bidirectinala pt freindship
    public void addFriend(User u) {
        if (!friends.contains(u)) {
            friends.add(u);
            u.addFriend(this);
        }
    }
    public void removeFriend(User u) {
        friends.remove(u);
        u.removeFriend(this);
    }
    public List<User> getFriends() {
        return friends;
    }

    public void setPassword(String password) {
//        try {
//            this.password = PasswordUtil.hashPassword(password);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to hash password", e);
//        }
        this.password = password;
    }

    public boolean checkPassword(String plainPassword) {
//        try {
//            return PasswordUtil.checkPassword(plainPassword, this.password);
//        } catch (Exception e) {
//            return false;
//        }
        return this.password.equals(plainPassword);
    }
    public User(Long id, String username, String email, String plainPassword) {
        this.id = id;
        this.username = username;
        this.email = email;
        setPassword(plainPassword);
    }

}
