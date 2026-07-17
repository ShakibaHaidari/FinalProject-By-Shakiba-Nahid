
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.MessageReaction;
import service.MessageReactionService;
import util.FormParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MessageReactionsHandler implements HttpHandler {

    private final MessageReactionService messageReactionService;

    public MessageReactionsHandler(
            MessageReactionService messageReactionService
    ) {
        this.messageReactionService = messageReactionService;
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

        if (query == null) {
            query = "";
        }

        Map<String, String> parameters =
                FormParser.parse(query);

        String messageId = parameters.get("messageId");

        if (isBlank(messageId)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "messageId is required"
                    }
                    """
            );
            return;
        }

        List<MessageReaction> reactions =
                messageReactionService
                        .getReactionsByMessageId(messageId);

        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < reactions.size(); i++) {

            MessageReaction reaction = reactions.get(i);

            json.append("{");

            json.append("\"userId\":\"")
                    .append(escapeJson(reaction.getUserId()))
                    .append("\",");

            json.append("\"messageId\":\"")
                    .append(escapeJson(reaction.getMessageId()))
                    .append("\",");

            json.append("\"reaction\":\"")
                    .append(escapeJson(reaction.getReaction()))
                    .append("\",");

            json.append("\"reactedAt\":\"")
                    .append(escapeJson(reaction.getReactedAt().toString()))
                    .append("\"");

            json.append("}");

            if (i < reactions.size() - 1) {
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