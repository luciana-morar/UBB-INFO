package org.example.lab6perfect.repository;

import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.FriendRequest;
import org.example.lab6perfect.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class FriendRequestRepoDB {
    private final UserRepoDB userRepo;
    private final Properties properties;

    public FriendRequestRepoDB(UserRepoDB userRepo, Properties properties) {
        this.userRepo = userRepo;
        this.properties = properties;
    }

    // Înlocuiește metoda statică cu una non-statică
    public Long addFriendRequest(FriendRequest request) {
        String sql = "INSERT INTO friend_requests (sender_id, receiver_id, status, created_at, updated_at)" +
                " VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, request.getSender().getId());
            ps.setLong(2, request.getReceiver().getId());
            ps.setString(3, request.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(request.getCreatedAt()));
            ps.setTimestamp(5, Timestamp.valueOf(request.getUpdatedAt()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            System.err.println("Error adding friend request: " + e.getMessage());
        }
        return null;
    }

    public void updateFriendRequest(FriendRequest request) {
        String sql = """
        UPDATE friend_requests
        SET status = ?, updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getStatus().name());
            ps.setLong(2, request.getId());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("FriendRequest with id " + request.getId() + " was not found");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update FriendRequest " + request.getId(), e);
        }
    }


    private void debugWhyNoUpdate(Connection conn, Long requestId) throws SQLException {

        String checkSql = "SELECT id, status, sender_id, receiver_id FROM friend_requests WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setLong(1, requestId);
            ps.executeQuery();

        }

    }

    private void verifyUpdate(Connection conn, Long requestId) throws SQLException {
        String sql = "SELECT status, updated_at FROM friend_requests WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            ps.executeQuery();

        }
    }

    public void removeFriendRequest(Long id){
        String sql=" DELETE FROM friend_requests WHERE id=?";
        try(Connection connection =DatabaseConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }catch (SQLException e){
            System.out.println(e);
        }
    }

    public List<FriendRequest> getPendingRequestForUser(Long userId){
        List<FriendRequest> requests  = new ArrayList<>();

        String sql ="SELECT * FROM friend_requests WHERE receiver_id = ? AND status ='PENDING'ORDER BY created_at DESC";
        try(Connection connection = DatabaseConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                requests.add(extractFriendRequest(rs));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return requests;
    }

    private FriendRequest extractFriendRequest(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String senderId = rs.getString("sender_id");
        String receiverId = rs.getString("receiver_id");
        String statusS = rs.getString("status");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();

        User sender = userRepo.findById(Long.valueOf(senderId)).orElse(null);
        User receiver = userRepo.findById(Long.valueOf(receiverId)).orElse(null);
        FriendRequest.Status status = FriendRequest.Status.valueOf(statusS);
        return new FriendRequest(id,sender,receiver,status,createdAt,updatedAt);
    }

    public List<FriendRequest> getSentRequestsByUser(Long userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests WHERE sender_id = ? ORDER BY created_at DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                requests.add(extractFriendRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return requests;
    }

    public Optional<FriendRequest> findRequestBetweenUsers(Long user1Id, Long user2Id) {
        String sql = "SELECT * FROM friend_requests WHERE " +
                "(sender_id = ? AND receiver_id = ?) OR " +
                "(sender_id = ? AND receiver_id = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, user1Id);
            ps.setLong(2, user2Id);
            ps.setLong(3, user2Id);
            ps.setLong(4, user1Id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(extractFriendRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return Optional.empty();
    }

    public List<FriendRequest> getAllRequests() {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests ORDER BY created_at DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                requests.add(extractFriendRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return requests;
    }
}
