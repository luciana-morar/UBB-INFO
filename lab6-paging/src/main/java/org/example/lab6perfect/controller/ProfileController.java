package org.example.lab6perfect.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.event.RaceEvent;
import org.example.lab6perfect.service.RaceEventService;
import org.example.lab6perfect.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label friendsCountLabel;
    @FXML private Label eventsCountLabel;

    @FXML private Label messagesCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label userStatusLabel;


    @FXML private ListView<String> friendsList;
    @FXML private ListView<String> eventsList;
    @FXML private ListView<String> activityList;
    @FXML private TabPane tabPane;

    private UserService userService;
    private RaceEventService raceEventService;
    private User loggedInUser;

    private final ObservableList<String> friends = FXCollections.observableArrayList();
    private final ObservableList<String> events = FXCollections.observableArrayList();
    private final ObservableList<String> activities = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        friendsList.setItems(friends);
        eventsList.setItems(events);
        activityList.setItems(activities);

        String[] avatars = {
                "person1.png",
                "person2.png",
                "person4.png",
                "person5.png",
                "person7.png",
                "person8.jpg",
                "profile.image",
                "rata1.png",
                "rata2.png",
                "rata3.png",
                "rata4.png",


        };

        for (String avatar : avatars) {
            String displayName = avatar.replace(".png", "").replace(".jpg", "");
            avatarMap.put(displayName, avatar);
            avatarCombo.getItems().add(displayName);
        }

        try {
            Image defaultImage = new Image(getClass().getResource("/org/example/lab6perfect/images/default.jpg").toExternalForm());
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            if (!avatars[0].isEmpty()) {
                Image fallback = new Image(getClass().getResource("/org/example/lab6perfect/images/" + avatars[0]).toExternalForm());
                profileImageView.setImage(fallback);
            }
        }
    }

    public void setServices(UserService userService, RaceEventService raceEventService) {
        this.userService = userService;
        this.raceEventService = raceEventService;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        refreshProfile();
    }
    @FXML
    public void refreshProfile() {
        if (loggedInUser == null) {
            return;
        }

        try {
            usernameLabel.setText(loggedInUser.getUsername());
            emailLabel.setText(loggedInUser.getEmail());
            userTypeLabel.setText(loggedInUser.getClass().getSimpleName());

            if (userService != null && userService.getFriendshipService() != null) {
                List<User> userFriends = userService.getFriendshipService()
                        .getFriendsOfUser(loggedInUser);

                friendsCountLabel.setText(String.valueOf(userFriends.size()));
                updateFriendsList(userFriends);
            }

           if (raceEventService != null) {
                List<RaceEvent> allEvents = raceEventService.getAllEvents();

                long registeredEvents = allEvents.stream()
                        .filter(event -> {
                            for (User subscriber : event.getSubscribers()) {
                                if (subscriber.getId().equals(loggedInUser.getId())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .count();

                eventsCountLabel.setText(String.valueOf(registeredEvents));
                updateEventsList(allEvents);

                eventsCountLabel.setText(String.valueOf(registeredEvents));
                Platform.runLater(() -> {eventsCountLabel.setText(String.valueOf(registeredEvents));
                });

            }

             updateActivityList();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFriendsList(List<User> userFriends) {
        friends.clear();
        for (User friend : userFriends) {
            friends.add(friend.getUsername() + " (" + friend.getClass().getSimpleName() + ")");
        }
    }
    private void updateEventsList(List<RaceEvent> allEvents) {
        events.clear();
        int userEventsCount = 0;
        for (RaceEvent event : allEvents) {

            boolean isSubscribed = false;
            for (User subscriber : event.getSubscribers()) {
                if (subscriber.getId().equals(loggedInUser.getId())) {
                    isSubscribed = true;
                    userEventsCount++;
                    break;
                }
            }

            if (isSubscribed) {
                String eventText = event.getNumeEveniment() +
                        " (" + event.getSubscribers().size() + " participants)";
                events.add(eventText);
                System.out.println("  Added to UI: " + eventText);
            }
        }

         if (events.isEmpty()) {
            events.add("No events yet");
        }
    }

    private void updateActivityList() {
        activities.clear();

        activities.add("✓ Logged in today");

        List<User> userFriends = userService.getFriendshipService()
                .getFriendsOfUser(loggedInUser);
        if (!userFriends.isEmpty()) {
            activities.add("✓ Has " + userFriends.size() + " friends");
        }

        List<RaceEvent> allEvents = raceEventService.getAllEvents();
        long registeredCount = allEvents.stream()
                .filter(event -> event.getSubscribers().contains(loggedInUser))
                .count();
        if (registeredCount > 0) {
            activities.add("✓ Registered for " + registeredCount + " race events");
        }

        // ultimele evenimente la care s-a înscris
        allEvents.stream()
                .filter(event -> event.getSubscribers().contains(loggedInUser))
                .limit(3)
                .forEach(event -> activities.add("✓ Joined: " + event.getNumeEveniment()));
    }

    //avatar
    @FXML private ImageView profileImageView;
    @FXML private ComboBox<String> avatarCombo;

    private Map<String, String> avatarMap = new HashMap<>();


    @FXML
    private void handleAvatarSelected() {
        String selected = avatarCombo.getSelectionModel().getSelectedItem();
        if (selected != null && avatarMap.containsKey(selected)) {
            String imageFile = avatarMap.get(selected);
                Image newImage = new Image(getClass().getResource("/org/example/lab6perfect/images/" + imageFile).toExternalForm());
                profileImageView.setImage(newImage);

        }
    }




    private void showStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("✓ " + message);
            statusLabel.setStyle("-fx-text-fill: #2E7D32;");
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("✗ " + message);
            statusLabel.setStyle("-fx-text-fill: #C62828;");
        });
    }
}