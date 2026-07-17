package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.UserService;
import storage.DataPaths;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class ProfilePictureHandler
        implements HttpHandler {

    private static final int MAX_IMAGE_SIZE =
            1_000_000;

    private final UserService userService;

    public ProfilePictureHandler(
            UserService userService
    ) {
        this.userService = userService;
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

            getProfilePicture(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {

            changeProfilePicture(exchange);
            return;
        }

        sendError(
                exchange,
                405,
                "Only GET and POST methods are allowed"
        );
    }

    private void getProfilePicture(
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

        String imageData =
                readImage(userId);

        String response = """
                {
                  "success": true,
                  "imageData": "%s"
                }
                """.formatted(
                escapeJson(imageData)
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
        );
    }

    private void changeProfilePicture(
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

        String action =
                form.get("action");

        if (isBlank(userId)
                || isBlank(action)) {

            sendError(
                    exchange,
                    400,
                    "userId and action are required"
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

        if (action.equalsIgnoreCase("save")) {

            saveImage(
                    exchange,
                    userId,
                    form.get("imageData")
            );

            return;
        }

        if (action.equalsIgnoreCase("remove")) {

            deleteImages(userId);

            HttpUtils.sendJson(
                    exchange,
                    200,
                    """
                    {
                      "success": true,
                      "message": "Profile picture removed successfully"
                    }
                    """
            );

            return;
        }

        sendError(
                exchange,
                400,
                "Unknown profile picture action"
        );
    }

    private void saveImage(
            HttpExchange exchange,
            String userId,
            String imageData
    ) throws IOException {

        if (isBlank(imageData)) {

            sendError(
                    exchange,
                    400,
                    "Please select a profile picture"
            );

            return;
        }

        String extension;

        if (imageData.startsWith(
                "data:image/png;base64,"
        )) {

            extension = "png";

        } else if (imageData.startsWith(
                "data:image/jpeg;base64,"
        )) {

            extension = "jpg";

        } else if (imageData.startsWith(
                "data:image/webp;base64,"
        )) {

            extension = "webp";

        } else {

            sendError(
                    exchange,
                    400,
                    "Only PNG, JPG and WebP images are allowed"
            );

            return;
        }

        int commaIndex =
                imageData.indexOf(',');

        String base64Text =
                imageData.substring(
                        commaIndex + 1
                );

        byte[] imageBytes;

        try {

            imageBytes =
                    Base64
                            .getDecoder()
                            .decode(base64Text);

        } catch (
                IllegalArgumentException exception
        ) {

            sendError(
                    exchange,
                    400,
                    "Invalid image data"
            );

            return;
        }

        if (imageBytes.length
                > MAX_IMAGE_SIZE) {

            sendError(
                    exchange,
                    400,
                    "Profile picture must be smaller than 1 MB"
            );

            return;
        }

        deleteImages(userId);

        Files.write(
                getImagePath(
                        userId,
                        extension
                ),

                imageBytes
        );

        HttpUtils.sendJson(
                exchange,
                200,
                """
                {
                  "success": true,
                  "message": "Profile picture saved successfully"
                }
                """
        );
    }

    private String readImage(
            String userId
    ) throws IOException {

        String[] extensions = {
                "png",
                "jpg",
                "webp"
        };

        for (String extension
                : extensions) {

            Path path =
                    getImagePath(
                            userId,
                            extension
                    );

            if (Files.exists(path)) {

                byte[] imageBytes =
                        Files.readAllBytes(path);

                String mimeType;

                if (extension.equals("png")) {

                    mimeType = "image/png";

                } else if (
                        extension.equals("webp")
                ) {

                    mimeType = "image/webp";

                } else {

                    mimeType = "image/jpeg";
                }

                return "data:"
                        + mimeType
                        + ";base64,"
                        + Base64
                        .getEncoder()
                        .encodeToString(
                                imageBytes
                        );
            }
        }

        return "";
    }

    private void deleteImages(
            String userId
    ) throws IOException {

        Files.deleteIfExists(
                getImagePath(
                        userId,
                        "png"
                )
        );

        Files.deleteIfExists(
                getImagePath(
                        userId,
                        "jpg"
                )
        );

        Files.deleteIfExists(
                getImagePath(
                        userId,
                        "webp"
                )
        );
    }

    private Path getImagePath(
            String userId,
            String extension
    ) {

        String safeUserId =
                userId.replaceAll(
                        "[^a-zA-Z0-9_-]",
                        "_"
                );

        return DataPaths.fileMedia.resolve(
                "profile_"
                        + safeUserId
                        + "."
                        + extension
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
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\r",
                        "\\r"
                );
    }
}