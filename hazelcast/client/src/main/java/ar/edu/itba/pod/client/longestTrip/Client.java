package ar.edu.itba.pod.client.longestTrip;

import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.longestTrip.*;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.CsvDataLoader;
import ar.edu.itba.pod.client.utilities.HazelcastClientFactory;
import ar.edu.itba.pod.client.utilities.ResultCsvWriter;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Client {

    private static final String QUERY2_CSV = "query2.csv";
    private static final String QUERY2_CSV_HEADERS = "pickUpZone;longestDOZone;longestReqDateTime;longestMiles;longestCompany";

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);

            IMap<String, LongestTrip> iMap = hazelcastInstance.getMap("g5-longest-trip");

            System.out.println("🚀 Starting CsvDataLoader...");
            CsvDataLoader.run(params, true, iMap);
            System.out.println("✅ CsvDataLoader finished.");

            KeyValueSource<String, LongestTrip> keyValueSource = KeyValueSource.fromMap(iMap);

            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-longest-trip");
            Job<String, LongestTrip> job = jobTracker.newJob(keyValueSource);

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
        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}


