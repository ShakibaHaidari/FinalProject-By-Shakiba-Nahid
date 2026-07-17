
package service;

import model.Message;
import model.MessageReaction;
import repository.MessageReactionRepository;

import java.util.ArrayList;
import java.util.List;

public class MessageReactionService {

    private final List<MessageReaction> reactions;
    private final MessageReactionRepository messageReactionRepository;
    private final MessageService messageService;

    public MessageReactionService(
            MessageService messageService
    ) {
        this.messageService = messageService;

        this.messageReactionRepository =
                new MessageReactionRepository();

        this.reactions =
                new ArrayList<>(
                        messageReactionRepository.loadAll()
                );

        System.out.println(
                "Loaded "
                        + reactions.size()
                        + " reaction(s) from reactions.txt."
        );
    }

    public synchronized boolean reactToMessage(
            String userId,
            String messageId,
            String reactionText
    ) {
        if (isBlank(userId)
                || isBlank(messageId)
                || isBlank(reactionText)) {
            return false;
        }

        Message message =
                messageService.getMessageById(messageId);

        if (message == null) {
            return false;
        }

        MessageReaction oldReaction =
                findReaction(userId, messageId);

        if (oldReaction != null) {
            oldReaction.setReaction(reactionText);
            persistReactions();
            return true;
        }

        MessageReaction reaction =
                new MessageReaction(
                        userId,
                        messageId,
                        reactionText
                );

        reactions.add(reaction);

        persistReactions();

        return true;
    }

    public synchronized boolean removeReaction(
            String userId,
            String messageId
    ) {
        MessageReaction found =
                findReaction(userId, messageId);

        if (found == null) {
            return false;
        }

        reactions.remove(found);

        persistReactions();

        return true;
    }

    public synchronized List<MessageReaction> getReactionsByMessageId(
            String messageId
    ) {
        List<MessageReaction> result =
                new ArrayList<>();

        for (MessageReaction reaction : reactions) {

            if (reaction
                    .getMessageId()
                    .equalsIgnoreCase(messageId)) {

                result.add(reaction);
            }
        }

        return result;
    }

    public synchronized List<MessageReaction> getReactionsByUserId(
            String userId
    ) {
        List<MessageReaction> result =
                new ArrayList<>();

        for (MessageReaction reaction : reactions) {

            if (reaction
                    .getUserId()
                    .equalsIgnoreCase(userId)) {

                result.add(reaction);
            }
        }

        return result;
    }

    private MessageReaction findReaction(
            String userId,
            String messageId
    ) {
        for (MessageReaction reaction : reactions) {

            boolean sameUser =
                    reaction
                            .getUserId()
                            .equalsIgnoreCase(userId);

            boolean sameMessage =
                    reaction
                            .getMessageId()
                            .equalsIgnoreCase(messageId);

            if (sameUser && sameMessage) {
                return reaction;
            }
        }

        return null;
    }

    private void persistReactions() {
        messageReactionRepository.saveAll(reactions);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}