package ar.edu.itba.pod.client.utilities;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeLogger {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSSS");
    private final Path outPath;

    public TimeLogger(String outPath) {
        this.outPath = Paths.get(outPath);
    }

    public void log(String message, int lineNumber) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logLine = String.format(
                "%s INFO  [main] Client (Client.j:%d) - %s%n",
                timestamp, lineNumber, message
        );
        try {
            Files.writeString(outPath, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

