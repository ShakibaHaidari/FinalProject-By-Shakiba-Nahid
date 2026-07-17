package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Group;
import service.GroupService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GroupsHandler implements HttpHandler {

    private final GroupService groupService;

    public GroupsHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            getGroups(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {
            createGroup(exchange);
            return;
        }

        HttpUtils.sendJson(
                exchange,
                405,
                """
                {
                  "success": false,
                  "message": "Only GET and POST methods are allowed"
                }
                """
        );
    }

    private void getGroups(HttpExchange exchange)
            throws IOException {

        List<Group> groups =
                groupService.getGroups();

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < groups.size(); i++) {

            Group group = groups.get(i);

            json.append("{");

            json.append("\"id\":\"")
                    .append(escapeJson(group.getId()))
                    .append("\",");

            json.append("\"name\":\"")
                    .append(escapeJson(group.getName()))
                    .append("\",");

            json.append("\"memberCount\":")
                    .append(group.getMemberCount());

            json.append("}");

            if (i < groups.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        HttpUtils.sendJson(
                exchange,
                200,
                json.toString()
        );
    }

    private void createGroup(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form =
                FormParser.parse(body);

        String id = form.get("id");
        String name = form.get("name");
        String userId = form.get("userId");

        if (isBlank(id)
                || isBlank(name)
                || isBlank(userId)) {

            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Group ID, group name and user ID are required"
                    }
                    """
            );

            return;
        }

        Group group =
                new Group(id.trim(), name.trim());

        group.addMember(userId);

        boolean added =
                groupService.addGroup(group);

        if (added == false) {

            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Group ID already exists"
                    }
                    """
            );

            return;
        }

        HttpUtils.sendJson(
                exchange,
                201,
                """
                {
                  "success": true,
                  "message": "Group created successfully"
                }
                """
        );
    }

    private boolean isBlank(String value) {

        return value == null
                || value.isBlank();
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}