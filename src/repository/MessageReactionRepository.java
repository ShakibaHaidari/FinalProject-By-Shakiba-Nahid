package repository;

import model.MessageReaction;
import storage.DataPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MessageReactionRepository {

    public synchronized List<MessageReaction> loadAll() {

        List<MessageReaction> reactions =
                new ArrayList<>();

        try {

            List<String> lines =
                    Files.readAllLines(
                            DataPaths.REACTIONS_FILE,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                if (line == null
                        || line.isBlank()) {

                    continue;
                }

                try {

                    MessageReaction reaction =
                            lineToMessageReaction(
                                    line
                            );

                    reactions.add(
                            reaction
                    );

                } catch (Exception exception) {

                    System.err.println(
                            "Invalid reaction record: "
                                    + exception.getMessage()
                    );
                }
            }

            return reactions;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not load reactions from reactions.txt",
                    exception
            );
        }
    }


    public synchronized void saveAll(
            List<MessageReaction> reactions
    ) {

        List<String> lines =
                new ArrayList<>();

        for (MessageReaction reaction
                : reactions) {

            lines.add(
                    messageReactionToLine(
                            reaction
                    )
            );
        }

        try {

            Files.write(
                    DataPaths.REACTIONS_FILE,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not save reactions in reactions.txt",
                    exception
            );
        }
    }


    private String messageReactionToLine(
            MessageReaction reaction
    ) {

        return encode(
                reaction.getUserId()
        )
                + "|"
                + encode(
                reaction.getMessageId()
        )
                + "|"
                + encode(
                reaction.getReaction()
        )
                + "|"
                + reaction
                .getReactedAt()
                .toString();
    }


    private MessageReaction lineToMessageReaction(
            String line
    ) {

        String[] fields =
                line.split(
                        "\\|",
                        -1
                );

        if (fields.length != 4) {

            throw new IllegalArgumentException(
                    "Reaction record must contain 4 fields"
            );
        }

        String userId =
                decode(
                        fields[0]
                );

        String messageId =
                decode(
                        fields[1]
                );

        String reaction =
                decode(
                        fields[2]
                );

        LocalDateTime reactedAt =
                LocalDateTime.parse(
                        fields[3]
                );

        return new MessageReaction(
                userId,
                messageId,
                reaction,
                reactedAt
        );
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

        byte[] decodedBytes =
                Base64
                        .getUrlDecoder()
                        .decode(
                                value
                        );

        return new String(
                decodedBytes,
                StandardCharsets.UTF_8
        );
    }
}