package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.card.Card;
import org.example.lab6perfect.domain.card.DuckCard;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.repository.CardRepoDB;

import java.util.Optional;


public class CardService {
    private final CardRepoDB cardRepo;

    public CardService(CardRepoDB cardRepo) {
        this.cardRepo = cardRepo;
    }

    public void createCard(String name) {
        if (cardRepo.cardExists(name)) {
            System.out.println("Exista deja un card cu numele: " + name);
            return;
        }

        DuckCard card = new DuckCard(name);
        cardRepo.addCard(card);
        System.out.println("Card creat: " + name);
    }

    public void addDuckToCard(String cardName, Duck duck) {
        if (!cardRepo.cardExists(cardName)) {
            System.out.println("Card inexistent: " + cardName);
            return;
        }

        cardRepo.addDuckToCard(cardName, duck);
        System.out.println("Rata adaugata in card!");
    }

    public void printCardPerformance(String cardName) {
        Optional<Card<? extends Duck>> card = cardRepo.getCardByName(cardName);
        card.ifPresentOrElse(
                c -> System.out.println("Performanta cardului '" + cardName + "': " + c.getPerformantaMedie()),
                () -> System.out.println("Card inexistent: " + cardName)
        );
    }

    public void listCards() {
        var cards = cardRepo.listAll();
        if (cards.isEmpty()) {
            System.out.println("Nu exista carduri");
            return;
        }

        for (Card<? extends Duck> card : cards) {
            System.out.println("Card: " + card.getNumeCard() + ", nr rate: " + card.getMembri().size());
        }
    }
}