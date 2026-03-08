package org.example.lab6perfect.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;
import org.example.lab6perfect.service.UserService;

import java.util.Set;

public class CommunitiesController {

    @FXML private TextArea communityArea;
    @FXML private Label statusLabel;

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setLoggedInUser(User user) {
    }

    @FXML
    private void showCommunities() {
        try {
            int numCommunities = userService.getNumberOfCommunities();
            Set<User> biggestCommunity = userService.getBiggestCommunity();

            StringBuilder sb = new StringBuilder();
            sb.append("Numar comunitati: ").append(numCommunities).append("\n");
            sb.append("Cea mai mare comunitate: ")
                    .append(biggestCommunity.size()).append(" membri\n\n");
            sb.append("--- MEMBRI ---\n");

            for (User user : biggestCommunity) {
                sb.append("- ").append(user.getUsername());

                if (user instanceof Persoana) sb.append(" (Persoana)");
                else if (user instanceof FlyingDuck) sb.append(" (Rata zburatoare)");
                else if (user instanceof SwimmingDuck) sb.append(" (Rata inotatoare)");

                sb.append("\n");
            }

            communityArea.setText(sb.toString());
            showInfo("Comunitati afisate: " + numCommunities);

        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }

    private void showInfo(String msg) {
        statusLabel.setText("i " + msg);
        statusLabel.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
    }

    private void showError(String msg) {
        statusLabel.setText("✗ " + msg);
        statusLabel.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
    }


}

