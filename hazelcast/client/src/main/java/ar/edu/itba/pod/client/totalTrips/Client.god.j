package ar.edu.itba.pod.client.totalTrips;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.ParsedRow;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.totalTrips.*;
import ar.edu.itba.pod.client.params.DefaultParams;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static ar.edu.itba.pod.client.QueryCLient.getFilesPath;
import static ar.edu.itba.pod.client.QueryCLient.getZones;

// TODO: Client<T>
//       GPT dice que puedo evitar todos los ? extends si uso tipo en la clase duhhhhhhh
//Opción robusta (si querés que sea realmente genérico a futuro): parametrizá el cliente con el tipo y un parser:
//
//        public interface RowParser<V extends ParsedRow> {
//            Optional<V> parse(String[] cols, Map<Integer, Zone> zones);
//        }
//
//        public class Client<V extends ParsedRow> {
//            private final RowParser<V> parser;
//    ...
//            Optional<V> parsed = parser.parse(cols, zones);
//    parsed.ifPresent(row -> batchMap.put(keyCounter.getAndIncrement(), row)); // sin cast
//        }
//
//
//        Esto elimina por completo el cast.
public class Client {
    private static final String CSV_EXTENSION = ".csv";
    private static final String DIVIDER = ";";
    private static final String TRIPS_IDENTIFIER = "trips";
    private static final String ZONES_IDENTIFIER = "zones";
    private static TimeLogger timeLogger = null;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Client.class);
    private final DefaultParams params;


    // TODO: Variables especificas de query1!
    private static final String QUERY1_CSV = "query1.csv";
    private static final String QUERY1_CSV_HEADERS = "pickUpZone;dropOffZone;trips";
    private static final int QUERY_NUMBER = 1;

    // TODO: Clase "params" que extienda de DefaultParams. El constructor de Client() recibe un objeto de esa clase
    //       Entonces cada queryClient crea su clase params o usa la default
    public Client(int queryNumber, DefaultParams params)  {
        String timeFile = params.getOutPath() + "/time" + queryNumber + ".txt";
        this.timeLogger = new TimeLogger(timeFile);
        this.params = params;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);
            // TODO: USAR TIPO GENERICO <T> EN VEZ DE TOTAL TRIPS
            IMap<Long, MinimalTrip> iMap = hazelcastInstance.getMap("g5");

            // TODO: POR QUE EL TIME LOGGER NECESITA EL LINE NUMBER??????????????
            timeLogger.log("Inicio de la lectura del archivo", 82);
            processAndLoadCSV(iMap);
            timeLogger.log("Fin de la lectura del archivo", 84);
            timeLogger.log("Inicio del trabajo map/reduce", 85);
            finishQuery(iMap, hazelcastInstance, params);
            timeLogger.log("Fin del trabajo map/reduce", 87);
        }finally {
            HazelcastClient.shutdownAll();
        }
    }

    public void finishQuery(IMap<Long, MinimalTrip> iMap, HazelcastInstance hazelcastInstance, DefaultParams params) throws IOException, ExecutionException, InterruptedException {
        KeyValueSource<Long, MinimalTrip> keyValueSource = KeyValueSource.fromMap(iMap);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("g5-total-trips");
        Job<Long, MinimalTrip> job = jobTracker.newJob(keyValueSource);

        ICompletableFuture<Map<String, TotalTripsResult>> future = job
                .mapper(new TotalTripsMapper())
                .combiner(new TotalTripsCombinerFactory())
                .reducer(new TotalTripsReducerFactory())
                .submit(new TotalTripsCollator());


        Map<String, TotalTripsResult> result = future.get();

        logger.info("Map/Reduce finalizado, post-procesando y escribiendo resultados en CSV...");
        SortedSet<TotalTripsResult> finalResult = new TreeSet<>(
                Comparator.comparing(TotalTripsResult::total).reversed()
                        .thenComparing(TotalTripsResult::pickUpZone)
                        .thenComparing(TotalTripsResult::dropOffZone));
        finalResult.addAll(result.values());

        ResultCsvWriter.writeCsv(params.getOutPath(), QUERY1_CSV, QUERY1_CSV_HEADERS, finalResult);
        logger.info("Se escribio el archivo");

        iMap.destroy();
        logger.info("IMap destruido");
    }

    // AHORA MISMO COMBINA ParseTrip con GenericParseRows de TotalTrips
    // esta hardcodeado para Trip//TotalTrip!!
    public Optional<? extends ParsedRow> parseRow(String[] cols, Map<Integer, Zone> zones) {

        //QUERY1 pide que solo se consideren los que salen y llegan a zonas difrentes
        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
        int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
        if(pu == doL || !zones.containsKey(pu) || !zones.containsKey(doL)) return Optional.empty();

        return Optional.of(new MinimalTrip((short)pu, (short)doL));
    }

    // implements o extends???
    public <V extends ParsedRow> void processAndLoadCSV(IMap<Long, V> iMap) throws IOException {
        // TODO Transformar QueryCLient en clase de utilidades static y usar estos dos metodos de ahi
        PairFiles csvsPath = getFilesPath(params.getInPath());
        Map<Integer, Zone> zones = getZones(csvsPath.getzonesFiles());

        // TODO: Probar batch sizes
        final int BATCH_SIZE = 1000;
        final AtomicLong keyCounter = new AtomicLong(0);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvsPath.gettripsFile()), StandardCharsets.UTF_8)) {
            reader.readLine(); // saltar header
            String line;

            Map<Long, V> batchMap = new HashMap<>((int)(BATCH_SIZE / 0.75f) + 1, 0.75f);

            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(";");
                Optional<? extends ParsedRow> parsedRow = parseRow(cols, zones);

                parsedRow.ifPresent(row -> {
                    batchMap.put(keyCounter.getAndIncrement(), (V) row); //Casteo de ? extends ParsedRow a V!!!
                });


                if (batchMap.size() >= BATCH_SIZE) {
                    loadBatchToCluster(iMap, batchMap);
                }
            }

            if (!batchMap.isEmpty()) {
                loadBatchToCluster(iMap, batchMap);
            }
        }

    }

    private <V> void loadBatchToCluster(IMap<Long, V> iMap, Map<Long, V> batchMap) {
        long start = System.nanoTime();
        iMap.putAll(batchMap);
        logger.info("Batch of {} took {} ms", batchMap.size(), (System.nanoTime() - start) / 1_000_000);
        batchMap.clear();
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        // TODO INTERFAZ PARAMS O ALGUNA MANERA DE RESOLVER HERENCIA (bah esta resuelta??...))
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        int queryNumber = 1; // TODO: Cambiar segun el query
        Client client = new Client(queryNumber, params);
        client.run();
    }
}
