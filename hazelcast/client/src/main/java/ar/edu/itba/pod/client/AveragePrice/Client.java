package ar.edu.itba.pod.client.AveragePrice;

import ar.edu.itba.pod.api.AveragePrice.*;
import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
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


public class Client extends QueryCLient<CompanyTrips> {

    private static final String QUERY3_CSV = "query3.csv";
    private static final String QUERY3_HEADERS = "pickUpBorough;company;avgFare";
    private static final String DIVIDER = ";";

    private static final Logger logger = LoggerFactory.getLogger(ar.edu.itba.pod.client.AveragePrice.Client.class);

    public Client() throws IOException, ExecutionException, InterruptedException {super();}

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        QueryCLient queryCLient = new Client();
    }

    @Override
    public void finishQuery(IMap<Long, CompanyTrips> iMap, HazelcastInstance hazelcastInstance, DefaultParams params) throws IOException, ExecutionException, InterruptedException {
        KeyValueSource<Long, CompanyTrips> keyValueSource = KeyValueSource.fromMap(iMap);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-average-price");
        Job<Long, CompanyTrips> job = jobTracker.newJob(keyValueSource);

        ICompletableFuture<Map<String, AveragePriceResult>> future = job
                .mapper(new AveragePriceMapper())
                .combiner(new AveragePriceCombinerFactory())
                .reducer(new AveragePriceReducerFactory())
                .submit(new AveragePriceCollator());

        Map<String, AveragePriceResult> result = future.get();
        logger.info(result.size() + " results found");
        SortedSet<AveragePriceResult> finalResult = new TreeSet<>(
                Comparator.comparing(AveragePriceResult::getAvgFare).reversed()
                        .thenComparing(AveragePriceResult::getPickUpBorough)
                        .thenComparing(AveragePriceResult::getCompany));
        finalResult.addAll(result.values());
        ResultCsvWriter.writeCsv(params.getOutPath(), QUERY3_CSV, QUERY3_HEADERS, finalResult);
    }


    @Override
    public Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        final String OUTSIDE = "Outside of NYC";

        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);

        Stream <Trip> tripStream = lines
                .skip(1)
                .map(String::trim)
                .map(line -> line.split(DIVIDER, -1))
                .filter(cols -> {
                    int pu;
                    try {
                        pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                    } catch (Exception e) {
                        return false;
                    }
                    Zone z = zones.get(pu);
                    if (z == null) return false;
                    return !(OUTSIDE.equalsIgnoreCase(z.getZoneName()) || OUTSIDE.equalsIgnoreCase(z.getBorough()));
                })
                .map(cols -> parseTrip(cols, zones));

        return tripStream.onClose(lines::close);
    }

}
