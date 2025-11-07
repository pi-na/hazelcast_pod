package ar.edu.itba.pod.client.utilities;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.ParsedRow;
import ar.edu.itba.pod.api.common.RowParser;
import ar.edu.itba.pod.api.common.Zone;
import com.hazelcast.core.IMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static ar.edu.itba.pod.client.utilities.CsvUtils.getFilesPath;
import static ar.edu.itba.pod.client.utilities.CsvUtils.getZones;

public class CsvParser<V extends ParsedRow> {
    IMap<Long, V> iMap;
    RowParser<V> rowParser;

    public CsvParser(IMap<Long, V> iMap, RowParser<V> rowParser) {
        this.iMap = iMap;
        this.rowParser = rowParser;
    }

    public void processAndLoadCSV(String inPath) throws IOException {
        PairFiles csvsPath = getFilesPath(inPath);
        Map<Integer, Zone> zones = getZones(csvsPath.getzonesFiles());

        final int BATCH_SIZE = 1000;
        final AtomicLong keyCounter = new AtomicLong(0);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvsPath.gettripsFile()), StandardCharsets.UTF_8)) {
            reader.readLine();
            String line;

            Map<Long, V> batchMap = new HashMap<>((int)(BATCH_SIZE / 0.75f) + 1, 0.75f);

            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(";");
                Optional<V> parsedRow = rowParser.parseRow(cols, zones);

                parsedRow.ifPresent(row -> {
                    batchMap.put(keyCounter.getAndIncrement(), row);
                });


                if (batchMap.size() >= BATCH_SIZE) {
                    loadBatchToCluster(batchMap);
                }
            }

            if (!batchMap.isEmpty()) {
                loadBatchToCluster(batchMap);
            }
        }

    }

    private void loadBatchToCluster(Map<Long, V> batchMap) {
        iMap.putAll(batchMap);
        batchMap.clear();
    }
}
