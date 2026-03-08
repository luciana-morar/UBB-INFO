package org.example.lab6perfect.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.event.DuckRaceSolver;
import org.example.lab6perfect.domain.event.RaceEvent;
import org.example.lab6perfect.service.MessageService;
import org.example.lab6perfect.service.RaceEventService;
import org.example.lab6perfect.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RaceEventController {

    @FXML private TableView<RaceEvent> eventTable;
    @FXML private TableColumn<RaceEvent, String> colEventId;
    @FXML private TableColumn<RaceEvent, String> colEventName;
    @FXML private TableColumn<RaceEvent, String> colLanes;
    @FXML private TableColumn<RaceEvent, String> colParticipants;

    @FXML private TextField eventNameField;
    @FXML private TextField distancesField;
    @FXML private TextField lanesField;

    @FXML private ListView<String> participantsList;
    @FXML private Label selectedEventLabel;
    @FXML private Label statusLabel;

    @FXML private Button createEventButton;
    @FXML private Button registerButton;
    @FXML private Button unregisterButton;
    @FXML private Button startRaceButton;
    @FXML private Button deleteEventButton;

    @FXML private VBox messageSection;
    @FXML private VBox raceProgressSection;
    @FXML private TextArea messageTextArea;
    @FXML private ProgressBar raceProgressBar;
    @FXML private Label raceStatusLabel;
    @FXML private TextArea raceLogTextArea;

    private RaceEventService raceEventService;
    private UserService userService;
    private MessageService messageService;
    private User loggedInUser;

    private final ObservableList<RaceEvent> events = FXCollections.observableArrayList();
    private final ObservableList<String> participantNames = FXCollections.observableArrayList();
    private RaceEvent selectedEvent;

    private ScheduledExecutorService raceExecutor;
    private boolean raceInProgress = false;

    private LocalDateTime raceStartTime;
    private double raceDuration = 0.0; // în secunde
    private ScheduledExecutorService timerExecutor;
    private DuckRaceSolver.Result raceResult;

    @FXML
    public void initialize() {
        setupEventTable();
        updateUI();

        messageSection.setVisible(false);
        raceProgressSection.setVisible(false);
    }

    private void setupEventTable() {
        colEventId.setCellValueFactory(cell -> {
            RaceEvent event = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    event.getId() != null ? event.getId().toString() : "N/A"
            );
        });

        colEventName.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeEveniment())
        );

        colLanes.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cell.getValue().getM())
                )
        );

        colParticipants.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(cell.getValue().getSubscribers().size())
                )
        );

        eventTable.setItems(events);

        eventTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedEvent = newVal;
                    updateSelectedEventUI();
                });
    }

    public void setServices(RaceEventService raceEventService, UserService userService,
                            MessageService messageService) {
        this.raceEventService = raceEventService;
        this.userService = userService;
        this.messageService = messageService;
        refreshEvents();
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateUI();
    }


    @FXML
    private void handleRefresh() {
        refreshEvents();
        showInfo("Events refreshed");
    }

    @FXML
    private void handleCreateEvent() {
        String name = eventNameField.getText().trim();
        String distancesStr = distancesField.getText().trim();
        String lanesStr = lanesField.getText().trim();

        if (name.isEmpty() || distancesStr.isEmpty() || lanesStr.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        try {
            String[] distParts = distancesStr.split(",");
            double[] distances = new double[distParts.length];
            for (int i = 0; i < distParts.length; i++) {
                distances[i] = Double.parseDouble(distParts[i].trim());
            }

            int lanes = Integer.parseInt(lanesStr);
            RaceEvent event = raceEventService.createRaceEvent(name, distances, lanes);
            showSuccess("Event created: " + name);

            clearEventForm();
            refreshEvents();

        } catch (NumberFormatException e) {
            showError("Invalid number format! Use numbers only.");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        if (selectedEvent == null) {
            showWarning("Please select an event first!");
            return;
        }

        if (loggedInUser == null) {
            showError("You must be logged in!");
            return;
        }

        try {
            raceEventService.registerParticipant(selectedEvent.getId(), loggedInUser.getId());
            showSuccess("Registered for: " + selectedEvent.getNumeEveniment());

            notifyProfileController();
            updateSelectedEventUI();

        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUnregister() {
        if (selectedEvent == null) {
            showWarning("Please select an event first!");
            return;
        }

        if (loggedInUser == null) {
            showError("You must be logged in!");
            return;
        }

        try {
            raceEventService.unregisterParticipant(selectedEvent.getId(), loggedInUser.getId());
            showSuccess("Unregistered from: " + selectedEvent.getNumeEveniment());

            notifyProfileController();
            updateSelectedEventUI();

        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteEvent() {
        if (selectedEvent == null) {
            showWarning("Please select an event first!");
            return;
        }

        if (loggedInUser == null || !loggedInUser.getUsername().equals("admin")) {
            showError("Only admin can delete events!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete: " + selectedEvent.getNumeEveniment());
        confirm.setContentText("This cannot be undone!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                raceEventService.deleteEvent(selectedEvent.getId());
                showSuccess("Event deleted");
                refreshEvents();
                selectedEvent = null;
                updateSelectedEventUI();
            }
        });
    }

    @FXML
    private void handleStartRace() {
        if (selectedEvent == null) {
            showWarning("Please select an event first!");
            return;
        }

        if (loggedInUser == null || !loggedInUser.getUsername().equals("admin")) {
            showError("Only admin can start races!");
            return;
        }

        if (selectedEvent.getSubscribers().isEmpty()) {
            showError("No participants registered for this event!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start Race");
        confirm.setHeaderText("Start Race: " + selectedEvent.getNumeEveniment());
        confirm.setContentText("Start the race simulation? This will send notifications to all participants.");

//        confirm.showAndWait().ifPresent(response -> {
//            if (response == ButtonType.OK) {
//                startRaceSimulation();
//                showInfo("Race started! Sending notifications...");
//            }
//        });
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Race Started");
                infoAlert.setHeaderText("Race '" + selectedEvent.getNumeEveniment() + "' Started");
                infoAlert.setContentText("Notifications have been sent to " +
                        selectedEvent.getSubscribers().size() + " participants.");
                infoAlert.show();

                startRaceSimulation();
                showInfo("Race started! Sending notifications...");
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        if (selectedEvent == null) {
            showWarning("Please select an event first!");
            return;
        }

        String messageText = messageTextArea.getText().trim();
        if (messageText.isEmpty()) {
            showError("Please enter a message!");
            return;
        }

        if (messageService == null) {
            showError("Message service not available!");
            return;
        }

        try {
            List<User> participants = selectedEvent.getSubscribers();
            messageService.sendMessage(loggedInUser, participants, messageText);
            List<User> selfList = List.of(loggedInUser);
            messageService.sendMessage(loggedInUser, selfList,
                    "You sent to all participants: " + messageText);

            showSuccess("Message sent to " + participants.size() + " participants!");
            messageTextArea.clear();


        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private void sendRaceNotification(String message) {
        if (messageService == null || selectedEvent == null) return;

        try {
            List<User> participants = selectedEvent.getSubscribers();

            messageService.sendMessage(loggedInUser, participants, "[Race Update] " + message);

            Platform.runLater(() -> {
                Alert notification = new Alert(Alert.AlertType.INFORMATION);
                notification.setTitle("Race Notification");
                notification.setHeaderText("Sent to " + participants.size() + " participants");
                notification.setContentText("Message: " + message);
                notification.initModality(Modality.NONE);
                notification.show();
            });

        } catch (Exception e) {
            System.err.println("Error sending race notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    private void sendRaceNotification(String message) {
//        if (messageService == null || selectedEvent == null) return;
//
//        try {
//            List<User> participants = selectedEvent.getSubscribers();
//            messageService.sendMessage(loggedInUser, participants,
//                    "[Race Update] " + message);
//            List<User> adminList = List.of(loggedInUser);
//            messageService.sendMessage(loggedInUser, adminList,
//                    "[Race Organizer] " + message);
//
//        } catch (Exception e) {
//            System.err.println("Error sending race notification: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    @FXML
    private void handleSimulateRace() {
        if (selectedEvent == null) {
            showWarning("Please select an event first!");
            return;
        }

        if (raceInProgress) {
            showWarning("Race already in progress!");
            return;
        }

        simulateRaceProgress();
    }

    private void startRaceSimulation() {
        raceProgressSection.setVisible(true);
        messageSection.setVisible(true);

        raceProgressBar.setProgress(0);
        raceLogTextArea.clear();

        calculateRaceResult();

        raceStartTime = LocalDateTime.now();
        raceDuration = 0.0;

        sendRaceNotification(" Race '" + selectedEvent.getNumeEveniment() + "' is starting!");
        appendToRaceLog("Race started: " + selectedEvent.getNumeEveniment());
        appendToRaceLog(" Participants: " + selectedEvent.getSubscribers().size());

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    startRaceTimer();
                    simulateRaceProgress();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void calculateRaceResult() {
        try {
            List<User> participants = selectedEvent.getSubscribers();
            Duck[] ducks = new Duck[participants.size()];

            for (int i = 0; i < participants.size(); i++) {
                if (participants.get(i) instanceof Duck) {
                    ducks[i] = (Duck) participants.get(i);
//                } else {
//                    ducks[i] = createMockDuck(participants.get(i));
//                }
                }

                raceResult = DuckRaceSolver.solve(
                        ducks,
                        selectedEvent.getDistante(),
                        selectedEvent.getM()
                );

                if (raceResult != null) {
                    appendToRaceLog("Race solver calculated minimum time: " +
                            formatTime(raceResult.time));
                }
            }

            } catch(Exception e){
                System.err.println("Error calculating race result: " + e.getMessage());
                e.printStackTrace();
            }
        }




    private void startRaceTimer() {
       if (timerExecutor != null && !timerExecutor.isShutdown()) {
            timerExecutor.shutdownNow();
        }


        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(() -> {
            raceDuration += 0.5;
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void simulateRaceProgress() {
        if (raceInProgress) return;

        raceInProgress = true;
        raceProgressBar.setProgress(0);
        raceStatusLabel.setText("Race in progress...");

        raceExecutor = Executors.newScheduledThreadPool(1);

        raceExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                double currentProgress = raceProgressBar.getProgress();
                double newProgress = currentProgress + 0.05;

                if (newProgress >= 1.0) {
                    newProgress = 1.0;
                    finishRace();
                }

                raceProgressBar.setProgress(newProgress);

                if (newProgress < 0.3) {
                    raceStatusLabel.setText("Starting... " + (int)(newProgress * 100) + "%");
                } else if (newProgress < 0.7) {
                    raceStatusLabel.setText("In progress... " + (int)(newProgress * 100) + "%");
                } else {
                    raceStatusLabel.setText("Final stretch! " + (int)(newProgress * 100) + "%");
                }
            });
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void finishRace() {
        if (raceExecutor != null) {
            raceExecutor.shutdown();
        }

        if (timerExecutor != null) {
            timerExecutor.shutdownNow();
        }

        raceInProgress = false;
        raceStatusLabel.setText("Race finished! ");

        if (raceResult != null && raceResult.aranjare != null && raceResult.aranjare.length > 0) {
            Duck winner = raceResult.aranjare[0];
            double winnerTime = raceResult.time;

            try {
                if (raceEventService != null && selectedEvent != null) {
                   raceEventService.saveWinnerTime(selectedEvent.getId(), winnerTime);
                }
            } catch (Exception e) {
                System.err.println("Error saving winner time: " + e.getMessage());
                e.printStackTrace();
            }

            String winnerMessage = "🎉 Winner: " + winner.getUsername() +
                    " with time: " + formatTime(winnerTime) ;
            appendToRaceLog(winnerMessage);
            appendToRaceLog(" Race completed successfully!");
            appendToRaceLog("Actual race duration: " + formatTime(raceDuration));

            for (int i = 0; i < raceResult.aranjare.length; i++) {
                if (raceResult.aranjare[i] != null) {
                    String position;
                    if (i == 0) {
                        position = "1 Lane " + (i+1) + ": ";
                    } else if (i == 1) {
                        position = "2 Lane " + (i+1) + ": ";
                    } else if (i == 2) {
                        position = "3 Lane " + (i+1) + ": ";
                    } else {
                        position = "   Lane " + (i+1) + ": ";
                    }
                    appendToRaceLog(position + raceResult.aranjare[i].getUsername());
                }
            }

            sendRaceNotification("!!! " + winner.getUsername() + " won the race!");
            sendRaceNotification("Race '" + selectedEvent.getNumeEveniment() + "' has finished!");

        } else {
            List<User> participants = selectedEvent.getSubscribers();
            if (!participants.isEmpty()) {
                Random rand = new Random();
                User winner = participants.get(rand.nextInt(participants.size()));
                double winnerTime = raceDuration;

                String winnerMessage = " Winner: " + winner.getUsername() +
                        " with time: " + formatTime(winnerTime) + " 🏆";
                appendToRaceLog(winnerMessage);
                appendToRaceLog(" Race completed successfully!");


                sendRaceNotification("!!! " + winner.getUsername() + " won the race!");
            }
        }

        showSuccess("Race simulation completed! Total time: " + formatTime(raceDuration));
    }

   private String formatTime(double seconds) {
        int minutes = (int) (seconds / 60);
        double remainingSeconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%d minutes %05.2f seconds", minutes, remainingSeconds);
        } else {
            return String.format("%.2f seconds", seconds);
        }
    }



    private void appendToRaceLog(String message) {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        raceLogTextArea.appendText("[" + timestamp + "] " + message + "\n");
    }


    private void updateSelectedEventUI() {
        participantNames.clear();
        messageSection.setVisible(false);

        if (selectedEvent != null) {
            selectedEventLabel.setText("Selected: " + selectedEvent.getNumeEveniment());

            for (User user : selectedEvent.getSubscribers()) {
                participantNames.add(user.getUsername() +
                        (user.equals(loggedInUser) ? " (You)" : ""));
            }

            boolean isParticipant = selectedEvent.getSubscribers().contains(loggedInUser);
            boolean isAdmin = loggedInUser != null && loggedInUser.getUsername().equals("admin");

            if (isAdmin || isParticipant) {
                messageSection.setVisible(true);
            }

            boolean isRegistered = selectedEvent.getSubscribers().contains(loggedInUser);
            registerButton.setDisable(isRegistered);
            unregisterButton.setDisable(!isRegistered);

        } else {
            selectedEventLabel.setText("No event selected");
            registerButton.setDisable(true);
            unregisterButton.setDisable(true);
        }

        participantsList.setItems(participantNames);
    }

    private void refreshEvents() {
        if (raceEventService != null) {
            events.setAll(raceEventService.getAllEvents());
        }
    }

    private void updateUI() {
        boolean isAdmin = loggedInUser != null && loggedInUser.getUsername().equals("admin");
        boolean isLoggedIn = loggedInUser != null;

        createEventButton.setVisible(isAdmin);
        deleteEventButton.setVisible(isAdmin);
        startRaceButton.setVisible(isAdmin);

        registerButton.setDisable(!isLoggedIn);
        unregisterButton.setDisable(!isLoggedIn);
    }

    private void clearEventForm() {
        eventNameField.clear();
        distancesField.clear();
        lanesField.clear();
        eventNameField.requestFocus();
    }

    private void notifyProfileController() {

        try {

        } catch (Exception e) {
            System.err.println("Error notifying profile: " + e.getMessage());
        }
    }


    private void showSuccess(String message) {
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

    private void showWarning(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("! " + message);
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("i " + message);
            statusLabel.setStyle("-fx-text-fill: #1565C0;");
        });
    }

}

//package org.example.lab6perfect.controller;
//
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.layout.VBox;
//import org.example.lab6perfect.domain.Message;
//import org.example.lab6perfect.domain.User;
//import org.example.lab6perfect.domain.event.RaceEvent;
//import org.example.lab6perfect.service.MessageService;
//import org.example.lab6perfect.service.RaceEventService;
//import org.example.lab6perfect.service.UserService;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class RaceEventController {
//
//    // UI Components
//    @FXML private TableView<RaceEvent> eventTable;
//    @FXML private TableColumn<RaceEvent, String> colEventId;
//    @FXML private TableColumn<RaceEvent, String> colEventName;
//    @FXML private TableColumn<RaceEvent, String> colLanes;
//    @FXML private TableColumn<RaceEvent, String> colParticipants;
//
//    @FXML private TextField eventNameField;
//    @FXML private TextField distancesField;
//    @FXML private TextField lanesField;
//
//    @FXML private ListView<String> participantsList;
//    @FXML private Label selectedEventLabel;
//    @FXML private Label statusLabel;
//
//    @FXML private Button createEventButton;
//    @FXML private Button registerButton;
//    @FXML private Button unregisterButton;
//    @FXML private Button startRaceButton;
//    @FXML private Button deleteEventButton;
//
//    // New components for messages and race progress
//    @FXML private VBox messageSection;
//    @FXML private VBox raceProgressSection;
//    @FXML private TextArea messageTextArea;
//    @FXML private ProgressBar raceProgressBar;
//    @FXML private Label raceStatusLabel;
//    @FXML private TextArea raceLogTextArea;
//
//    // Services
//    private RaceEventService raceEventService;
//    private UserService userService;
//    private MessageService messageService;
//    private User loggedInUser;
//
//    // Data
//    private final ObservableList<RaceEvent> events = FXCollections.observableArrayList();
//    private final ObservableList<String> participantNames = FXCollections.observableArrayList();
//    private RaceEvent selectedEvent;
//
//    // Race simulation
//    private ScheduledExecutorService raceExecutor;
//    private boolean raceInProgress = false;
//
//    @FXML
//    public void initialize() {
//        setupEventTable();
//        updateUI();
//
//        // Hide message and progress sections initially
//        messageSection.setVisible(false);
//        raceProgressSection.setVisible(false);
//    }
//
//    private void setupEventTable() {
//        colEventId.setCellValueFactory(cell -> {
//            RaceEvent event = cell.getValue();
//            return new javafx.beans.property.SimpleStringProperty(
//                    event.getId() != null ? event.getId().toString() : "N/A"
//            );
//        });
//
//        colEventName.setCellValueFactory(cell ->
//                new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeEveniment())
//        );
//
//        colLanes.setCellValueFactory(cell ->
//                new javafx.beans.property.SimpleStringProperty(
//                        String.valueOf(cell.getValue().getM())
//                )
//        );
//
//        colParticipants.setCellValueFactory(cell ->
//                new javafx.beans.property.SimpleStringProperty(
//                        String.valueOf(cell.getValue().getSubscribers().size())
//                )
//        );
//
//        eventTable.setItems(events);
//
//        eventTable.getSelectionModel().selectedItemProperty().addListener(
//                (obs, oldVal, newVal) -> {
//                    selectedEvent = newVal;
//                    updateSelectedEventUI();
//                });
//    }
//
////    public void setServices(RaceEventService raceEventService, UserService userService,
////                            MessageService messageService) {
////        this.raceEventService = raceEventService;
////        this.userService = userService;
////        this.messageService = messageService;
////        refreshEvents();
////    }
//
//    public void setLoggedInUser(User user) {
//        this.loggedInUser = user;
//        updateUI();
//    }
//
//    public void setServices(RaceEventService raceEventService, UserService userService,
//                            MessageService messageService) {
//        System.out.println("RaceEventController.setServices() called");
//        System.out.println("raceEventService: " + (raceEventService != null ? "not null" : "null"));
//        System.out.println("userService: " + (userService != null ? "not null" : "null"));
//        System.out.println("messageService: " + (messageService != null ? "not null" : "null"));
//
//        this.raceEventService = raceEventService;
//        this.userService = userService;
//        this.messageService = messageService;
//        refreshEvents();
//    }
//
//    private void refreshEvents() {
//        System.out.println("RaceEventController.refreshEvents() called");
//        if (raceEventService != null) {
//            List<RaceEvent> allEvents = raceEventService.getAllEvents();
//            System.out.println("Found " + allEvents.size() + " events in database");
//
//            // Clear și adaugă pe UI thread
//            Platform.runLater(() -> {
//                events.clear();
//                events.addAll(allEvents);
//                System.out.println("UI updated with " + events.size() + " events");
//
//                // Debug pentru a vedea ce e în TableView
//                System.out.println("TableView items count: " + eventTable.getItems().size());
//
//                // Forțează refresh-ul TableView
//                eventTable.refresh();
//            });
//
//            // Debug: afișează evenimentele găsite
//            for (RaceEvent event : allEvents) {
//                System.out.println("Event: " + event.getNumeEveniment() +
//                        " ID: " + event.getId() +
//                        " Participants: " + event.getSubscribers().size());
//            }
//        } else {
//            System.out.println("ERROR: raceEventService is null!");
//        }
//    }
//
//    // ========== EXISTING HANDLERS ==========
//
//    @FXML
//    private void handleRefresh() {
//        refreshEvents();
//        showInfo("Events refreshed");
//    }
//
//    @FXML
//    private void handleCreateEvent() {
//        String name = eventNameField.getText().trim();
//        String distancesStr = distancesField.getText().trim();
//        String lanesStr = lanesField.getText().trim();
//
//        if (name.isEmpty() || distancesStr.isEmpty() || lanesStr.isEmpty()) {
//            showError("Please fill all fields!");
//            return;
//        }
//
//        try {
//            String[] distParts = distancesStr.split(",");
//            double[] distances = new double[distParts.length];
//            for (int i = 0; i < distParts.length; i++) {
//                distances[i] = Double.parseDouble(distParts[i].trim());
//            }
//
//            int lanes = Integer.parseInt(lanesStr);
//            RaceEvent event = raceEventService.createRaceEvent(name, distances, lanes);
//            showSuccess("Event created: " + name);
//
//            clearEventForm();
//            refreshEvents();
//
//        } catch (NumberFormatException e) {
//            showError("Invalid number format! Use numbers only.");
//        } catch (Exception e) {
//            showError("Error: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    private void handleRegister() {
//        if (selectedEvent == null) {
//            showWarning("Please select an event first!");
//            return;
//        }
//
//        if (loggedInUser == null) {
//            showError("You must be logged in!");
//            return;
//        }
//
//        try {
//            raceEventService.registerParticipant(selectedEvent.getId(), loggedInUser.getId());
//            showSuccess("Registered for: " + selectedEvent.getNumeEveniment());
//
//            notifyProfileController();
//            updateSelectedEventUI();
//
//        } catch (Exception e) {
//            showError("Error: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    private void handleUnregister() {
//        if (selectedEvent == null) {
//            showWarning("Please select an event first!");
//            return;
//        }
//
//        if (loggedInUser == null) {
//            showError("You must be logged in!");
//            return;
//        }
//
//        try {
//            raceEventService.unregisterParticipant(selectedEvent.getId(), loggedInUser.getId());
//            showSuccess("Unregistered from: " + selectedEvent.getNumeEveniment());
//
//            notifyProfileController();
//            updateSelectedEventUI();
//
//        } catch (Exception e) {
//            showError("Error: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    private void handleDeleteEvent() {
//        if (selectedEvent == null) {
//            showWarning("Please select an event first!");
//            return;
//        }
//
//        if (loggedInUser == null || !loggedInUser.getUsername().equals("admin")) {
//            showError("Only admin can delete events!");
//            return;
//        }
//
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Delete Event");
//        confirm.setHeaderText("Delete: " + selectedEvent.getNumeEveniment());
//        confirm.setContentText("This cannot be undone!");
//
//        confirm.showAndWait().ifPresent(response -> {
//            if (response == ButtonType.OK) {
//                raceEventService.deleteEvent(selectedEvent.getId());
//                showSuccess("Event deleted");
//                refreshEvents();
//                selectedEvent = null;
//                updateSelectedEventUI();
//            }
//        });
//    }
//
//    // ========== NEW HANDLERS FOR LAB 10 ==========
//
//    @FXML
//    private void handleStartRace() {
//        if (selectedEvent == null) {
//            showWarning("Please select an event first!");
//            return;
//        }
//
//        if (loggedInUser == null || !loggedInUser.getUsername().equals("admin")) {
//            showError("Only admin can start races!");
//            return;
//        }
//
//        if (selectedEvent.getSubscribers().isEmpty()) {
//            showError("No participants registered for this event!");
//            return;
//        }
//
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Start Race");
//        confirm.setHeaderText("Start Race: " + selectedEvent.getNumeEveniment());
//        confirm.setContentText("Start the race simulation? This will send notifications to all participants.");
//
//        confirm.showAndWait().ifPresent(response -> {
//            if (response == ButtonType.OK) {
//                startRaceSimulation();
//                showInfo("Race started! Sending notifications...");
//            }
//        });
//    }
//
//
//    @FXML
//    private void handleSendMessage() {
//        if (selectedEvent == null) {
//            showWarning("Please select an event first!");
//            return;
//        }
//
//        String messageText = messageTextArea.getText().trim();
//        if (messageText.isEmpty()) {
//            showError("Please enter a message!");
//            return;
//        }
//
//        if (messageService == null) {
//            showError("Message service not available!");
//            return;
//        }
//
//        try {
//            // Send message to all participants
//            List<User> participants = selectedEvent.getSubscribers();
//
//            // Folosește metoda existentă sendMessage care primește List<User>
//            messageService.sendMessage(loggedInUser, participants, messageText);
//
//            // Also create a self-message for the sender
//            List<User> selfList = List.of(loggedInUser);
//            messageService.sendMessage(loggedInUser, selfList,
//                    "You sent to all participants: " + messageText);
//
//            showSuccess("Message sent to " + participants.size() + " participants!");
//            messageTextArea.clear();
//
//            // Log to race log
//            appendToRaceLog("📢 Message sent: " + messageText);
//
//        } catch (Exception e) {
//            showError("Error sending message: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private void sendRaceNotification(String message) {
//        if (messageService == null || selectedEvent == null) return;
//
//        try {
//            // Send to all participants
//            List<User> participants = selectedEvent.getSubscribers();
//
//            // Folosește metoda existentă
//            messageService.sendMessage(loggedInUser, participants,
//                    "[Race Update] " + message);
//
//            // Also send to admin/organizer
//            List<User> adminList = List.of(loggedInUser);
//            messageService.sendMessage(loggedInUser, adminList,
//                    "[Race Organizer] " + message);
//
//        } catch (Exception e) {
//            System.err.println("Error sending race notification: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    private void handleSimulateRace() {
//        if (selectedEvent == null) {
//            showWarning("Please select an event first!");
//            return;
//        }
//
//        if (raceInProgress) {
//            showWarning("Race already in progress!");
//            return;
//        }
//
//        simulateRaceProgress();
//    }
//
//    // ========== RACE SIMULATION METHODS ==========
//
//    private void startRaceSimulation() {
//        // Show race progress section
//        raceProgressSection.setVisible(true);
//        messageSection.setVisible(true);
//
//        // Reset progress
//        raceProgressBar.setProgress(0);
//        raceLogTextArea.clear();
//
//        // Send starting notifications
//        sendRaceNotification("Race '" + selectedEvent.getNumeEveniment() + "' is starting!");
//        appendToRaceLog(" Race started: " + selectedEvent.getNumeEveniment());
//        appendToRaceLog("Participants: " + selectedEvent.getSubscribers().size());
//
//        // Start simulation after 2 seconds
//        new Thread(() -> {
//            try {
//                Thread.sleep(2000);
//                Platform.runLater(this::simulateRaceProgress);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    private void simulateRaceProgress() {
//        if (raceInProgress) return;
//
//        raceInProgress = true;
//        raceProgressBar.setProgress(0);
//        raceStatusLabel.setText("Race in progress...");
//
//        // Create executor for scheduled tasks
//        raceExecutor = Executors.newScheduledThreadPool(1);
//
//        // Simulate race progress
//        raceExecutor.scheduleAtFixedRate(() -> {
//            Platform.runLater(() -> {
//                double currentProgress = raceProgressBar.getProgress();
//                double newProgress = currentProgress + 0.05;
//
//                if (newProgress >= 1.0) {
//                    newProgress = 1.0;
//                    finishRace();
//                }
//
//                raceProgressBar.setProgress(newProgress);
//
//                // Update status based on progress
//                if (newProgress < 0.3) {
//                    raceStatusLabel.setText("Starting... " + (int)(newProgress * 100) + "%");
//                    if (Math.random() > 0.7) {
//                        appendToRaceLog("🎯 Participants at starting line...");
//                    }
//                } else if (newProgress < 0.7) {
//                    raceStatusLabel.setText("In progress... " + (int)(newProgress * 100) + "%");
//                    if (Math.random() > 0.8) {
//                        sendRandomRaceUpdate();
//                    }
//                } else {
//                    raceStatusLabel.setText("Final stretch! " + (int)(newProgress * 100) + "%");
//                }
//            });
//        }, 0, 500, TimeUnit.MILLISECONDS); // Update every 500ms
//    }
//
//    private void sendRandomRaceUpdate() {
//        if (selectedEvent == null || selectedEvent.getSubscribers().isEmpty()) return;
//
//        Random rand = new Random();
//        List<User> participants = selectedEvent.getSubscribers();
//        User randomParticipant = participants.get(rand.nextInt(participants.size()));
//
//        String[] updates = {
//                randomParticipant.getUsername() + " is taking the lead! 🚀",
//                randomParticipant.getUsername() + " is struggling a bit... 🐢",
//                randomParticipant.getUsername() + " made a great turn! 🔄",
//                "Close competition between participants! ⚡",
//                randomParticipant.getUsername() + " is picking up speed! 💨"
//        };
//
//        String update = updates[rand.nextInt(updates.length)];
//        appendToRaceLog(update);
//
//        // Send notification to all participants
//        sendRaceNotification(update);
//    }
//
//    private void finishRace() {
//        if (raceExecutor != null) {
//            raceExecutor.shutdown();
//        }
//
//        raceInProgress = false;
//        raceStatusLabel.setText("Race finished!");
//
//        // Determine winner
//        List<User> participants = selectedEvent.getSubscribers();
//        if (!participants.isEmpty()) {
//            Random rand = new Random();
//            User winner = participants.get(rand.nextInt(participants.size()));
//
//            String winnerMessage = "🎉 Winner: " + winner.getUsername() + "!";
//            appendToRaceLog(winnerMessage);
//            appendToRaceLog("🏆 Race completed successfully!");
//
//            // Send winner notification to all
//            sendRaceNotification(winnerMessage);
//            sendRaceNotification("Race '" + selectedEvent.getNumeEveniment() + "' has finished!");
//
//            // Send congratulations message to winner (single user)
//            sendMessageToSingleUser(winner,
//                    "Congratulations! You won the race: " + selectedEvent.getNumeEveniment());
//        }
//
//        showSuccess("Race simulation completed!");
//    }
//
//    private void sendMessageToSingleUser(User recipient, String message) {
//        if (messageService == null) return;
//
//        try {
//            List<User> recipientList = List.of(recipient);
//            messageService.sendMessage(loggedInUser, recipientList, message);
//        } catch (Exception e) {
//            System.err.println("Error sending message to " + recipient.getUsername() +
//                    ": " + e.getMessage());
//        }
//    }
//
//
//    private void appendToRaceLog(String message) {
//        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
//        raceLogTextArea.appendText("[" + timestamp + "] " + message + "\n");
//    }
//
//    // ========== HELPER METHODS ==========
//
//    private void updateSelectedEventUI() {
//        participantNames.clear();
//        messageSection.setVisible(false);
//
//        if (selectedEvent != null) {
//            selectedEventLabel.setText("Selected: " + selectedEvent.getNumeEveniment());
//
//            // Show participants
//            for (User user : selectedEvent.getSubscribers()) {
//                participantNames.add(user.getUsername() +
//                        (user.equals(loggedInUser) ? " (You)" : ""));
//            }
//
//            // Show message section if user is admin or participant
//            boolean isParticipant = selectedEvent.getSubscribers().contains(loggedInUser);
//            boolean isAdmin = loggedInUser != null && loggedInUser.getUsername().equals("admin");
//
//            if (isAdmin || isParticipant) {
//                messageSection.setVisible(true);
//            }
//
//            // Update button states
//            boolean isRegistered = selectedEvent.getSubscribers().contains(loggedInUser);
//            registerButton.setDisable(isRegistered);
//            unregisterButton.setDisable(!isRegistered);
//
//        } else {
//            selectedEventLabel.setText("No event selected");
//            registerButton.setDisable(true);
//            unregisterButton.setDisable(true);
//        }
//
//        participantsList.setItems(participantNames);
//    }
//
////    private void refreshEvents() {
////        if (raceEventService != null) {
////            events.setAll(raceEventService.getAllEvents());
////        }
////    }
//
//    private void updateUI() {
//        boolean isAdmin = loggedInUser != null && loggedInUser.getUsername().equals("admin");
//        boolean isLoggedIn = loggedInUser != null;
//
//        createEventButton.setVisible(isAdmin);
//        deleteEventButton.setVisible(isAdmin);
//        startRaceButton.setVisible(isAdmin);
//
//        registerButton.setDisable(!isLoggedIn);
//        unregisterButton.setDisable(!isLoggedIn);
//    }
//
//    private void clearEventForm() {
//        eventNameField.clear();
//        distancesField.clear();
//        lanesField.clear();
//        eventNameField.requestFocus();
//    }
//
//    private void notifyProfileController() {
//        // Existing notification logic
//        try {
//            // Find and refresh profile controller
//            // (Keep your existing implementation here)
//        } catch (Exception e) {
//            System.err.println("Error notifying profile: " + e.getMessage());
//        }
//    }
//
//    // Status message methods
//    private void showSuccess(String message) {
//        Platform.runLater(() -> {
//            statusLabel.setText("✓ " + message);
//            statusLabel.setStyle("-fx-text-fill: #2E7D32;");
//        });
//    }
//
//    private void showError(String message) {
//        Platform.runLater(() -> {
//            statusLabel.setText("✗ " + message);
//            statusLabel.setStyle("-fx-text-fill: #C62828;");
//        });
//    }
//
//    private void showWarning(String message) {
//        Platform.runLater(() -> {
//            statusLabel.setText("! " + message);
//            statusLabel.setStyle("-fx-text-fill: #FF9800;");
//        });
//    }
//
//    private void showInfo(String message) {
//        Platform.runLater(() -> {
//            statusLabel.setText("i " + message);
//            statusLabel.setStyle("-fx-text-fill: #1565C0;");
//        });
//    }
//
//    // Cleanup
//    public void cleanup() {
//        if (raceExecutor != null) {
//            raceExecutor.shutdownNow();
//        }
//    }
//}
//
////package org.example.lab6perfect.controller;
////
////import javafx.application.Platform;
////import javafx.collections.FXCollections;
////import javafx.collections.ObservableList;
////import javafx.fxml.FXML;
////import javafx.scene.control.*;
////import javafx.scene.layout.VBox;
////import org.example.lab6perfect.domain.User;
////import org.example.lab6perfect.domain.event.RaceEvent;
////import org.example.lab6perfect.service.RaceEventService;
////import org.example.lab6perfect.service.UserService;
////
////import java.util.concurrent.ScheduledExecutorService;
////
////
////public class RaceEventController {
////
////    @FXML private TableView<RaceEvent> eventTable;
////    @FXML private TableColumn<RaceEvent, String> colEventId;
////    @FXML private TableColumn<RaceEvent, String> colEventName;
////    @FXML private TableColumn<RaceEvent, String> colLanes;
////    @FXML private TableColumn<RaceEvent, String> colParticipants;
////
////    @FXML private TextField eventNameField;
////    @FXML private TextField distancesField;
////    @FXML private TextField lanesField;
////
////    @FXML private ListView<String> participantsList; // Simplificat - doar nume
////    @FXML private Label selectedEventLabel;
////    @FXML private Label statusLabel;
////
////    @FXML private VBox messageSection;
////    @FXML private VBox raceProgressSection;
////    @FXML private TextArea messageTextArea;
////    @FXML private ProgressBar raceProgressBar;
////    @FXML private Label raceStatusLabel;
////    @FXML private TextArea raceLogTextArea;
////
////    @FXML private Button createEventButton;
////    @FXML private Button registerButton;
////    @FXML private Button unregisterButton;
////    @FXML private Button startRaceButton;
////    @FXML private Button deleteEventButton;
////
////    private RaceEventService raceEventService;
////    private UserService userService;
////    private User loggedInUser;
////
////    private final ObservableList<RaceEvent> events = FXCollections.observableArrayList();
////    private final ObservableList<String> participantNames = FXCollections.observableArrayList();
////
////    private ScheduledExecutorService raceExecutor;
////    private boolean raceInProgress = false;
////
////    private RaceEvent selectedEvent;
////
////    @FXML
////    public void initialize() {
////        setupEventTable();
////        updateUI();
////    }
////
////    private void setupEventTable() {
////        // Setează valorile pentru coloane
////        colEventId.setCellValueFactory(cell -> {
////            RaceEvent event = cell.getValue();
////            return new javafx.beans.property.SimpleStringProperty(
////                    event.getId() != null ? event.getId().toString() : "N/A"
////            );
////        });
////
////        colEventName.setCellValueFactory(cell ->
////                new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeEveniment())
////        );
////
////        colLanes.setCellValueFactory(cell ->
////                new javafx.beans.property.SimpleStringProperty(
////                        String.valueOf(cell.getValue().getM())
////                )
////        );
////
////        colParticipants.setCellValueFactory(cell ->
////                new javafx.beans.property.SimpleStringProperty(
////                        String.valueOf(cell.getValue().getSubscribers().size())
////                )
////        );
////
////        eventTable.setItems(events);
////
////        // Ascultă selecția în tabel
////        eventTable.getSelectionModel().selectedItemProperty().addListener(
////                (obs, oldVal, newVal) -> {
////                    selectedEvent = newVal;
////                    updateSelectedEventUI();
////                });
////    }
////
////    public void setServices(RaceEventService raceEventService, UserService userService) {
////        this.raceEventService = raceEventService;
////        this.userService = userService;
////        refreshEvents();
////    }
////
////    public void setLoggedInUser(User user) {
////        this.loggedInUser = user;
////        updateUI();
////    }
////
////    // ========== HANDLERE BUTOANE ==========
////
////    @FXML
////    private void handleRefresh() {
////        refreshEvents();
////        showInfo("Events refreshed");
////    }
////
////    @FXML
////    private void handleCreateEvent() {
////        String name = eventNameField.getText().trim();
////        String distancesStr = distancesField.getText().trim();
////        String lanesStr = lanesField.getText().trim();
////
////        if (name.isEmpty() || distancesStr.isEmpty() || lanesStr.isEmpty()) {
////            showError("Please fill all fields!");
////            return;
////        }
////
////        try {
////            // Parsează distanțele
////            String[] distParts = distancesStr.split(",");
////            double[] distances = new double[distParts.length];
////            for (int i = 0; i < distParts.length; i++) {
////                distances[i] = Double.parseDouble(distParts[i].trim());
////            }
////
////            int lanes = Integer.parseInt(lanesStr);
////
////            // Creează evenimentul
////            RaceEvent event = raceEventService.createRaceEvent(name, distances, lanes);
////            showSuccess("Event created: " + name);
////
////            // Curăță formularul și reîncarcă lista
////            clearEventForm();
////            refreshEvents();
////
////        } catch (NumberFormatException e) {
////            showError("Invalid number format! Use numbers only.");
////        } catch (Exception e) {
////            showError("Error: " + e.getMessage());
////        }
////    }
////
////    @FXML
////    private void handleRegister() {
////        if (selectedEvent == null) {
////            showWarning("Please select an event first!");
////            return;
////        }
////
////        if (loggedInUser == null) {
////            showError("You must be logged in!");
////            return;
////        }
////
////        try {
////            raceEventService.registerParticipant(selectedEvent.getId(), loggedInUser.getId());
////            showSuccess("Registered for: " + selectedEvent.getNumeEveniment());
////            updateSelectedEventUI();
////
////        } catch (Exception e) {
////            showError("Error: " + e.getMessage());
////        }
////    }
////
////    @FXML
////    private void handleUnregister() {
////        if (selectedEvent == null) {
////            showWarning("Please select an event first!");
////            return;
////        }
////
////        if (loggedInUser == null) {
////            showError("You must be logged in!");
////            return;
////        }
////
////        try {
////            raceEventService.unregisterParticipant(selectedEvent.getId(), loggedInUser.getId());
////            showSuccess("Unregistered from: " + selectedEvent.getNumeEveniment());
////            updateSelectedEventUI();
////
////        } catch (Exception e) {
////            showError("Error: " + e.getMessage());
////        }
////    }
////
////    @FXML
////    private void handleStartRace() {
////        if (selectedEvent == null) {
////            showWarning("Please select an event first!");
////            return;
////        }
////
////        if (loggedInUser == null || !loggedInUser.getUsername().equals("admin")) {
////            showError("Only admin can start races!");
////            return;
////        }
////
////        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
////        confirm.setTitle("Start Race");
////        confirm.setHeaderText("Start Race: " + selectedEvent.getNumeEveniment());
////        confirm.setContentText("Are you sure? The race will run in background.");
////
////        confirm.showAndWait().ifPresent(response -> {
////            if (response == ButtonType.OK) {
////                raceEventService.runRaceAsync(selectedEvent);
////                showInfo("Race started! Participants will be notified.");
////            }
////        });
////    }
////
////    @FXML
////    private void handleDeleteEvent() {
////        if (selectedEvent == null) {
////            showWarning("Please select an event first!");
////            return;
////        }
////
////        if (loggedInUser == null || !loggedInUser.getUsername().equals("admin")) {
////            showError("Only admin can delete events!");
////            return;
////        }
////
////        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
////        confirm.setTitle("Delete Event");
////        confirm.setHeaderText("Delete: " + selectedEvent.getNumeEveniment());
////        confirm.setContentText("This cannot be undone!");
////
////        confirm.showAndWait().ifPresent(response -> {
////            if (response == ButtonType.OK) {
////                raceEventService.deleteEvent(selectedEvent.getId());
////                showSuccess("Event deleted");
////                refreshEvents();
////                selectedEvent = null;
////                updateSelectedEventUI();
////            }
////        });
////    }
////
////    @FXML
////    private void handleShowMyEvents() {
////        if (loggedInUser == null) {
////            showError("You must be logged in!");
////            return;
////        }
////
////        StringBuilder sb = new StringBuilder();
////        sb.append("My Registered Events:\n");
////
////        int count = 0;
////        for (RaceEvent event : events) {
////            if (event.getSubscribers().contains(loggedInUser)) {
////                sb.append("- ").append(event.getNumeEveniment())
////                        .append(" (").append(event.getSubscribers().size()).append(" participants)\n");
////                count++;
////            }
////        }
////
////        if (count == 0) {
////            sb.append("You are not registered for any events.");
////        }
////
////        Alert info = new Alert(Alert.AlertType.INFORMATION);
////        info.setTitle("My Events");
////        info.setHeaderText("Registered Events");
////        info.setContentText(sb.toString());
////        info.show();
////    }
////
////
////    private void refreshEvents() {
////        if (raceEventService != null) {
////            events.setAll(raceEventService.getAllEvents());
////        }
////    }
////
////    private void updateSelectedEventUI() {
////        participantNames.clear();
////
////        if (selectedEvent != null) {
////            selectedEventLabel.setText("Selected: " + selectedEvent.getNumeEveniment());
////
////            // Adaugă numele participanților
////            for (User user : selectedEvent.getSubscribers()) {
////                participantNames.add(user.getUsername());
////            }
////
////            // Verifică dacă utilizatorul curent e înregistrat
////            boolean isRegistered = selectedEvent.getSubscribers().contains(loggedInUser);
////            registerButton.setDisable(isRegistered);
////            unregisterButton.setDisable(!isRegistered);
////
////        } else {
////            selectedEventLabel.setText("No event selected");
////            registerButton.setDisable(true);
////            unregisterButton.setDisable(true);
////        }
////
////        participantsList.setItems(participantNames);
////    }
////
////    private void updateUI() {
////        boolean isAdmin = loggedInUser != null && loggedInUser.getUsername().equals("admin");
////        boolean isLoggedIn = loggedInUser != null;
////
////        createEventButton.setVisible(isAdmin);
////        deleteEventButton.setVisible(isAdmin);
////        startRaceButton.setVisible(isAdmin);
////
////        registerButton.setDisable(!isLoggedIn);
////        unregisterButton.setDisable(!isLoggedIn);
////    }
////
////    private void clearEventForm() {
////        eventNameField.clear();
////        distancesField.clear();
////        lanesField.clear();
////        eventNameField.requestFocus();
////    }
////
////    private void showSuccess(String message) {
////        Platform.runLater(() -> {
////            statusLabel.setText("✓ " + message);
////            statusLabel.setStyle("-fx-text-fill: #2E7D32;");
////        });
////    }
////
////    private void showError(String message) {
////        Platform.runLater(() -> {
////            statusLabel.setText("✗ " + message);
////            statusLabel.setStyle("-fx-text-fill: #C62828;");
////        });
////    }
////
////    private void showWarning(String message) {
////        Platform.runLater(() -> {
////            statusLabel.setText("! " + message);
////            statusLabel.setStyle("-fx-text-fill: #FF9800;");
////        });
////    }
////
////    private void showInfo(String message) {
////        Platform.runLater(() -> {
////            statusLabel.setText("i " + message);
////            statusLabel.setStyle("-fx-text-fill: #1565C0;");
////        });
////    }
////}
////
////
////
////
////
//////public class RaceEventController {
//////
//////    @FXML private TableView<RaceEvent> eventTable;
//////    @FXML private TableColumn<RaceEvent, Long> colEventId;
//////    @FXML private TableColumn<RaceEvent, String> colEventName;
//////    @FXML private TableColumn<RaceEvent, Integer> colLanes;
//////    @FXML private TableColumn<RaceEvent, Integer> colParticipants;
//////
//////    @FXML private TextField eventNameField;
//////    @FXML private TextField distancesField;
//////    @FXML private TextField lanesField;
//////
//////    @FXML private ListView<User> participantsList;
//////    @FXML private ListView<RaceEvent> myEventsList;
//////
//////    @FXML private Button createEventButton;
//////    @FXML private Button registerButton;
//////    @FXML private Button unregisterButton;
//////    @FXML private Button startRaceButton;
//////    @FXML private Button deleteEventButton;
//////
//////    @FXML private Label statusLabel;
//////
//////    private RaceEventService raceEventService;
//////    private UserService userService;
//////    private User loggedInUser;
//////
//////    private final ObservableList<RaceEvent> events = FXCollections.observableArrayList();
//////    private final ObservableList<User> participants = FXCollections.observableArrayList();
//////    private final ObservableList<RaceEvent> myEvents = FXCollections.observableArrayList();
//////
//////    @FXML
//////    public void initialize() {
//////        setupEventTable();
//////        refreshEvents();
//////    }
//////
//////    private void setupEventTable() {
//////        colEventId.setCellValueFactory(new PropertyValueFactory<>("id"));
//////        colEventName.setCellValueFactory(new PropertyValueFactory<>("numeEveniment"));
//////        colLanes.setCellValueFactory(new PropertyValueFactory<>("M"));
//////        colParticipants.setCellValueFactory(cell -> {
//////            RaceEvent event = cell.getValue();
//////            return new SimpleIntegerProperty(event.getSubscribers().size()).asObject();
//////        });
//////        eventTable.setItems(events);
//////
//////        eventTable.getSelectionModel().selectedItemProperty().addListener(
//////                (obs, oldVal, newVal) -> {
//////                    if (newVal != null) {
//////                        loadEventParticipants(newVal.getId());
//////                    }
//////                });
//////
//////        participantsList.setItems(participants);
//////        myEventsList.setItems(myEvents);
//////    }
//////
//////    public void setServices(RaceEventService raceEventService, UserService userService) {
//////        this.raceEventService = raceEventService;
//////        this.userService = userService;
//////    }
//////
//////    public void setLoggedInUser(User user) {
//////        this.loggedInUser = user;
//////        updateUI();
//////        refreshMyEvents();
//////    }
//////
//////    @FXML
//////    private void handleCreateEvent() {
//////        String name = eventNameField.getText().trim();
//////        String distancesStr = distancesField.getText().trim();
//////        String lanesStr = lanesField.getText().trim();
//////
//////        if (name.isEmpty() || distancesStr.isEmpty() || lanesStr.isEmpty()) {
//////            showError("Complete all fields!");
//////            return;
//////        }
//////
//////        try {
//////            String[] distParts = distancesStr.split(",");
//////            double[] distances = new double[distParts.length];
//////            for (int i = 0; i < distParts.length; i++) {
//////                distances[i] = Double.parseDouble(distParts[i].trim());
//////            }
//////
//////            int lanes = Integer.parseInt(lanesStr);
//////
//////            RaceEvent event = raceEventService.createRaceEvent(name, distances, lanes);
//////            showSuccess("Event created: " + name);
//////
//////            clearEventForm();
//////            refreshEvents();
//////
//////        } catch (NumberFormatException e) {
//////            showError("Invalid number format!");
//////        } catch (Exception e) {
//////            showError("Error: " + e.getMessage());
//////        }
//////    }
//////
//////    @FXML
//////    private void handleRegister() {
//////        RaceEvent selectedEvent = eventTable.getSelectionModel().getSelectedItem();
//////        if (selectedEvent == null) {
//////            showWarning("Select an event first!");
//////            return;
//////        }
//////
//////        if (loggedInUser == null) {
//////            showError("You must be logged in!");
//////            return;
//////        }
//////
//////        try {
//////            raceEventService.registerParticipant(selectedEvent.getId(), loggedInUser.getId());
//////            showSuccess("Registered for event: " + selectedEvent.getNumeEveniment());
//////
//////            loadEventParticipants(selectedEvent.getId());
//////            refreshMyEvents();
//////
//////        } catch (Exception e) {
//////            showError("Error: " + e.getMessage());
//////        }
//////    }
//////
//////    @FXML
//////    private void handleUnregister() {
//////        RaceEvent selectedEvent = eventTable.getSelectionModel().getSelectedItem();
//////        if (selectedEvent == null) {
//////            showWarning("Select an event first!");
//////            return;
//////        }
//////
//////        if (loggedInUser == null) {
//////            showError("You must be logged in!");
//////            return;
//////        }
//////
//////        try {
//////            raceEventService.unregisterParticipant(selectedEvent.getId(), loggedInUser.getId());
//////            showSuccess("Unregistered from event: " + selectedEvent.getNumeEveniment());
//////
//////            loadEventParticipants(selectedEvent.getId());
//////            refreshMyEvents();
//////
//////        } catch (Exception e) {
//////            showError("Error: " + e.getMessage());
//////        }
//////    }
//////
//////    @FXML
//////    private void handleStartRace() {
//////        RaceEvent selectedEvent = eventTable.getSelectionModel().getSelectedItem();
//////        if (selectedEvent == null) {
//////            showWarning("Select an event first!");
//////            return;
//////        }
//////
//////        if (!loggedInUser.getUsername().equals("admin")) {
//////            showError("Only admin can start races!");
//////            return;
//////        }
//////
//////        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//////        confirm.setTitle("Start Race");
//////        confirm.setHeaderText("Start Race: " + selectedEvent.getNumeEveniment());
//////        confirm.setContentText("Are you sure you want to start this race?");
//////
//////        confirm.showAndWait().ifPresent(response -> {
//////            if (response == ButtonType.OK) {
//////                raceEventService.runRaceAsync(selectedEvent);
//////                showInfo("Race started! Participants will receive notifications.");
//////            }
//////        });
//////    }
//////
//////    @FXML
//////    private void handleDeleteEvent() {
//////        RaceEvent selectedEvent = eventTable.getSelectionModel().getSelectedItem();
//////        if (selectedEvent == null) {
//////            showWarning("Select an event first!");
//////            return;
//////        }
//////
//////        if (!loggedInUser.getUsername().equals("admin")) {
//////            showError("Only admin can delete events!");
//////            return;
//////        }
//////
//////        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//////        confirm.setTitle("Delete Event");
//////        confirm.setHeaderText("Delete Event: " + selectedEvent.getNumeEveniment());
//////        confirm.setContentText("This will permanently delete the event and all its data.");
//////
//////        confirm.showAndWait().ifPresent(response -> {
//////            if (response == ButtonType.OK) {
//////                raceEventService.deleteEvent(selectedEvent.getId());
//////                showSuccess("Event deleted: " + selectedEvent.getNumeEveniment());
//////                refreshEvents();
//////            }
//////        });
//////    }
//////
//////    private void loadEventParticipants(Long eventId) {
//////        participants.clear();
//////        List<org.example.lab6perfect.domain.duck.SwimmingDuck> ducks =
//////                raceEventService.getEventParticipants(eventId);
//////        participants.addAll(ducks);
//////    }
//////
//////    @FXML
//////    private void refreshEvents() {
//////        if (raceEventService != null) {
//////            events.setAll(raceEventService.getAllEvents());
//////        }
//////    }
//////
//////    @FXML
//////    private void refreshMyEvents() {
//////        myEvents.clear();
//////        if (loggedInUser != null && raceEventService != null) {
//////            List<RaceEvent> allEvents = raceEventService.getAllEvents();
//////            for (RaceEvent event : allEvents) {
//////                if (event.getSubscribers().contains(loggedInUser)) {
//////                    myEvents.add(event);
//////                }
//////            }
//////        }
//////    }
//////
//////    private void updateUI() {
//////        boolean isAdmin = loggedInUser != null && loggedInUser.getUsername().equals("admin");
//////        boolean isLoggedIn = loggedInUser != null;
//////
//////        createEventButton.setVisible(isAdmin);
//////        deleteEventButton.setVisible(isAdmin);
//////        startRaceButton.setVisible(isAdmin);
//////
//////        registerButton.setDisable(!isLoggedIn);
//////        unregisterButton.setDisable(!isLoggedIn);
//////    }
//////
//////    private void clearEventForm() {
//////        eventNameField.clear();
//////        distancesField.clear();
//////        lanesField.clear();
//////    }
//////
//////    private void showSuccess(String message) {
//////        Platform.runLater(() -> {
//////            statusLabel.setText("✓ " + message);
//////            statusLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
//////        });
//////    }
//////
//////    private void showError(String message) {
//////        Platform.runLater(() -> {
//////            statusLabel.setText("✗ " + message);
//////            statusLabel.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
//////        });
//////    }
//////
//////    private void showWarning(String message) {
//////        Platform.runLater(() -> {
//////            statusLabel.setText("! " + message);
//////            statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
//////        });
//////    }
//////
//////    private void showInfo(String message) {
//////        Platform.runLater(() -> {
//////            statusLabel.setText("i " + message);
//////            statusLabel.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
//////        });
//////    }
//////}
