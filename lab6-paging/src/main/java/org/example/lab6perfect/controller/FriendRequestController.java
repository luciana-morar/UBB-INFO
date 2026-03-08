package org.example.lab6perfect.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.lab6perfect.domain.FriendRequest;
import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.obs.Observable;
import org.example.lab6perfect.obs.Observer;
import org.example.lab6perfect.service.UserService;

import java.util.List;
import java.util.Optional;

public class FriendRequestController implements Observer {


    @FXML
    private TextField friendRequestField;
    @FXML private Button sendFriendRequestButton;
    @FXML private ListView<FriendRequest> pendingRequestsList;
    @FXML private Button acceptRequestButton;
    @FXML private Button rejectRequestButton;
    @FXML private Label pendingRequestsLabel;
    @FXML private ListView<FriendRequest> sentRequestsList;
    @FXML private ListView<FriendRequest> allRequestsList;

    @FXML private Label statusLabel;


    private UserService userService;
    private User loggedInUser;

    private ObservableList<FriendRequest> pendingRequests = FXCollections.observableArrayList();

    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @FXML
    public void initialize() {
        pendingRequestsList.setItems(pendingRequests);
    }


    @Override
    public void onNewMessage(Message message) {

    }

    @Override
    public void onNewFriendRequest(FriendRequest request) {
        Platform.runLater(() -> {
            if (loggedInUser == null) return;

            if (request.getReceiver().getId().equals(loggedInUser.getId())) {
                loadPendingRequests();
                loadAllRequests();

                showNotification("Cerere nouă de prietenie de la: " + request.getSender().getUsername());

            }

            if (request.getSender().getId().equals(loggedInUser.getId())) {
                loadSentFriendRequests();
                loadAllRequests();
            }
        });
    }

    private void showNotification(String message) {
        Platform.runLater(() -> {

            showInfo(message);

                //showAlertNotification(message);
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Notificare");
                 alert.setHeaderText("Cerere nouă de prietenie");
                 alert.setContentText(message);
                 alert.show();

        });
    }



    @FXML
    private void handleSendFriendRequest() {
        if (loggedInUser == null) {
            showError("Trebuie să fii autentificat pentru a trimite cereri de prietenie!");
            return;
        }

        String targetUsername = friendRequestField.getText().trim();
        if (targetUsername.isEmpty()) {
            showError("Introdu un username!");
            return;
        }

        if (targetUsername.equals(loggedInUser.getUsername())) {
            showError("Nu poți trimite cerere de prietenie ție însuți!");
            return;
        }

        try {
            Optional<User> targetUserOpt = userService.findUserByUsername(targetUsername);
            if (!targetUserOpt.isPresent()) {
                showError("Utilizatorul " + targetUsername + " nu există!");
                return;
            }
            if (userService.getFriendRequestService() != null) {
                FriendRequest request = userService.getFriendRequestService().sendFriendRequest(
                        loggedInUser,
                        targetUserOpt.get()
                );
                showSuccess("Cerere de prietenie trimisă către " + targetUsername);
                friendRequestField.clear();

                loadSentFriendRequests();
            } else {
                userService.getFriendshipService().addFriendship(
                        loggedInUser,
                        targetUserOpt.get()
                );
                showSuccess("Prietenie adăugată cu " + targetUsername);
                friendRequestField.clear();

            }

        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }

    private void loadPendingRequests() {

        if (loggedInUser == null || userService.getFriendRequestService() == null) {
            return;
        }

        pendingRequests.clear();
        List<FriendRequest> requests = userService.getFriendRequestService()
                .getPendingRequestsForUser(loggedInUser);

        for (FriendRequest req : requests) {
            System.out.println("  - " + req.getSender().getUsername() +
                    " -> " + req.getReceiver().getUsername() +
                    " (Status: " + req.getStatus() + ")");
        }

        pendingRequests.addAll(requests);
        pendingRequestsLabel.setText("Cereri în așteptare: " + requests.size());
    }

    private void loadSentFriendRequests() {
        if (loggedInUser == null || userService.getFriendRequestService() == null) {
            return;
        }

        ObservableList<FriendRequest> sentRequests = sentRequestsList.getItems();
        sentRequests.clear();
        List<FriendRequest> requests = userService.getFriendRequestService()
                .getSentRequestsByUser(loggedInUser);
        sentRequests.addAll(requests);
    }

    private void loadAllRequests() {
        if (userService.getFriendRequestService() == null) {
            return;
        }

        ObservableList<FriendRequest> allRequests = allRequestsList.getItems();
        allRequests.clear();
        List<FriendRequest> requests = userService.getFriendRequestService()
                .getAllRequests();
        allRequests.addAll(requests);
    }

    @FXML
    private void handleAcceptRequest() {
        FriendRequest selected = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectează o cerere din listă!");
            return;
        }

        try {
            if (userService.getFriendRequestService() != null) {
                userService.getFriendRequestService().acceptFriendRequest(selected.getId());
                showSuccess("Cerere acceptată!");


                loadPendingRequests();
                loadSentFriendRequests();
                loadAllRequests();
               // loadChatFriends();

                notifyChatControllerToRefreshFriends();
                pendingRequestsList.getSelectionModel().clearSelection();

            }
        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }

    private void notifyChatControllerToRefreshFriends() {
        List<Friendship> refreshedFriendships = userService.getFriendshipService().listAll();
        if (loggedInUser != null) {
            Optional<User> refreshedUser = userService.findUserByUsername(loggedInUser.getUsername());
            if (refreshedUser.isPresent()) {
                loggedInUser = refreshedUser.get();
            }
        }
    }


    @FXML
    private void handleRejectRequest() {
        FriendRequest selected = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selectează o cerere din listă!");
            return;
        }

        try {
            if (userService.getFriendRequestService() != null) {
                userService.getFriendRequestService().rejectFriendRequest(selected.getId());
                showInfo("Cerere respinsă!");
                loadPendingRequests();
                loadSentFriendRequests();
                loadAllRequests();

                pendingRequestsList.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;

        if (user != null) {
            loadPendingRequests();
            loadSentFriendRequests();
            loadAllRequests();
            Observable.getInstance().addObserver(this);
        }
    }


    @FXML
    private void handleRefreshFriendRequests() {
        loadAllRequests();
    }


    private void showSuccess(String message) {
        statusLabel.setText("✓ " + message);
        statusLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        statusLabel.setText("✗ " + message);
        statusLabel.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
    }

    private void showWarning(String message) {
        statusLabel.setText("! " + message);
        statusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
    }

    private void showInfo(String message) {
        statusLabel.setText("i " + message);
        statusLabel.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
    }
}


