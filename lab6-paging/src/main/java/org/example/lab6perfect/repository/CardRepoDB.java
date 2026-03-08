package org.example.lab6perfect.repository;

import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.card.Card;
import org.example.lab6perfect.domain.card.DuckCard;
import org.example.lab6perfect.domain.card.FlyingCard;
import org.example.lab6perfect.domain.card.SwimmingCard;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class CardRepoDB {
    private Properties properties;
    private UserRepoDB userRepo;

    public CardRepoDB(Properties properties, UserRepoDB userRepo) {
        this.properties = properties;
        this.userRepo = userRepo;
    }

    public void addCard(Card<? extends Duck> card) {
        String sql = "INSERT INTO cards (name, card_type) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, card.getNumeCard());
            pstmt.setString(2, getCardType(card));
            pstmt.executeUpdate();
            System.out.println("Card '" + card.getNumeCard() + "' salvat in baza de date");

        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea cardului: " + e.getMessage());
        }
    }

    public void addDuckToCard(String cardName, Duck duck) {
        String sql = "INSERT INTO card_members (card_id, user_id) VALUES ((SELECT id FROM cards WHERE name = ?), ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, cardName);
            pstmt.setLong(2, duck.getId());
            pstmt.executeUpdate();
            System.out.println("Rata '" + duck.getUsername() + " adaugata in cardul " + cardName);

        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea ratei in card: " + e.getMessage());
        }
    }

    public Optional<Card<? extends Duck>> getCardByName(String name) {
        String sql = "SELECT * FROM cards WHERE name = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String cardType = rs.getString("card_type");
                String cardName = rs.getString("name");
                Long cardId = rs.getLong("id");

                Card<? extends Duck> card = createCardByType(cardType, cardName);
                loadDucksIntoCard(card, cardId);
                return Optional.of(card);
            }
        } catch (SQLException e) {
            System.err.println("Eroare la cautarea cardului: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Card<? extends Duck>> listAll() {
        List<Card<? extends Duck>> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String cardType = rs.getString("card_type");
                String cardName = rs.getString("name");
                Long cardId = rs.getLong("id");

                Card<? extends Duck> card = createCardByType(cardType, cardName);
                loadDucksIntoCard(card, cardId);
                cards.add(card);
            }
        } catch (SQLException e) {
            System.err.println("Eroare la obtinerea cardurilor: " + e.getMessage());
        }
        return cards;
    }

    public void removeCard(String cardName) {
        String sql = "DELETE FROM cards WHERE name = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, cardName);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Card '" + cardName + "' șters");
            } else {
                System.out.println("Card '" + cardName + "' nu există");
            }

        } catch (SQLException e) {
            System.err.println("Eroare la stergerea cardului: " + e.getMessage());
        }
    }

    public void removeDuckFromCard(String cardName, Duck duck) {
        String sql = "DELETE FROM card_members WHERE card_id = (SELECT id FROM cards WHERE name = ?) AND user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, cardName);
            pstmt.setLong(2, duck.getId());
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Rata '" + duck.getUsername() + " eliminata din cardul " + cardName );
            }

        } catch (SQLException e) {
            System.err.println("Eroare la eliminarea ratei din card: " + e.getMessage());
        }
    }

    private void loadDucksIntoCard(Card<? extends Duck> card, Long cardId) {
        String sql = "SELECT user_id FROM card_members WHERE card_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, cardId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Long duckId = rs.getLong("user_id");
                userRepo.findById(duckId).ifPresent(user -> {
                    if (user instanceof Duck duck) {
                        addDuckToCardObject(card, duck);
                    }
                });
            }
        } catch (SQLException e) {
            System.err.println("Eroare la incarcarea ratelor: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addDuckToCardObject(Card<? extends Duck> card, Duck duck) {
        try {
            if (card instanceof DuckCard) {
                ((DuckCard) card).addDuck(duck);
            } else if (card instanceof FlyingCard && duck instanceof FlyingDuck) {
                ((FlyingCard) card).addDuck((FlyingDuck) duck);
            } else if (card instanceof SwimmingCard && duck instanceof SwimmingDuck) {
                ((SwimmingCard) card).addDuck((SwimmingDuck) duck);
            }
        } catch (Exception e) {
            System.err.println("Eroare la adaugarea ratei in card: " + e.getMessage());
        }
    }

    private String getCardType(Card<? extends Duck> card) {
        if (card instanceof FlyingCard) return "FLYING";
        if (card instanceof SwimmingCard) return "SWIMMING";
        return "DUCK";
    }

    private Card<? extends Duck> createCardByType(String cardType, String cardName) {
        switch (cardType) {
            case "FLYING": return new FlyingCard(cardName);
            case "SWIMMING": return new SwimmingCard(cardName);
            default: return new DuckCard(cardName);
        }
    }

    public boolean cardExists(String cardName) {
        return getCardByName(cardName).isPresent();
    }

    public int getMemberCount(String cardName) {
        String sql = "SELECT COUNT(*) as count FROM card_members WHERE card_id = (SELECT id FROM cards WHERE name = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, cardName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Eroare la numararea membrilor: " + e.getMessage());
        }
        return 0;
    }

}