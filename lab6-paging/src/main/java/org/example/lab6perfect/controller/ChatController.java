package org.example.lab6perfect.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.lab6perfect.StartApplication;
import org.example.lab6perfect.domain.FriendRequest;
import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.obs.Observable;
import org.example.lab6perfect.obs.Observer;
import org.example.lab6perfect.service.MessageService;
import org.example.lab6perfect.service.UserService;

import javafx.scene.control.Button;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.awt.*;

import java.util.Optional;

public class ChatController implements Observer {

    @FXML private Label chatUserLabel;
    @FXML private Button chatLoginButton;
    @FXML private Button chatLogoutButton;

    @FXML private Label replyToLabel;
    @FXML private Button replyButton;

    @FXML private Label typingIndicator;
    @FXML private ProgressIndicator typingProgress;

    @FXML private ListView<User> chatFriendsList;
    @FXML private TextArea chatArea;
    @FXML private TextField chatMessageField;
    @FXML private Button chatSendButton;

    private UserService userService;
    private MessageService messageService;

    private User loggedInUser;
    private User selectedChatFriend;
    private Message messageToReply;

    private final ObservableList<User> chatFriends = FXCollections.observableArrayList();
    private Message selectedMessage;

    private DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private javafx.animation.Timeline typingHideTimer;

    @Override
    public void onNewMessage(Message message) {

        receiveExternalMessage(message);
    }

    @Override
    public void onNewFriendRequest(FriendRequest request) {

    }

    @FXML
    public void initialize() {
        setupChatFriendsList();
        replyButton.setVisible(false);
        replyToLabel.setVisible(false);
        typingIndicator.setVisible(false);
        typingProgress.setVisible(false);

        Observable.getInstance().addObserver(this);

        startAutoRefresh();
    }

    public void setServices(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateChatUI();
        loadChatFriends();
    }

    private void setupChatFriendsList() {
        chatFriendsList.setItems(chatFriends);
        chatFriendsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText((empty || user == null) ? null : user.getUsername());
            }
        });

        chatFriendsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedChatFriend = newVal;
                    if (newVal != null) {
                        loadChatConversation();
                    }
                });
    }

    @FXML
    private void handleChatLogin() {
        if (loggedInUser != null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Login Chat");
        dialog.setHeaderText("Autentificare pentru Chat");
        dialog.setContentText("Username:");

        dialog.showAndWait().ifPresent(username -> {
            Optional<User> userOpt = userService.findUserByUsername(username);
            userOpt.ifPresent(u -> {
                loggedInUser = u;
                updateChatUI();
                loadChatFriends();
            });
        });
    }

    @FXML
    private void handleChatLogout() {
        loggedInUser = null;
        selectedChatFriend = null;
        messageToReply = null;
        chatFriends.clear();
        chatArea.clear();
        clearReply();
        updateChatUI();
    }


    @FXML
    private void handleSendChatMessage() {
        if (loggedInUser == null || selectedChatFriend == null) return;

        String content = chatMessageField.getText().trim();
        if (content.isEmpty()) return;

        List<User> receivers = new ArrayList<>();
        receivers.add(selectedChatFriend);

        Message message;
        if (messageToReply != null) {
            message = messageService.sendReply(loggedInUser, receivers, content, messageToReply.getId());
            clearReply();
        } else {
            message = messageService.sendMessage(loggedInUser, receivers, content);
        }

        displayChatMessage(message);
        chatMessageField.clear();
    }


    private void displayChatMessage(Message message) {
        String time = message.getTimestamp().format(TIME_FORMATTER);
        String senderName = message.getSender() != null ? message.getSender().getUsername() : "System";

        String prefix = message.getClass().getSimpleName().contains("Reply") ? "↪ " : "";
        String line = String.format("[%s] %s: %s%s\n", time, senderName, prefix, message.getContent());

        if (!chatArea.getText().contains(line.trim())) chatArea.appendText(line);
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void handleMessageClick() {
        String selectedText = chatArea.getSelectedText();
        if (selectedText != null && !selectedText.trim().isEmpty()) {
            findAndSelectMessage(selectedText);
        }
    }

    private void findAndSelectMessage(String selectedText) {
        if (messageService == null || loggedInUser == null || selectedChatFriend == null) return;

        List<Message> conversation = messageService.getConversation(loggedInUser, selectedChatFriend);
        for (Message msg : conversation) {
            if (msg.getContent().contains(selectedText) || selectedText.contains(msg.getContent())) {
                selectedMessage = msg;
                messageToReply = msg;

                replyToLabel.setText("Răspunzi la: \"" +
                        msg.getContent().substring(0, Math.min(30, msg.getContent().length())) +
                        (msg.getContent().length() > 30 ? "..." : "") + "\"");
                replyToLabel.setVisible(true);
                replyButton.setVisible(true);
                return;
            }
        }
    }

    @FXML
    private void handleReply() {
        if (selectedMessage == null) return;
        messageToReply = selectedMessage;

        replyToLabel.setText("Răspunzi la: \"" +
                selectedMessage.getContent().substring(0, Math.min(50, selectedMessage.getContent().length())) +
                (selectedMessage.getContent().length() > 50 ? "..." : "") + "\"");
        replyToLabel.setVisible(true);
        replyButton.setVisible(false);

        chatMessageField.requestFocus();
        chatMessageField.setText("");
    }

    @FXML
    private void handleClearReply() {
        clearReply();
    }

    private void clearReply() {
        selectedMessage = null;
        messageToReply = null;
        replyToLabel.setText("");
        replyToLabel.setVisible(false);
        replyButton.setVisible(false);
    }

    private void loadChatFriends() {
        if (loggedInUser == null) return;

        chatFriends.clear();
        List<Friendship> allFriendships = userService.getFriendshipService().listAll();

        for (Friendship f : allFriendships) {
            if (f.getUser1().getId().equals(loggedInUser.getId())) chatFriends.add(f.getUser2());
            else if (f.getUser2().getId().equals(loggedInUser.getId())) chatFriends.add(f.getUser1());
        }


    }

    private void loadChatConversation() {
        if (loggedInUser == null || selectedChatFriend == null || messageService == null) return;

        chatArea.clear();
        List<Message> conversation = messageService.getConversation(loggedInUser, selectedChatFriend);
        for (Message msg : conversation) displayChatMessage(msg);
    }

    public void receiveExternalMessage(Message message) {
        Platform.runLater(() -> {
            if (loggedInUser == null) {
                return;
            }

            boolean isForMe = false;
            if (message.getReceiver() != null) {
                for (User receiver : message.getReceiver()) {
                    if (receiver.getId().equals(loggedInUser.getId())) {
                        isForMe = true;
                        break;
                    }
                }
            }

            boolean isFromMe = message.getSender() != null &&
                    message.getSender().getId().equals(loggedInUser.getId());

            if (isForMe || isFromMe) {
                displayChatMessage(message);
                loadChatFriends();
                if (isForMe && message.getSender() != null) {
                    User sender = message.getSender();
                    User friendInList = chatFriends.stream().filter(friend -> friend.getId().equals(sender.getId())).findFirst().orElse(null);

                    if (friendInList != null) {
                        if (selectedChatFriend != null && selectedChatFriend.getId().equals(sender.getId())) {
                            loadChatConversation();
                        } else {
                            selectedChatFriend = friendInList;
                            Platform.runLater(() -> {
                                chatFriendsList.getSelectionModel().select(friendInList);
                                loadChatConversation();
                            });
                        }
                    }
                } else if (isFromMe) {
                    if (selectedChatFriend != null && message.getReceiver() != null &&
                            !message.getReceiver().isEmpty()) {
                        loadChatConversation();
                    }
                }
            }
        });
    }

    @FXML
    private void handleTyping() {
        if (loggedInUser == null || selectedChatFriend == null) return;

        typingIndicator.setText("Scrii...");
        typingIndicator.setVisible(true);
        typingProgress.setVisible(true);

        new Thread(() -> {
            try {
                StartApplication.notifyTyping(
                        loggedInUser.getId(),
                        loggedInUser.getUsername(),
                        selectedChatFriend.getId()
                );
            } catch (Exception e) {
                System.err.println("Eroare la trimiterea notificării de typing: " + e.getMessage());
            }
        }).start();

        if (typingHideTimer != null) typingHideTimer.stop();

        typingHideTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(2),
                        event -> {
                            typingIndicator.setVisible(false);
                            typingProgress.setVisible(false);
                        }
                )
        );
        typingHideTimer.play();
    }

    public void showSomeoneTyping(String username) {
        Platform.runLater(() -> {
            typingIndicator.setText(username + " scrie...");
            typingIndicator.setVisible(true);
            typingProgress.setVisible(true);

            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.seconds(2),
                            event -> {
                                typingIndicator.setVisible(false);
                                typingProgress.setVisible(false);
                            }
                    )
            ).play();
        });
    }

//    public Long getLoggedInUserId() {
//        return loggedInUser != null ? loggedInUser.getId() : null;
//    }

    private void updateChatUI() {
        boolean loggedIn = loggedInUser != null;

        chatUserLabel.setText(loggedIn ? "Autentificat ca: " + loggedInUser.getUsername()
                : "Nu ești autentificat");

        chatFriendsList.setDisable(!loggedIn);
        chatArea.setDisable(!loggedIn);
        chatMessageField.setDisable(!loggedIn);
        chatSendButton.setDisable(!loggedIn);
        replyButton.setDisable(!loggedIn);

        chatLoginButton.setVisible(!loggedIn);
        chatLogoutButton.setVisible(loggedIn);
    }

    private javafx.animation.Timeline autoRefreshTimer;
    private void startAutoRefresh() {
        autoRefreshTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(20),
                        event -> {
                            if (loggedInUser != null) {
                                loadChatFriends();
                            }
                        }
                )
        );
        autoRefreshTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefreshTimer.play();
    }
}





