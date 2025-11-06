package ar.edu.itba.pod.client.averagePrice;

import ar.edu.itba.pod.api.AveragePrice.*;
import ar.edu.itba.pod.api.AveragePrice.AveragePriceOutput;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class Client {
    private static final String QUERY3_CSV = "query3.csv";
    private static final String QUERY3_HEADERS = "pickUpBorough;company;avgFare";
    private static final int QUERY_NUMBER = 3;
    private static TimeLogger timeLogger = null;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);
    private final DefaultParams params;

    public Client(DefaultParams params)  {
        String timeFile = params.getOutPath() + "/time" + QUERY_NUMBER + ".txt";
        this.timeLogger = new TimeLogger(timeFile);
        this.params = params;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);
        IMap<Long, TripData> iMap = hazelcastInstance.getMap("g5");
        Map<Integer, Zone> zones = CsvUtils.getZones(CsvUtils.getFilesPath(params.getInPath()).getzonesFiles());

        timeLogger.log("Inicio de la lectura del archivo", 42);
        CsvParser<TripData> csvParser = new CsvParser<>(iMap, new AveragePriceParser(zones));
        csvParser.processAndLoadCSV(params.getInPath());
        timeLogger.log("Fin de la lectura del archivo", 45);

        timeLogger.log("Inicio del trabajo map/reduce", 47);

        KeyValueSource<Long, TripData> keyValueSource = KeyValueSource.fromMap(iMap);
        JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-average-price");
        Job<Long, TripData> job = jobTracker.newJob(keyValueSource);

        ICompletableFuture<Map<AverageKeyOut, Double>> future = job
                .mapper(new AveragePriceMapper())
                .reducer(new AveragePriceReducerFactory())
                .submit();

        Map<AverageKeyOut, Double> result = future.get();
        
        timeLogger.log("Inicio del trabajo map/reduce", 47);

        // Orden: DESC por avgFare, luego alfabetico por borough y luego por company
        SortedSet<AveragePriceOutput> finalOutput = new TreeSet<>(
                Comparator.<AveragePriceOutput>comparingDouble(AveragePriceOutput::avgFare).reversed()
                        .thenComparing(AveragePriceOutput::pickUpBorough)
                        .thenComparing(AveragePriceOutput::company)
        );

        for (Map.Entry<AverageKeyOut, Double> e : result.entrySet()) {
            finalOutput.add(new AveragePriceOutput(
                    e.getKey().getPickUpBorough(),
                    e.getKey().getCompany(),
                    e.getValue() // ya viene truncado a 2 decimales desde el Reducer
            ));
        }

        // Escribimos CSV
        ResultCsvWriter.writeCsv(
                params.getOutPath(),
                "query3.csv",
                "pickUpBorough;company;avgFare",
                finalOutput
        );
        logger.info("Se escribio el archivo");

        iMap.destroy();
        logger.info("IMap destruido");

        timeLogger.log("Fin del trabajo map/reduce", 87);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        Client client = new Client(params);
        client.run();
    }
}
