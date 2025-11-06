package ar.edu.itba.pod.client.utilities;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.ParsedRow;
import ar.edu.itba.pod.api.common.Zone;
import com.hazelcast.core.IMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvUtils {
    private static final String CSV_EXTENSION = ".csv";
    private static final String DIVIDER = ";";
    private static final String TRIPS_IDENTIFIER = "trips";
    private static final String ZONES_IDENTIFIER = "zones";

    public static Map<Integer, Zone> getZones(String zonesFilePath) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(zonesFilePath), StandardCharsets.UTF_8)) {
            return lines.skip(1) // skip header
                    .map(line -> line.split(DIVIDER))
                    .collect(Collectors.toMap(
                            cols -> Integer.parseInt(cols[0].trim()),
                            cols -> new Zone(
                                    Integer.parseInt(cols[0].trim()),
                                    cols[1].trim(),
                                    cols[2].trim()
                            )
                    ));
        }
    }


    public static PairFiles getFilesPath(String inPath) {
        File folder = new File(inPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("The input path does not exist or is not a valid directory: " + inPath);
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("The input directory is empty or could not be read: " + inPath);
        }

        String tripsFile = null;
        String zonesFile = null;
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(CSV_EXTENSION)) {
                if (file.getName().contains(TRIPS_IDENTIFIER)) {
                    tripsFile = file.getAbsolutePath();
                }
                if (file.getName().contains(ZONES_IDENTIFIER)) {
                    zonesFile = file.getAbsolutePath();
                }
            }
        }

        if (tripsFile == null || zonesFile == null) {
            throw new IllegalArgumentException("Trips or zones CSV not found in: " + inPath);
        }

        return new PairFiles(tripsFile, zonesFile);
    }


}
