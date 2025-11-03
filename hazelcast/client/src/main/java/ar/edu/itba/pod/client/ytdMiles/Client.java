package ar.edu.itba.pod.client.ytdMiles;

import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.ytdMiles.*;
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

    private static final String QUERY5_CSV = "query5.csv";
    private static final String QUERY5_CSV_HEADERS = "company;year;month;milesYTD";

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);

            IMap<String, YtdMilesTrip> iMap = hazelcastInstance.getMap("g5-ytd-miles");

            System.out.println("🚀 Starting CsvDataLoader...");
            CsvDataLoader.run(params, false, iMap);
            System.out.println("✅ CsvDataLoader finished.");

            KeyValueSource<String, YtdMilesTrip> keyValueSource = KeyValueSource.fromMap(iMap);

            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-ytd-miles");
            Job<String, YtdMilesTrip> job = jobTracker.newJob(keyValueSource);

            ICompletableFuture<List<YtdMilesResult>> future = job
                    .mapper(new YtdMilesMapper())
                    .combiner(new YtdMilesCombinerFactory())
                    .reducer(new YtdMilesReducerFactory())
                    .submit(new YtdMilesCollator());


            List<YtdMilesResult> result = future.get();

            ResultCsvWriter.writeCsv(params.getOutPath(), QUERY5_CSV, QUERY5_CSV_HEADERS, result);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}
