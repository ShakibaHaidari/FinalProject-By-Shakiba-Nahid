package service;

import model.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final List<User> users = new ArrayList<>();

    public void addUser(User user) {
        users.add(user);
    }

    public boolean removeUser(String id) {
        return users.removeIf(user ->
                user.getId().equals(id));
    }

    public User findUser(String id) {

        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean usernameExists(String username) {

        for (User user : users) {

            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }

        }

        return false;
    }

    public boolean login(String username,
                         String password) {

        for (User user : users) {

            if (user.getUsername().equals(username)
                    &&
                    user.getPassword().equals(password)) {

                return true;
            }

        }

        return false;
    }

    public boolean register(User user) {

        if (usernameExists(user.getUsername())) {
            return false;
        }

        users.add(user);
        return true;
    }

    public User findByUsername(String username) {

        for (User user : users) {

            if (user.getUsername().equals(username)) {
                return user;
            }

        }

        return null;
    }
}