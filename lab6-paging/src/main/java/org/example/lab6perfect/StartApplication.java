package org.example.lab6perfect;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.lab6perfect.controller.LoginController;
import org.example.lab6perfect.controller.UserManagementController;
import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.ReplyMessage;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.repository.*;
import org.example.lab6perfect.service.*;
import org.example.lab6perfect.validator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StartApplication extends Application {

    private static List<UserManagementController> chatControllers = new ArrayList<>();

    private static UserService userService;
    private static UserPagingService userPagingService;
    private static MessageService messageService;
    private static RaceEventService raceEventService;

    public static void notifyTyping(Long fromUserId, String fromUsername, Long toUserId) {
        for (UserManagementController controller : chatControllers) {
            if (controller.getLoggedInUserId() != null &&
                    controller.getLoggedInUserId().equals(toUserId)) {
                controller.showSomeoneTyping(fromUsername);
                break;
            }
        }
    }

    public static void addChatController(UserManagementController controller) {
        if (!chatControllers.contains(controller)) {
            chatControllers.add(controller);
            }
    }

    public static void notifyAllChatControllers(Message message) {
        if (message.getReceiver() != null) {
            System.out.println("Către: " + message.getReceiver().stream()
                    .map(u -> u.getUsername() + " (ID: " + u.getId() + ")")
                    .collect(java.util.stream.Collectors.joining(", ")));
        }

        for (int i = 0; i < chatControllers.size(); i++) {
            UserManagementController controller = chatControllers.get(i);
            System.out.println("  [" + i + "] " + controller.getCurrentUserInfo());
        }

        for (UserManagementController controller : chatControllers) {
            controller.receiveExternalMessage(message);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Properties prop = new Properties();
            prop.setProperty("db.url", "jdbc:postgresql://localhost:5432/lab4");
            prop.setProperty("db.username", "postgres");
            prop.setProperty("db.password", "luciana29072005");

            DatabaseConnection.setProperties(prop);

            UserValidator userValidator = new UserValidator();
            FriendshipValidator friendshipValidator = new FriendshipValidator();
            MessageValidator messageValidator = new MessageValidator();

            UserRepoDB userRepo = new UserRepoDB(prop);
            FriendshipRepoDB friendshipRepo = new FriendshipRepoDB(prop, userRepo);
            MessageRepoDB messageRepo = new MessageRepoDB(prop, userRepo);

            FriendshipService friendshipService = new FriendshipService(friendshipRepo, friendshipValidator);

            FriendRequestRepoDB friendRequestRepo = new FriendRequestRepoDB(userRepo, prop);
            FriendRequestService friendRequestService = new FriendRequestService(friendRequestRepo, friendshipService);

            EventRepoDB eventRepo = new EventRepoDB(prop, userRepo);
            raceEventService = new RaceEventService(eventRepo, userRepo);


            messageService = new MessageService(messageRepo, messageValidator) {
                @Override
                public org.example.lab6perfect.domain.Message sendMessage(
                        org.example.lab6perfect.domain.User sender,
                        java.util.List<org.example.lab6perfect.domain.User> receiver,
                        String content) {

                    org.example.lab6perfect.domain.Message msg = super.sendMessage(sender, receiver, content);
                    notifyAllChatControllers(msg);
                    return msg;
                }

                @Override
                public ReplyMessage sendReply(
                        User sender,
                        java.util.List<User> receiver,
                        String content,
                        Long originalMessageId) {

                    org.example.lab6perfect.domain.ReplyMessage msg = super.sendReply(sender, receiver, content, originalMessageId);
                    notifyAllChatControllers(msg);
                    return msg;
                }
            };

            userService = new UserService(userRepo, friendshipService, userValidator,friendRequestService);

            UserRepoDBPaged userRepoPaged = new UserRepoDBPaged(prop, userRepo);
            userPagingService = new UserPagingService(userRepoPaged);

            openLoginWindow(primaryStage, 100, 100, "Fereastra 1");

            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                    openSecondLoginWindow();
                    openThirdLoginWindow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Eroare fatală", e);
        }
    }

    private void openLoginWindow(Stage stage, double x, double y, String windowName) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        LoginController loginController = loader.getController();
        loginController.setServices(userService, userPagingService, messageService, raceEventService);

        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Login - Duck Social Network (" + windowName + ")");
        stage.setX(x);
        stage.setY(y);
        stage.setScene(scene);
        stage.show();
    }

    private void openSecondLoginWindow() {
        try {
            Stage secondStage = new Stage();
            openLoginWindow(secondStage, 550, 100, "Fereastra 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openThirdLoginWindow() {
        try {
            Stage thirdStage = new Stage();
            openLoginWindow(thirdStage, 1000, 100, "Fereastra 3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Aplicația nu a putut fi pornită");
        alert.setContentText("Eroare: " + e.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
