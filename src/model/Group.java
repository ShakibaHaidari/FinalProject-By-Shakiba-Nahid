package model;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final String id;
    private String name;

    private final List<String> memberIds =
            new ArrayList<>();

    public Group(String id,
                 String name) {

        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
//    emtiazi faz 1

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMemberIds() {
        return new ArrayList<>(memberIds);
    }

    public boolean addMember(String userId) {

        if (memberIds.contains(userId)) {
            return false;
        }

        memberIds.add(userId);

        return true;
    }

    public boolean removeMember(String userId) {
        return memberIds.remove(userId);
    }

    public int getMemberCount() {
        return memberIds.size();
    }

    @Override
    public String toString() {

        return "Group{id='"
                + id
                + "', name='"
                + name
                + "', members="
                + memberIds.size()
                + "}";
    }
}