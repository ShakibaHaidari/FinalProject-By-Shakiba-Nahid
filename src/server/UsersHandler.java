package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.UserService;

import java.io.IOException;
import java.util.List;

public class UsersHandler implements HttpHandler {

    private final UserService userService;

    public UsersHandler(UserService userService) {
        this.userService = userService;
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

        List<User> users = userService.getUsers();

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < users.size(); i++) {

            User user = users.get(i);

            json.append("{");

            json.append("\"id\":\"")
                    .append(escapeJson(user.getId()))
                    .append("\",");

            json.append("\"username\":\"")
                    .append(escapeJson(user.getUsername()))
                    .append("\"");

            json.append("}");

            if (i < users.size() - 1) {
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
