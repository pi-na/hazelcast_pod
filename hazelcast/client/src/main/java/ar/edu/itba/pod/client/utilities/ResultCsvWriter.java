package ar.edu.itba.pod.client.utilities;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

public final class ResultCsvWriter {
    private static final int HEADER_LINE = 0;

    private ResultCsvWriter() {
        throw new AssertionError();
    }

    public static <T> void writeCsv(String outPath, String fileName, String header, SortedSet<T> results) throws IOException {
        Path outputPath = Paths.get(outPath).resolve(fileName);
        Files.createDirectories(outputPath.getParent());

        List<String> lines = results.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        lines.add(HEADER_LINE, header);
        Files.write(outputPath, lines, StandardCharsets.UTF_8);
    }

    public static <T> void writeCsv(String outPath, String fileName, String header, List<T> results) throws IOException {
        Path outputPath = Paths.get(outPath).resolve(fileName);
        Files.createDirectories(outputPath.getParent());

        List<String> lines = results.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        lines.add(HEADER_LINE, header);
        Files.write(outputPath, lines, StandardCharsets.UTF_8);
    }

}
