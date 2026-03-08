package org.example.lab6perfect.repository;



import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class UserRepoDBPaged {
    private Properties properties;
    private UserRepoDB userRepoDB;

    public UserRepoDBPaged(Properties properties, UserRepoDB userRepoDB) {
        this.properties = properties;
        this.userRepoDB = userRepoDB;
        DatabaseConnection.setProperties(properties);
    }

    public List<User> getUsersPage(int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, page * pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare paginare utilizatori: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
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
            LocalDate dataNasterii = (dataNasteriiSql != null) ?
                    dataNasteriiSql.toLocalDate() : LocalDate.now();

            int nivelEmpatie = rs.getInt("nivel_empatie");
            if (rs.wasNull()) nivelEmpatie = 0;

            return new Persoana(id, username, email, password,
                    nume, prenume, ocupatie, dataNasterii, nivelEmpatie);

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

    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Eroare numărare utilizatori: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }


    public int getTotalPages(int pageSize) {
        int totalUsers = getTotalUsers();
        return (int) Math.ceil((double) totalUsers / pageSize);
    }

}