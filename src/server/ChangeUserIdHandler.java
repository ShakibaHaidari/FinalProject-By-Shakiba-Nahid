package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.UserIdChangeService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ChangeUserIdHandler
        implements HttpHandler {

    private final UserIdChangeService
            userIdChangeService;

    public ChangeUserIdHandler(
            UserIdChangeService userIdChangeService
    ) {

        this.userIdChangeService =
                userIdChangeService;
    }

    @Override
    public void handle(
            HttpExchange exchange
    ) throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        if (!exchange
                .getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendError(
                    exchange,
                    405,
                    "Only POST method is allowed"
            );

            return;
        }

        String body =
                new String(
                        exchange
                                .getRequestBody()
                                .readAllBytes(),

                        StandardCharsets.UTF_8
                );

        Map<String, String> form =
                FormParser.parse(body);

        String oldUserId =
                form.get("oldUserId");

        String newUserId =
                form.get("newUserId");

        String currentPassword =
                form.get("currentPassword");

        UserIdChangeService.ChangeIdResult result =
                userIdChangeService.changeUserId(
                        oldUserId,
                        newUserId,
                        currentPassword
                );

        switch (result) {

            case SUCCESS -> {

                String response = """
                        {
                          "success": true,
                          "message": "User ID changed successfully",
                          "newUserId": "%s"
                        }
                        """.formatted(
                        escapeJson(newUserId.trim())
                );

                HttpUtils.sendJson(
                        exchange,
                        200,
                        response
                );
            }

            case USER_NOT_FOUND ->
                    sendError(
                            exchange,
                            404,
                            "User not found"
                    );

            case ID_EXISTS ->
                    sendError(
                            exchange,
                            409,
                            "User ID already exists"
                    );

            case INVALID_ID ->
                    sendError(
                            exchange,
                            400,
                            "User ID can only contain letters, numbers, underscore and dash"
                    );

            case SAME_ID ->
                    sendError(
                            exchange,
                            400,
                            "New User ID must be different"
                    );

            case WRONG_PASSWORD ->
                    sendError(
                            exchange,
                            401,
                            "Current password is incorrect"
                    );

            case ERROR ->
                    sendError(
                            exchange,
                            500,
                            "User ID could not be changed"
                    );
        }
    }

    private void sendError(
            HttpExchange exchange,
            int statusCode,
            String message
    ) throws IOException {

        String response = """
                {
                  "success": false,
                  "message": "%s"
                }
                """.formatted(
                escapeJson(message)
        );

        HttpUtils.sendJson(
                exchange,
                statusCode,
                response
        );
    }

    private String escapeJson(
            String value
    ) {

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