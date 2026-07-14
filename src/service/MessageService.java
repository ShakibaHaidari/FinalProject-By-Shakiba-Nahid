package service;
import model.Message;
//faz 2
import repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageService {
    private static final int MAX_MESSAGE_LENGTH = 500;
    private final List<Message> messages;
    private final GroupService groupService;
    private final MessageRepository messageRepository;

    public MessageService(GroupService groupService) {
        this.groupService = groupService;
        this.messageRepository = new MessageRepository();
        this.messages = new ArrayList<>(messageRepository.loadAll());
        loadReportedMessages();
        System.out.println("Loaded " + messages.size() + " message(s) from messages.txt.");
    }

    public synchronized Message sendMessage(String chatId, String senderId, String content) {
        if (isBlank(chatId) || isBlank(senderId) || isBlank(content)) {
            return null;
         }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            return null;
        }
        Message message = new Message(UUID.randomUUID().toString(), chatId, senderId, content.trim());messages.add(message);
        persistMessages();
        return message;
    }
    public synchronized List<Message> getMessagesByChatId(
            String chatId) {
        List<Message> result = new ArrayList<>();
        for (Message message : messages) {
            if (message.getChatId().equalsIgnoreCase(chatId)) {

                result.add(message);
              }
        }
        return result;
        }
    public synchronized Message getMessageById(
            String messageId) {
        if (messageId == null) {
            return null;
           }
        for (Message message : messages) {
            if (message.getId().equalsIgnoreCase(messageId)) {
                return message;
            }
         }
        return null;
      }
    public synchronized boolean reportMessage(String messageId) {
        Message message = getMessageById(messageId);
        if (message == null) {
            return false;
         }if (message.isReported()) {
            return false;
           }
        groupService.reportMessage(message);

        persistMessages();
        return true;
    }
//faz 2 changes
    private void loadReportedMessages() {

        for (Message message : messages) {

            if (message.isReported()) {
                groupService.reportMessage(message);
              }
        }
    }
    private void persistMessages() {
        messageRepository.saveAll(messages);
    }
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
     }
}