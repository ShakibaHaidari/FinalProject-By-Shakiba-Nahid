package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DataPaths {

    public static final Path DATA_DIR =
            Paths.get("data");

    public static final Path userfile =
            DATA_DIR.resolve(
                    "user.txt"
            );

    public static final Path groupfile =
            DATA_DIR.resolve(
                    "group.txt"
            );

    public static final Path messagefile =
            DATA_DIR.resolve(
                    "messages.txt"
            );

    public static final Path reportfilee =
            DATA_DIR.resolve(
                    "reports.txt"
            );

    public static final Path saveMessageFile =
            DATA_DIR.resolve(
                    "save-message.txt"
            );

    public static final Path chatSettingFile =
            DATA_DIR.resolve(
                    "chat_settings.txt"
            );

    public static final Path fileMedia =
            DATA_DIR.resolve(
                    "shabakeh"
            );

    public static final Path blockUser =
            DATA_DIR.resolve(
                    "blocked_users.txt"
            );

    public static final Path REACTIONS_FILE =
            DATA_DIR.resolve(
                    "reactions.txt"
            );


    private DataPaths() {
    }


    public static void initialize() {

        try {

            Files.createDirectories(
                    DATA_DIR
            );

            Files.createDirectories(
                    fileMedia
            );

            createFileIfMissing(
                    userfile
            );

            createFileIfMissing(
                    groupfile
            );

            createFileIfMissing(
                    messagefile
            );

            createFileIfMissing(
                    reportfilee
            );

            createFileIfMissing(
                    saveMessageFile
            );

            createFileIfMissing(
                    chatSettingFile
            );

            createFileIfMissing(
                    blockUser
            );

            createFileIfMissing(
                    REACTIONS_FILE
            );

            System.out.println(
                    "Database files created successfully."
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not create database files",
                    exception
            );
        }
    }


    private static void createFileIfMissing(
            Path file
    ) throws IOException {

        if (Files.notExists(file)) {

            Files.createFile(file);
        }
    }
}