package ar.edu.itba.pod.client.longestTrip;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.ParsedRow;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.longestTrip.*;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.CsvUtils;
import ar.edu.itba.pod.client.utilities.HazelcastClientFactory;
import ar.edu.itba.pod.client.utilities.ResultCsvWriter;
import ar.edu.itba.pod.client.utilities.TimeLogger;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class Client{

    private static final String QUERY2_CSV = "query2.csv";
    private static final String QUERY2_CSV_HEADERS = "pickUpZone;longestDOZone;longestReqDateTime;longestMiles;longestCompany";
    private static final String DIVIDER = ";";
    private static final int OUTSIDE_NYC_LOCATION_ID = 265;
    private static final int QUERY_NUMBER = 2;
    private final TimeLogger timeLogger;
    private final DefaultParams params;
    

    public Client(int queryNumber, DefaultParams params)  {
        String timeFile = params.getOutPath() + "/time" + queryNumber + ".txt";
        this.timeLogger = new TimeLogger(timeFile);
        this.params = params;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);
            IMap<Long, LongestTrip> iMap = hazelcastInstance.getMap("g5");

            timeLogger.log("Inicio de la lectura del archivo", 82);
            processAndLoadCSV(iMap);
            timeLogger.log("Fin de la lectura del archivo", 84);
            timeLogger.log("Inicio del trabajo map/reduce", 85);
            finishQuery(iMap, hazelcastInstance, params);
            timeLogger.log("Fin del trabajo map/reduce", 87);
        }finally {
            HazelcastClient.shutdownAll();
        }
    }

    public void finishQuery(IMap<Long, LongestTrip> iMap, HazelcastInstance hazelcastInstance, DefaultParams params)
            throws IOException, ExecutionException, InterruptedException {
        
        KeyValueSource<Long, LongestTrip> keyValueSource = KeyValueSource.fromMap(iMap);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-longest-trip");
        Job<Long, LongestTrip> job = jobTracker.newJob(keyValueSource);

        ICompletableFuture<Map<String, LongestTripResult>> future = job
                .mapper(new LongestTripMapper())
                .combiner(new LongestTripCombinerFactory())
                .reducer(new LongestTripReducerFactory())
                .submit(new LongestTripCollator());

        Map<String, LongestTripResult> result = future.get();
        Map<Integer, Zone> zones = CsvUtils.getZones(CsvUtils.getFilesPath(params.getInPath()).getzonesFiles());

        SortedSet<LongestTripOutput> finalResult = new TreeSet<>(
                Comparator.comparing(LongestTripOutput::pickUpZone));

        for (LongestTripResult r : result.values()) {
            Zone puZone = zones.get((int) r.getKey());
            Zone doZone = zones.get((int) r.getLongestDOZone());

            if (puZone == null || doZone == null) {
                return;
            }

            String puZoneName = puZone.getZoneName();
            String doZoneName = doZone.getZoneName();


            finalResult.add(new LongestTripOutput(
                    puZoneName, doZoneName, r.getLongestReqDateTime(), r.getLongestMiles(), r.getLongestCompany()
            ));
        }

        ResultCsvWriter.writeCsv(params.getOutPath(), QUERY2_CSV, QUERY2_CSV_HEADERS, finalResult);
    }


    public <V extends ParsedRow> void processAndLoadCSV(IMap<Long, V> iMap) throws IOException {
        PairFiles csvsPath = CsvUtils.getFilesPath(params.getInPath());
        Map<Integer, Zone> zones = CsvUtils.getZones(csvsPath.getzonesFiles());

        final int BATCH_SIZE = 1000;
        final AtomicLong keyCounter = new AtomicLong(0);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvsPath.gettripsFile()), StandardCharsets.UTF_8)) {
            reader.readLine(); // saltar header
            String line;

            Map<Long, V> batchMap = new HashMap<>((int)(BATCH_SIZE / 0.75f) + 1, 0.75f);

            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(";");
                Optional<? extends ParsedRow> parsedRow = parseRow(cols, zones);

                parsedRow.ifPresent(row -> {
                    batchMap.put(keyCounter.getAndIncrement(), (V) row); //Casteo de ? extends ParsedRow a V!!!
                });


                if (batchMap.size() >= BATCH_SIZE) {
                    loadBatchToCluster(iMap, batchMap);
                }
            }

            if (!batchMap.isEmpty()) {
                loadBatchToCluster(iMap, batchMap);
            }
        }
    }

    private <V> void loadBatchToCluster(IMap<Long, V> iMap, Map<Long, V> batchMap) {
        long start = System.nanoTime();
        iMap.putAll(batchMap);
        batchMap.clear();
    }

    public Optional<? extends ParsedRow> parseRow(String[] cols, Map<Integer, Zone> zones) {
        int OUTSIDE_NYC_LOCATION_ID = 265;

        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()]);
        int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()]);
        if (pu == OUTSIDE_NYC_LOCATION_ID || doL == OUTSIDE_NYC_LOCATION_ID
                || !zones.containsKey(pu) || !zones.containsKey(doL))
            return Optional.empty();

        String request = cols[TripsColumns.REQUEST_DATETIME.getIndex()].trim();
        double miles = Double.parseDouble(cols[TripsColumns.TRIP_MILES.getIndex()]);
        String company = cols[TripsColumns.COMPANY.getIndex()].trim();

        return Optional.of(new LongestTrip((short) pu, (short) doL, request, miles, company));
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        int queryNumber = 2;
        Client client = new Client(queryNumber, params);
        client.run();
    }
}
