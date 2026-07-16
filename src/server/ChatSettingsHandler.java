package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.ChatSetting;
import service.ChatSettingService;
import util.FormParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChatSettingsHandler implements HttpHandler {

    private final ChatSettingService chatSettingService;

    public ChatSettingsHandler(ChatSettingService chatSettingService) {
        this.chatSettingService = chatSettingService;
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

        String query =
                exchange.getRequestURI().getRawQuery();

        Map<String, String> parameters =
                FormParser.parse(query);

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

        List<ChatSetting> settings =
                chatSettingService.getSettingsByUserId(userId);

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < settings.size(); i++) {

            ChatSetting setting = settings.get(i);

            json.append("{");

            json.append("\"userId\":\"")
                    .append(escapeJson(setting.getUserId()))
                    .append("\",");

            json.append("\"chatId\":\"")
                    .append(escapeJson(setting.getChatId()))
                    .append("\",");

            json.append("\"pinned\":")
                    .append(setting.isPinned())
                    .append(",");

            json.append("\"archived\":")
                    .append(setting.isArchived())
                    .append(",");

            json.append("\"updatedAt\":\"")
                    .append(escapeJson(setting.getUpdatedAt().toString()))
                    .append("\"");

            json.append("}");

            if (i < settings.size() - 1) {
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