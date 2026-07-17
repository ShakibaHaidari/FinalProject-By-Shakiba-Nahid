package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

public class UserActivityHandler implements HttpHandler {

    private final UserService userService;
    private final Path activityFile;

    public UserActivityHandler(
            UserService userService
    ) {

        this.userService =
                userService;

        this.activityFile =
                Paths.get(
                        "data",
                        "user_activity.txt"
                );

        createActivityFile();
    }

    @Override
    public void handle(
            HttpExchange exchange
    ) throws IOException {

        HttpUtils.addCorsHeaders(exchange);

        if (HttpUtils.handleOptions(exchange)) {
            return;
        }

        String method =
                exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {

            getActivity(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {

            updateActivity(exchange);
            return;
        }

        sendError(
                exchange,
                405,
                "Only GET and POST methods are allowed"
        );
    }

    private void getActivity(
            HttpExchange exchange
    ) throws IOException {

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

        long lastSeenMillis =
                findLastSeen(userId);

        String response = """
                {
                  "success": true,
                  "userId": "%s",
                  "lastSeenMillis": %d
                }
                """.formatted(
                escapeJson(userId),
                lastSeenMillis
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
        );
    }

    private void updateActivity(
            HttpExchange exchange
    ) throws IOException {

        String body =
                new String(
                        exchange
                                .getRequestBody()
                                .readAllBytes(),

                        StandardCharsets.UTF_8
                );

        Map<String, String> form =
                FormParser.parse(body);

        String userId =
                form.get("userId");

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

        saveLastSeen(
                userId,
                System.currentTimeMillis()
        );

        HttpUtils.sendJson(
                exchange,
                200,
                """
                {
                  "success": true,
                  "message": "User activity updated"
                }
                """
        );
    }

    private long findLastSeen(
            String userId
    ) {

        List<Activity> activities =
                loadActivities();

        for (Activity activity
                : activities) {

            if (activity.userId
                    .equalsIgnoreCase(userId)) {

                return activity.lastSeenMillis;
            }
        }

        return 0;
    }

    private synchronized void saveLastSeen(
            String userId,
            long lastSeenMillis
    ) {

        List<Activity> activities =
                loadActivities();

        boolean found =
                false;

        for (Activity activity
                : activities) {

            if (activity.userId
                    .equalsIgnoreCase(userId)) {

                activity.lastSeenMillis =
                        lastSeenMillis;

                found =
                        true;

                break;
            }
        }

        if (!found) {

            activities.add(
                    new Activity(
                            userId,
                            lastSeenMillis
                    )
            );
        }

        List<String> lines =
                new ArrayList<>();

        for (Activity activity
                : activities) {

            lines.add(
                    encode(activity.userId)
                            + "|"
                            + activity.lastSeenMillis
            );
        }

        try {

            Files.write(
                    activityFile,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not save user activity",
                    exception
            );
        }
    }

    private List<Activity> loadActivities() {

        List<Activity> activities =
                new ArrayList<>();

        try {

            List<String> lines =
                    Files.readAllLines(
                            activityFile,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                if (line == null
                        || line.isBlank()) {

                    continue;
                }

                String[] fields =
                        line.split("\\|", -1);

                if (fields.length != 2) {
                    continue;
                }

                activities.add(
                        new Activity(
                                decode(fields[0]),
                                Long.parseLong(
                                        fields[1]
                                )
                        )
                );
            }

            return activities;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not load user activity",
                    exception
            );
        }
    }

    private void createActivityFile() {

        try {

            Files.createDirectories(
                    activityFile.getParent()
            );

            if (Files.notExists(
                    activityFile
            )) {

                Files.createFile(
                        activityFile
                );
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not create user_activity.txt",
                    exception
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

    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }

    private String encode(
            String value
    ) {

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
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

    private static class Activity {

        private final String userId;
        private long lastSeenMillis;

        private Activity(
                String userId,
                long lastSeenMillis
        ) {

            this.userId =
                    userId;

            this.lastSeenMillis =
                    lastSeenMillis;
        }
    }
}