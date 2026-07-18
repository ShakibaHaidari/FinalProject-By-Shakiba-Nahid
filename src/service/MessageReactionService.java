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

        this.messageService =
                messageService;

        this.messageReactionRepository =
                new MessageReactionRepository();

        this.reactions =
                new ArrayList<>();

        reloadData();

        System.out.println(
                "Loaded "
                        + reactions.size()
                        + " reaction(s) from reactions.txt."
        );
    }


    public synchronized void reloadData() {

        reactions.clear();

        reactions.addAll(
                messageReactionRepository.loadAll()
        );
    }


    public synchronized boolean reactToMessage(
            String userId,
            String messageId,
            String reactionText
    ) {

        Message message;
        MessageReaction oldReaction;
        MessageReaction newReaction;


        if (isBlank(userId)
                || isBlank(messageId)
                || isBlank(reactionText)) {

            return false;
        }


        message =
                messageService.getMessageById(
                        messageId
                );


        if (message == null) {

            return false;
        }


        oldReaction =
                findReaction(
                        userId,
                        messageId
                );


        if (oldReaction != null) {

            oldReaction.setReaction(
                    reactionText.trim()
            );

            persistReactions();

            return true;
        }


        newReaction =
                new MessageReaction(
                        userId,
                        messageId,
                        reactionText.trim()
                );


        reactions.add(
                newReaction
        );

        persistReactions();

        return true;
    }


    public synchronized boolean removeReaction(
            String userId,
            String messageId
    ) {

        MessageReaction foundReaction;


        if (isBlank(userId)
                || isBlank(messageId)) {

            return false;
        }


        foundReaction =
                findReaction(
                        userId,
                        messageId
                );


        if (foundReaction == null) {

            return false;
        }


        reactions.remove(
                foundReaction
        );

        persistReactions();

        return true;
    }


    public synchronized List<MessageReaction> getReactionsByMessageId(
            String messageId
    ) {

        List<MessageReaction> result =
                new ArrayList<>();


        if (isBlank(messageId)) {

            return result;
        }


        for (MessageReaction reaction
                : reactions) {

            if (reaction
                    .getMessageId()
                    .equalsIgnoreCase(
                            messageId
                    )) {

                result.add(
                        reaction
                );
            }
        }


        return result;
    }


    public synchronized List<MessageReaction> getReactionsByUserId(
            String userId
    ) {

        List<MessageReaction> result =
                new ArrayList<>();


        if (isBlank(userId)) {

            return result;
        }


        for (MessageReaction reaction
                : reactions) {

            if (reaction
                    .getUserId()
                    .equalsIgnoreCase(
                            userId
                    )) {

                result.add(
                        reaction
                );
            }
        }


        return result;
    }


    private MessageReaction findReaction(
            String userId,
            String messageId
    ) {

        boolean sameUser;
        boolean sameMessage;


        for (MessageReaction reaction
                : reactions) {

            sameUser =
                    reaction
                            .getUserId()
                            .equalsIgnoreCase(
                                    userId
                            );

            sameMessage =
                    reaction
                            .getMessageId()
                            .equalsIgnoreCase(
                                    messageId
                            );


            if (sameUser
                    && sameMessage) {

                return reaction;
            }
        }


        return null;
    }


    private void persistReactions() {

        messageReactionRepository.saveAll(
                reactions
        );
    }


    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }
}