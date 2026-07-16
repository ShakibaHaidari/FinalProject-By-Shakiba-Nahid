package repository;

import model.User;
import storage.DataPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserRepository {
    public synchronized List<User> loadAll() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(DataPaths.userfile, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                try {
                    User user = lineUser(line);
                    users.add(user);

                } catch (Exception e) {
                    System.err.println("Invalid user record: "+e.getMessage());
                }
            }
            return users;
        } catch (IOException e) {
            throw new IllegalStateException(" not load userس from user.txt",e);
        }
    }
    public synchronized void saveAll(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(userLine(user));
        }
        try {
            Files.write(
                    DataPaths.userfile,
                    lines,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("not save users in users.txt",e);
        }
    }
    private String userLine(User user) {
        return encode(user.getId()) + "|" + encode(user.getUsername()) + "|" +  encode(user.getPassword()) + "|" + user.getFailedLoginAttempts() + "|" + user.getLockedUntilMillis();
    }
    private User lineUser(String line) {
        String[] feild = line.split("\\|", -1);
        if (feild.length != 5) {
            throw new IllegalArgumentException("contain 5 fields");
        }
        String id = decode(feild[0]);
        String username = decode(feild[1]);
        String password = decode(feild[2]);
        int failedLoginAttempts =
                Integer.parseInt(feild[3]);
        long lockedUntilMillis =
                Long.parseLong(feild[4]);
        return new User(id, username, password, failedLoginAttempts, lockedUntilMillis);
    }
    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
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