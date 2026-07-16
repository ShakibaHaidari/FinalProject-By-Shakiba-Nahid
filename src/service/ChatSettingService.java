
package service;

import model.ChatSetting;
import repository.ChatSettingRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatSettingService {

    private final List<ChatSetting> settings;
    private final ChatSettingRepository chatSettingRepository;

    public ChatSettingService() {

        this.chatSettingRepository =
                new ChatSettingRepository();

        this.settings =
                new ArrayList<>(
                        chatSettingRepository.loadAll()
                );

        System.out.println(
                "Loaded "
                        + settings.size()
                        + " chat setting(s) from chat_settings.txt."
        );
    }

    public synchronized boolean setPinned(
            String userId,
            String chatId,
            boolean pinned
    ) {
        if (isBlank(userId) || isBlank(chatId)) {
            return false;
        }

        ChatSetting setting =
                getOrCreateSetting(userId, chatId);

        setting.setPinned(pinned);

        persistSettings();

        return true;
    }

    public synchronized boolean setArchived(
            String userId,
            String chatId,
            boolean archived
    ) {
        if (isBlank(userId) || isBlank(chatId)) {
            return false;
        }

        ChatSetting setting =
                getOrCreateSetting(userId, chatId);

        setting.setArchived(archived);

        persistSettings();

        return true;
    }

    public synchronized List<ChatSetting> getSettingsByUserId(
            String userId
    ) {
        List<ChatSetting> result = new ArrayList<>();

        for (ChatSetting setting : settings) {

            if (setting
                    .getUserId()
                    .equalsIgnoreCase(userId)) {

                result.add(setting);
            }
        }

        return result;
    }

    public synchronized List<ChatSetting> getPinnedChats(
            String userId
    ) {
        List<ChatSetting> result = new ArrayList<>();

        for (ChatSetting setting : settings) {

            boolean sameUser =
                    setting
                            .getUserId()
                            .equalsIgnoreCase(userId);

            if (sameUser && setting.isPinned()) {
                result.add(setting);
            }
        }

        return result;
    }

    public synchronized List<ChatSetting> getArchivedChats(
            String userId
    ) {
        List<ChatSetting> result = new ArrayList<>();

        for (ChatSetting setting : settings) {

            boolean sameUser =
                    setting
                            .getUserId()
                            .equalsIgnoreCase(userId);

            if (sameUser && setting.isArchived()) {
                result.add(setting);
            }
        }

        return result;
    }

    private ChatSetting getOrCreateSetting(
            String userId,
            String chatId
    ) {
        for (ChatSetting setting : settings) {

            boolean sameUser =
                    setting
                            .getUserId()
                            .equalsIgnoreCase(userId);

            boolean sameChat =
                    setting
                            .getChatId()
                            .equalsIgnoreCase(chatId);

            if (sameUser && sameChat) {
                return setting;
            }
        }

        ChatSetting newSetting =
                new ChatSetting(userId, chatId);

        settings.add(newSetting);

        return newSetting;
    }

    private void persistSettings() {
        chatSettingRepository.saveAll(settings);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}