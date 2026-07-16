package model;

import java.time.LocalDateTime;

public class BlockUser {

    private final String userId;
    private final String blockedUserId;
    private final LocalDateTime blockedAt;

    public BlockUser(String userId, String blockedUserId){
        this(
                userId,
                blockedUserId,
                LocalDateTime.now()
        );
    }

    public BlockUser(
            String userId,
            String blockedUserId,
            LocalDateTime blockedAt
    ) {
        this.userId = userId;
        this.blockedUserId = blockedUserId;
        this.blockedAt = blockedAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getBlockedUserId() {
        return blockedUserId;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    @Override
    public String toString() {
        return "BlockedUser{userId='"
                + userId
                + "', blockedUserId='"
                + blockedUserId
                + "', blockedAt="
                + blockedAt
                + "}";
    }
}