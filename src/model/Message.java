package model;

import java.time.LocalDateTime;

public class Message {

    private String senderId;
    private String text;

    private LocalDateTime sendTime;

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
        this.sendTime = LocalDateTime.now();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }
}