
package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.BlockUser;
import service.BlockedUserService;
import util.FormParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BlockedUsersHandler implements HttpHandler {
    private final BlockedUserService blockedUserService;
    public BlockedUsersHandler(BlockedUserService blockedUserService) {
        this.blockedUserService = blockedUserService;
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

        String query = exchange.getRequestURI().getRawQuery();
        if(query == null){
            query = "";
        }

        Map<String, String> parameters = FormParser.parse(query);
        String userId = parameters.get("userId");
        if (isBlank(userId)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "userId is required"
                    }
                    """
            );
                return;
        }

        List<BlockUser> blockedUsers =
                blockedUserService.getBlockedUsers(userId);

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < blockedUsers.size(); i++) {

            BlockUser blockedUser = blockedUsers.get(i);

            json.append("{");

            json.append("\"userId\":\"")
                    .append(escapeJson(blockedUser.getUserId()))
                    .append("\",");

            json.append("\"blockedUserId\":\"")
                    .append(escapeJson(blockedUser.getBlockedUserId()))
                    .append("\",");

            json.append("\"blockedAt\":\"")
                    .append(escapeJson(blockedUser.getBlockedAt().toString()))
                    .append("\"");

            json.append("}");

            if (i < blockedUsers.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        HttpUtils.sendJson(exchange, 200, json.toString());
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
}