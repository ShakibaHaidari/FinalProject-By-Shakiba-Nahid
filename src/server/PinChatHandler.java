
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.ChatSettingService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PinChatHandler implements HttpHandler {

    private final ChatSettingService chatSettingService;

    public PinChatHandler(ChatSettingService chatSettingService) {
        this.chatSettingService = chatSettingService;
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
        String chatId = form.get("chatId");
        String pinnedText = form.get("pinned");

        if (isBlank(userId) || isBlank(chatId) || isBlank(pinnedText)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "userId, chatId and pinned are required"
                    }
                    """
            );
            return;
        }

        boolean pinned =
                Boolean.parseBoolean(pinnedText);

        boolean updated =
                chatSettingService.setPinned(
                        userId,
                        chatId,
                        pinned
                );

        if (!updated) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Chat pin setting could not be updated"
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
                  "message": "Chat pin setting updated successfully"
                }
                """
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}