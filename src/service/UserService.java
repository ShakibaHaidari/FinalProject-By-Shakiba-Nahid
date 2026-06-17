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
}