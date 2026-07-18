package model;

import java.time.LocalDateTime;

public class MessageReaction {

    private final String userId;
    private final String messageId;

    private String reaction;
    private LocalDateTime reactedAt;


    public MessageReaction(
            String userId,
            String messageId,
            String reaction
    ) {

        this(
                userId,
                messageId,
                reaction,
                LocalDateTime.now()
        );
    }


    public MessageReaction(
            String userId,
            String messageId,
            String reaction,
            LocalDateTime reactedAt
    ) {

        this.userId =
                userId;

        this.messageId =
                messageId;

        this.reaction =
                reaction;

        this.reactedAt =
                reactedAt;
    }


    public String getUserId() {

        return userId;
    }


    public String getMessageId() {

        return messageId;
    }


    public String getReaction() {

        return reaction;
    }


    public LocalDateTime getReactedAt() {

        return reactedAt;
    }


    public void setReaction(
            String reaction
    ) {

        this.reaction =
                reaction;

        this.reactedAt =
                LocalDateTime.now();
    }


    @Override
    public String toString() {

        return "MessageReaction{userId='"
                + userId
                + "', messageId='"
                + messageId
                + "', reaction='"
                + reaction
                + "', reactedAt="
                + reactedAt
                + "}";
    }
}