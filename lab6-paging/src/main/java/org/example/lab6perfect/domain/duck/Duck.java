package org.example.lab6perfect.domain.duck;

import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.card.Card;
import org.example.lab6perfect.domain.event.Event;

public abstract class Duck extends User {
    public enum TipRata { FLYING, SWIMMING, FLYING_AND_SWIMMING }

    private double viteza;
    private double rezistenta;
    private Card card;

    public Duck(Long id, String username, String email, String password, double viteza, double rezistenta) {
        super(id, username, email, password);

        this.viteza = viteza;
        this.rezistenta = rezistenta;
    }


    public abstract String getType();


    public double getViteza() {
        return viteza;
    }

    public double getRezistenta() {
        return rezistenta;
    }
    public double getTime(double distanta){
        return (2*distanta)/viteza;
    }

    public void participateEvent(Event event){
        event.subscribe(this);
    }
    public void quack(){
        System.out.println("Quack! Am terminat antrenamentul!");
    }


}
