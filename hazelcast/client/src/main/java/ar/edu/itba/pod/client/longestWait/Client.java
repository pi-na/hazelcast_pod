package ar.edu.itba.pod.client.longestWait;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.longestWait.*;
import ar.edu.itba.pod.api.totalTrips.TotalTrips;
import ar.edu.itba.pod.client.QueryCLient;
import ar.edu.itba.pod.client.params.DefaultParams;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import static ar.edu.itba.pod.api.enums.Params.BOROUGH;

public class Client extends QueryCLient<LongestWaitMapperValueIn> {
    private static final String QUERY4_CSV = "query4.csv";
    private static final String QUERY4_CSV_HEADERS = "pickUpZone;dropOffZone;delayInSeconds";
    private static final String DIVIDER = ";";
    private static Map<Integer, Zone> zones = null;
    private String borough;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client() throws IOException, ExecutionException, InterruptedException {
        super();
    }

    // <borough, List<locationId>>
    private Map<String, List<Long>> getLocationsByBorough(Map<Integer, Zone> zones) {
        return zones.values().stream()
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                Zone::getBorough, // agrupa por borough
                                java.util.stream.Collectors.mapping(
                                        zone -> (long) zone.getLocationId(), // de cada Zone extrae el locationId
                                        java.util.stream.Collectors.toList() // los acumula en una lista
                                )
                        )
                );
    }

    private Boolean locationIsInBorough(int locationId, String borough, Map<String, List<Long>> locationsByBorough) {
        List<Long> locationsInBorough = locationsByBorough.get(borough);
        if (locationsInBorough == null) {
            return false;
        }
        return locationsInBorough.contains((long) locationId);
    }

    @Override
    public Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        this.borough = System.getProperty(BOROUGH.getParam());
        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);
        Map<String, List<Long>> locationsByBorough = getLocationsByBorough(zones);

        if (!locationsByBorough.containsKey(this.borough)) {
            logger.warn("No zones found for borough: '" + this.borough + "'. No trips will be processed.");
            return Stream.empty();
        }

        Stream<Trip> tripStream= lines
                .skip(1)
                .map(String::trim)
                .map(line -> line.split(DIVIDER))
                // Query4 requiere que solo se traten zonas que coinciden con la Borough dada
                .filter(cols -> {
                    int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                    return locationsByBorough.get(this.borough).contains((long) pu);
                })
                .map(pair -> parseTrip(pair, zones));
        return tripStream.onClose(lines::close);
    }

    @Override
    public void finishQuery(IMap iMap, HazelcastInstance hazelcastInstance, DefaultParams params) throws IOException, ExecutionException, InterruptedException {
        KeyValueSource<Long, LongestWaitMapperValueIn> keyValueSource = KeyValueSource.fromMap(iMap);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-longest-wait");
        Job<Long, LongestWaitMapperValueIn> job = jobTracker.newJob(keyValueSource);

        logger.info("Running MapReduce job for Query 4 (Longest Wait)...");

        ICompletableFuture<List<LongestWaitResultRow>> future = job
                .mapper(new LongestWaitMapper())
                .combiner(new LongestWaitCombinerFactory())
                .reducer(new LongestWaitReducerFactory())
                .submit(new LongestWaitCollator());

        List<LongestWaitResultRow> results = future.get();

        if (results == null || results.isEmpty()) {
            logger.warn("No results found for Query 4.");
            return;
        }

        ar.edu.itba.pod.client.utilities.ResultCsvWriter.writeCsv(
                params.getOutPath(),
                QUERY4_CSV,
                QUERY4_CSV_HEADERS,
                results
        );

        logger.info("Query 4 (Longest Wait) completed successfully. Results written to {}", QUERY4_CSV);

    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        QueryCLient queryCLient = new Client();
    }

}
