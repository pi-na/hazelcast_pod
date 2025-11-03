package ar.edu.itba.pod.client.totalTrips;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.totalTrips.*;
import ar.edu.itba.pod.client.QueryCLient;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Client extends QueryCLient {

    private static final String QUERY1_CSV = "query1.csv";
    private static final String QUERY1_CSV_HEADERS = "pickUpZone;dropOffZone;trips";
    private static final String DIVIDER = ";";

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client() throws IOException, ExecutionException, InterruptedException {super();}

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        QueryCLient queryCLient = new Client();
    }

    @Override
    public void finishQuery(IMap<String, TotalTrips> iMap, HazelcastInstance hazelcastInstance, DefaultParams params) throws IOException, ExecutionException, InterruptedException {
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
        finalResult.addAll(result.values());

        ResultCsvWriter.writeCsv(params.getOutPath(), QUERY1_CSV, QUERY1_CSV_HEADERS, finalResult);
    }

    @Override
    public Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);
        Stream<Trip> tripStream= lines
                .skip(1)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.split(DIVIDER))
                .filter(cols -> cols.length >= 8)
                .filter(cols -> {
                    int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                    int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
                    return pu != doL;
                })
                //TODO esto se puede sacar a una funcion que se llame solo si me mandaron filterByZone
                .filter(cols -> {
                    int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                    int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
                    return zones.containsKey(pu) && zones.containsKey(doL);
                })
                //TODO: esto es a modo de test, habria que ver como hacerlo ams eficiente con todos los
                .limit(100000)
                .map(pair -> parseTrip(pair, zones));
        return tripStream.onClose(lines::close);
    }

    @Override
    public Trip parseTrip(String[] cols, Map<Integer, Zone> zones) {
        Trip trip = new Trip();
        int puCode = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
        int doCode = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());

        trip.setPULocation(puCode);
        trip.setDOLocation(doCode);
        trip.setTrip_miles(Double.parseDouble(cols[TripsColumns.TRIP_MILES.getIndex()]));
        trip.setBase_passenger_fare(Double.parseDouble(cols[TripsColumns.BASE_PASSENGER_FARE.getIndex()]));

        Zone puZone = zones.get(puCode);
        Zone doZone = zones.get(doCode);
        if (puZone != null) {
            trip.setPickup_location(puZone.getZoneName());
        }
        if (doZone != null) {
            trip.setDropoff_location(doZone.getZoneName());
        }

        return trip;
    }
}
