package ar.edu.itba.pod.client.ytdMiles;

import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.ytdMiles.*;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.*;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Client {
    private static final String QUERY5_CSV = "query5.csv";
    private static final String QUERY5_CSV_HEADERS = "company;year;month;milesYTD";
    private static final int QUERY_NUMBER = 5;
    private static TimeLogger timeLogger = null;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final DefaultParams params;

    public Client(DefaultParams params) {
        String timeFile = params.getOutPath() + "/time" + QUERY_NUMBER + ".txt";
        this.timeLogger = new TimeLogger(timeFile);
        this.params = params;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);
            IMap<Long, YtdMilesTrip> iMap = hazelcastInstance.getMap("g5-ytdMiles");

            timeLogger.log("Inicio de la lectura del archivo", 40);
            CsvParser<YtdMilesTrip> csvParser = new CsvParser<>(iMap, new YtdMilesTripParser());
            csvParser.processAndLoadCSV(params.getInPath());
            timeLogger.log("Fin de la lectura del archivo", 43);

            timeLogger.log("Inicio del trabajo map/reduce", 45);

            KeyValueSource<Long, YtdMilesTrip> keyValueSource = KeyValueSource.fromMap(iMap);
            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-ytdMiles");
            Job<Long, YtdMilesTrip> job = jobTracker.newJob(keyValueSource);

            ICompletableFuture<List<YtdMilesResult>> future = job
                    .mapper(new YtdMilesMapper())
                    .combiner(new YtdMilesCombinerFactory())
                    .reducer(new YtdMilesReducerFactory())
                    .submit(new YtdMilesCollator());

            List<YtdMilesResult> result = future.get();
            ResultCsvWriter.writeCsv(params.getOutPath(), QUERY5_CSV, QUERY5_CSV_HEADERS, result);
            iMap.clear();

            timeLogger.log("Fin del trabajo map/reduce", 61);
        } finally {
            HazelcastClient.shutdownAll();
        }

    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(), Params.OUTPATH.getParam());
        Client client = new Client(params);
        client.run();
    }
}
