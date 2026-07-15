package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Message;
import service.MessageService;
import util.FormParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MessageHistoryHandler implements HttpHandler {

    private final MessageService messageService;

    public MessageHistoryHandler(MessageService messageService) {
        this.messageService = messageService;
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

        String chatId = parameters.get("chatId");

        if (isBlank(chatId)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "chatId is required"
                    }
                    """
            );
            return;
        }

        List<Message> history =
                messageService.getMessageHistory(chatId);

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < history.size(); i++) {

            Message message = history.get(i);

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
                    .append(escapeJson(message.getContent()))
                    .append("\",");

            json.append("\"previousContent\":\"")
                    .append(escapeJson(message.getPreviousContent()))
                    .append("\",");

            json.append("\"edited\":")
                    .append(message.isEdited())
                    .append(",");

            json.append("\"deleted\":")
                    .append(message.isDeleted())
                    .append(",");

            json.append("\"editedAt\":\"")
                    .append(message.getEditedAt() == null
                            ? ""
                            : escapeJson(message.getEditedAt().toString()))
                    .append("\",");

            json.append("\"deletedAt\":\"")
                    .append(message.getDeletedAt() == null
                            ? ""
                            : escapeJson(message.getDeletedAt().toString()))
                    .append("\"");

            json.append("}");

            if (i < history.size() - 1) {
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