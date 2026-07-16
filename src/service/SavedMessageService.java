package service;

import model.Message;
import model.SavedMessage;
import repository.SavedMessageRepository;

import java.util.ArrayList;
import java.util.List;

public class SavedMessageService {

    private final List<SavedMessage> savedMessages;
    private final SavedMessageRepository savedMessageRepository;
    private final MessageService messageService;

    public SavedMessageService(
            MessageService messageService
    ) {
        this.messageService = messageService;

        this.savedMessageRepository =
                new SavedMessageRepository();

        this.savedMessages =
                new ArrayList<>(
                        savedMessageRepository.loadAll()
                );

        System.out.println(
                "Loaded "
                        + savedMessages.size()
                        + " saved message(s) from saved_messages.txt."
        );
    }

    public synchronized boolean saveMessage(
            String userId,
            String messageId
    ) {
        if (isBlank(userId) || isBlank(messageId)) {
            return false;
        }

        Message message =
                messageService.getMessageById(messageId);

        if (message == null) {
            return false;
        }

        if (isAlreadySaved(userId, messageId)) {
            return false;
        }

        SavedMessage savedMessage =
                new SavedMessage(
                        userId,
                        messageId
                );

        savedMessages.add(savedMessage);

        persistSavedMessages();

        return true;
    }

    public synchronized boolean removeSavedMessage(
            String userId,
            String messageId
    ) {
        SavedMessage found = null;

        for (SavedMessage savedMessage : savedMessages) {

            boolean sameUser =
                    savedMessage
                            .getUserId()
                            .equalsIgnoreCase(userId);

            boolean sameMessage =
                    savedMessage
                            .getMessageId()
                            .equalsIgnoreCase(messageId);

            if (sameUser && sameMessage) {
                found = savedMessage;
                break;
            }
        }

        if (found == null) {
            return false;
        }

        savedMessages.remove(found);

        persistSavedMessages();

        return true;
    }

    public synchronized List<Message> getSavedMessages(
            String userId
    ) {
        List<Message> result = new ArrayList<>();

        for (SavedMessage savedMessage : savedMessages) {

            if (savedMessage
                    .getUserId()
                    .equalsIgnoreCase(userId)) {

                Message message =
                        messageService.getMessageById(
                                savedMessage.getMessageId()
                        );

                if (message != null) {
                    result.add(message);
                }
            }
        }

        return result;
    }

    private boolean isAlreadySaved(
            String userId,
            String messageId
    ) {
        for (SavedMessage savedMessage : savedMessages) {

            boolean sameUser =
                    savedMessage
                            .getUserId()
                            .equalsIgnoreCase(userId);

            boolean sameMessage =
                    savedMessage
                            .getMessageId()
                            .equalsIgnoreCase(messageId);

            if (sameUser && sameMessage) {
                return true;
            }
        }

        return false;
    }

    private void persistSavedMessages() {
        savedMessageRepository.saveAll(savedMessages);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}