
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.ChatSettingService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ArchiveChatHandler implements HttpHandler {

    private final ChatSettingService chatSettingService;

    public ArchiveChatHandler(ChatSettingService chatSettingService) {
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
        String archivedText = form.get("archived");

        if (isBlank(userId) || isBlank(chatId) || isBlank(archivedText)) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "userId, chatId and archived are required"
                    }
                    """
            );
            return;
        }

        boolean archived =
                Boolean.parseBoolean(archivedText);

        boolean updated =
                chatSettingService.setArchived(
                        userId,
                        chatId,
                        archived
                );

        if (!updated) {
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Chat archive setting could not be updated"
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
                  "message": "Chat archive setting updated successfully"
                }
                """
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}