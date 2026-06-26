
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.MessageService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ReportMessageHandler implements HttpHandler {

    private final MessageService messageService;

    public ReportMessageHandler(
            MessageService messageService) {

        this.messageService = messageService;
    }

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

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

        Map<String, String> form =
                FormParser.parse(body);

        String messageId = form.get("messageId");

        if (messageId == null || messageId.isBlank()) {

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

        boolean success =
                messageService.reportMessage(messageId);

        if (!success) {

            HttpUtils.sendJson(
                    exchange,
                    404,
                    """
                    {
                      "success": false,
                      "message": "Message was not found or already reported"
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
                  "message": "Message reported successfully"
                }
                """
        );
    }
}