package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AccountSettingsHandler implements HttpHandler {

    private final UserService userService;

    public AccountSettingsHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            getAccount(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {
            updateAccount(exchange);
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

    private void getAccount(HttpExchange exchange)
            throws IOException {

        String userId = getQueryValue(
                exchange.getRequestURI().getRawQuery(),
                "userId"
        );

        if (isBlank(userId)) {
            sendError(
                    exchange,
                    400,
                    "userId is required"
            );
            return;
        }

        User user = userService.getUserById(userId);

        if (user == null) {
            sendError(
                    exchange,
                    404,
                    "User not found"
            );
            return;
        }

        String response = """
                {
                  "success": true,
                  "id": "%s",
                  "username": "%s"
                }
                """.formatted(
                escapeJson(user.getId()),
                escapeJson(user.getUsername())
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
        );
    }

    private void updateAccount(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form =
                FormParser.parse(body);

        String action = form.get("action");
        String userId = form.get("userId");

        if (isBlank(action) || isBlank(userId)) {
            sendError(
                    exchange,
                    400,
                    "action and userId are required"
            );
            return;
        }

        if (action.equalsIgnoreCase("changeUsername")) {

            changeUsername(
                    exchange,
                    userId,
                    form.get("newUsername")
            );

            return;
        }

        if (action.equalsIgnoreCase("changePassword")) {

            changePassword(
                    exchange,
                    userId,
                    form.get("currentPassword"),
                    form.get("newPassword")
            );

            return;
        }

        if (action.equalsIgnoreCase("deleteAccount")) {

            deleteAccount(
                    exchange,
                    userId,
                    form.get("currentPassword")
            );

            return;
        }

        sendError(
                exchange,
                400,
                "Unknown settings action"
        );
    }

    private void changeUsername(
            HttpExchange exchange,
            String userId,
            String newUsername
    ) throws IOException {

        UserService.UpdateUsernameResult result =
                userService.updateUsername(
                        userId,
                        newUsername
                );

        switch (result) {

            case SUCCESS -> HttpUtils.sendJson(
                    exchange,
                    200,
                    """
                    {
                      "success": true,
                      "message": "Username changed successfully"
                    }
                    """
            );

            case USER_NOT_FOUND -> sendError(
                    exchange,
                    404,
                    "User not found"
            );

            case USERNAME_EXISTS -> sendError(
                    exchange,
                    409,
                    "Username already exists"
            );

            case INVALID_USERNAME -> sendError(
                    exchange,
                    400,
                    "New username is required"
            );
        }
    }

    private void changePassword(
            HttpExchange exchange,
            String userId,
            String currentPassword,
            String newPassword
    ) throws IOException {

        User user =
                userService.getUserById(userId);

        if (user == null) {
            sendError(
                    exchange,
                    404,
                    "User not found"
            );
            return;
        }

        if (isBlank(currentPassword)
                || !user.getPassword()
                .equals(currentPassword)) {

            sendError(
                    exchange,
                    401,
                    "Current password is incorrect"
            );

            return;
        }

        UserService.ChangePasswordResult result =
                userService.changePassword(
                        userId,
                        newPassword
                );

        if (result
                == UserService.ChangePasswordResult.SUCCESS) {

            HttpUtils.sendJson(
                    exchange,
                    200,
                    """
                    {
                      "success": true,
                      "message": "Password changed successfully"
                    }
                    """
            );

            return;
        }

        if (result
                == UserService.ChangePasswordResult.INVALID_PASSWORD) {

            sendError(
                    exchange,
                    400,
                    "Password must contain uppercase, lowercase, number and special character"
            );

            return;
        }

        sendError(
                exchange,
                404,
                "User not found"
        );
    }

    private void deleteAccount(
            HttpExchange exchange,
            String userId,
            String currentPassword
    ) throws IOException {

        User user =
                userService.getUserById(userId);

        if (user == null) {
            sendError(
                    exchange,
                    404,
                    "User not found"
            );
            return;
        }

        if (isBlank(currentPassword)
                || !user.getPassword()
                .equals(currentPassword)) {

            sendError(
                    exchange,
                    401,
                    "Current password is incorrect"
            );

            return;
        }

        boolean removed =
                userService.removeUser(userId);

        if (!removed) {
            sendError(
                    exchange,
                    400,
                    "Account could not be deleted"
            );
            return;
        }

        HttpUtils.sendJson(
                exchange,
                200,
                """
                {
                  "success": true,
                  "message": "Account deleted successfully"
                }
                """
        );
    }

    private String getQueryValue(
            String query,
            String name
    ) {

        if (query == null || query.isBlank()) {
            return null;
        }

        String[] parts = query.split("&");

        for (String part : parts) {

            String[] pair =
                    part.split("=", 2);

            String key =
                    URLDecoder.decode(
                            pair[0],
                            StandardCharsets.UTF_8
                    );

            if (key.equals(name)) {

                if (pair.length == 1) {
                    return "";
                }

                return URLDecoder.decode(
                        pair[1],
                        StandardCharsets.UTF_8
                );
            }
        }

        return null;
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
