package org.example.lab6perfect.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.example.lab6perfect.domain.*;
import org.example.lab6perfect.domain.event.RaceEvent;
import org.example.lab6perfect.repository.EventRepoDB;
import org.example.lab6perfect.service.MessageService;
import org.example.lab6perfect.service.RaceEventService;
import org.example.lab6perfect.service.UserPagingService;
import org.example.lab6perfect.service.UserService;


import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class UserManagementController {

    @FXML private TabPane tabPane;
    @FXML private Tab raceEventsTab;

    private UserService userService;
    private UserPagingService userPagingService;
    private MessageService messageService;
    private RaceEventService raceEventService;

    private ObservableList<User> chatFriends = FXCollections.observableArrayList();
    private ObservableList<FriendRequest> pendingRequests = FXCollections.observableArrayList();

    private User loggedInUser;


    @FXML private FriendshipController friendshipsViewController;
    @FXML private UsersController usersViewController;
    @FXML private CommunitiesController communitiesViewController;
    @FXML private FriendRequestController friendRequestViewController;
    @FXML private ChatController chatViewController;
    @FXML private RaceEventController raceEventsViewController;
    @FXML private ProfileController profileViewController;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "Profile".equals(newTab.getText())) {
                System.out.println("Profile tab selected - refreshing...");
                refreshProfileTab();
            }
        });

        try {
            if (raceEventsViewController == null && raceEventsTab != null) {
                Node raceEventsContent = raceEventsTab.getContent();
                if (raceEventsContent != null && raceEventsContent.getUserData() instanceof RaceEventController) {
                    raceEventsViewController = (RaceEventController) raceEventsContent.getUserData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getCurrentUserInfo() {
        if (loggedInUser == null) return "Not logged in";
        return loggedInUser.getUsername() + " (ID: " + loggedInUser.getId() + ")";
    }

    public void receiveExternalMessage(Message message) {
         if (chatViewController != null) {
            chatViewController.receiveExternalMessage(message);
        }
    }


    public void setLoggedInUser(User user) {
        this.loggedInUser = user;

        try {
            usersViewController.setLoggedInUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            friendRequestViewController.setLoggedInUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            communitiesViewController.setLoggedInUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            chatViewController.setLoggedInUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            friendRequestViewController.setLoggedInUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (raceEventsViewController != null) {
            raceEventsViewController.setLoggedInUser(user);
        }
        if (profileViewController != null) {
            profileViewController.setLoggedInUser(user);
        }

    }

    public void setService(UserService userService, UserPagingService userPagingService, MessageService messageService) {
        this.userService = userService;
        this.userPagingService = userPagingService;
        this.messageService = messageService;

        Properties prop = new Properties();
        prop.setProperty("db.url", "jdbc:postgresql://localhost:5432/lab4");
        prop.setProperty("db.username", "postgres");
        prop.setProperty("db.password", "luciana29072005");

        EventRepoDB eventRepo = new EventRepoDB(prop, userService.getUserRepo());
        this.raceEventService = new RaceEventService(eventRepo, userService.getUserRepo());

        if (raceEventsViewController != null) {
            raceEventsViewController.setServices(raceEventService, userService, messageService);
        }

        friendshipsViewController.setService(userService);
        usersViewController.setServices(userService, userPagingService);
        communitiesViewController.setUserService(userService);
        friendRequestViewController.setUserService(userService);
        chatViewController.setServices(userService, messageService);

        if (profileViewController != null) {
            profileViewController.setServices(userService, raceEventService);
        }

    }

//    public void setService(UserService userService, UserPagingService userPagingService,MessageService messageService) {
//        this.userService = userService;
//        this.userPagingService = userPagingService;
//        this.messageService = messageService;
//
//        Properties prop = new Properties();
//        prop.setProperty("db.url", "jdbc:postgresql://localhost:5432/lab4");
//        prop.setProperty("db.username", "postgres");
//        prop.setProperty("db.password", "luciana29072005");
//
//        EventRepoDB eventRepo = new EventRepoDB(prop, userService.getUserRepo());
//        this.raceEventService = new RaceEventService(eventRepo, userService.getUserRepo());
//
//
//        friendshipsViewController.setService(userService);
//        usersViewController.setServices(userService, userPagingService);
//        communitiesViewController.setUserService(userService);
//        friendRequestViewController.setUserService(userService);
//        chatViewController.setServices(userService, messageService);
//        raceEventsViewController.setServices(raceEventService,userService,messageService);
//
//        if (profileViewController != null) {
//            profileViewController.setServices(userService, raceEventService);
//        } else {
//            System.out.println("ERROR: profileViewController is null!");
//        }
//
//
//    }

    public void refreshProfileTab() {
        System.out.println("UserManagementController.refreshProfileTab() called");

        if (profileViewController != null) {
            profileViewController.refreshProfile();
            System.out.println("ProfileController refreshed via profileViewController");
        } else {
            System.out.println("profileViewController is null, trying to find it...");

            // Încearcă să găsești controller-ul în tab
            for (Tab tab : tabPane.getTabs()) {
                if ("Profile".equals(tab.getText())) {
                    Node content = tab.getContent();
                    if (content != null) {
                        Object controller = content.getProperties().get("controller");
                        if (controller instanceof ProfileController) {
                            profileViewController = (ProfileController) controller;
                            profileViewController.refreshProfile();
                            System.out.println("Found and refreshed ProfileController");
                            break;
                        }
                    }
                }
            }
        }
    }


    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;

        if (chatViewController != null && userService != null) {
            chatViewController.setServices(userService, messageService);

        }
    }

    public void showSomeoneTyping(String username) {
        if (chatViewController != null) {
            chatViewController.showSomeoneTyping(username);
        }
    }


    public Long getLoggedInUserId() {
        return loggedInUser != null ? loggedInUser.getId() : null;
    }
}
