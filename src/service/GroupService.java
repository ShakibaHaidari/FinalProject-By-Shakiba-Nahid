package service;

import model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupService {

    private final List<Group> groups =
            new ArrayList<>();

    public void addGroup(Group group) {
        groups.add(group);
    }

    public boolean removeGroup(String id) {
        return groups.removeIf(group ->
                group.getId().equals(id));
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Group findGroup(String id) {

        for (Group group : groups) {
            if (group.getId().equals(id)) {
                return group;
            }
        }

        return null;
    }
}