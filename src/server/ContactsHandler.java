package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.UserService;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ContactsHandler implements HttpHandler {

    private final UserService userService;
    private final Path relationFile;

    public ContactsHandler(
            UserService userService
    ) {

        this.userService =
                userService;

        this.relationFile =
                Paths.get(
                        "data",
                        "chat_relations.txt"
                );
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
                .equalsIgnoreCase("GET")) {

            HttpUtils.sendJson(
                    exchange,
                    405,
                    """
                    {
                      "success": false,
                      "message": "Only GET method is allowed"
                    }
                    """
            );

            return;
        }

        Map<String, String> query =
                FormParser.parse(
                        exchange
                                .getRequestURI()
                                .getRawQuery()
                );

        String userId =
                query.get("userId");

        if (isBlank(userId)) {

            sendError(
                    exchange,
                    400,
                    "userId is required"
            );

            return;
        }

        if (userService.getUserById(userId)
                == null) {

            sendError(
                    exchange,
                    404,
                    "User not found"
            );

            return;
        }

        List<User> contacts =
                loadContacts(userId);

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < contacts.size();
             i++) {

            User contact =
                    contacts.get(i);

            json.append("{");

            json.append("\"id\":\"")
                    .append(
                            escapeJson(
                                    contact.getId()
                            )
                    )
                    .append("\",");

            json.append("\"username\":\"")
                    .append(
                            escapeJson(
                                    contact.getUsername()
                            )
                    )
                    .append("\"");

            json.append("}");

            if (i < contacts.size() - 1) {

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

    private List<User> loadContacts(
            String userId
    ) {

        List<User> contacts =
                new ArrayList<>();

        if (Files.notExists(
                relationFile
        )) {

            return contacts;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            relationFile,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                if (line == null
                        || line.isBlank()) {

                    continue;
                }

                String[] fields =
                        line.split(
                                "\\|",
                                -1
                        );

                if (fields.length != 4) {

                    continue;
                }

                String ownerId;
                String targetUserId;

                try {

                    ownerId =
                            decode(
                                    fields[0]
                            );

                    targetUserId =
                            decode(
                                    fields[1]
                            );

                } catch (
                        IllegalArgumentException exception
                ) {

                    continue;
                }

                boolean contact =
                        Boolean.parseBoolean(
                                fields[3]
                        );

                if (!ownerId
                        .equalsIgnoreCase(userId)
                        || !contact) {

                    continue;
                }

                User targetUser =
                        userService.getUserById(
                                targetUserId
                        );

                if (targetUser != null
                        && !containsUser(
                        contacts,
                        targetUser.getId()
                )) {

                    contacts.add(
                            targetUser
                    );
                }
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not load contacts",
                    exception
            );
        }

        return contacts;
    }

    private boolean containsUser(
            List<User> users,
            String userId
    ) {

        for (User user : users) {

            if (user
                    .getId()
                    .equalsIgnoreCase(userId)) {

                return true;
            }
        }

        return false;
    }

    private String decode(
            String value
    ) {

        byte[] bytes =
                Base64
                        .getUrlDecoder()
                        .decode(value);

        return new String(
                bytes,
                StandardCharsets.UTF_8
        );
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

    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
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