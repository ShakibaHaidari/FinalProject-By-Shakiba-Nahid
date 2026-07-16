package repository;

import model.Message;
import storage.DataPaths;
import storage.EncryptionUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MessageRepository {

    public synchronized List<Message> loadAll() {

        List<Message> messages = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(
                    DataPaths.messagefile,
                    StandardCharsets.UTF_8
            );

            for (String line : lines) {

                if (line == null || line.isBlank()) {
                    continue;
                }

                try {
                    Message message = lineMessage(line);
                    messages.add(message);

                } catch (Exception e) {
                    System.err.println(
                            "Invalid message record: "
                                    + e.getMessage()
                    );
                }
            }

            return messages;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load messages from messages.txt",
                    e
            );
        }
    }

    public synchronized void saveAll(List<Message> messages) {

        List<String> lines = new ArrayList<>();

        for (Message message : messages) {
            lines.add(messageLine(message));
        }

        try {
            Files.write(
                    DataPaths.messagefile,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not save messages in messages.txt",
                    e
            );
        }
    }

    private String messageLine(Message message) {

        String encryptedContent =
                EncryptionUtil.encrypt(
                        message.getContent()
                );

        String encryptedPreviousContent = "";

        if (message.getPreviousContent() != null
                && !message.getPreviousContent().isBlank()) {

            encryptedPreviousContent =
                    EncryptionUtil.encrypt(
                            message.getPreviousContent()
                    );
        }

        return encode(message.getId())
                + "|"
                + encode(message.getChatId())
                + "|"
                + encode(message.getSenderId())
                + "|"
                + encode(encryptedContent)
                + "|"
                + message.getCreatedAt().toString()
                + "|"
                + message.isReported()
                + "|"
                + message.isEdited()
                + "|"
                + message.isDeleted()
                + "|"
                + encode(encryptedPreviousContent)
                + "|"
                + dateToText(message.getEditedAt())
                + "|"
                + dateToText(message.getDeletedAt());
    }

    private Message lineMessage(String line) {
        String[] field = line.split("\\|", -1);
        if (field.length == 6) {
            return oldLineMessage(field);
        }
        if (field.length != 11) {
            throw new IllegalArgumentException(
                    "message record must contain 11 fields"
            );
        }

        String id = decode(field[0]);
        String chatId = decode(field[1]);
        String senderId = decode(field[2]);

        String encryptedContent =
                decode(field[3]);

        String content =
                EncryptionUtil.decrypt(encryptedContent);

        LocalDateTime createdAt =
                LocalDateTime.parse(field[4]);

        boolean reported =
                Boolean.parseBoolean(field[5]);

        boolean edited =
                Boolean.parseBoolean(field[6]);

        boolean deleted =
                Boolean.parseBoolean(field[7]);

        String previousContent = "";

        String encryptedPreviousContent =
                decode(field[8]);

        if (!encryptedPreviousContent.isBlank()) {
            previousContent =
                    EncryptionUtil.decrypt(
                            encryptedPreviousContent
                    );
        }

        LocalDateTime editedAt =
                textToDate(field[9]);

        LocalDateTime deletedAt =
                textToDate(field[10]);

        return new Message(
                id,
                chatId,
                senderId,
                content,
                createdAt,
                reported,
                edited,
                deleted,
                previousContent,
                editedAt,
                deletedAt
        );
    }

    private Message oldLineMessage(String[] field) {

        String id = decode(field[0]);
        String chatId = decode(field[1]);
        String senderId = decode(field[2]);

        String encryptedContent =
                decode(field[3]);

        String content =
                EncryptionUtil.decrypt(encryptedContent);

        LocalDateTime createdAt =
                LocalDateTime.parse(field[4]);

        boolean reported =
                Boolean.parseBoolean(field[5]);

        return new Message(
                id,
                chatId,
                senderId,
                content,
                createdAt,
                reported
        );
    }

    private String dateToText(LocalDateTime date) {

        if (date == null) {
            return "";
        }

        return date.toString();
    }

    private LocalDateTime textToDate(String text) {

        if (text == null || text.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(text);
    }

    private String encode(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    private String decode(String value) {

        byte[] decodedBytes =
                Base64.getUrlDecoder().decode(value);

        return new String(
                decodedBytes,
                StandardCharsets.UTF_8
        );
    }
}