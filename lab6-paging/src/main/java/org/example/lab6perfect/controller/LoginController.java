package org.example.lab6perfect.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.lab6perfect.StartApplication;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.repository.EventRepoDB;
import org.example.lab6perfect.repository.UserRepoDB;
import org.example.lab6perfect.service.MessageService;
import org.example.lab6perfect.service.RaceEventService;
import org.example.lab6perfect.service.UserPagingService;
import org.example.lab6perfect.service.UserService;

import java.util.Optional;



public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private UserService userService;
    private UserPagingService userPagingService;
    private MessageService messageService;
    private RaceEventService  raceEventService;

    public void setServices(UserService userService, UserPagingService userPagingService,
                            MessageService messageService, RaceEventService raceEventService) {
        this.userService = userService;
        this.userPagingService = userPagingService;
        this.messageService = messageService;
        this.raceEventService = raceEventService;

    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Eroare", "Completează username-ul și parola!");
            return;
        }

        if (userService == null) {
            showAlert("Eroare", "Serviciile nu sunt inițializate!");
            return;
        }

        try {
            Optional<User> userOpt = userService.findUserByUsername(username);
            if (userOpt.isEmpty()) {
                showAlert("Eroare", "Utilizatorul nu există!");
                return;
            }

            User user = userOpt.get();
            if (user.checkPassword(password)) {
                openUserManagement(user);
            } else {
                showAlert("Eroare", "Parolă incorectă!");
            }

        } catch (Exception e) {
            showAlert("Eroare", "Eroare la autentificare: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openUserManagement(User loggedUser) {
        try {
            String fxmlPath = "/org/example/lab6perfect/user-management.fxml";

            java.net.URL url = getClass().getResource(fxmlPath);
//

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            UserManagementController controller = loader.getController();

            controller.setService(userService, userPagingService, messageService);
            controller.setLoggedInUser(loggedUser);

            StartApplication.addChatController(controller);

            Stage stage = new Stage();
            stage.setTitle("Duck Social Network - " + loggedUser.getUsername());
            stage.setScene(new Scene(root, 1100, 750));
            stage.show();

            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
