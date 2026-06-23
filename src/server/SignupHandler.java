package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SignupHandler
        implements HttpHandler {

    private final UserService userService;

    public SignupHandler(
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

        String id = form.get("id");
        String username = form.get("username");
        String password = form.get("password");

        if (isBlank(id)
                || isBlank(username)
                || isBlank(password)) {

            HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "ID, username and password are required"
                    }
                    """
            );

            return;
        }

        UserService.RegistrationResult result =
                userService.register(
                        new User(
                                id.trim(),
                                username.trim(),
                                password
                        )
                );

        switch (result) {

            case SUCCESS -> HttpUtils.sendJson(
                    exchange,
                    201,
                    """
                    {
                      "success": true,
                      "message": "User registered successfully"
                    }
                    """
            );

            case ID_EXISTS -> HttpUtils.sendJson(
                    exchange,
                    409,
                    """
                    {
                      "success": false,
                      "message": "This ID already exists"
                    }
                    """
            );

            case USERNAME_EXISTS -> HttpUtils.sendJson(
                    exchange,
                    409,
                    """
                    {
                      "success": false,
                      "message": "This username already exists"
                    }
                    """
            );

            case INVALID_PASSWORD -> HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Password must have 8+ characters, uppercase, lowercase, number and !@%$#^&*; it cannot contain username"
                    }
                    """
            );

            case INVALID_INPUT -> HttpUtils.sendJson(
                    exchange,
                    400,
                    """
                    {
                      "success": false,
                      "message": "Invalid input"
                    }
                    """
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}