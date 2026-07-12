package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DataPaths {
    public static final Path DATA_DIR = Paths.get("data");
    public static final Path USERS_FILE = DATA_DIR.resolve("user.txt");
    public static final Path GROUPS_FILE = DATA_DIR.resolve("group.txt");
    public static final Path MESSAGES_FILE = DATA_DIR.resolve("messages.txt");
    public static final Path REPORTS_FILE = DATA_DIR.resolve("reports.txt");
    public static final Path MEDIA_DIR = DATA_DIR.resolve("shabakeh");
    private DataPaths() {
    }
    public static void initialize() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(MEDIA_DIR);
            createFileIfMissing(USERS_FILE);
            createFileIfMissing(GROUPS_FILE);
            createFileIfMissing(MESSAGES_FILE);
            createFileIfMissing(REPORTS_FILE);
            System.out.println(
                    "database file created susccessful."
            );
        } catch (IOException e) {
            throw new IllegalStateException("Could not create file",e);
        }
    }
    private static void createFileIfMissing(Path file)
            throws IOException {
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
    }
}