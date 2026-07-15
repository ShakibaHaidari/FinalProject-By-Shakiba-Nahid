package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private final String id;
    private final String chatId;
    private final String senderId;

    private String content;

    private final LocalDateTime createdAt;

    private boolean reported;
    private boolean edited;
    private boolean deleted;

    private String previousContent;

    private LocalDateTime editedAt;
    private LocalDateTime deletedAt;

    public Message(
            String id,
            String chatId,
            String senderId,
            String content
    ) {
        this(
                id,
                chatId,
                senderId,
                content,
                LocalDateTime.now(),
                false,
                false,
                false,
                "",
                null,
                null
        );
    }

    public Message(
            String id,
            String chatId,
            String senderId,
            String content,
            LocalDateTime createdAt,
            boolean reported
    ) {
        this(
                id,
                chatId,
                senderId,
                content,
                createdAt,
                reported,
                false,
                false,
                "",
                null,
                null
        );
    }

    public Message(
            String id,
            String chatId,
            String senderId,
            String content,
            LocalDateTime createdAt,
            boolean reported,
            boolean edited,
            boolean deleted,
            String previousContent,
            LocalDateTime editedAt,
            LocalDateTime deletedAt
    ) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.reported = reported;
        this.edited = edited;
        this.deleted = deleted;
        this.previousContent = previousContent;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
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

    public boolean isEdited(){
        return edited;
    }
    public boolean isDeleted(){
        return deleted;
    }
    public String getPreviousContent(){
        return previousContent;
    }

    public LocalDateTime getEditedAt(){
        return editedAt;
      }
    public LocalDateTime getDeletedAt(){
        return deletedAt;
      }

    public void setReported(boolean reported){
        this.reported =reported;
    }
    public boolean editContent(String newContent){
        if(deleted){
            return false;
             }

        if(newContent == null || newContent.isBlank()){
            return false;
        }

        previousContent = content;
        content = newContent.trim();
        edited = true;
        editedAt = LocalDateTime.now();

        return true;
    }
    public boolean deleteMessage(){
        if(deleted){
            return false;
          }
        deleted = true;
        deletedAt = LocalDateTime.now();
        return true;
       }
    public String getVisibleContent(){

        if(deleted){
            return "deleted message";
         }
        return content;
      }
    @Override
    public String toString(){
        String date = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return "Message{id="+ id +", chatId=" + chatId + ", senderId=" + senderId + ", content="+ getVisibleContent() +", date=" + date + ", reported=" + reported + ", edited=" + edited + ", deleted=" + deleted + "}";
    }
}