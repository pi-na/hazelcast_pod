package ar.edu.itba.pod.client.totalTrips;

import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.totalTrips.*;
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

    private static final String QUERY1_CSV = "query1.csv";
    private static final String QUERY1_CSV_HEADERS = "pickUpZone;dropOffZone;trips";

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);

            IMap<String, TotalTrips> iMap = hazelcastInstance.getMap("g5-total-trips");

            System.out.println("🚀 Starting CsvDataLoader...");
            CsvDataLoader.run(params, true, iMap);
            System.out.println("✅ CsvDataLoader finished.");

            KeyValueSource<String, TotalTrips> keyValueSource = KeyValueSource.fromMap(iMap);

            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-total-trips");
            Job<String, TotalTrips> job = jobTracker.newJob(keyValueSource);

            ICompletableFuture<Map<String, TotalTripsResult>> future = job
                    .mapper(new TotalTripsMapper())
                    .combiner(new TotalTripsCombinerFactory())
                    .reducer(new TotalTripsReducerFactory())
                    .submit(new TotalTripsCollator());


            Map<String, TotalTripsResult> result = future.get();
            SortedSet<TotalTripsResult> finalResult = new TreeSet<>(
                    Comparator.comparing(TotalTripsResult::total).reversed()
                            .thenComparing(TotalTripsResult::pickUpZone)
                            .thenComparing(TotalTripsResult::dropOffZone));

            ResultCsvWriter.writeCsv(params.getOutPath(), QUERY1_CSV, QUERY1_CSV_HEADERS, finalResult);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}
