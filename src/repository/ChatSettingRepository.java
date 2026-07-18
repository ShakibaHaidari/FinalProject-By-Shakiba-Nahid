package repository;

import model.ChatSetting;
import storage.DataPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ChatSettingRepository{
    public synchronized List<ChatSetting> loadAll(){
        List<ChatSetting> settings = new ArrayList<>();
        try{
            List<String> lines = Files.readAllLines(DataPaths.chatSettingFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()){
                    continue;
                }
                try{
                    ChatSetting setting = lineChatSetting(line);
                    settings.add(setting);

                }catch (Exception e){
                    System.err.println(
                            "Invalid chat setting record: " + e.getMessage());
                }
            }
            return settings;
        }catch (IOException e){
            throw new IllegalStateException(
                    "Could not load chat settings from chat_settings.txt", e);
        }
    }

    public synchronized void saveAll(
            List<ChatSetting> settings
    ) {
        List<String> lines = new ArrayList<>();

        for (ChatSetting setting : settings) {
            lines.add(chatSettingLine(setting));
        }

        try {
            Files.write(
                    DataPaths.chatSettingFile,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not save chat settings in chat_settings.txt",
                    e
            );
        }
    }

    private String chatSettingLine(
            ChatSetting setting
    ) {
        return encode(setting.getUserId())
                + "|"
                + encode(setting.getChatId())
                + "|"
                + setting.isPinned()
                + "|"
                + setting.isArchived()
                + "|"
                + setting.getUpdatedAt().toString();
    }

    private ChatSetting lineChatSetting(String line) {

        String[] field = line.split("\\|", -1);

        if (field.length != 5) {
            throw new IllegalArgumentException(
                    "chat setting record must contain 5 fields"
            );
        }

        String userId = decode(field[0]);
        String chatId = decode(field[1]);

        boolean pinned =
                Boolean.parseBoolean(field[2]);

        boolean archived =
                Boolean.parseBoolean(field[3]);

        LocalDateTime updatedAt =
                LocalDateTime.parse(field[4]);

        return new ChatSetting(
                userId,
                chatId,
                pinned,
                archived,
                updatedAt
        );
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