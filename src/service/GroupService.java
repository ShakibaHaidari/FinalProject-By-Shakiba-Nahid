package service;

import model.Group;
import model.Message;
import repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;
public class GroupService {
    private final List<Group> groups;
    private final List<Message> reportedMessages;
    private final GroupRepository groupRepository;
    public enum UpdateGroupResult {
        SUCCESS,
        GROUP_NOT_FOUND,
        INVALID_GROUP_NAME
    }
    public GroupService() {
        this.groupRepository = new GroupRepository();
        this.groups = new ArrayList<>(groupRepository.loadAll());
        this.reportedMessages = new ArrayList<>();
        System.out.println("Loaded " + groups.size() + " group(s) from group.txt.");
    }
    public synchronized boolean addGroup(Group group) {
        if (group == null || group.getId() == null || group.getId().isBlank() || group.getName() == null || group.getName().isBlank()) {
            return false;
        }
        if (getGroupById(group.getId()) != null) {
            return false;
        }
        groups.add(group);
        persistGroups();
        return true;
    }
    public synchronized boolean removeGroup(String groupId) {
        Group group = getGroupById(groupId);
        if (group == null) {
            return false;
        }
        groups.remove(group);
        persistGroups();
        return true;
    }
    public synchronized Group getGroupById(String id) {
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
    public synchronized boolean addUserToGroup(String groupId, String userId) {
        Group group = getGroupById(groupId);
        if (group == null) {
            return false;
        }
        boolean added = group.addMember(userId);
        if (added) {
            persistGroups();
        }
        return added;
    }
    public synchronized boolean removeUserFromGroup(String groupId, String userId) {
        Group group =
                getGroupById(groupId);
        if (group == null) {
            return false;
        }
        boolean removed =
                group.removeMember(userId);
        if (removed) {
            persistGroups();
        }
        return removed;
    }
    public synchronized void removeUserFromAllGroups(String userId) {
        boolean changed = false;
        for (Group group : groups) {
            boolean removed = group.removeMember(userId);
            if (removed) {
                changed = true;
            }
        }
        if (changed) {
            persistGroups();
        }
    }
    public synchronized List<String> getGroupMembers(String groupId) {
        Group group =
                getGroupById(groupId);
        if (group == null) {
            return new ArrayList<>();
        }
        return group.getMemberIds();
    }
    public synchronized void reportMessage(Message message) {
        if (message == null) {
            return;
        }
        message.setReported(true);
        reportedMessages.add(message);
    }
    public synchronized List<Message> getReportedMessages() {
        return new ArrayList<>(reportedMessages);
    }
    public synchronized UpdateGroupResult updateGroupName(
            String groupId,
            String newName
    ) {
        if (newName == null || newName.trim().isEmpty()) {
            return UpdateGroupResult.INVALID_GROUP_NAME;
        }
        Group group =
                getGroupById(groupId);
        if (group == null) {
            return UpdateGroupResult.GROUP_NOT_FOUND;
        }
        group.setName(newName.trim());
        persistGroups();
        return UpdateGroupResult.SUCCESS;
    }
    private void persistGroups() {
        groupRepository.saveAll(groups);
    }
}