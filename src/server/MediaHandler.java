package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Message;
import service.MessageService;
import service.UserService;
import storage.DataPaths;
import util.FormParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class MediaHandler implements HttpHandler {

    private static final int MAX_FILE_SIZE =
            5_000_000;

    private static final String MEDIA_PREFIX =
            "__MEDIA__|";

    private final MessageService messageService;
    private final UserService userService;

    public MediaHandler(
            MessageService messageService,
            UserService userService
    ) {

        this.messageService =
                messageService;

        this.userService =
                userService;
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

            getMedia(exchange);
            return;
        }

        if (method.equalsIgnoreCase("POST")) {

            saveMedia(exchange);
            return;
        }

        sendError(
                exchange,
                405,
                "Only GET and POST methods are allowed"
        );
    }

    private void getMedia(
            HttpExchange exchange
    ) throws IOException {

        Map<String, String> query =
                FormParser.parse(
                        exchange
                                .getRequestURI()
                                .getRawQuery()
                );

        String fileName =
                query.get("file");

        if (isBlank(fileName)) {

            sendError(
                    exchange,
                    400,
                    "file is required"
            );

            return;
        }

        String safeFileName =
                Path.of(fileName)
                        .getFileName()
                        .toString();

        if (!safeFileName.equals(fileName)) {

            sendError(
                    exchange,
                    400,
                    "Invalid file name"
            );

            return;
        }

        Path mediaDirectory =
                DataPaths.fileMedia
                        .toAbsolutePath()
                        .normalize();

        Path filePath =
                mediaDirectory
                        .resolve(safeFileName)
                        .normalize();

        if (!filePath.startsWith(mediaDirectory)
                || Files.notExists(filePath)
                || !Files.isRegularFile(filePath)) {

            sendError(
                    exchange,
                    404,
                    "Media file not found"
            );

            return;
        }

        byte[] fileBytes =
                Files.readAllBytes(filePath);

        String mimeType =
                Files.probeContentType(filePath);

        if (isBlank(mimeType)) {

            mimeType =
                    "application/octet-stream";
        }

        exchange
                .getResponseHeaders()
                .set(
                        "Content-Type",
                        mimeType
                );

        exchange
                .getResponseHeaders()
                .set(
                        "Cache-Control",
                        "no-cache"
                );

        exchange.sendResponseHeaders(
                200,
                fileBytes.length
        );

        exchange
                .getResponseBody()
                .write(fileBytes);

        exchange.close();
    }

    private void saveMedia(
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

        String action =
                form.get("action");

        String chatId =
                form.get("chatId");

        String senderId =
                form.get("senderId");

        String messageId =
                form.get("messageId");

        String originalFileName =
                form.get("fileName");

        String mimeType =
                form.get("mimeType");

        String fileData =
                form.get("fileData");

        if (isBlank(action)) {

            action =
                    "send";
        }

        if (isBlank(chatId)
                || isBlank(senderId)
                || isBlank(originalFileName)
                || isBlank(fileData)) {

            sendError(
                    exchange,
                    400,
                    "chatId, senderId, fileName and fileData are required"
            );

            return;
        }

        if (userService.getUserById(senderId)
                == null) {

            sendError(
                    exchange,
                    404,
                    "Sender user was not found"
            );

            return;
        }

        byte[] fileBytes =
                decodeFileData(fileData);

        if (fileBytes == null) {

            sendError(
                    exchange,
                    400,
                    "Invalid file data"
            );

            return;
        }

        if (fileBytes.length
                > MAX_FILE_SIZE) {

            sendError(
                    exchange,
                    400,
                    "File must be smaller than 5 MB"
            );

            return;
        }

        String cleanFileName =
                cleanFileName(
                        originalFileName
                );

        String cleanMimeType =
                cleanMimeType(
                        mimeType
                );

        String storedFileName =
                createStoredFileName(
                        cleanFileName,
                        cleanMimeType
                );

        Files.createDirectories(
                DataPaths.fileMedia
        );

        Path storedFile =
                DataPaths.fileMedia
                        .resolve(
                                storedFileName
                        );

        Files.write(
                storedFile,
                fileBytes
        );

        String mediaContent =
                createMediaContent(
                        storedFileName,
                        cleanFileName,
                        cleanMimeType
                );

        if (action.equalsIgnoreCase(
                "send"
        )) {

            Message newMessage =
                    messageService.sendMessage(
                            chatId,
                            senderId,
                            mediaContent
                    );

            if (newMessage == null) {

                Files.deleteIfExists(
                        storedFile
                );

                sendError(
                        exchange,
                        400,
                        "Media could not be sent because it is invalid or spam"
                );

                return;
            }

            sendSuccess(
                    exchange,
                    "Media sent successfully",
                    newMessage.getId()
            );

            return;
        }

        if (action.equalsIgnoreCase(
                "replace"
        )) {

            if (isBlank(messageId)) {

                Files.deleteIfExists(
                        storedFile
                );

                sendError(
                        exchange,
                        400,
                        "messageId is required"
                );

                return;
            }

            Message oldMessage =
                    messageService
                            .getMessageById(
                                    messageId
                            );

            if (oldMessage == null
                    || !oldMessage
                    .getSenderId()
                    .equalsIgnoreCase(
                            senderId
                    )
                    || !isMediaContent(
                    oldMessage.getContent()
            )) {

                Files.deleteIfExists(
                        storedFile
                );

                sendError(
                        exchange,
                        400,
                        "Media message could not be replaced"
                );

                return;
            }

            boolean edited =
                    messageService.editMessage(
                            messageId,
                            senderId,
                            mediaContent
                    );

            if (!edited) {

                Files.deleteIfExists(
                        storedFile
                );

                sendError(
                        exchange,
                        400,
                        "Media message could not be replaced"
                );

                return;
            }

            sendSuccess(
                    exchange,
                    "Media replaced successfully",
                    messageId
            );

            return;
        }

        Files.deleteIfExists(
                storedFile
        );

        sendError(
                exchange,
                400,
                "Unknown media action"
        );
    }

    private byte[] decodeFileData(
            String fileData
    ) {

        int commaIndex =
                fileData.indexOf(',');

        if (commaIndex < 0
                || commaIndex
                == fileData.length() - 1) {

            return null;
        }

        String base64Text =
                fileData.substring(
                        commaIndex + 1
                );

        try {

            return Base64
                    .getDecoder()
                    .decode(base64Text);

        } catch (
                IllegalArgumentException exception
        ) {

            return null;
        }
    }

    private String cleanFileName(
            String fileName
    ) {

        String cleanName =
                Path.of(fileName)
                        .getFileName()
                        .toString()
                        .replace("|", "_")
                        .replace("\n", "_")
                        .replace("\r", "_")
                        .trim();

        if (cleanName.isBlank()) {

            cleanName =
                    "file";
        }

        if (cleanName.length() > 120) {

            cleanName =
                    cleanName.substring(
                            0,
                            120
                    );
        }

        return cleanName;
    }

    private String cleanMimeType(
            String mimeType
    ) {

        if (isBlank(mimeType)) {

            return "application/octet-stream";
        }

        String clean =
                mimeType
                        .replace("|", "")
                        .replace("\n", "")
                        .replace("\r", "")
                        .trim();

        if (clean.length() > 100) {

            clean =
                    clean.substring(
                            0,
                            100
                    );
        }

        return clean;
    }

    private String createStoredFileName(
            String originalFileName,
            String mimeType
    ) {

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                + findExtension(
                originalFileName,
                mimeType
        );
    }

    private String findExtension(
            String fileName,
            String mimeType
    ) {

        int dotIndex =
                fileName.lastIndexOf('.');

        if (dotIndex >= 0
                && dotIndex
                < fileName.length() - 1) {

            String extension =
                    fileName
                            .substring(dotIndex)
                            .toLowerCase();

            if (extension.matches(
                    "\\.[a-z0-9]{1,10}"
            )) {

                return extension;
            }
        }

        if (mimeType.equalsIgnoreCase(
                "image/png"
        )) {

            return ".png";
        }

        if (mimeType.equalsIgnoreCase(
                "image/jpeg"
        )) {

            return ".jpg";
        }

        if (mimeType.equalsIgnoreCase(
                "image/webp"
        )) {

            return ".webp";
        }

        if (mimeType.equalsIgnoreCase(
                "image/gif"
        )) {

            return ".gif";
        }

        if (mimeType.equalsIgnoreCase(
                "application/pdf"
        )) {

            return ".pdf";
        }

        if (mimeType.equalsIgnoreCase(
                "text/plain"
        )) {

            return ".txt";
        }

        return ".bin";
    }

    public static String createMediaContent(
            String storedFileName,
            String originalFileName,
            String mimeType
    ) {

        return MEDIA_PREFIX
                + storedFileName
                + "|"
                + originalFileName
                + "|"
                + mimeType;
    }

    public static boolean isMediaContent(
            String content
    ) {

        return content != null
                && content.startsWith(
                MEDIA_PREFIX
        );
    }

    private void sendSuccess(
            HttpExchange exchange,
            String message,
            String messageId
    ) throws IOException {

        String response = """
                {
                  "success": true,
                  "message": "%s",
                  "messageId": "%s"
                }
                """.formatted(
                escapeJson(message),
                escapeJson(messageId)
        );

        HttpUtils.sendJson(
                exchange,
                200,
                response
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