package repository;

import model.Group;
import storage.DataPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GroupRepository {
    public synchronized List<Group> loadAll() {

        List<Group> groups = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(
                    DataPaths.GROUPS_FILE,
                    StandardCharsets.UTF_8
            );
            for (String line : lines) {

                if (line == null || line.isBlank()) {
                    continue;
                }
                try {
                    Group group = lineGroup(line);
                    groups.add(group);

                } catch (Exception e) {
                    System.err.println("Invalid group record: " + e.getMessage());
                }
            }
            return groups;
        } catch (IOException e) {
            throw new IllegalStateException(
                    " not load groups from group.txt", e);
        }
    }
    public synchronized void saveAll(List<Group> groups) {
        List<String> lines = new ArrayList<>();
        for (Group group : groups) {
            lines.add(groupLine(group));
        }
        try {
            Files.write(
                    DataPaths.GROUPS_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not save groups in group.txt", e);
        }
    }
    private String groupLine(Group group) {
        return encode(group.getId()) + "|" + encode(group.getName()) + "|" + membersLine(group.getMemberIds());
    }

    private Group lineGroup(String line) {
        String[] field = line.split("\\|", -1);
        if (field.length != 3) {
            throw new IllegalArgumentException(
                    "group record most contain 3 fields");
        }
        String id = decode(field[0]);
        String name = decode(field[1]);
        List<String> memberIds = lineMembers(field[2]);
        return new Group(id, name, memberIds);
    }
    private String membersLine(List<String> memberIds) {
        List<String> encodedMembers = new ArrayList<>();
        for (String memberId : memberIds) {
            encodedMembers.add(encode(memberId));
        }
        return String.join(",", encodedMembers);
    }
    private List<String> lineMembers(String text) {
        List<String> memberIds = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return memberIds;
        }
        String[] parts = text.split(",");
        for (String part : parts) {
            memberIds.add(decode(part));
        }
        return memberIds;
    }
    private String encode(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8));
    }
    private String decode(String value) {
        byte[] decodedBytes =
                Base64.getUrlDecoder().decode(value);
        return new String(
                decodedBytes,
                StandardCharsets.UTF_8
        );
    }
}