package repository;
import model.Message;
import storage.DataPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ReportRepository {
    public synchronized List<String> loadAllMessageIds() {

        List<String> messageIds = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(
                    DataPaths.REPORTS_FILE,
                    StandardCharsets.UTF_8
            );

            for (String line : lines) {

                if (line == null || line.isBlank()) {
                    continue;
                }

                try {
                    String messageId = lineMessageId(line);

                    if (!messageIds.contains(messageId)) {
                        messageIds.add(messageId);
                    }

                } catch (Exception e) {
                    System.err.println(
                            "Invalid report record: "
                                    + e.getMessage()
                    );
                }
            }

            return messageIds;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load reports from reports.txt",
                    e
            );
        }
    }

    public synchronized void saveAll(
            List<Message> reportedMessages
    ) {
        List<String> lines = new ArrayList<>();

        for (Message message : reportedMessages) {
            lines.add(reportLine(message));
        }

        try {
            Files.write(
                    DataPaths.REPORTS_FILE,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not save reports in reports.txt",
                    e
            );
        }
    }

    private String reportLine(Message message) {

        return encode(message.getId())
                + "|"
                + encode(message.getChatId())
                + "|"
                + encode(message.getSenderId())
                + "|"
                + message.getCreatedAt().toString();
    }

    private String lineMessageId(String line) {

        String[] field = line.split("\\|", -1);

        if (field.length != 4) {
            throw new IllegalArgumentException(
                    "report record must contain 4 fields"
            );
        }

        return decode(field[0]);
    }

    private String encode(String value) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        value.getBytes(StandardCharsets.UTF_8)
                );
    }

    private String decode(String value) {

        byte[] decodedBytes =
                Base64.getUrlDecoder().decode(value);

        return new String(
                decodedBytes,
                StandardCharsets.UTF_8
        );
    }
}