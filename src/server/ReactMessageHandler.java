
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.MessageReactionService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ReactMessageHandler implements HttpHandler {

    private final MessageReactionService messageReactionService;

    public ReactMessageHandler(
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

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            HttpUtils.sendJson(
                    exchange,
                    405,
                    """
                    {
                      "success": false,
                      "message": "Only POST method is allowed"
                    }
                    """
            );
            return;
        }

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form = FormParser.parse(body);

        String userId = form.get("userId");
        String messageId = form.get("messageId");
        String reaction = form.get("reaction");

        if (isBlank(userId) || isBlank(messageId) || isBlank(reaction)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "userId, messageId and reaction are required"
                    }
                    """
            );
            return;
        }

        boolean reacted =
                messageReactionService.reactToMessage(
                        userId,
                        messageId,
                        reaction
                );

        if (!reacted) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Reaction could not be added"
                    }
                    """
            );
            return;
        }

        HttpUtils.sendJson(
                exchange,
                200,
                """
                {
                  "success": true,
                  "message": "Reaction added successfully"
                }
                """
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}