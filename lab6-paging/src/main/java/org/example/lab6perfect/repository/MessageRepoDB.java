package org.example.lab6perfect.repository;

import org.example.lab6perfect.database.DatabaseConnection;
import org.example.lab6perfect.domain.Message;
import org.example.lab6perfect.domain.ReplyMessage;
import org.example.lab6perfect.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MessageRepoDB {
    private Properties properties;
    private UserRepoDB userRepoDB;

    public MessageRepoDB(Properties properties, UserRepoDB userRepoDB) {
        this.properties = properties;
        this.userRepoDB = userRepoDB;
        DatabaseConnection.setProperties(properties);
    }

    public Long addMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content,reply_to_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {

            if (message.getSender() != null) {
                pstmt.setLong(1, message.getSender().getId());
            } else {
                pstmt.setNull(1, Types.BIGINT);
            }

            Long firstReceiverId = null;
            if (message.getReceiver() != null && !message.getReceiver().isEmpty()) {
                firstReceiverId = message.getReceiver().get(0).getId();
                pstmt.setLong(2, firstReceiverId);
            } else {
                pstmt.setNull(2, Types.BIGINT);

            }
            pstmt.setString(3, message.getContent());

            if (message instanceof ReplyMessage reply) {
                pstmt.setLong(4, reply.getOriginalMessageId());
            } else {
                pstmt.setNull(4, Types.BIGINT);
            }
            pstmt.executeUpdate();


            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                Long messageId = rs.getLong(1);
                message.setId(messageId);

                if (message.getReceiver() != null) {
                    addMessageRecipients(messageId, message.getReceiver());
                }
                return messageId;
            }

        } catch (SQLException e) {
            System.err.println("Eroare la adaugarea mesajului: " + e.getMessage());
        }
        return null;
    }

    public void addMessageRecipients(Long messageId, List<User> receivers) {
        String sql = "INSERT INTO message_recipients (message_id, user_id) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (User user : receivers) {
                pstmt.setLong(1, messageId);
                pstmt.setLong(2, user.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Eroare !!!" + e.getMessage());
        }

    }

    public List<Message> getMessagesForUser(Long userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.* FROM messages m " +
                "WHERE m.id IN (SELECT mr.message_id FROM message_recipients mr WHERE mr.user_id = ?) " +
                "   OR m.sender_id = ? " +
                "ORDER BY m.sent_at ASC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message message = mapResultSetToMessage(rs);
                if (message != null) {
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la obtinerea mesajelor: " + e.getMessage());
        }
        return messages;
    }

    public List<Message> getConversation(Long user1Id, Long user2Id) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.* FROM messages m " +
                "WHERE m.id IN (SELECT mr.message_id FROM message_recipients mr " +
                "               WHERE mr.user_id IN (?, ?)) " +
                "AND (m.sender_id = ? OR m.sender_id = ?) " +
                "ORDER BY m.sent_at ASC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, user1Id);
            pstmt.setLong(2, user2Id);
            pstmt.setLong(3, user1Id);
            pstmt.setLong(4, user2Id);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = mapResultSetToMessage(rs);
                if (message != null) {
                    messages.add(message);
                }
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Long id=rs.getLong("id");
        Long senderId = rs.getLong("sender_id");
        Long receiverId = rs.getLong("receiver_id");
        String content = rs.getString("content");
        Long reply_to_id = rs.getLong("reply_to_id");
        Timestamp sent_at = rs.getTimestamp("sent_at");

        User sender=null;
        if (senderId >0){
            sender=userRepoDB.findById(senderId).orElse(null);
        }
        List<User> receivers=getMessageRecipients(id);

        if (receiverId > 0) {
            userRepoDB.findById(receiverId).ifPresent(receiver -> {
                if (!receivers.contains(receiver)) {
                    receivers.add(receiver);
                }
            });
        }

        LocalDateTime timestamp = sent_at != null ? sent_at.toLocalDateTime() : LocalDateTime.now();

        if(reply_to_id !=null && reply_to_id >0){
            return new ReplyMessage(id,sender,receivers,content,timestamp,reply_to_id);
        }else{
            return new Message(id,sender,receivers,content,timestamp);
        }
    }

    private List<User> getMessageRecipients(Long messageId){
        List<User> recipients = new ArrayList<>();
        String sql ="SELECT user_id FROM message_recipients mr WHERE mr.message_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1,messageId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                userRepoDB.findById(userId).ifPresent(recipients::add);
            }

        } catch (SQLException e) {
            System.err.println("Eroare la obtinerea destinatarilor: " + e.getMessage());
        }
        return  recipients;
    }

    public Message getMessageById(Long messageId){
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1,messageId);

            pstmt.setLong(1, messageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            System.err.println("Eroare la obtinerea mesajului: " + e.getMessage());
        }
        return null;
    }

}

