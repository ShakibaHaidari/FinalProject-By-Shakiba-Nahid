package service;

import model.Group;
import model.Message;

import java.util.ArrayList;
import java.util.List;

public class GroupService {

    private final List<Group> groups =
            new ArrayList<>();

    private final List<Message> reportedMessages =
            new ArrayList<>();

    public synchronized boolean addGroup(
            Group group) {

        if (group == null
                || group.getId() == null
                || group.getId().isBlank()
                || group.getName() == null
                || group.getName().isBlank()) {

            return false;
        }

        if (getGroupById(group.getId()) != null) {
            return false;
        }

        groups.add(group);

        return true;
    }

    public synchronized boolean removeGroup(
            String groupId) {

        Group group = getGroupById(groupId);

        if (group == null) {
            return false;
        }

        groups.remove(group);

        return true;
    }

    public synchronized Group getGroupById(
            String id) {

        if (id == null) {
            return null;
        }

        for (Group group : groups) {

            if (group.getId()
                    .equalsIgnoreCase(id)) {

                return group;
            }
        }

        return null;
    }

    public synchronized List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    public synchronized boolean addUserToGroup(
            String groupId,
            String userId) {

        Group group = getGroupById(groupId);

        if (group == null) {
            return false;
        }

        return group.addMember(userId);
    }

    public synchronized boolean removeUserFromGroup(
            String groupId,
            String userId) {

        Group group = getGroupById(groupId);

        if (group == null) {
            return false;
        }

        return group.removeMember(userId);
    }

    public synchronized void removeUserFromAllGroups(
            String userId) {

        for (Group group : groups) {
            group.removeMember(userId);
        }
    }

    public synchronized List<String> getGroupMembers(
            String groupId) {

        Group group = getGroupById(groupId);

        if (group == null) {
            return new ArrayList<>();
        }

        return group.getMemberIds();
    }

    public synchronized void reportMessage(
            Message message) {

        if (message == null) {
            return;
        }

        message.setReported(true);

        reportedMessages.add(message);
    }

    public synchronized List<Message>
    getReportedMessages() {

        return new ArrayList<>(reportedMessages);
    }
}