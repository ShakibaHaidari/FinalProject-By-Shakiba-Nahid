package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LoginHandler
        implements HttpHandler {

    private final UserService userService;

    public LoginHandler(
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

        String username =
                form.get("username");

        String password =
                form.get("password");

        if (isBlank(username)
                || isBlank(password)) {

            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Username and password are required"
                    }
                    """
            );

            return;
        }

        UserService.LoginResult result =
                userService.login(
                        username,
                        password
                );

        switch (result) {

            case SUCCESS -> {

                User user =
                        userService.getUserByUsername(
                                username
                        );

                String response = """
                        {
                          "success": true,
                          "message": "Login successful",
                          "userId": "%s",
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

            case ACCOUNT_LOCKED -> {

                User user =
                        userService.getUserByUsername(
                                username
                        );

                long seconds =
                        user == null
                                ? 60
                                : user.getRemainingLockSeconds();

                String response = """
                        {
                          "success": false,
                          "message": "Account is temporarily locked. Try again in %d seconds."
                        }
                        """.formatted(seconds);

                HttpUtils.sendJson(
                        exchange,
                        423,
                        response
                );
            }

            case USER_NOT_FOUND, WRONG_PASSWORD ->

                    HttpUtils.sendJson(
                            exchange,
                            401,
                            """
                            {
                              "success": false,
                              "message": "Invalid username or password"
                            }
                            """
                    );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}