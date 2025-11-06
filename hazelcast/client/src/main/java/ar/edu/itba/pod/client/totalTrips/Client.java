package ar.edu.itba.pod.client.totalTrips;

import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.totalTrips.*;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.*;
import com.hazelcast.client.HazelcastClient;
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
    private static final String QUERY1_CSV = "query1.csv";
    private static final String QUERY1_CSV_HEADERS = "pickUpZone;dropOffZone;trips";
    private static final int QUERY_NUMBER = 1;
    private static TimeLogger timeLogger = null;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);
    private final DefaultParams params;

    public Client(DefaultParams params)  {
        String timeFile = params.getOutPath() + "/time" + QUERY_NUMBER + ".txt";
        this.timeLogger = new TimeLogger(timeFile);
        this.params = params;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);
            IMap<Long, MinimalTrip> iMap = hazelcastInstance.getMap("g5");

            long start = System.nanoTime();
            timeLogger.log("Inicio de la lectura del archivo", 82);
            CsvParser<MinimalTrip> csvParser = new CsvParser<>(iMap, new MinimalTripParser());
            csvParser.processAndLoadCSV(params.getInPath());
            timeLogger.log("Fin de la lectura del archivo", 84);
            long end = System.nanoTime();
            System.out.println("Tiempo csv load: " + (end - start)/1_000_000 + " ms");

            // TODO: POR QUE EL TIME LOGGER NECESITA EL LINE NUMBER??????????????
            timeLogger.log("Inicio del trabajo map/reduce", 85);

            KeyValueSource<Long, MinimalTrip> keyValueSource = KeyValueSource.fromMap(iMap);
            JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-total-trips");
            Job<Long, MinimalTrip> job = jobTracker.newJob(keyValueSource);

            start = System.nanoTime();
            ICompletableFuture<Map<String, TotalTripsResult>> future = job
                    .mapper(new TotalTripsMapper())
                    .combiner(new TotalTripsCombinerFactory())
                    .reducer(new TotalTripsReducerFactory())
                    .submit(new TotalTripsCollator());

            Map<String, TotalTripsResult> result = future.get();
            end = System.nanoTime();
            System.out.println("MapReduce time: " + (end - start)/1_000_000 + " ms");
            // TODO AJUSTAR LOGS PARA MATCHEAR CONSIGNA
            logger.info("Map/Reduce finalizado, post-procesando y escribiendo resultados en CSV...");

            Map<Integer, Zone> zones = CsvUtils.getZones(CsvUtils.getFilesPath(params.getInPath()).getzonesFiles());

            SortedSet<TotalTripOutput> finalOutput = new TreeSet<>(
                    Comparator.comparing(TotalTripOutput::total).reversed()
                            .thenComparing(TotalTripOutput::pickUpZone)
                            .thenComparing(TotalTripOutput::dropOffZone)
            );

            for (TotalTripsResult r : result.values()) {
                String puName = zones.get(r.pickUpZone()).getZoneName();
                String doName = zones.get(r.dropOffZone()).getZoneName();

                finalOutput.add(new TotalTripOutput(
                        puName,
                        doName,
                        r.total()
                ));
            }

            ResultCsvWriter.writeCsv(
                    params.getOutPath(),
                    QUERY1_CSV,
                    QUERY1_CSV_HEADERS,
                    finalOutput
            );

            timeLogger.log("Fin del trabajo map/reduce", 87);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // TODO INTERFAZ PARAMS O ALGUNA MANERA DE RESOLVER HERENCIA (bah esta resuelta??...))
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        int queryNumber = 1; // TODO: Cambiar segun el query
        Client client = new Client(params);
        client.run();
    }
}
