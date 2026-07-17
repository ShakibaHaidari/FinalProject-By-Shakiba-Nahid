package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.GroupService;
import storage.DataPaths;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class GroupProfilePictureHandler
        implements HttpHandler {

    private static final int MAX_IMAGE_SIZE =
            1_000_000;

    private final GroupService groupService;

    public GroupProfilePictureHandler(
            GroupService groupService
    ) {
        this.groupService = groupService;
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

            getGroupPicture(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {

            changeGroupPicture(exchange);
            return;
        }

        sendError(
                exchange,
                405,
                "Only GET and POST methods are allowed"
        );
    }

    private void getGroupPicture(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> query =
                FormParser.parse(
                        exchange
                                .getRequestURI()
                                .getRawQuery()
                );

        String groupId =
                query.get("groupId");

        if (isBlank(groupId)) {

            sendError(
                    exchange,
                    400,
                    "groupId is required"
            );

            return;
        }

        if (groupService.getGroupById(groupId)
                == null) {

            sendError(
                    exchange,
                    404,
                    "Group not found"
            );

            return;
        }

        String imageData =
                readImage(groupId);

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

    private void changeGroupPicture(
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

        String groupId =
                form.get("groupId");

        String action =
                form.get("action");

        if (isBlank(groupId)
                || isBlank(action)) {

            sendError(
                    exchange,
                    400,
                    "groupId and action are required"
            );

            return;
        }

        if (groupService.getGroupById(groupId)
                == null) {

            sendError(
                    exchange,
                    404,
                    "Group not found"
            );

            return;
        }

        if (action.equalsIgnoreCase("save")) {

            saveImage(
                    exchange,
                    groupId,
                    form.get("imageData")
            );

            return;
        }

        if (action.equalsIgnoreCase("remove")) {

            deleteImages(groupId);

            HttpUtils.sendJson(
                    exchange,
                    200,
                    """
                    {
                      "success": true,
                      "message": "Group picture removed successfully"
                    }
                    """
            );

            return;
        }

        sendError(
                exchange,
                400,
                "Unknown group picture action"
        );
    }

    private void saveImage(
            HttpExchange exchange,
            String groupId,
            String imageData
    ) throws IOException {

        if (isBlank(imageData)) {

            sendError(
                    exchange,
                    400,
                    "Please select a group picture"
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

        } catch (IllegalArgumentException exception) {

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
                    "Group picture must be smaller than 1 MB"
            );

            return;
        }

        deleteImages(groupId);

        Files.write(
                getImagePath(
                        groupId,
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
                  "message": "Group picture saved successfully"
                }
                """
        );
    }

    private String readImage(
            String groupId
    ) throws IOException {

        String[] extensions = {
                "png",
                "jpg",
                "webp"
        };

        for (String extension : extensions) {

            Path path =
                    getImagePath(
                            groupId,
                            extension
                    );

            if (Files.exists(path)) {

                byte[] imageBytes =
                        Files.readAllBytes(path);

                String mimeType;

                if (extension.equals("png")) {

                    mimeType =
                            "image/png";

                } else if (
                        extension.equals("webp")
                ) {

                    mimeType =
                            "image/webp";

                } else {

                    mimeType =
                            "image/jpeg";
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
            String groupId
    ) throws IOException {

        Files.deleteIfExists(
                getImagePath(
                        groupId,
                        "png"
                )
        );

        Files.deleteIfExists(
                getImagePath(
                        groupId,
                        "jpg"
                )
        );

        Files.deleteIfExists(
                getImagePath(
                        groupId,
                        "webp"
                )
        );
    }

    private Path getImagePath(
            String groupId,
            String extension
    ) {

        String safeGroupId =
                groupId.replaceAll(
                        "[^a-zA-Z0-9_-]",
                        "_"
                );

        return DataPaths.fileMedia.resolve(
                "group_"
                        + safeGroupId
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
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}