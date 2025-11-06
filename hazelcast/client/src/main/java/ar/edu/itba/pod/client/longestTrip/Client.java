package ar.edu.itba.pod.client.longestTrip;

import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.longestTrip.*;
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
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class Client {
    private static final String QUERY2_CSV = "query2.csv";
    private static final String QUERY2_CSV_HEADERS = "pickUpZone;longestDOZone;longestReqDateTime;longestMiles;longestCompany";
    private static final int QUERY_NUMBER = 2;
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
            IMap<Long, LongestTrip> iMap = hazelcastInstance.getMap("g5");

            timeLogger.log("Inicio de la lectura del archivo", 82);
            CsvParser<LongestTrip> csvParser = new CsvParser<>(iMap, new LongestTripParser());
            csvParser.processAndLoadCSV(params.getInPath());
            timeLogger.log("Fin de la lectura del archivo", 84);

            timeLogger.log("Inicio del trabajo map/reduce", 85);

            KeyValueSource<Long, LongestTrip> keyValueSource = KeyValueSource.fromMap(iMap);
            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-longest-trip");
            Job<Long, LongestTrip> job = jobTracker.newJob(keyValueSource);

            ICompletableFuture<Map<String, LongestTripResult>> future = job
                    .mapper(new LongestTripMapper())
                    .combiner(new LongestTripCombinerFactory())
                    .reducer(new LongestTripReducerFactory())
                    .submit(new LongestTripCollator());

            Map<String, LongestTripResult> result = future.get();
            logger.info("Map/Reduce finalizado, post-procesando y escribiendo resultados en CSV...");

            Map<Integer, Zone> zones = CsvUtils.getZones(CsvUtils.getFilesPath(params.getInPath()).getzonesFiles());

            SortedSet<LongestTripOutput> finalResult = new TreeSet<>(
                    Comparator.comparing(LongestTripOutput::pickUpZone));

            for (LongestTripResult r : result.values()) {
                Zone puZone = zones.get((int) r.getKey());
                Zone doZone = zones.get((int) r.getLongestDOZone());

                if (puZone == null || doZone == null) {
                    continue;
                }

                String puZoneName = puZone.getZoneName();
                String doZoneName = doZone.getZoneName();

                finalResult.add(new LongestTripOutput(
                        puZoneName, doZoneName, r.getLongestReqDateTime(), r.getLongestMiles(), r.getLongestCompany()
                ));
            }

            ResultCsvWriter.writeCsv(params.getOutPath(), QUERY2_CSV, QUERY2_CSV_HEADERS, finalResult);
            logger.info("Se escribio el archivo");

            iMap.destroy();
            logger.info("IMap destruido");

            timeLogger.log("Fin del trabajo map/reduce", 87);
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
