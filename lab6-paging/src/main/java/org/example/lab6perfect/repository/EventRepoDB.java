package org.example.lab6perfect.repository;

import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.User;
import org.example.lab6perfect.domain.duck.Duck;
import org.example.lab6perfect.domain.event.RaceEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EventRepoDB {
    private Properties properties;
    private UserRepoDB userRepoDB;

    public EventRepoDB(Properties properties, UserRepoDB userRepoDB) {
        this.properties = properties;
        this.userRepoDB = userRepoDB;
        DatabaseConnection.setProperties(properties);
    }

    public void addEvent(RaceEvent event) throws SQLException {
        String sql = "INSERT INTO events (name, distante, lanes) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, event.getNumeEveniment());
            Array distArray = createDoubleArray(connection, event.getDistante());
            ps.setArray(2, distArray);
            ps.setInt(3, event.getM());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Long eventId = rs.getLong(1);
                addEventParticipants(eventId, event.getSubscribers());
            }
        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea evenimentului: " + e.getMessage());
        }
    }

    private Array createDoubleArray(Connection connection, double[] values) throws SQLException {
        Double[] objectArray = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            objectArray[i] = values[i];
        }
        return connection.createArrayOf("double", objectArray);
    }

    private void addEventParticipants(Long eventId, List<User> participants) {
        String sql = "INSERT INTO event_participants (event_id, user_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (User user : participants) {
                pstmt.setLong(1, eventId);
                pstmt.setLong(2, user.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea participantilor: " + e.getMessage());
        }
    }

    public List<RaceEvent> getAllEvents() {
        List<RaceEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM events";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");

                // Prelucram array-ul de distante
                Array distanteArray = rs.getArray("distante");
                Double[] distanteDouble = (Double[]) distanteArray.getArray();
                double[] distante = Arrays.stream(distanteDouble).mapToDouble(Double::doubleValue).toArray();

                int lanes = rs.getInt("lanes");

                RaceEvent event = new RaceEvent(id, name, distante, lanes);
                loadEventParticipants(event, id);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Eroare la obtinerea evenimentelor: " + e.getMessage());
        }
        return events;
    }

    private void loadEventParticipants(RaceEvent event, Long eventId) {
        String sql = "SELECT user_id FROM event_participants WHERE event_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                userRepoDB.findById(userId).ifPresent(user -> {
                    event.subscribe(user);
                });
            }
        } catch (SQLException e) {
            System.err.println("Eroare la incarcarea participantilor: " + e.getMessage());
        }
    }

    public void deleteEvent(Long eventId) {
        String sql = "DELETE FROM events WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, eventId);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted < 0) {
                System.out.println("Nu s-a găsit eveniment cu ID: " + eventId);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la ștergerea evenimentului: " + e.getMessage());
        }
    }
public void saveRaceResults(Long eventId, double timpCurent, Duck[] participanti) {
    if (participanti == null) {
        System.out.println("Nu există participanți pentru a salva rezultate!");
        return;
    }

    try (Connection connection = DatabaseConnection.getConnection()) {
        String deleteSql = "DELETE FROM race_results WHERE event_id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setLong(1, eventId);
            deleteStmt.executeUpdate();
        }

        String insertSql = "INSERT INTO race_results (event_id, user_id, lane, finish_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (int i = 0; i < participanti.length; i++) {
                if (participanti[i] != null) {
                    pstmt.setLong(1, eventId);
                    pstmt.setLong(2, participanti[i].getId());
                    pstmt.setInt(3, i + 1); // lane
                    pstmt.setDouble(4, timpCurent);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }

    } catch (SQLException e) {
        System.err.println("Eroare la salvarea rezultatelor: " + e.getMessage());
    }
    }

    private void addEventParticipants(Long eventId, List<User> participants, Connection connection) throws SQLException {
        String sql = "INSERT INTO event_participants (event_id, user_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (User user : participants) {
                pstmt.setLong(1, eventId);
                pstmt.setLong(2, user.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void deleteEventParticipants(Long eventId, Connection connection) throws SQLException {
        String sql = "DELETE FROM event_participants WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, eventId);
            pstmt.executeUpdate();
        }
    }

    public void updateEvent(RaceEvent event) {
        String updateEventSql = "UPDATE events SET name = ?, distante = ?, lanes = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Începe tranzacția
            connection.setAutoCommit(false);

            try {
                // Șterge participanții existenți
                deleteEventParticipants(event.getId(), connection);

                // Actualizează detalii eveniment
                try (PreparedStatement updateStmt = connection.prepareStatement(updateEventSql)) {
                    updateStmt.setString(1, event.getNumeEveniment());
                    Array distArray = createDoubleArray(connection, event.getDistante());
                    updateStmt.setArray(2, distArray);
                    updateStmt.setInt(3, event.getM());
                    updateStmt.setLong(4, event.getId());
                    updateStmt.executeUpdate();
                }

                // Adaugă participanții noi (folosește aceeași conexiune!)
                addEventParticipants(event.getId(), event.getSubscribers(), connection);

                // Comită tranzacția
                connection.commit();

            } catch (SQLException e) {
                // Rollback în caz de eroare
                connection.rollback();
                System.err.println("Eroare la actualizarea evenimentului: " + e.getMessage());
                throw new RuntimeException("Error updating event: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la conexiunea bazei de date: " + e.getMessage());
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
    }


//    public void updateEvent(RaceEvent event) {
//        String deleteParticipantsSql = "DELETE FROM event_participants WHERE event_id = ?";
//        String updateEventSql = "UPDATE events SET name = ?, distante = ?, lanes = ? WHERE id = ?";
//
//        try (Connection connection = DatabaseConnection.getConnection()) {
//            // Începe tranzacția
//            connection.setAutoCommit(false);
//
//            try {
//                // Șterge participanții existenți
//                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteParticipantsSql)) {
//                    deleteStmt.setLong(1, event.getId());
//                    deleteStmt.executeUpdate();
//                }
//
//                // Actualizează detalii eveniment
//                try (PreparedStatement updateStmt = connection.prepareStatement(updateEventSql)) {
//                    updateStmt.setString(1, event.getNumeEveniment());
//                    Array distArray = createDoubleArray(connection, event.getDistante());
//                    updateStmt.setArray(2, distArray);
//                    updateStmt.setInt(3, event.getM());
//                    updateStmt.setLong(4, event.getId());
//                    updateStmt.executeUpdate();
//                }
//
//                // Adaugă participanții noi
//                addEventParticipants(event.getId(), event.getSubscribers());
//
//                // Comită tranzacția
//                connection.commit();
//
//            } catch (SQLException e) {
//                connection.rollback();
//                throw e;
//            } finally {
//                connection.setAutoCommit(true);
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Eroare la actualizarea evenimentului: " + e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }

    public void updateWinnerTime(Long eventId, double winnerTime) {
        String sql = "UPDATE events SET winner_time = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setDouble(1, winnerTime);
            pstmt.setLong(2, eventId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Eroare la actualizarea timpului câștigător: " + e.getMessage());
        }
    }

    public RaceEvent findEventById(Long eventId) {
        String sql = "SELECT * FROM events WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");

                Array distanteArray = rs.getArray("distante");
                Double[] distanteDouble = (Double[]) distanteArray.getArray();
                double[] distante = Arrays.stream(distanteDouble).mapToDouble(Double::doubleValue).toArray();

                int lanes = rs.getInt("lanes");

                RaceEvent event = new RaceEvent(id, name, distante, lanes);
                loadEventParticipants(event, id);
                return event;
            }

        } catch (SQLException e) {
            System.err.println("Eroare la găsirea evenimentului: " + e.getMessage());
        }

        return null;
    }
}