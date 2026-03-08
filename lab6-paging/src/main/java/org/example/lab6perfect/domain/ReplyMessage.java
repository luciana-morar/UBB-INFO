package org.example.lab6perfect.domain;

import java.time.LocalDateTime;
import java.util.List;

public class ReplyMessage extends Message{

    private Long originalMessageId;

    public ReplyMessage(User sender, List<User> receiver, String content, Long originalMessageId) {
        super(sender, receiver, content);
        this.originalMessageId = originalMessageId;
    }

    public ReplyMessage(Long id, User sender, List<User> receivers, String content,
                        LocalDateTime timestamp, Long originalMessageId) {
        super(id, sender, receivers, content, timestamp);
        this.originalMessageId = originalMessageId;
    }

    public Long getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(Long originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    @Override
    public String toString() {
        return "↪ " + getContent() + " (reply to msg #" + originalMessageId + ")";
    }
}
