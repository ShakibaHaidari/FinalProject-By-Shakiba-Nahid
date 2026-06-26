package service;

import model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageService {

    private static final int MAX_MESSAGE_LENGTH = 500;

    private final List<Message> messages =
            new ArrayList<>();

    private final GroupService groupService;

    public MessageService(GroupService groupService) {
        this.groupService = groupService;
    }

    public synchronized Message sendMessage(
            String chatId,
            String senderId,
            String content) {

        if (isBlank(chatId)
                || isBlank(senderId)
                || isBlank(content)) {

            return null;
        }

        if (content.length() > MAX_MESSAGE_LENGTH) {
            return null;
        }

        Message message = new Message(
                UUID.randomUUID().toString(),
                chatId,
                senderId,
                content.trim()
        );

        messages.add(message);

        return message;
    }

    public synchronized List<Message> getMessagesByChatId(
            String chatId) {

        List<Message> result = new ArrayList<>();

        for (Message message : messages) {

            if (message.getChatId()
                    .equalsIgnoreCase(chatId)) {

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

            if (message.getId()
                    .equalsIgnoreCase(messageId)) {

                return message;
            }
        }

        return null;
    }

    public synchronized boolean reportMessage(
            String messageId) {

        Message message = getMessageById(messageId);

        if (message == null) {
            return false;
        }

        if (message.isReported()) {
            return false;
        }

        groupService.reportMessage(message);

        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}