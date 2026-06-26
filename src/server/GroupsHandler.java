package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Group;
import service.GroupService;

import java.io.IOException;
import java.util.List;

public class GroupsHandler implements HttpHandler {

    private final GroupService groupService;

    public GroupsHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            HttpUtils.sendJson(
                    exchange,
                    405,
                    """
                    {
                      "success": false,
                      "message": "Only GET method is allowed"
                    }
                    """
            );

            return;
        }

        List<Group> groups = groupService.getGroups();

        StringBuilder json = new StringBuilder("[");

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

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


}
