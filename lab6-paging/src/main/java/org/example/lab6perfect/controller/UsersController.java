package org.example.lab6perfect.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.lab6perfect.domain.*;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;
import org.example.lab6perfect.service.*;


public class UsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colType;

    @FXML private ComboBox<String> userTypeCombo;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;

    @FXML private TextField numeField;
    @FXML private TextField prenumeField;
    @FXML private TextField ocupatieField;
    @FXML private DatePicker dataNasteriiPicker;

    @FXML private TextField vitezaField;
    @FXML private TextField rezistentaField;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    @FXML private Label lblNume;
    @FXML private Label lblPrenume;
    @FXML private Label lblOcupatie;
    @FXML private Label lblDataNasterii;
    @FXML private Label lblViteza;
    @FXML private Label lblRezistenta;

    @FXML private Label statusLabel;



    private UserService userService;
    private UserPagingService userPagingService;

    private final ObservableList<User> userList = FXCollections.observableArrayList();

    public void setLoggedInUser(User user) {
    }
    @FXML
    public void initialize() {
        setupUserTable();
        setupUserTypeCombo();
    }

    private void setupUserTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colType.setCellValueFactory(cell -> {
            User u = cell.getValue();
            if (u instanceof Persoana) return new SimpleStringProperty("Persoana");
            if (u instanceof FlyingDuck) return new SimpleStringProperty("Rata Zburatoare");
            if (u instanceof SwimmingDuck) return new SimpleStringProperty("Rata Inotatoare");
            return new SimpleStringProperty("?");
        });

        userTable.setItems(userList);
    }


    private void setupUserTypeCombo() {
        userTypeCombo.getItems().addAll("Persoana", "Rata Zburatoare", "Rata Inotatoare");
        userTypeCombo.setValue("Persoana");

        userTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Persoana".equals(newVal)) {
                showPersonFields();
            } else {
                showDuckFields();
            }
        });
    }

    private void showPersonFields() {
        lblNume.setVisible(true);
        numeField.setVisible(true);
        lblPrenume.setVisible(true);
        prenumeField.setVisible(true);
        lblOcupatie.setVisible(true);
        ocupatieField.setVisible(true);
        lblDataNasterii.setVisible(true);
        dataNasteriiPicker.setVisible(true);

        lblViteza.setVisible(false);
        vitezaField.setVisible(false);
        lblRezistenta.setVisible(false);
        rezistentaField.setVisible(false);
    }

    private void showDuckFields() {
        lblNume.setVisible(false);
        numeField.setVisible(false);
        lblPrenume.setVisible(false);
        prenumeField.setVisible(false);
        lblOcupatie.setVisible(false);
        ocupatieField.setVisible(false);
        lblDataNasterii.setVisible(false);
        dataNasteriiPicker.setVisible(false);

        lblViteza.setVisible(true);
        vitezaField.setVisible(true);
        lblRezistenta.setVisible(true);
        rezistentaField.setVisible(true);
    }

    public void setServices(UserService userService,
                            UserPagingService userPagingService) {
        this.userService = userService;
        this.userPagingService = userPagingService;
        refreshUsers();
    }
    public void refreshUsers() {
        userPagingService.resetToFirstPage();
        userList.setAll(userPagingService.getCurrentPage());
        updatePagingUI();
    }

    @FXML
    private void nextPage() {
        userList.setAll(userPagingService.getNextPage());
        updatePagingUI();
    }

    @FXML
    private void previousPage() {
        userList.setAll(userPagingService.getPreviousPage());
        updatePagingUI();
    }

    private void updatePagingUI() {
        pageLabel.setText(userPagingService.getPageInfo());
        prevButton.setDisable(!userPagingService.hasPreviousPage());
        nextButton.setDisable(!userPagingService.hasNextPage());
    }

        @FXML
    private void addUser() {
        try {
            String type = userTypeCombo.getValue();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Completeaza toate campurile obligatorii!");
                return;
            }

            User newUser;

            if ("Persoana".equals(type)) {
                String nume = numeField.getText().trim();
                String prenume = prenumeField.getText().trim();
                if (nume.isEmpty() || prenume.isEmpty()) {
                    showError("Completeaza numele si prenumele!");
                    return;
                }

                newUser = new Persoana(
                        null,
                        username,
                        email,
                        password,
                        nume,
                        prenume,
                        ocupatieField.getText().trim().isEmpty() ? "Nespecificat" : ocupatieField.getText().trim(),
                        dataNasteriiPicker.getValue(),
                        50
                );
            } else {
                double viteza, rezistenta;
                try {
                    viteza = Double.parseDouble(vitezaField.getText().trim());
                    rezistenta = Double.parseDouble(rezistentaField.getText().trim());
                } catch (NumberFormatException e) {
                    showError("Viteza si rezistenta trebuie sa fie numere!");
                    return;
                }

                if ("Rata Zburatoare".equals(type)) {
                    newUser = new FlyingDuck(
                            null,
                            username,
                            email,
                            password,
                            viteza,
                            rezistenta
                    );
                } else {
                    newUser = new SwimmingDuck(
                            null,
                            username,
                            email,
                            password,
                            viteza,
                            rezistenta
                    );
                }
            }

            userService.addUser(newUser);
            clearForm();
            refreshUsers();

            showSuccess("Utilizator adaugat: " + username + " (" + type + ")");

        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }

    @FXML
    private void removeSelectedUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Selecteaza un utilizator din tabel pentru stergere!");
            return;
        }

        try {
            userService.removeUser(selectedUser);
            refreshUsers();
            showSuccess("Utilizator sters: " + selectedUser.getUsername());
        } catch (Exception e) {
            showError("Eroare la stergere: " + e.getMessage());
        }
    }

        @FXML
    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        numeField.clear();
        prenumeField.clear();
        ocupatieField.clear();
        vitezaField.clear();
        rezistentaField.clear();
        //friend1Field.clear();
        //friend2Field.clear();

        usernameField.requestFocus();
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

