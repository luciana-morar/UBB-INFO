// RaceEventService.java
package org.example.lab6perfect.service;

import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;
import org.example.lab6perfect.domain.event.RaceEvent;
import org.example.lab6perfect.repository.EventRepoDB;
import org.example.lab6perfect.repository.UserRepoDB;
import org.example.lab6perfect.validator.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RaceEventService {
    private final EventRepoDB eventRepo;
    private final UserRepoDB userRepo;

    public RaceEventService(EventRepoDB eventRepo, UserRepoDB userRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    public RaceEvent createRaceEvent(String name, double[] distances, int lanes) {
        RaceEvent event = new RaceEvent(null, name, distances, lanes);
        try {
            eventRepo.addEvent(event);
            return event;
        } catch (Exception e) {
            throw new RuntimeException("Error creating race event: " + e.getMessage(), e);
        }
    }

    public void registerParticipant(Long eventId, Long userId) {
        try {
            RaceEvent event = eventRepo.findEventById(eventId);
            Optional<User> userOpt = userRepo.findById(userId);

            if (event == null) {
                throw new ValidationException("Event not found");
            }
            if (userOpt.isEmpty()) {
                throw new ValidationException("User not found");
            }

            User user = userOpt.get();
            if (!(user instanceof SwimmingDuck)) {
                throw new ValidationException("Only SwimmingDucks can participate in races");
            }

            if (event.getSubscribers().contains(user)) {
                throw new ValidationException("User already registered for this event");
            }

            event.subscribe(user);
            eventRepo.updateEvent(event);

        } catch (Exception e) {
            throw new RuntimeException("Error registering participant: " + e.getMessage(), e);
        }
    }

    public void unregisterParticipant(Long eventId, Long userId) {
        try {
            RaceEvent event = eventRepo.findEventById(eventId);
            Optional<User> userOpt = userRepo.findById(userId);

            if (event == null || userOpt.isEmpty()) {
                throw new ValidationException("Event or user not found");
            }

            User user = userOpt.get();
            event.unsubscribe(user);
            eventRepo.updateEvent(event);

        } catch (Exception e) {
            throw new RuntimeException("Error unregistering participant: " + e.getMessage(), e);
        }
    }
    public void saveWinnerTime(Long eventId, double winnerTime) {
        try {
            ((EventRepoDB) eventRepo).updateWinnerTime(eventId, winnerTime);
        } catch (Exception e) {
            System.err.println("Error saving winner time: " + e.getMessage());
            throw new RuntimeException("Error saving winner time", e);
        }
    }

    public List<RaceEvent> getAllEvents() {
        return eventRepo.getAllEvents();
    }

    public RaceEvent getEventById(Long eventId) {
        return eventRepo.findEventById(eventId);
    }

    public void deleteEvent(Long eventId) {
        eventRepo.deleteEvent(eventId);
    }

    public List<SwimmingDuck> getEventParticipants(Long eventId) {
        RaceEvent event = eventRepo.findEventById(eventId);
        if (event == null) {
            return List.of();
        }

        return event.getSubscribers().stream()
                .filter(u -> u instanceof SwimmingDuck)
                .map(u -> (SwimmingDuck) u)
                .collect(Collectors.toList());
    }

    public void updateEventResults(Long eventId, double winnerTime, Duck[] participants) {
        try {
            RaceEvent event = eventRepo.findEventById(eventId);
            if (event == null) {
                throw new ValidationException("Event not found");
            }

            eventRepo.updateWinnerTime(eventId, winnerTime);
            eventRepo.saveRaceResults(eventId, winnerTime, participants);

            String message = String.format("Race %s finished! Winner time: %.2f seconds",
                    event.getNumeEveniment(), winnerTime);
            event.notifySubscribers(message);

        } catch (Exception e) {
            throw new RuntimeException("Error updating event results: " + e.getMessage(), e);
        }
    }

    public void runRaceAsync(RaceEvent event) {
        Thread raceThread = new Thread(() -> {
            try {
                event.runRace();

                if (event.getParticipanti() != null && event.getParticipanti().length > 0) {
                    updateEventResults(
                            event.getId(),
                            event.getTimpCurent(),
                            event.getParticipanti()
                    );
                }

            } catch (Exception e) {
                System.err.println("Error in race thread: " + e.getMessage());
                e.printStackTrace();
            }
        });

        raceThread.setDaemon(true);
        raceThread.start();
    }
}
