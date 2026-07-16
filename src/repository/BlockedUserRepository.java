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

                if (line == null || line.isBlank()) {
                    continue;
                }

                try {
                    BlockUser blockedUser =
                            lineBlockedUser(line);

                    blockedUsers.add(blockedUser);

                } catch (Exception e) {
                    System.err.println(
                            "Invalid blocked user record: "
                                    + e.getMessage()
                    );
                }
            }

            return blockedUsers;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load blocked users from blocked_users.txt",
                    e
            );
        }
    }

    public synchronized void saveAll(
            List<BlockUser> blockedUsers
    ) {
        List<String> lines =
                new ArrayList<>();

        for (BlockUser blockUser : blockedUsers) {
            lines.add(blockedUserLine(blockUser));
        }

        try {
            Files.write(
                    DataPaths.blockUser,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not save blocked users in blocked_users.txt",
                    e
            );
        }
    }

    private String blockedUserLine(
            BlockUser blockedUser
    ) {
        return encode(blockedUser.getUserId())
                + "|"
                + encode(blockedUser.getBlockedUserId())
                + "|"
                + blockedUser.getBlockedAt().toString();
    }

    private BlockUser lineBlockedUser(String line) {

        String[] field =
                line.split("\\|", -1);

        if (field.length != 3) {
            throw new IllegalArgumentException(
                    "blocked user record must contain 3 fields"
            );
        }

        String userId =
                decode(field[0]);

        String blockedUserId =
                decode(field[1]);

        LocalDateTime blockedAt =
                LocalDateTime.parse(field[2]);

        return new BlockUser(
                userId,
                blockedUserId,
                blockedAt
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