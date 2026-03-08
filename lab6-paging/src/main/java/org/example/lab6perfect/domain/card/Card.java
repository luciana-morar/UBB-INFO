package org.example.lab6perfect.domain.card;

import org.example.lab6perfect.domain.duck.Duck;

import java.util.ArrayList;
import java.util.List;

public abstract class Card<T extends Duck> {

    private final String numeCard;
    private final List<Duck> membri =new ArrayList<>();

    public Card(String numeCard) {

        this.numeCard = numeCard;
    }

    public List<Duck> getMembri() {
        return membri;
    }
    public String getNumeCard() { return numeCard; }

    public void addDuck(T duck) {
        membri.add(duck);
        duck.quack();
    }

    public void removeDuck(T duck) {
        membri.remove(duck);
    }


    public double getPerformantaMedie() {
        if (membri.isEmpty()) return 0.0;

        double sumaPerformanta = 0.0;
        for (Duck duck : membri) {
            double performantaDuck = (duck.getViteza() + duck.getRezistenta()) / 2.0;
            sumaPerformanta += performantaDuck;
        }
        return sumaPerformanta / membri.size();
    }

}
