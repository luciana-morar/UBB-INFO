package org.example.lab6perfect.repository;

import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.Persoana;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.duck.FlyingDuck;
import org.example.lab6perfect.domain.duck.SwimmingDuck;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class UserRepoDB {
    private Properties properties;

    public UserRepoDB(Properties properties) {
        this.properties = properties;
        DatabaseConnection.setProperties(properties);
    }

    public void addUser(User user) throws SQLException {
        if (user instanceof Persoana){
            addPersoana((Persoana)user);
        }else if (user instanceof Duck) {
            addDuck((Duck)user);
        }
    }

    private void addPersoana(Persoana persoana) throws SQLException {
        String sql ="INSERT INTO users (username,email,password,nume, prenume,ocupatie,data_nasterii,nivel_empatie)"+
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection=DatabaseConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, persoana.getUsername());
            ps.setString(2, persoana.getEmail());
            ps.setString(3, persoana.getPassword());
            ps.setString(4, persoana.getNume());
            ps.setString(5, persoana.getPrenume());
            ps.setString(6, persoana.getOcupatie());
            ps.setDate(7, Date.valueOf(persoana.getDataNasterii()));
            ps.setInt(8, persoana.getNivelEmpatie());

            ps.executeUpdate();
        }catch (SQLException e){
            System.err.println("Eroare la adăugarea persoanei: " + e.getMessage());
        }
    }

    private void addDuck(Duck duck){
        String sql = "INSERT INTO users (username,email,password,type,viteza,rezistenta)" + "VALUES (?,?,?,?,?,?)";
        try (Connection connection =DatabaseConnection.getConnection();
            PreparedStatement ps =connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,duck.getUsername());
                ps.setString(2,duck.getEmail());
                ps.setString(3,duck.getPassword());
                String duckType=(duck instanceof FlyingDuck)? "FLYING_DUCK" : "SWIMMING_DUCK" ;
                ps.setString(4,duckType);
                ps.setDouble(5,duck.getViteza());
                ps.setDouble(6,duck.getRezistenta());

                ps.executeUpdate();

        }catch (SQLException e){
            System.err.println("Eroare la adăugarea ratei: " + e.getMessage());
        }
    }

    public void removeUser(User user) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();


        } catch (SQLException e) {
            System.err.println("Eroare la stergerea utilizatorului: " + e.getMessage());
        }
    }
    public List<User> getAllUsers() {
        return listAllUsers();
    }

    public List<User> listAllUsers(){
        List <User> users = new ArrayList<>();
        String sql ="SELECT * FROM users";

        try(Connection connection = DatabaseConnection.getConnection();
        Statement st=connection.createStatement();
        ResultSet rs = st.executeQuery(sql)){
            while(rs.next()) {
                User user = mapResultSetToUser(rs);
                if(user !=null) {
                users.add(user);
                }
                }
        }catch (SQLException e){
            System.err.println("Eroare la obținerea utilizatorilor: " + e.getMessage());
        }
        return users;
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.ofNullable(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Eroare la căutarea utilizatorului: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.ofNullable(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Eroare la căutarea utilizatorului: " + e.getMessage());
        }
        return Optional.empty();
    }
    User mapResultSetToUser(ResultSet rs) throws SQLException {
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



    public void afiseazaToatePersoanele() {
        String sql = "SELECT * FROM users WHERE type IS NULL";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Nume: " + rs.getString("nume") + " " + rs.getString("prenume"));
                System.out.println("Ocupatie: " + rs.getString("ocupatie"));
                System.out.println("Data nasterii: " + rs.getDate("data_nasterii"));
                System.out.println("Nivel empatie: " + rs.getInt("nivel_empatie"));
                System.out.println("---------------");
            }
        } catch (SQLException e) {
            System.err.println("Eroare la afișarea persoanelor: " + e.getMessage());
        }
    }

    public void afiseazaToateRatele() {
        String sql = "SELECT * FROM users WHERE type IS NOT NULL";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Tip: " + rs.getString("type"));
                System.out.println("Viteza: " + rs.getDouble("viteza"));
                System.out.println("Rezistenta: " + rs.getDouble("rezistenta"));
                System.out.println("---------------");
            }
        } catch (SQLException e) {
            System.err.println("Eroare la afișarea ratelor: " + e.getMessage());
        }
    }

    //paginare
    public List<Duck> listDuckPaged(int pageIndex, int pageSize) {
        List<Duck> ducks = new ArrayList<>();

        String sql = "SELECT * FROM users WHERE type IS NOT NULL ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, pageIndex * pageSize);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = mapResultSetToUser(rs);
                if (u instanceof Duck)
                    ducks.add((Duck) u);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la listDuckPaged: " + e.getMessage());
        }
        return ducks;
    }

    public List<Persoana> listPeoplePaged(int pageIndex, int pageSize) {
        List<Persoana> persons = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE type IS NULL ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, pageIndex * pageSize);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = mapResultSetToUser(rs);
                if (u instanceof Persoana)
                    persons.add((Persoana) u);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la listPeoplePaged: " + e.getMessage());
        }
        return persons;
    }

    public List<Duck> listDucksByTypePaged(String type, int pageIndex, int pageSize) {
        List<Duck> ducks = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE type = ? ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type + "_DUCK");            // "FLYING" -> "FLYING_DUCK"
            ps.setInt(2, pageSize);
            ps.setInt(3, pageIndex * pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = mapResultSetToUser(rs);
                if (u instanceof Duck) {
                    ducks.add((Duck) u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la listDucksByTypePaged: " + e.getMessage());
        }

        return ducks;
    }

    public int countDucks() {
        String sql = "SELECT COUNT(*) FROM users WHERE type IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next())
                return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Eroare la countDucks: " + e.getMessage());
        }
        return 0;
    }

    public int countPeople(){
        String sql = "SELECT COUNT(*) FROM users WHERE type IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Eroare la countPeople: " + e.getMessage());
        }
        return 0;
    }

}
