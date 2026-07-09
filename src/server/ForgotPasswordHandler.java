package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ForgotPasswordHandler implements HttpHandler {

    private final UserService userService;

    public ForgotPasswordHandler(
            UserService userService) {

        this.userService = userService;
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
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> form =
                FormParser.parse(body);

        String username = form.get("username");

        if (username == null || username.isBlank()) {

            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Username is required"
                    }
                    """
            );

            return;
        }

        UserService.PasswordResetData resetData =
                userService.resetPassword(
                        username.trim()
                );

        switch (resetData.getResult()) {

            case SUCCESS -> {

                String response = """
                        {
                          "success": true,
                          "message": "Password reset successfully",
                          "temporaryPassword": "%s"
                        }
                        """.formatted(
                        resetData.getTemporaryPassword()
                );

                HttpUtils.sendJson(
                        exchange,
                        200,
                        response
                );
            }

            case USER_NOT_FOUND -> HttpUtils.sendJson(
                    exchange,
                    404,
                    """
                    {
                      "success": false,
                      "message": "User not found"
                    }
                    """
            );
        }
    }
}