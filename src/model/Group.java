package model;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String id;
    private String name;

    private List<model.User> members = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMember(model.User user) {
        members.add(user);
    }

    public void removeMember(model.User user) {
        members.remove(user);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<model.User> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", members=" + members.size() +
                '}';
    }
}