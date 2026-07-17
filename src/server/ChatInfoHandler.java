package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Group;
import model.User;
import service.GroupService;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ChatInfoHandler implements HttpHandler {

    private final UserService userService;
    private final GroupService groupService;
    private final Path relationFile;

    public ChatInfoHandler(
            UserService userService,
            GroupService groupService
    ) {
        this.userService = userService;
        this.groupService = groupService;

        this.relationFile = Paths.get(
                "data",
                "chat_relations.txt"
        );

        createRelationFile();
    }

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        String method =
                exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {

            getChatInformation(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {

            changeChatInformation(exchange);
            return;
        }

        sendError(
                exchange,
                405,
                "Only GET and POST methods are allowed"
        );
    }

    private void getChatInformation(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> query;

        String type;
        String userId;
        String chatId;

        query = FormParser.parse(
                exchange
                        .getRequestURI()
                        .getRawQuery()
        );

        type = query.get("type");
        userId = query.get("userId");
        chatId = query.get("chatId");

        if (isBlank(type)
                || isBlank(userId)
                || isBlank(chatId)) {

            sendError(
                    exchange,
                    400,
                    "type, userId and chatId are required"
            );

            return;
        }

        if (type.equalsIgnoreCase("user")) {

            getUserChatInformation(
                    exchange,
                    userId,
                    query.get("targetUserId")
            );

            return;
        }

        if (type.equalsIgnoreCase("group")) {

            getGroupChatInformation(
                    exchange,
                    userId,
                    chatId
            );

            return;
        }

        sendError(
                exchange,
                400,
                "Unknown chat type"
        );
    }

    private void getUserChatInformation(
            HttpExchange exchange,
            String userId,
            String targetUserId
    ) throws IOException {

        User targetUser;
        Relation relation;

        String commonGroups;
        String response;

        if (isBlank(targetUserId)) {

            sendError(
                    exchange,
                    400,
                    "targetUserId is required"
            );

            return;
        }

        targetUser =
                userService.getUserById(
                        targetUserId
                );

        if (targetUser == null) {

            sendError(
                    exchange,
                    404,
                    "User not found"
            );

            return;
        }

        relation =
                getRelation(
                        userId,
                        targetUserId
                );

        commonGroups =
                createCommonGroupsJson(
                        userId,
                        targetUserId
                );

        response = """
                {
                  "success": true,
                  "type": "user",
                  "id": "%s",
                  "username": "%s",
                  "blocked": %s,
                  "contact": %s,
                  "commonGroups": %s
                }
                """.formatted(
                escapeJson(
                        targetUser.getId()
                ),
                escapeJson(
                        targetUser.getUsername()
                ),
                relation.blocked,
                relation.contact,
                commonGroups
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
        );
    }

    private void getGroupChatInformation(
            HttpExchange exchange,
            String userId,
            String groupId
    ) throws IOException {

        Group group;

        List<String> memberIds;

        String membersJson;
        String response;

        boolean currentUserIsMember;

        group =
                groupService.getGroupById(
                        groupId
                );

        if (group == null) {

            sendError(
                    exchange,
                    404,
                    "Group not found"
            );

            return;
        }

        memberIds =
                group.getMemberIds();

        membersJson =
                createMembersJson(
                        memberIds
                );

        currentUserIsMember =
                containsIgnoreCase(
                        memberIds,
                        userId
                );

        response = """
                {
                  "success": true,
                  "type": "group",
                  "id": "%s",
                  "name": "%s",
                  "memberCount": %d,
                  "currentUserIsMember": %s,
                  "members": %s
                }
                """.formatted(
                escapeJson(group.getId()),
                escapeJson(group.getName()),
                group.getMemberCount(),
                currentUserIsMember,
                membersJson
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
        );
    }

    private void changeChatInformation(
            HttpExchange exchange
    ) throws IOException {

        String body;
        String action;
        String userId;

        Map<String, String> form;

        body = new String(
                exchange
                        .getRequestBody()
                        .readAllBytes(),

                StandardCharsets.UTF_8
        );

        form =
                FormParser.parse(body);

        action =
                form.get("action");

        userId =
                form.get("userId");

        if (isBlank(action)
                || isBlank(userId)) {

            sendError(
                    exchange,
                    400,
                    "action and userId are required"
            );

            return;
        }

        if (action.equalsIgnoreCase("block")
                || action.equalsIgnoreCase("unblock")
                || action.equalsIgnoreCase("addContact")
                || action.equalsIgnoreCase("removeContact")) {

            changeUserRelation(
                    exchange,
                    action,
                    userId,
                    form.get("targetUserId")
            );

            return;
        }

        changeGroupInformation(
                exchange,
                action,
                userId,
                form
        );
    }

    private void changeUserRelation(
            HttpExchange exchange,
            String action,
            String userId,
            String targetUserId
    ) throws IOException {

        Relation relation;
        String resultMessage;

        if (isBlank(targetUserId)
                || userService.getUserById(
                targetUserId
        ) == null) {

            sendError(
                    exchange,
                    404,
                    "Target user not found"
            );

            return;
        }

        if (userId.equalsIgnoreCase(
                targetUserId
        )) {

            sendError(
                    exchange,
                    400,
                    "You cannot change relation with yourself"
            );

            return;
        }

        relation =
                getRelation(
                        userId,
                        targetUserId
                );

        if (action.equalsIgnoreCase("block")) {

            relation.blocked = true;

            resultMessage =
                    "User blocked successfully";

        } else if (
                action.equalsIgnoreCase("unblock")
        ) {

            relation.blocked = false;

            resultMessage =
                    "User unblocked successfully";

        } else if (
                action.equalsIgnoreCase("addContact")
        ) {

            relation.contact = true;

            resultMessage =
                    "User added to contacts";

        } else {

            relation.contact = false;

            resultMessage =
                    "User removed from contacts";
        }

        saveRelation(relation);

        sendSuccess(
                exchange,
                resultMessage
        );
    }

    private void changeGroupInformation(
            HttpExchange exchange,
            String action,
            String userId,
            Map<String, String> form
    ) throws IOException {

        String groupId;
        String memberId;

        boolean changed;

        GroupService.UpdateGroupResult result;

        groupId =
                form.get("groupId");

        if (isBlank(groupId)) {

            sendError(
                    exchange,
                    400,
                    "groupId is required"
            );

            return;
        }

        if (groupService.getGroupById(
                groupId
        ) == null) {

            sendError(
                    exchange,
                    404,
                    "Group not found"
            );

            return;
        }

        if (action.equalsIgnoreCase(
                "addMember"
        )) {

            memberId =
                    form.get("memberId");

            if (isBlank(memberId)
                    || userService.getUserById(
                    memberId
            ) == null) {

                sendError(
                        exchange,
                        404,
                        "User not found"
                );

                return;
            }

            changed =
                    groupService.addUserToGroup(
                            groupId,
                            memberId
                    );

            if (!changed) {

                sendError(
                        exchange,
                        400,
                        "User is already a group member"
                );

                return;
            }

            sendSuccess(
                    exchange,
                    "Member added successfully"
            );

            return;
        }

        if (action.equalsIgnoreCase(
                "removeMember"
        )) {

            memberId =
                    form.get("memberId");

            changed =
                    groupService.removeUserFromGroup(
                            groupId,
                            memberId
                    );

            if (!changed) {

                sendError(
                        exchange,
                        400,
                        "User is not a group member"
                );

                return;
            }

            sendSuccess(
                    exchange,
                    "Member removed successfully"
            );

            return;
        }

        if (action.equalsIgnoreCase(
                "leaveGroup"
        )) {

            changed =
                    groupService.removeUserFromGroup(
                            groupId,
                            userId
                    );

            if (!changed) {

                sendError(
                        exchange,
                        400,
                        "You are not a member of this group"
                );

                return;
            }

            sendSuccess(
                    exchange,
                    "You left the group successfully"
            );

            return;
        }

        if (action.equalsIgnoreCase(
                "changeGroupName"
        )) {

            result =
                    groupService.updateGroupName(
                            groupId,
                            form.get("newName")
                    );

            if (result
                    == GroupService
                    .UpdateGroupResult
                    .SUCCESS) {

                sendSuccess(
                        exchange,
                        "Group name changed successfully"
                );

                return;
            }

            if (result
                    == GroupService
                    .UpdateGroupResult
                    .INVALID_GROUP_NAME) {

                sendError(
                        exchange,
                        400,
                        "Group name cannot be empty"
                );

                return;
            }

            sendError(
                    exchange,
                    404,
                    "Group not found"
            );

            return;
        }

        sendError(
                exchange,
                400,
                "Unknown chat information action"
        );
    }

    private String createCommonGroupsJson(
            String userId,
            String targetUserId
    ) {

        StringBuilder json;

        int count;

        json = new StringBuilder("[");

        count = 0;

        for (Group group
                : groupService.getGroups()) {

            List<String> members =
                    group.getMemberIds();

            boolean hasCurrentUser =
                    containsIgnoreCase(
                            members,
                            userId
                    );

            boolean hasTargetUser =
                    containsIgnoreCase(
                            members,
                            targetUserId
                    );

            if (hasCurrentUser
                    && hasTargetUser) {

                if (count > 0) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"id\":\"")
                        .append(
                                escapeJson(
                                        group.getId()
                                )
                        )
                        .append("\",");

                json.append("\"name\":\"")
                        .append(
                                escapeJson(
                                        group.getName()
                                )
                        )
                        .append("\"");

                json.append("}");

                count++;
            }
        }

        json.append("]");

        return json.toString();
    }

    private String createMembersJson(
            List<String> memberIds
    ) {

        StringBuilder json;

        json = new StringBuilder("[");

        for (int i = 0;
             i < memberIds.size();
             i++) {

            String memberId =
                    memberIds.get(i);

            User user =
                    userService.getUserById(
                            memberId
                    );

            String memberUsername =
                    memberId;

            if (user != null) {

                memberUsername =
                        user.getUsername();
            }

            json.append("{");

            json.append("\"id\":\"")
                    .append(
                            escapeJson(memberId)
                    )
                    .append("\",");

            json.append("\"username\":\"")
                    .append(
                            escapeJson(
                                    memberUsername
                            )
                    )
                    .append("\"");

            json.append("}");

            if (i < memberIds.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }

    private Relation getRelation(
            String userId,
            String targetUserId
    ) {

        List<Relation> relations;

        relations =
                loadRelations();

        for (Relation relation : relations) {

            boolean sameUser =
                    relation.userId
                            .equalsIgnoreCase(
                                    userId
                            );

            boolean sameTarget =
                    relation.targetUserId
                            .equalsIgnoreCase(
                                    targetUserId
                            );

            if (sameUser && sameTarget) {
                return relation;
            }
        }

        return new Relation(
                userId,
                targetUserId,
                false,
                false
        );
    }

    private List<Relation> loadRelations() {

        List<Relation> relations;

        relations =
                new ArrayList<>();

        try {

            List<String> lines =
                    Files.readAllLines(
                            relationFile,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                if (line == null
                        || line.isBlank()) {

                    continue;
                }

                String[] fields =
                        line.split("\\|", -1);

                if (fields.length != 4) {
                    continue;
                }

                Relation relation =
                        new Relation(
                                decode(fields[0]),
                                decode(fields[1]),
                                Boolean.parseBoolean(
                                        fields[2]
                                ),
                                Boolean.parseBoolean(
                                        fields[3]
                                )
                        );

                relations.add(relation);
            }

            return relations;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not load chat relations",
                    exception
            );
        }
    }

    private synchronized void saveRelation(
            Relation changedRelation
    ) {

        List<Relation> relations;
        List<String> lines;

        boolean found;

        relations =
                loadRelations();

        lines =
                new ArrayList<>();

        found = false;

        for (Relation relation : relations) {

            boolean sameUser =
                    relation.userId
                            .equalsIgnoreCase(
                                    changedRelation.userId
                            );

            boolean sameTarget =
                    relation.targetUserId
                            .equalsIgnoreCase(
                                    changedRelation.targetUserId
                            );

            if (sameUser && sameTarget) {

                relation.blocked =
                        changedRelation.blocked;

                relation.contact =
                        changedRelation.contact;

                found = true;
            }
        }

        if (!found) {

            relations.add(
                    changedRelation
            );
        }

        for (Relation relation : relations) {

            String line =
                    encode(relation.userId)
                            + "|"
                            + encode(
                            relation.targetUserId
                    )
                            + "|"
                            + relation.blocked
                            + "|"
                            + relation.contact;

            lines.add(line);
        }

        try {

            Files.write(
                    relationFile,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not save chat relations",
                    exception
            );
        }
    }

    private void createRelationFile() {

        try {

            Files.createDirectories(
                    relationFile.getParent()
            );

            if (Files.notExists(
                    relationFile
            )) {

                Files.createFile(
                        relationFile
                );
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not create chat_relations.txt",
                    exception
            );
        }
    }

    private void sendSuccess(
            HttpExchange exchange,
            String message
    ) throws IOException {

        String response = """
                {
                  "success": true,
                  "message": "%s"
                }
                """.formatted(
                escapeJson(message)
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
        );
    }

    private void sendError(
            HttpExchange exchange,
            int statusCode,
            String message
    ) throws IOException {

        String response = """
                {
                  "success": false,
                  "message": "%s"
                }
                """.formatted(
                escapeJson(message)
        );

        HttpUtils.sendJson(
                exchange,
                statusCode,
                response
        );
    }

    private boolean containsIgnoreCase(
            List<String> values,
            String wantedValue
    ) {

        for (String value : values) {

            if (value.equalsIgnoreCase(
                    wantedValue
            )) {

                return true;
            }
        }

        return false;
    }

    private String encode(String value) {

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    private String decode(String value) {

        byte[] decodedBytes =
                Base64
                        .getUrlDecoder()
                        .decode(value);

        return new String(
                decodedBytes,
                StandardCharsets.UTF_8
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static class Relation {

        private final String userId;
        private final String targetUserId;

        private boolean blocked;
        private boolean contact;

        private Relation(
                String userId,
                String targetUserId,
                boolean blocked,
                boolean contact
        ) {
            this.userId = userId;
            this.targetUserId = targetUserId;
            this.blocked = blocked;
            this.contact = contact;
        }
    }
}