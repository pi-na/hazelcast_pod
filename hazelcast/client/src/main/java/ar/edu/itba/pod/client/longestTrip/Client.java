package ar.edu.itba.pod.client.longestTrip;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.longestTrip.*;
import ar.edu.itba.pod.client.QueryCLient;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.ResultCsvWriter;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Client extends QueryCLient<LongestTrip> {

    private static final String QUERY2_CSV = "query2.csv";
    private static final String QUERY2_CSV_HEADERS = "pickUpZone;longestDOZone;longestReqDateTime;longestMiles;longestCompany";
    private static final String DIVIDER = ";";
    private static final int OUTSIDE_NYC_LOCATION_ID = 265;

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client() throws IOException, ExecutionException, InterruptedException {
        super();
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        QueryCLient<LongestTrip> queryCLient = new Client();
    }

    @Override
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
        SortedSet<LongestTripResult> finalResult = new TreeSet<>(
                Comparator.comparing(LongestTripResult::pickUpZone));
        finalResult.addAll(result.values());

        ResultCsvWriter.writeCsv(params.getOutPath(), QUERY2_CSV, QUERY2_CSV_HEADERS, finalResult);
    }

    @Override
    public Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);
        Stream<Trip> tripStream = lines
                .skip(1)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.split(DIVIDER))
                .filter(cols -> cols.length >= 8)
                // Query 2: Filter trips that start and end within NYC (not Outside of NYC)
                .filter(cols -> {
                    int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                    int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
                    return pu != OUTSIDE_NYC_LOCATION_ID && doL != OUTSIDE_NYC_LOCATION_ID;
                })
                .filter(cols -> {
                    int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                    int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
                    return zones.containsKey(pu) && zones.containsKey(doL);
                })
                .map(pair -> parseTrip(pair, zones));
        return tripStream.onClose(lines::close);
    }
}
