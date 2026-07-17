package service;

import model.User;
import repository.UserRepository;

import java.security.SecureRandom;
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

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final String UPPERCASE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String LOWERCASE =
            "abcdefghijklmnopqrstuvwxyz";

    private static final String DIGITS =
            "0123456789";

    private static final String SPECIAL =
            "!@%$#^&*";

    private static final String ALL_PASSWORD_CHARACTERS =
            UPPERCASE
                    + LOWERCASE
                    + DIGITS
                    + SPECIAL;

    private final List<User> users;
    private final UserRepository userRepository;


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


    public enum ResetPasswordResult {

        SUCCESS,
        USER_NOT_FOUND
    }


    public static class PasswordResetData {

        private final ResetPasswordResult result;
        private final String temporaryPassword;


        public PasswordResetData(
                ResetPasswordResult result,
                String temporaryPassword
        ) {

            this.result =
                    result;

            this.temporaryPassword =
                    temporaryPassword;
        }


        public ResetPasswordResult getResult() {

            return result;
        }


        public String getTemporaryPassword() {

            return temporaryPassword;
        }
    }


    public UserService() {

        this.userRepository =
                new UserRepository();

        this.users =
                new ArrayList<>(
                        userRepository.loadAll()
                );

        System.out.println(
                "Loaded "
                        + users.size()
                        + " user(s) from users.txt."
        );
    }


    public synchronized RegistrationResult register(
            User user
    ) {

        if (user == null
                || isBlank(user.getId())
                || isBlank(user.getUsername())
                || isBlank(user.getPassword())) {

            return RegistrationResult.INVALID_INPUT;
        }


        if (getUserById(
                user.getId()
        ) != null) {

            return RegistrationResult.ID_EXISTS;
        }


        if (getUserByUsername(
                user.getUsername()
        ) != null) {

            return RegistrationResult.USERNAME_EXISTS;
        }


        if (!isValidPassword(
                user.getUsername(),
                user.getPassword()
        )) {

            return RegistrationResult.INVALID_PASSWORD;
        }


        users.add(
                user
        );

        persistUsers();

        return RegistrationResult.SUCCESS;
    }


    public synchronized LoginResult login(
            String username,
            String password
    ) {

        User user =
                getUserByUsername(
                        username
                );


        if (user == null) {

            return LoginResult.USER_NOT_FOUND;
        }


        if (user.isLocked()) {

            return LoginResult.ACCOUNT_LOCKED;
        }


        if (!user.getPassword()
                .equals(password)) {

            user.registerFailedLogin(
                    LOCK_DURATION_MILLIS
            );

            persistUsers();


            if (user.isLocked()) {

                return LoginResult.ACCOUNT_LOCKED;
            }


            return LoginResult.WRONG_PASSWORD;
        }


        user.resetFailedLoginAttempts();

        persistUsers();

        return LoginResult.SUCCESS;
    }


    public synchronized boolean removeUser(
            String userId
    ) {

        User user =
                getUserById(
                        userId
                );


        if (user == null) {

            return false;
        }


        users.remove(
                user
        );

        persistUsers();

        return true;
    }


    public synchronized User getUserById(
            String id
    ) {

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
            String username
    ) {

        if (username == null) {

            return null;
        }


        for (User user : users) {

            if (user.getUsername()
                    .equalsIgnoreCase(
                            username
                    )) {

                return user;
            }
        }


        return null;
    }


    public synchronized List<User> getUsers() {

        return new ArrayList<>(
                users
        );
    }


    public boolean isValidPassword(
            String username,
            String password
    ) {

        if (password == null
                || username == null) {

            return false;
        }


        if (password.toLowerCase()
                .contains(
                        username.toLowerCase()
                )) {

            return false;
        }


        return PASSWORD_PATTERN
                .matcher(password)
                .matches();
    }


    public synchronized UpdateUsernameResult updateUsername(
            String userId,
            String newUsername
    ) {

        if (newUsername == null
                || newUsername.trim().isEmpty()) {

            return UpdateUsernameResult.INVALID_USERNAME;
        }


        User user =
                getUserById(
                        userId
                );


        if (user == null) {

            return UpdateUsernameResult.USER_NOT_FOUND;
        }


        String cleanUsername =
                newUsername.trim();


        for (User existingUser : users) {

            boolean isAnotherUser =
                    !existingUser
                            .getId()
                            .equalsIgnoreCase(
                                    userId
                            );


            if (isAnotherUser
                    && existingUser
                    .getUsername()
                    .equalsIgnoreCase(
                            cleanUsername
                    )) {

                return UpdateUsernameResult.USERNAME_EXISTS;
            }
        }


        user.setUsername(
                cleanUsername
        );

        persistUsers();

        return UpdateUsernameResult.SUCCESS;
    }


    public synchronized ChangePasswordResult changePassword(
            String userId,
            String newPassword
    ) {

        User user =
                getUserById(
                        userId
                );


        if (user == null) {

            return ChangePasswordResult.USER_NOT_FOUND;
        }


        if (!isValidPassword(
                user.getUsername(),
                newPassword
        )) {

            return ChangePasswordResult.INVALID_PASSWORD;
        }


        user.setPassword(
                newPassword
        );

        persistUsers();

        return ChangePasswordResult.SUCCESS;
    }


    public synchronized PasswordResetData resetPassword(
            String username
    ) {

        User user =
                getUserByUsername(
                        username
                );


        if (user == null) {

            return new PasswordResetData(
                    ResetPasswordResult.USER_NOT_FOUND,
                    null
            );
        }


        String temporaryPassword =
                generateTemporaryPassword(
                        user.getUsername()
                );


        user.setPassword(
                temporaryPassword
        );

        user.resetFailedLoginAttempts();

        persistUsers();


        return new PasswordResetData(
                ResetPasswordResult.SUCCESS,
                temporaryPassword
        );
    }


    private String generateTemporaryPassword(
            String username
    ) {

        String temporaryPassword;


        do {

            StringBuilder password =
                    new StringBuilder();


            password.append(
                    getRandomCharacter(
                            UPPERCASE
                    )
            );

            password.append(
                    getRandomCharacter(
                            LOWERCASE
                    )
            );

            password.append(
                    getRandomCharacter(
                            DIGITS
                    )
            );

            password.append(
                    getRandomCharacter(
                            SPECIAL
                    )
            );


            for (int index = 0;
                 index < 8;
                 index++) {

                password.append(
                        getRandomCharacter(
                                ALL_PASSWORD_CHARACTERS
                        )
                );
            }


            temporaryPassword =
                    shufflePassword(
                            password.toString()
                    );


        } while (!isValidPassword(
                username,
                temporaryPassword
        ));


        return temporaryPassword;
    }


    private char getRandomCharacter(
            String characters
    ) {

        int randomIndex =
                RANDOM.nextInt(
                        characters.length()
                );


        return characters.charAt(
                randomIndex
        );
    }


    private String shufflePassword(
            String password
    ) {

        char[] characters =
                password.toCharArray();


        for (int index =
             characters.length - 1;
             index > 0;
             index--) {

            int randomIndex =
                    RANDOM.nextInt(
                            index + 1
                    );


            char temporary =
                    characters[index];

            characters[index] =
                    characters[randomIndex];

            characters[randomIndex] =
                    temporary;
        }


        return new String(
                characters
        );
    }


    public synchronized void reloadData() {

        users.clear();

        users.addAll(
                userRepository.loadAll()
        );
    }


    private void persistUsers() {

        userRepository.saveAll(
                users
        );
    }


    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }
}