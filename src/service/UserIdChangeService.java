package service;

import model.User;
import storage.DataPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserIdChangeService {

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;
    private final SavedMessageService savedMessageService;
    private final ChatSettingService chatSettingService;

    private final Path relationFile;
    private final Path activityFile;

    public enum ChangeIdResult {

        SUCCESS,
        USER_NOT_FOUND,
        ID_EXISTS,
        INVALID_ID,
        SAME_ID,
        WRONG_PASSWORD,
        ERROR
    }

    public UserIdChangeService(
            UserService userService,
            GroupService groupService,
            MessageService messageService,
            SavedMessageService savedMessageService,
            ChatSettingService chatSettingService
    ) {

        this.userService =
                userService;

        this.groupService =
                groupService;

        this.messageService =
                messageService;

        this.savedMessageService =
                savedMessageService;

        this.chatSettingService =
                chatSettingService;

        this.relationFile =
                Paths.get(
                        "data",
                        "chat_relations.txt"
                );

        this.activityFile =
                Paths.get(
                        "data",
                        "user_activity.txt"
                );
    }

    public synchronized ChangeIdResult changeUserId(
            String oldUserId,
            String newUserId,
            String currentPassword
    ) {

        if (isBlank(oldUserId)
                || isBlank(newUserId)) {

            return ChangeIdResult.INVALID_ID;
        }

        String cleanOldId =
                oldUserId.trim();

        String cleanNewId =
                newUserId.trim();

        if (!cleanNewId.matches(
                "[A-Za-z0-9_-]{1,40}"
        )) {

            return ChangeIdResult.INVALID_ID;
        }

        if (cleanOldId.equalsIgnoreCase(
                cleanNewId
        )) {

            return ChangeIdResult.SAME_ID;
        }

        User user =
                userService.getUserById(
                        cleanOldId
                );

        if (user == null) {

            return ChangeIdResult.USER_NOT_FOUND;
        }

        if (isBlank(currentPassword)
                || !user.getPassword()
                .equals(currentPassword)) {

            return ChangeIdResult.WRONG_PASSWORD;
        }

        if (userService.getUserById(
                cleanNewId
        ) != null) {

            return ChangeIdResult.ID_EXISTS;
        }

        List<String> otherUserIds =
                new ArrayList<>();

        for (User otherUser
                : userService.getUsers()) {

            if (!otherUser
                    .getId()
                    .equalsIgnoreCase(
                            cleanOldId
                    )) {

                otherUserIds.add(
                        otherUser.getId()
                );
            }
        }

        try {

            rewriteUserFile(
                    cleanOldId,
                    cleanNewId
            );

            rewriteGroupFile(
                    cleanOldId,
                    cleanNewId
            );

            rewriteMessageFile(
                    cleanOldId,
                    cleanNewId,
                    otherUserIds
            );

            rewriteSavedMessagesFile(
                    cleanOldId,
                    cleanNewId
            );

            rewriteChatSettingsFile(
                    cleanOldId,
                    cleanNewId,
                    otherUserIds
            );

            rewriteRelationsFile(
                    cleanOldId,
                    cleanNewId
            );

            rewriteActivityFile(
                    cleanOldId,
                    cleanNewId
            );

            renameProfilePicture(
                    cleanOldId,
                    cleanNewId
            );

            reloadServices();

            return ChangeIdResult.SUCCESS;

        } catch (Exception exception) {

            System.err.println(
                    "Could not change user ID: "
                            + exception.getMessage()
            );

            return ChangeIdResult.ERROR;
        }
    }

    private void rewriteUserFile(
            String oldUserId,
            String newUserId
    ) throws IOException {

        List<String> lines =
                Files.readAllLines(
                        DataPaths.userfile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 5) {

                String savedUserId =
                        decode(fields[0]);

                if (savedUserId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    fields[0] =
                            encode(newUserId);
                }
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                DataPaths.userfile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private void rewriteGroupFile(
            String oldUserId,
            String newUserId
    ) throws IOException {

        List<String> lines =
                Files.readAllLines(
                        DataPaths.groupfile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 3
                    && !fields[2].isBlank()) {

                String[] members =
                        fields[2].split(",");

                for (int i = 0;
                     i < members.length;
                     i++) {

                    String memberId =
                            decode(members[i]);

                    if (memberId
                            .equalsIgnoreCase(
                                    oldUserId
                            )) {

                        members[i] =
                                encode(newUserId);
                    }
                }

                fields[2] =
                        String.join(
                                ",",
                                members
                        );
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                DataPaths.groupfile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private void rewriteMessageFile(
            String oldUserId,
            String newUserId,
            List<String> otherUserIds
    ) throws IOException {

        List<String> lines =
                Files.readAllLines(
                        DataPaths.messagefile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 6
                    || fields.length == 11) {

                String chatId =
                        decode(fields[1]);

                String senderId =
                        decode(fields[2]);

                chatId =
                        replacePersonalChatId(
                                chatId,
                                oldUserId,
                                newUserId,
                                otherUserIds
                        );

                if (senderId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    senderId =
                            newUserId;
                }

                fields[1] =
                        encode(chatId);

                fields[2] =
                        encode(senderId);
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                DataPaths.messagefile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private void rewriteSavedMessagesFile(
            String oldUserId,
            String newUserId
    ) throws IOException {

        List<String> lines =
                Files.readAllLines(
                        DataPaths.saveMessageFile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 3) {

                String ownerId =
                        decode(fields[0]);

                if (ownerId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    fields[0] =
                            encode(newUserId);
                }
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                DataPaths.saveMessageFile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private void rewriteChatSettingsFile(
            String oldUserId,
            String newUserId,
            List<String> otherUserIds
    ) throws IOException {

        List<String> lines =
                Files.readAllLines(
                        DataPaths.chatSettingFile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 5) {

                String ownerId =
                        decode(fields[0]);

                String chatId =
                        decode(fields[1]);

                if (ownerId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    ownerId =
                            newUserId;
                }

                chatId =
                        replacePersonalChatId(
                                chatId,
                                oldUserId,
                                newUserId,
                                otherUserIds
                        );

                fields[0] =
                        encode(ownerId);

                fields[1] =
                        encode(chatId);
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                DataPaths.chatSettingFile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private void rewriteRelationsFile(
            String oldUserId,
            String newUserId
    ) throws IOException {

        if (Files.notExists(
                relationFile
        )) {

            return;
        }

        List<String> lines =
                Files.readAllLines(
                        relationFile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 4) {

                String ownerId =
                        decode(fields[0]);

                String targetUserId =
                        decode(fields[1]);

                if (ownerId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    ownerId =
                            newUserId;
                }

                if (targetUserId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    targetUserId =
                            newUserId;
                }

                fields[0] =
                        encode(ownerId);

                fields[1] =
                        encode(targetUserId);
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                relationFile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private void rewriteActivityFile(
            String oldUserId,
            String newUserId
    ) throws IOException {

        if (Files.notExists(
                activityFile
        )) {

            return;
        }

        List<String> lines =
                Files.readAllLines(
                        activityFile,
                        StandardCharsets.UTF_8
                );

        List<String> newLines =
                new ArrayList<>();

        for (String line : lines) {

            if (line == null
                    || line.isBlank()) {

                continue;
            }

            String[] fields =
                    line.split(
                            "\\|",
                            -1
                    );

            if (fields.length == 2) {

                String savedUserId =
                        decode(fields[0]);

                if (savedUserId
                        .equalsIgnoreCase(
                                oldUserId
                        )) {

                    fields[0] =
                            encode(newUserId);
                }
            }

            newLines.add(
                    String.join(
                            "|",
                            fields
                    )
            );
        }

        Files.write(
                activityFile,
                newLines,
                StandardCharsets.UTF_8
        );
    }

    private String replacePersonalChatId(
            String chatId,
            String oldUserId,
            String newUserId,
            List<String> otherUserIds
    ) {

        for (String otherUserId
                : otherUserIds) {

            String oldChatId =
                    makePersonalChatId(
                            oldUserId,
                            otherUserId
                    );

            if (chatId.equalsIgnoreCase(
                    oldChatId
            )) {

                return makePersonalChatId(
                        newUserId,
                        otherUserId
                );
            }
        }

        return chatId;
    }

    private String makePersonalChatId(
            String firstUserId,
            String secondUserId
    ) {

        if (String.valueOf(firstUserId)
                .compareTo(
                        String.valueOf(
                                secondUserId
                        )
                ) < 0) {

            return "p_"
                    + firstUserId
                    + "_"
                    + secondUserId;
        }

        return "p_"
                + secondUserId
                + "_"
                + firstUserId;
    }

    private void renameProfilePicture(
            String oldUserId,
            String newUserId
    ) throws IOException {

        String[] extensions = {
                "png",
                "jpg",
                "webp"
        };

        String safeOldId =
                cleanFileId(oldUserId);

        String safeNewId =
                cleanFileId(newUserId);

        for (String extension
                : extensions) {

            Path oldFile =
                    DataPaths.fileMedia.resolve(
                            "profile_"
                                    + safeOldId
                                    + "."
                                    + extension
                    );

            if (Files.exists(oldFile)) {

                Path newFile =
                        DataPaths.fileMedia.resolve(
                                "profile_"
                                        + safeNewId
                                        + "."
                                        + extension
                        );

                Files.move(
                        oldFile,
                        newFile,
                        StandardCopyOption
                                .REPLACE_EXISTING
                );
            }
        }
    }

    private String cleanFileId(
            String userId
    ) {

        return userId.replaceAll(
                "[^a-zA-Z0-9_-]",
                "_"
        );
    }

    private void reloadServices() {

        userService.reloadData();

        groupService.reloadData();

        messageService.reloadData();

        savedMessageService.reloadData();

        chatSettingService.reloadData();
    }

    private String encode(
            String value
    ) {

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    private String decode(
            String value
    ) {

        byte[] bytes =
                Base64
                        .getUrlDecoder()
                        .decode(value);

        return new String(
                bytes,
                StandardCharsets.UTF_8
        );
    }

    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }
}