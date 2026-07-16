package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Message;
import service.SavedMessageService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SavedMessagesHandler implements HttpHandler {

    private final SavedMessageService savedMessageService;

    public SavedMessagesHandler(SavedMessageService savedMessageService) {
        this.savedMessageService = savedMessageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleSaveMessage(exchange);
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGetSavedMessages(exchange);
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

    private void handleSaveMessage(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form = FormParser.parse(body);

        String userId = form.get("userId");
        String messageId = form.get("messageId");

        if (isBlank(userId) || isBlank(messageId)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "userId and messageId are required"
                    }
                    """
            );
            return;
        }

        boolean saved =
                savedMessageService.saveMessage(
                        userId,
                        messageId
                );

        if (!saved) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Message could not be saved"
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
                  "message": "Message saved successfully"
                }
                """
        );
    }

    private void handleGetSavedMessages(HttpExchange exchange)
            throws IOException {

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

        List<Message> messages =
                savedMessageService.getSavedMessages(userId);

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < messages.size(); i++) {

            Message message = messages.get(i);

            json.append("{");

            json.append("\"id\":\"")
                    .append(escapeJson(message.getId()))
                    .append("\",");

            json.append("\"chatId\":\"")
                    .append(escapeJson(message.getChatId()))
                    .append("\",");

            json.append("\"senderId\":\"")
                    .append(escapeJson(message.getSenderId()))
                    .append("\",");

            json.append("\"content\":\"")
                    .append(escapeJson(message.getVisibleContent()))
                    .append("\",");

            json.append("\"createdAt\":\"")
                    .append(escapeJson(message.getCreatedAt().toString()))
                    .append("\",");

            json.append("\"edited\":")
                    .append(message.isEdited())
                    .append(",");

            json.append("\"deleted\":")
                    .append(message.isDeleted());

            json.append("}");

            if (i < messages.size() - 1) {
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