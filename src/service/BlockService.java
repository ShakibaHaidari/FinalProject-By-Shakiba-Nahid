package service;

import model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class BlockService {

    private final UserService userService;
    private final Path relationFile;

    public BlockService(
            UserService userService
    ) {

        this.userService =
                userService;

        this.relationFile =
                Paths.get(
                        "data",
                        "chat_relations.txt"
                );
    }

    public boolean isBlockedChat(
            String chatId,
            String senderId
    ) {

        if (chatId == null
                || senderId == null
                || !chatId.startsWith("p_")) {

            return false;
        }

        String otherUserId =
                findOtherUserId(
                        chatId,
                        senderId
                );

        if (otherUserId == null) {

            return false;
        }

        return isBlockedBetween(
                senderId,
                otherUserId
        );
    }

    private String findOtherUserId(
            String chatId,
            String senderId
    ) {

        for (User user
                : userService.getUsers()) {

            if (user.getId()
                    .equalsIgnoreCase(
                            senderId
                    )) {

                continue;
            }

            String personalChatId =
                    makePersonalChatId(
                            senderId,
                            user.getId()
                    );

            if (personalChatId
                    .equalsIgnoreCase(
                            chatId
                    )) {

                return user.getId();
            }
        }

        return null;
    }

    private boolean isBlockedBetween(
            String firstUserId,
            String secondUserId
    ) {

        if (Files.notExists(
                relationFile
        )) {

            return false;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            relationFile,
                            StandardCharsets.UTF_8
                    );

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

                if (fields.length != 4) {

                    continue;
                }

                String userId;
                String targetUserId;

                try {

                    userId =
                            decode(
                                    fields[0]
                            );

                    targetUserId =
                            decode(
                                    fields[1]
                            );

                } catch (
                        IllegalArgumentException exception
                ) {

                    continue;
                }

                boolean blocked =
                        Boolean.parseBoolean(
                                fields[2]
                        );

                if (!blocked) {

                    continue;
                }

                boolean firstDirection =
                        userId.equalsIgnoreCase(
                                firstUserId
                        )
                                && targetUserId
                                .equalsIgnoreCase(
                                        secondUserId
                                );

                boolean secondDirection =
                        userId.equalsIgnoreCase(
                                secondUserId
                        )
                                && targetUserId
                                .equalsIgnoreCase(
                                        firstUserId
                                );

                if (firstDirection
                        || secondDirection) {

                    return true;
                }
            }

        } catch (IOException exception) {

            return false;
        }

        return false;
    }

    private String makePersonalChatId(
            String firstUserId,
            String secondUserId
    ) {

        if (firstUserId.compareTo(
                secondUserId
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
}