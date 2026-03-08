package org.example.lab6perfect.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.service.UserService;


public class FriendshipController {

    @FXML private TableView<Friendship> friendshipTable;
    @FXML private TableColumn<Friendship, String> colFriendUser1;
    @FXML private TableColumn<Friendship, String> colFriendUser2;
    @FXML private TextField friend1Field;
    @FXML private TextField friend2Field;

    private final ObservableList<Friendship> friendships =
            FXCollections.observableArrayList();

    private UserService userService;

    @FXML
    public void initialize() {
        colFriendUser1.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser1().getUsername()));

        colFriendUser2.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser2().getUsername()));

        friendshipTable.setItems(friendships);
    }

    public void setService(UserService userService) {
        this.userService = userService;
        refreshAll();
    }

    public void setLoggedInUser(User user) {
        refreshAll();
    }

    @FXML
    private void addFriendship() {
        String u1 = friend1Field.getText().trim();
        String u2 = friend2Field.getText().trim();

        if (u1.isEmpty() || u2.isEmpty() || u1.equals(u2)) return;

        userService.getFriendshipService()
                .addFriendshipByUsernames(u1, u2);

        clearForm();
        refreshAll();
    }

    @FXML
    private void removeSelectedFriendship() {
        Friendship selected =
                friendshipTable.getSelectionModel().getSelectedItem();

        if (selected == null) return;

        userService.getFriendshipService()
                .removeFriendship(selected.getUser1(), selected.getUser2());

        refreshAll();
    }

    @FXML
    private void refreshAll() {
        if (userService != null) {
            friendships.setAll(
                    userService.getFriendshipService().listAll()
            );
        }
    }

    @FXML
    private void clearForm() {
        friend1Field.clear();
        friend2Field.clear();
    }
}
