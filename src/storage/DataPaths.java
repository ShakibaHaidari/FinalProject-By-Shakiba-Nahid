package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DataPaths {
    public static final Path DATA_DIR = Paths.get("data");
    public static final Path userfile = DATA_DIR.resolve("user.txt");
    public static final Path groupfile = DATA_DIR.resolve("group.txt");
    public static final Path messagefile = DATA_DIR.resolve("messages.txt");
    public static final Path reportfilee = DATA_DIR.resolve("reports.txt");
    public static final Path saveMessageFile = DATA_DIR.resolve("save-message.txt");
    public static final Path chatSettingFile = DATA_DIR.resolve("chat_settings.txt");
    public static final Path fileMedia = DATA_DIR.resolve("shabakeh");
    private DataPaths() {
    }
    public static void initialize() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(fileMedia);
            createFileIfMissing(userfile);
            createFileIfMissing(groupfile);
            createFileIfMissing(messagefile);
            createFileIfMissing(reportfilee);
            createFileIfMissing(saveMessageFile);
            createFileIfMissing(chatSettingFile);

            System.out.println("database file created susccessful.");
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