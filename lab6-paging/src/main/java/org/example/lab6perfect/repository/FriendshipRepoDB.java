package org.example.lab6perfect.repository;

import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.Friendship;
import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FriendshipRepoDB {
    private Properties properties;
    private UserRepoDB userRepoDB;

    public FriendshipRepoDB(Properties properties, UserRepoDB userRepoDB) {
        this.properties = properties;
        this.userRepoDB = userRepoDB;
        DatabaseConnection.setProperties(properties);
    }

    public void addFriendship(Friendship friendship) {
        Long user1Id = Math.min(friendship.getUser1().getId(), friendship.getUser2().getId());
        Long user2Id = Math.max(friendship.getUser1().getId(), friendship.getUser2().getId());

        String sql = "INSERT INTO friendships (user1_id, user2_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, friendship.getUser1().getId());
            pstmt.setLong(2, friendship.getUser2().getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea prieteniei: " + e.getMessage());
        }
    }

    public void removeFriendship(User u1, User u2) {
        String sql = "DELETE FROM friendships WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, u1.getId());
            pstmt.setLong(2, u2.getId());
            pstmt.setLong(3, u2.getId());
            pstmt.setLong(4, u1.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Eroare la ștergerea prieteniei: " + e.getMessage());
        }
    }

    public List<Friendship> listAll() {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT f.*, u1.username as u1_username, u2.username as u2_username " +
                "FROM friendships f " +
                "JOIN users u1 ON f.user1_id = u1.id " +
                "JOIN users u2 ON f.user2_id = u2.id";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Optional<User> u1 = userRepoDB.findByUsername(rs.getString("u1_username"));
                Optional<User> u2 = userRepoDB.findByUsername(rs.getString("u2_username"));

                if (u1.isPresent() && u2.isPresent()) {
                    friendships.add(new Friendship(u1.get(), u2.get()));
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la obținerea prieteniilor: " + e.getMessage());
        }
        return friendships;
    }



    public boolean exists(User u1, User u2) {
        String sql = "SELECT 1 FROM friendships WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, u1.getId());
            pstmt.setLong(2, u2.getId());
            pstmt.setLong(3, u2.getId());
            pstmt.setLong(4, u1.getId());

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Eroare la verificarea prieteniei: " + e.getMessage());
            return false;
        }
    }

    public List<User> getFriendsOf(User user) {
        Long userId = user.getId();

        return listAll().stream()
                .filter(f ->
                        f.getUser1().getId().equals(userId) ||
                                f.getUser2().getId().equals(userId))
                .map(f ->
                        f.getUser1().getId().equals(userId)
                                ? f.getUser2()
                                : f.getUser1())
                .collect(Collectors.toList());
    }

//    public List<User> getFriendsOf(User user) {
//        return listAll().stream()
//                .filter(f -> f.getUser1().equals(user) || f.getUser2().equals(user))
//                .map(f -> f.getUser1().equals(user) ? f.getUser2() : f.getUser1())
//                .collect(Collectors.toList());
//    }


    public List<User> getFriendsForUser(Long userId) {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON (u.id = f.user1_id OR u.id = f.user2_id) " +
                "WHERE (f.user1_id = ? OR f.user2_id = ?) AND u.id != ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            pstmt.setLong(3, userId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User friend = mapResultSetToUser(rs); // Ai nevoie de această metodă
                if (friend != null) {
                    friends.add(friend);
                }
            }

        } catch (SQLException e) {
            System.err.println("Eroare la obținerea prietenilor: " + e.getMessage());
        }
        return friends;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String type = rs.getString("type");

        if (type == null) {
            String nume = rs.getString("nume");
            if (rs.wasNull()) nume = "";

            String prenume = rs.getString("prenume");
            if (rs.wasNull()) prenume = "";

            String ocupatie = rs.getString("ocupatie");
            if (rs.wasNull()) ocupatie = "";

            Date dataNasteriiSql = rs.getDate("data_nasterii");
            LocalDate dataNasterii = (dataNasteriiSql != null) ? dataNasteriiSql.toLocalDate() : LocalDate.now();

            int nivelEmpatie = rs.getInt("nivel_empatie");
            if (rs.wasNull()) nivelEmpatie = 0;

            return new Persoana(id, username, email, password, nume, prenume, ocupatie, dataNasterii, nivelEmpatie);

        } else if ("FLYING_DUCK".equals(type)) {
            double viteza = rs.getDouble("viteza");
            double rezistenta = rs.getDouble("rezistenta");
            return new FlyingDuck(id, username, email, password, viteza, rezistenta);

        } else if ("SWIMMING_DUCK".equals(type)) {
            double viteza = rs.getDouble("viteza");
            double rezistenta = rs.getDouble("rezistenta");
            return new SwimmingDuck(id, username, email, password, viteza, rezistenta);
        }

        return null;
    }

    public UserRepoDB getUserRepo() {
        return userRepoDB;
    }
}
