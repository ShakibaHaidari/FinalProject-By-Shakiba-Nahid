
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.SavedMessageService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RemoveSavedMessageHandler implements HttpHandler {

    private final SavedMessageService savedMessageService;

    public RemoveSavedMessageHandler(
            SavedMessageService savedMessageService
    ) {
        this.savedMessageService = savedMessageService;
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

        boolean removed =
                savedMessageService.removeSavedMessage(
                        userId,
                        messageId
                );

        if (!removed) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Saved message could not be removed"
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
                  "message": "Saved message removed successfully"
                }
                """
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}