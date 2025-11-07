package ar.edu.itba.pod.client.longestWait;

import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.longestWait.*;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.CsvParser;
import ar.edu.itba.pod.client.utilities.CsvUtils;
import ar.edu.itba.pod.client.utilities.HazelcastClientFactory;
import ar.edu.itba.pod.client.utilities.ResultCsvWriter;
import ar.edu.itba.pod.client.utilities.TimeLogger;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Client {
    private static final String QUERY4_CSV = "query4.csv";
    private static final String QUERY4_HEADERS = "pickUpZone;dropOffZone;delayInSeconds";
    private static final int QUERY_NUMBER = 4;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);
    private static TimeLogger timeLogger = null;

    private final DefaultParams params;

    public Client(DefaultParams params) {
        String timeFile = params.getOutPath() + "/time" + QUERY_NUMBER + ".txt";
        timeLogger = new TimeLogger(timeFile);
        this.params = params;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        try {
            HazelcastInstance hz = HazelcastClientFactory.newHazelcastClient(params);
            IMap<Long, LongestWaitTripData> iMap = hz.getMap("g5-longestWait");

            Map<Integer, Zone> zones =
                    CsvUtils.getZones(CsvUtils.getFilesPath(params.getInPath()).getzonesFiles());
            String borough = System.getProperty(Params.BOROUGH.getParam());
            if (borough == null || borough.isBlank()) {
                throw new IllegalArgumentException("Falta parámetro -D" + Params.BOROUGH.getParam() + " (ej: -Dborough=Manhattan)");
            }

            timeLogger.log("Inicio de la lectura del archivo", 53);
            CsvParser<LongestWaitTripData> csvParser =
                    new CsvParser<>(iMap, new LongestWaitParser(zones, borough));
            csvParser.processAndLoadCSV(params.getInPath());
            timeLogger.log("Fin de la lectura del archivo", 57);

            KeyValueSource<Long, LongestWaitTripData> kvs = KeyValueSource.fromMap(iMap);
            JobTracker jobTracker = hz.getJobTracker("g5-longestWait");
            Job<Long, LongestWaitTripData> job = jobTracker.newJob(kvs);

            timeLogger.log("Inicio del trabajo map/reduce", 60);
            ICompletableFuture<List<LongestWaitResultRow>> future = job
                    .mapper(new LongestWaitMapper())
                    .combiner(new LongestWaitCombinerFactory())
                    .reducer(new LongestWaitReducerFactory())
                    .submit(new LongestWaitCollator());

            List<LongestWaitResultRow> rows = future.get();
            timeLogger.log("Fin el trabajo map/reduce", 87);

            rows.sort(
                    Comparator.comparing(LongestWaitResultRow::getPickUpZone)
                            .thenComparing(LongestWaitResultRow::getDropOffZone)
            );

            ResultCsvWriter.writeCsv(
                    params.getOutPath(),
                    QUERY4_CSV,
                    QUERY4_HEADERS,
                    rows
            );
            logger.info("Se escribió {}", QUERY4_CSV);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        DefaultParams params = DefaultParams.getParams(
                Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),
                Params.OUTPATH.getParam()
        );
        Client client = new Client(params);
        client.run();
    }
}
