package ar.edu.itba.pod.client.AveragePrice;

import ar.edu.itba.pod.api.AveragePrice.*;
import ar.edu.itba.pod.api.enums.Params;
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

    private static final String QUERY3_CSV = "query3.csv";
    private static final String QUERY3_HEADERS = "pickUpBorough;company;avgFare";

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);

            IMap<String, CompanyTrips> iMap = hazelcastInstance.getMap("g5-average-price");

            System.out.println("🚀 Starting CsvDataLoader...");
            CsvDataLoader.run(params, true, iMap);
            System.out.println("✅ CsvDataLoader finished.");

            KeyValueSource<String, CompanyTrips> keyValueSource = KeyValueSource.fromMap(iMap);

            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-average-price");
            Job<String, CompanyTrips> job = jobTracker.newJob(keyValueSource);

            ICompletableFuture<Map<String, AveragePriceResult>> future = job
                    .mapper(new AveragePriceMapper())
                    .combiner(new AveragePriceCombinerFactory())
                    .reducer(new AveragePriceReducerFactory())
                    .submit(new AveragePriceCollator());


            Map<String, AveragePriceResult> result = future.get();
            SortedSet<AveragePriceResult> finalResult = new TreeSet<>(
                    Comparator.comparing(AveragePriceResult::getAvgFare).reversed()
                            .thenComparing(AveragePriceResult::getPickUpBorough)
                            .thenComparing(AveragePriceResult::getCompany));
            finalResult.addAll(result.values());

            ResultCsvWriter.writeCsv(params.getOutPath(), QUERY3_CSV, QUERY3_HEADERS, finalResult);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}
