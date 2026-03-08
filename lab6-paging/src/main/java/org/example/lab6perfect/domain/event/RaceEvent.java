package org.example.lab6perfect.domain.event;


import javafx.beans.property.*;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.example.lab6perfect.domain.event.DuckRaceSolver.Result.rezultat;


public class RaceEvent extends Event {
    private final double[] distante;
    private final int M;
    private double timpCurent;
    private Duck[] participanti;

    public RaceEvent(Long id, String numeEveniment, double[] distante, int m) {
        super(id, numeEveniment);
        this.distante = distante;
        M = m;
    }


    public void runRace() {

        List<SwimmingDuck> inotatoare = subscribers.stream()
                .filter(u -> u instanceof SwimmingDuck)
                .map(u -> (SwimmingDuck) u)
                .toList();

        if (inotatoare.size() < M) {
            notifySubscribers("Nu sunt suficiente rate pentru cursă!");
            return;
        }

        for (SwimmingDuck d : inotatoare) {
            d.inoata();
        }

        notifySubscribers(" Cursa începe...");

        try {
            for (int i = 0; i < distante.length; i++) {
                Thread.sleep(1000);
                notifySubscribers(
                        "Distanta " + distante[i] + " parcursă"
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Duck[] ducksArray = inotatoare.toArray(new Duck[0]);
        rezultat = DuckRaceSolver.solve(ducksArray, distante, M);

        if (rezultat == null) {
            notifySubscribers("Cursa nu a putut fi finalizată.");
            return;
        }

        this.timpCurent = rezultat.time;
        this.participanti = Arrays.stream(rezultat.aranjare)
                .filter(Objects::nonNull)
                .toArray(Duck[]::new);

        notifySubscribers(
                "Cursa s-a terminat! Timp: " + timpCurent
        );
    }


    public double getTimpCurent() {
        return timpCurent;
    }
    public double[] getDistante() { return distante; }
    public Duck[] getParticipanti() {
        return participanti;
    }
    public int getM() { return M; }

}
