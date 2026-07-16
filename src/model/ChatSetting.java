
package model;
import java.time.LocalDateTime;
public class ChatSetting {
    private final String userId;
    private final String chatId;
    private boolean pinned;
    private boolean archived;
    private LocalDateTime updatedAt;
    public ChatSetting(String userId, String chatId){this(userId,chatId, false, false,LocalDateTime.now());
    }

    public ChatSetting(
            String userId,
            String chatId,
            boolean pinned,
            boolean archived,
            LocalDateTime updatedAt){
        this.userId = userId;
        this.chatId = chatId;
        this.pinned = pinned;
        this.archived = archived;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getChatId() {
        return chatId;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        this.updatedAt = LocalDateTime.now();
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ChatSetting{userId='"
                + userId
                + "', chatId='"
                + chatId
                + "', pinned="
                + pinned
                + ", archived="
                + archived
                + "}";
    }
}