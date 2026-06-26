package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private final String id;
    private final String chatId;
    private final String senderId;
    private final String content;
    private final LocalDateTime createdAt;

    private boolean reported;

    public Message(
            String id,
            String chatId,
            String senderId,
            String content) {

        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.reported = false;
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    @Override
    public String toString() {

        String date = createdAt.format(
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm"
                )
        );

        return "Message{id='"
                + id
                + "', chatId='"
                + chatId
                + "', senderId='"
                + senderId
                + "', content='"
                + content
                + "', date="
                + date
                + "}";
    }
}