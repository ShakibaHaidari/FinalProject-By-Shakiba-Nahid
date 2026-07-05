package model;

public class User {

    private final String id;
    private String username;
    private String password;

    private int failedLoginAttempts;
    private long lockedUntilMillis;

    public User(String id,
                String username,
                String password) {

        this.id = id;
        this.username = username;
        this.password = password;

        this.failedLoginAttempts = 0;
        this.lockedUntilMillis = 0;
    }

//    امتیازی بحش اول
public void setUsernames(String username) {
    this.username = username;
}

    public void setPasswords(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLocked() {
        return System.currentTimeMillis() < lockedUntilMillis;
    }

    public long getRemainingLockSeconds() {

        long remainingMillis =
                lockedUntilMillis - System.currentTimeMillis();

        return Math.max(
                0,
                (remainingMillis + 999) / 1000
        );
    }

    public void registerFailedLogin(
            long lockDurationMillis) {

        failedLoginAttempts++;

        if (failedLoginAttempts >= 5) {

            lockedUntilMillis =
                    System.currentTimeMillis()
                            + lockDurationMillis;

            failedLoginAttempts = 0;
        }
    }

    public void resetFailedLoginAttempts() {

        failedLoginAttempts = 0;
        lockedUntilMillis = 0;
    }

    @Override
    public String toString() {

        return "User{id='"
                + id
                + "', username='"
                + username
                + "'}";
    }
}