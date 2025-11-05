package ar.edu.itba.pod.client.ytdMiles;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.ytdMiles.*;
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

public class Client extends QueryCLient<YtdMilesTrip> {

    private static final String QUERY5_CSV = "query5.csv";
    private static final String QUERY5_CSV_HEADERS = "company;year;month;milesYTD";
    private static final String DIVIDER = ";";
    private static final int QUERY_NUMBER = 5;

    public Client() throws IOException, ExecutionException, InterruptedException {
        super(QUERY_NUMBER);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        QueryCLient<YtdMilesTrip> queryCLient = new Client();
    }

    @Override
    public void finishQuery(IMap<Long, YtdMilesTrip> iMap, HazelcastInstance hazelcastInstance, DefaultParams params) 
            throws IOException, ExecutionException, InterruptedException {
        
        KeyValueSource<Long, YtdMilesTrip> keyValueSource = KeyValueSource.fromMap(iMap);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-ytd-miles");
        Job<Long, YtdMilesTrip> job = jobTracker.newJob(keyValueSource);

        ICompletableFuture<List<YtdMilesResult>> future = job
                .mapper(new YtdMilesMapper())
                .combiner(new YtdMilesCombinerFactory())
                .reducer(new YtdMilesReducerFactory())
                .submit(new YtdMilesCollator());

        List<YtdMilesResult> result = future.get();

        ResultCsvWriter.writeCsv(params.getOutPath(), QUERY5_CSV, QUERY5_CSV_HEADERS, result);
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
                // Query 5 doesn't need zone filtering
                .map(pair -> parseTrip(pair, zones));
        return tripStream.onClose(lines::close);
    }
}
