package websocket;
import model.Message;
import service.MessageService;
import util.FormParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

public class WebsocketClientHandler implements Runnable {

    private static final String WEB_SOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final MessageService messageService;
    private final WebSocketServer webSocketServer;

    public WebsocketClientHandler(
            Socket socket,
            MessageService messageService,
            WebSocketServer webSocketServer
    ) throws IOException {

        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.messageService = messageService;
        this.webSocketServer = webSocketServer;
    }

    @Override
    public void run() {
        try {
            boolean connected = doHandshake();

            if (!connected) {
                closeClient();
                return;
            }

            System.out.println("WebSocket client connected.");

            while (true) {
                String text = readMessage();

                if (text == null) {
                    break;
                }

                System.out.println(
                        "WebSocket message received: " + text
                );

                handleChatMessage(text);
            }

        } catch (Exception e) {
            System.out.println("WebSocket client disconnected.");

        } finally {
            closeClient();
        }
    }

    private void handleChatMessage(String text) {

        Map<String, String> form = FormParser.parse(text);

        String chatId = form.get("chatId");
        String senderId = form.get("senderId");
        String content = form.get("content");

        Message message =
                messageService.sendMessage(
                        chatId,
                        senderId,
                        content
                );

        if (message == null) {
            try {
                sendMessage(
                        "{"
                                + "\"success\":false,"
                                + "\"message\":\"Message could not be sent\""
                                + "}"
                );
            } catch (IOException e) {
                closeClient();
            }

            return;
        }

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"type\":\"new_message\","
                        + "\"id\":\"" + escapeJson(message.getId()) + "\","
                        + "\"chatId\":\"" + escapeJson(message.getChatId()) + "\","
                        + "\"senderId\":\"" + escapeJson(message.getSenderId()) + "\","
                        + "\"content\":\"" + escapeJson(message.getContent()) + "\","
                        + "\"createdAt\":\"" + message.getCreatedAt().toString() + "\""
                        + "}";

        webSocketServer.broadcast(json);
    }

    private boolean doHandshake() throws Exception {

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );

        String line;
        String webSocketKey = null;

        while ((line = reader.readLine()) != null) {

            if (line.isEmpty()) {
                break;
            }

            if (line.startsWith("Sec-WebSocket-Key:")) {
                webSocketKey =
                        line.substring(
                                "Sec-WebSocket-Key:".length()
                        ).trim();
            }
        }

        if (webSocketKey == null) {
            return false;
        }

        String acceptKey =
                createAcceptKey(webSocketKey);

        String response =
                "HTTP/1.1 101 Switching Protocols\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Sec-WebSocket-Accept: "
                        + acceptKey
                        + "\r\n\r\n";

        outputStream.write(
                response.getBytes(StandardCharsets.UTF_8)
        );

        outputStream.flush();

        return true;
    }

    private String createAcceptKey(String key)
            throws Exception {

        String text = key + WEB_SOCKET_GUID;

        MessageDigest digest =
                MessageDigest.getInstance("SHA-1");

        byte[] hash =
                digest.digest(
                        text.getBytes(StandardCharsets.UTF_8)
                );

        return Base64
                .getEncoder()
                .encodeToString(hash);
    }

    private String readMessage() throws IOException {

        int firstByte = inputStream.read();

        if (firstByte == -1) {
            return null;
        }

        int secondByte = inputStream.read();

        if (secondByte == -1) {
            return null;
        }

        int opcode = firstByte & 0x0F;

        if (opcode == 8) {
            return null;
        }

        int payloadLength = secondByte & 0x7F;

        if (payloadLength == 126) {
            int byte1 = inputStream.read();
            int byte2 = inputStream.read();
            payloadLength = (byte1 << 8) + byte2;
        }

        byte[] mask = new byte[4];

        int readMask = inputStream.read(mask);

        if (readMask != 4) {
            return null;
        }

        byte[] payload = new byte[payloadLength];

        int totalRead = 0;

        while (totalRead < payloadLength) {

            int read =
                    inputStream.read(
                            payload,
                            totalRead,
                            payloadLength - totalRead
                    );

            if (read == -1) {
                return null;
            }

            totalRead += read;
        }

        for (int i = 0; i < payload.length; i++) {
            payload[i] =
                    (byte) (payload[i] ^ mask[i % 4]);
        }

        return new String(
                payload,
                StandardCharsets.UTF_8
        );
    }

    public synchronized void sendMessage(String message)
            throws IOException {

        byte[] messageBytes =
                message.getBytes(StandardCharsets.UTF_8);

        outputStream.write(0x81);

        if (messageBytes.length <= 125) {
            outputStream.write(messageBytes.length);

        } else {
            outputStream.write(126);
            outputStream.write(
                    (messageBytes.length >> 8) & 0xFF
            );
            outputStream.write(
                    messageBytes.length & 0xFF
            );
        }

        outputStream.write(messageBytes);
        outputStream.flush();
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

    private void closeClient() {

        webSocketServer.removeClient(this);

        try {
            socket.close();

        } catch (IOException ignored) {
        }
    }
}