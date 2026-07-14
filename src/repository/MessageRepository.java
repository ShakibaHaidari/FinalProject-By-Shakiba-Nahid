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
             List<String> line = Files.readAllLines(DataPaths.MESSAGES_FILE, StandardCharsets.UTF_8);
             for (String line1 : line) {
                if (line1 == null || line1.isBlank()) {
                    continue;
                  }
                try {
                    Message massege = lineMessage(line1);
                    messages.add(massege);
                  } catch (Exception e) {
                    System.err.println("Invalid message:" + e.getMessage());
                }
             }
            return messages;
         } catch (IOException e) {
            throw new IllegalStateException("not load messages from messages.txt", e);
        }
       }

    public synchronized void saveAll(List<Message> messages) {
        List<String> lines = new ArrayList<>();
        for (Message message : messages) {
            lines.add(messagetext(message));
           }
        try {
            Files.write(DataPaths.MESSAGES_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(" not save messages in messages.txt", e);
        }
        }
    private String messagetext(Message message) {
        String encryptCont = EncryptionUtil.encrypt(message.getContent());
        return encode(message.getId()) + "|" + encode(message.getChatId()) + "|" + encode(message.getSenderId()) + "|" + encode(encryptCont) + "|" + message.getCreatedAt().toString() + "|" + message.isReported();
       }
    private Message lineMessage(String line) {
        String[] field = line.split("\\|", -1);
        if (field.length != 6) {
            throw new IllegalArgumentException("message record must contain 6 fields");
        }

        String id = decode(field[0]);
        String chatId = decode(field[1]);
        String senderId = decode(field[2]);
        String encryptedContent = decode(field[3]);
        String content = EncryptionUtil.decrypt(encryptedContent);

        LocalDateTime createdAt = LocalDateTime.parse(field[4]);

        boolean reported = Boolean.parseBoolean(field[5]);
        return new Message(id, chatId, senderId, content, createdAt, reported);
          }
    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String meghdar) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(meghdar);
        return new String(decodedBytes, StandardCharsets.UTF_8);
     }
   }