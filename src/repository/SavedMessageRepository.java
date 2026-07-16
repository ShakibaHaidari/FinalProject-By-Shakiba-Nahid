
package repository;

import model.SavedMessage;
import storage.DataPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SavedMessageRepository {
    public synchronized List<SavedMessage> loadAll(){
        List<SavedMessage> savedMessages = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(DataPaths.saveMessageFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()){
                    continue;
                }try{
                    SavedMessage savedMessage =
                            lineSavedMessage(line);

                    savedMessages.add(savedMessage);

                }catch (Exception e) {
                    System.err.println("Invalid saved message record: " + e.getMessage());
                }
            }
            return savedMessages;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load saved messages from saved_messages.txt", e);
        }
    }
    public synchronized void saveAll(List<SavedMessage> savedMessages) {
        List<String> lines = new ArrayList<>();
        for (SavedMessage savedMessage : savedMessages) {
            lines.add(savedMessageLine(savedMessage));
        }
        try{
            Files.write(DataPaths.saveMessageFile, lines, StandardCharsets.UTF_8);

        }catch(IOException e){throw new IllegalStateException("Could not save saved messages in saved_messages.txt",
                    e);
        }
    }
    private String savedMessageLine(SavedMessage savedMessage){
        return encode(savedMessage.getUserId()) + "|" + encode(savedMessage.getMessageId()) + "|" + savedMessage.getSavedAt().toString();
    }
    private SavedMessage lineSavedMessage(String line) {
        String[] field = line.split("\\|", -1);

        if (field.length != 3) {
            throw new IllegalArgumentException(
                    "saved message record must contain 3 fields");
        }
        String userId = decode(field[0]);
        String messageId = decode(field[1]);
        LocalDateTime savedAt = LocalDateTime.parse(field[2]);
        return new SavedMessage(
                userId,
                messageId,
                savedAt);
    }
    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8));
    }
    private String decode(String value) {
        byte[] decodedBytes =
                Base64.getUrlDecoder().decode(value);
        return new String(
                decodedBytes,
                StandardCharsets.UTF_8);
    }
}