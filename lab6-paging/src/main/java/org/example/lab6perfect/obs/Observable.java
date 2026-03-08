package org.example.lab6perfect.obs;

import org.example.lab6perfect.domain.FriendRequest;
import org.example.lab6perfect.domain.Message;

import java.util.ArrayList;
import java.util.List;

public class Observable {

    private List<Observer> observers=new ArrayList<>();
    private static Observable instance;

    public static Observable getInstance(){
        if(instance==null){
            instance=new Observable();
        }
        return instance;
    }
    private Observable(){}

    public void addObserver(Observer observer){
       if(!observers.contains(observer))
            observers.add(observer);
    }
    public void removeObserver(Observer observer){
        observers.remove(observer);
    }
    // Metodă pentru notificare Message
    public void notifyObservers(Message message) {
        for (Observer observer : observers) {
            observer.onNewMessage(message);
        }
    }
    public void notifyObservers(FriendRequest friendRequest) {
        for (Observer observer : observers) {
            observer.onNewFriendRequest(friendRequest);
        }
    }
}
