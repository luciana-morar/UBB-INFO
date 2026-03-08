package org.example.lab6perfect.domain.event;

import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.User;

import java.util.ArrayList;
import java.util.List;

public abstract class Event {
    protected Long id;
    protected String numeEveniment;
    protected List<User> subscribers=new ArrayList<User>();

    public Event(Long id, String numeEveniment) {
        this.id = id;
        this.numeEveniment = numeEveniment;
    }

    public Long getId() {return id;}
    public String getNumeEveniment() { return numeEveniment;}
    public List<User> getSubscribers(){ return subscribers;}

    public void subscribe(User user){
        if(!subscribers.contains(user)) {
            subscribers.add(user);
            System.out.println("Subscriber " + user.getUsername() + " subscribed to " + numeEveniment);
        }
    }

    public void unsubscribe(User user){
        subscribers.remove(user);
        System.out.println("Subscriber "+user.getUsername()+" unsubscribed to "+numeEveniment);
    }

    public void notifySubscribers(String message){
        List<User> allSubscribers=new ArrayList<>(subscribers);
        Message notification = new Message(null,allSubscribers,message);
        for(User user:subscribers){
            user.receiveMessage(notification);
        }
    }
}
