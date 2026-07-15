package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Message;
import model.User;
import service.MessageService;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MessagesHandler implements HttpHandler {

    private final MessageService messageService;
    private final UserService userService;

    public MessagesHandler(
            MessageService messageService,
            UserService userService) {

        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange)
            throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            getMessages(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {
            sendMessage(exchange);
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

    private void getMessages(HttpExchange exchange)
            throws IOException {

        String query = exchange.getRequestURI()
                .getRawQuery();

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

        List<Message> messages =
                messageService.getMessagesByChatId(chatId);

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
                    .append(escapeJson(
                            message.getCreatedAt().toString()
                    ))
                    .append("\"");
            json.append("\"edited\":")
                    .append(message.isEdited())
                    .append(",");

            json.append("\"deleted\":")
                    .append(message.isDeleted())
                    .append(",");

            json.append("}");

            if (i < messages.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        HttpUtils.sendJson(
                exchange,
                200,
                json.toString()
        );
    }

    private void sendMessage(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form =
                FormParser.parse(body);

        String chatId = form.get("chatId");
        String senderId = form.get("senderId");
        String content = form.get("content");

        if (isBlank(chatId) || isBlank(senderId) || isBlank(content)){
            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "chatId, senderId and content are required"
                    }
                    """
            );

            return;
        }
        User sender = userService.getUserById(senderId);
        if (sender == null) {
            HttpUtils.sendJson(
                    exchange,
                    404,
                    """
 {"success": false "message": "Sender user was not found" }""");
            return;
        }
        Message message = messageService.sendMessage(
                chatId,
                senderId,
                content
        );

        if (message == null) {

            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Message is invalid or longer than 500 characters"
                    }
                    """
            );

            return;
        }

        String response = """
                {
                  "success": true,
                  "message": "Message sent successfully",
                  "messageId": "%s"
                }
                """.formatted(
                escapeJson(message.getId())
        );

        HttpUtils.sendJson(
                exchange,
                201,
                response
        );
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