package model;

 import java.time.LocalDateTime;
 public class SavedMessage {
    private final String userId;
    private final String messageId;
    private final LocalDateTime savedAt;
    public SavedMessage(String userId, String messageId, LocalDateTime savedAt) {
         this.userId = userId;
        this.messageId = messageId;
        this.savedAt = savedAt;
      }
    public SavedMessage(String userId, String messageId) {
          this(userId, messageId, LocalDateTime.now());
     }

    public String getUserId() {
        return userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }
    @Override
    public String toString() {
        return "SavedMessage{userId='" + userId + "', messageId='" + messageId + "', savedAt=" + savedAt + "}";
    }
}