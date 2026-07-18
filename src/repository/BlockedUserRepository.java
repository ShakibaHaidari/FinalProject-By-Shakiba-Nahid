package repository;

import model.BlockUser;
import storage.DataPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BlockedUserRepository {

    public synchronized List<BlockUser> loadAll() {

        List<BlockUser> blockedUsers =
                new ArrayList<>();

        try {

            List<String> lines =
                    Files.readAllLines(
                            DataPaths.blockUser,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                if (line == null
                        || line.isBlank()) {

                    continue;
                }

                try {

                    BlockUser blockedUser =
                            lineToBlockedUser(
                                    line
                            );

                    blockedUsers.add(
                            blockedUser
                    );

                } catch (Exception exception) {

                    System.err.println(
                            "Invalid blocked user record: "
                                    + exception.getMessage()
                    );
                }
            }

            return blockedUsers;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not load blocked users from blocked_users.txt",
                    exception
            );
        }
    }


    public synchronized void saveAll(
            List<BlockUser> blockedUsers
    ) {

        List<String> lines =
                new ArrayList<>();

        for (BlockUser blockedUser
                : blockedUsers) {

            lines.add(
                    blockedUserToLine(
                            blockedUser
                    )
            );
        }

        try {

            Files.write(
                    DataPaths.blockUser,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not save blocked users in blocked_users.txt",
                    exception
            );
        }
    }


    private String blockedUserToLine(
            BlockUser blockedUser
    ) {

        return encode(
                blockedUser.getUserId()
        )
                + "|"
                + encode(
                blockedUser.getBlockedUserId()
        )
                + "|"
                + blockedUser
                .getBlockedAt()
                .toString();
    }


    private BlockUser lineToBlockedUser(
            String line
    ) {

        String[] fields =
                line.split(
                        "\\|",
                        -1
                );

        if (fields.length != 3) {

            throw new IllegalArgumentException(
                    "Blocked user record must contain 3 fields"
            );
        }

        String userId =
                decode(
                        fields[0]
                );

        String blockedUserId =
                decode(
                        fields[1]
                );

        LocalDateTime blockedAt =
                LocalDateTime.parse(
                        fields[2]
                );

        return new BlockUser(
                userId,
                blockedUserId,
                blockedAt
        );
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

        byte[] decodedBytes =
                Base64
                        .getUrlDecoder()
                        .decode(
                                value
                        );

        return new String(
                decodedBytes,
                StandardCharsets.UTF_8
        );
    }
}