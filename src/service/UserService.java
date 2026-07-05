package service;

import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    private static final long LOCK_DURATION_MILLIS =
            60_000;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@%$#^&*])[A-Za-z\\d!@%$#^&*]{8,}$"
            );

    private final List<User> users =
            new ArrayList<>();

    public enum RegistrationResult {
        SUCCESS,
        ID_EXISTS,
        USERNAME_EXISTS,
        INVALID_PASSWORD,
        INVALID_INPUT
    }

    public enum LoginResult {
        SUCCESS,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        ACCOUNT_LOCKED
    }

    public synchronized RegistrationResult register(
            User user) {

        if (user == null
                || isBlank(user.getId())
                || isBlank(user.getUsername())
                || isBlank(user.getPassword())) {

            return RegistrationResult.INVALID_INPUT;
        }

        if (getUserById(user.getId()) != null) {
            return RegistrationResult.ID_EXISTS;
        }

        if (getUserByUsername(
                user.getUsername()) != null) {

            return RegistrationResult.USERNAME_EXISTS;
        }

        if (!isValidPassword(
                user.getUsername(),
                user.getPassword())) {

            return RegistrationResult.INVALID_PASSWORD;
        }

        users.add(user);

        return RegistrationResult.SUCCESS;
    }

    public synchronized LoginResult login(
            String username,
            String password) {

        User user = getUserByUsername(username);

        if (user == null) {
            return LoginResult.USER_NOT_FOUND;
        }

        if (user.isLocked()) {
            return LoginResult.ACCOUNT_LOCKED;
        }

        if (!user.getPassword().equals(password)) {

            user.registerFailedLogin(
                    LOCK_DURATION_MILLIS
            );

            if (user.isLocked()) {
                return LoginResult.ACCOUNT_LOCKED;
            }

            return LoginResult.WRONG_PASSWORD;
        }

        user.resetFailedLoginAttempts();

        return LoginResult.SUCCESS;
    }

    public synchronized boolean removeUser(
            String userId) {

        User user = getUserById(userId);

        if (user == null) {
            return false;
        }

        users.remove(user);

        return true;
    }

    public synchronized User getUserById(
            String id) {

        if (id == null) {
            return null;
        }

        for (User user : users) {

            if (user.getId()
                    .equalsIgnoreCase(id)) {

                return user;
            }
        }

        return null;
    }

    public synchronized User getUserByUsername(
            String username) {

        if (username == null) {
            return null;
        }

        for (User user : users) {

            if (user.getUsername()
                    .equalsIgnoreCase(username)) {

                return user;
            }
        }

        return null;
    }

    public synchronized List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public boolean isValidPassword(
            String username,
            String password) {

        if (password == null || username == null) {
            return false;
        }

        if (password.toLowerCase()
                .contains(username.toLowerCase())) {

            return false;
        }

        return PASSWORD_PATTERN
                .matcher(password)
                .matches();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }


    public enum UpdateUsernameResult {
        SUCCESS,
        USER_NOT_FOUND,
        USERNAME_EXISTS,
        INVALID_USERNAME
    }

    public enum ChangePasswordResult {
        SUCCESS,
        USER_NOT_FOUND,
        INVALID_PASSWORD
    }

    public synchronized UpdateUsernameResult updateUsername(String userId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return UpdateUsernameResult.INVALID_USERNAME;
        }
        User user = getUserById(userId);
        if (user == null) {
            return UpdateUsernameResult.USER_NOT_FOUND;
        }

        String cleanUsername = newUsername.trim();

        for (User existingUser : users) {
            boolean isAnotherUser = !existingUser.getId().equals(userId);

            if (isAnotherUser && existingUser.getUsername().equalsIgnoreCase(cleanUsername)) {
                return UpdateUsernameResult.USERNAME_EXISTS;
            }
        }
        user.setUsername(cleanUsername);
        return UpdateUsernameResult.SUCCESS;
    }
    public synchronized ChangePasswordResult changePassword(String userId, String newPassword) {
        User user = getUserById(userId);

        if (user == null) {
            return ChangePasswordResult.USER_NOT_FOUND;
        }
        if (!isValidPassword(user.getUsername(), newPassword)) {
            return ChangePasswordResult.INVALID_PASSWORD;
        }
        user.setPassword(newPassword);
        return ChangePasswordResult.SUCCESS;
    }


}