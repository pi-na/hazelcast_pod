package ar.edu.itba.pod.client;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.Params;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.totalTrips.TotalTrips;
import ar.edu.itba.pod.client.params.DefaultParams;
import ar.edu.itba.pod.client.utilities.HazelcastClientFactory;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class QueryCLient <T>{

    private static final String CSV_EXTENSION = ".csv";
    private static final String DIVIDER = ";";
    private static final String TRIPS_IDENTIFIER = "trips";
    private static final String ZONES_IDENTIFIER = "zones";

    public QueryCLient() throws IOException, ExecutionException, InterruptedException {
        DefaultParams params = DefaultParams.getParams(Params.ADDRESSES.getParam(),
                Params.INPATH.getParam(),  Params.OUTPATH.getParam());
        try {
            HazelcastInstance hazelcastInstance = HazelcastClientFactory.newHazelcastClient(params);
            IMap<String, T> iMap = hazelcastInstance.getMap("g5");
            run(params, iMap);
            finishQuery(iMap, hazelcastInstance, params);
        }finally {
            HazelcastClient.shutdownAll();
        }
    }

    private static PairFiles getFilesPath(String inPath) {
        File folder = new File(inPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("The input path does not exist or is not a valid directory: " + inPath);
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("The input directory is empty or could not be read: " + inPath);
        }

        String tripsFile = null;
        String zonesFile = null;
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(CSV_EXTENSION)) {
                if (file.getName().contains(TRIPS_IDENTIFIER)) {
                    tripsFile = file.getAbsolutePath();
                }
                if (file.getName().contains(ZONES_IDENTIFIER)) {
                    zonesFile = file.getAbsolutePath();
                }
            }
        }

        if (tripsFile == null || zonesFile == null) {
            throw new IllegalArgumentException("Trips or zones CSV not found in: " + inPath);
        }

        return new PairFiles(tripsFile, zonesFile);
    }

    public abstract void finishQuery(IMap<String, T> iMap, HazelcastInstance hazelcastInstance, DefaultParams params) throws IOException, ExecutionException, InterruptedException;

    public static Trip parseTrip(String[] cols, Map<Integer, Zone> zones){
        Trip trip = new Trip();
        trip.setCompany(cols[TripsColumns.COMPANY.getIndex()]);
        trip.setRequest_datetime(cols[TripsColumns.REQUEST_DATETIME.getIndex()]);
        trip.setPickup_datetime(cols[TripsColumns.PICKUP_DATETIME.getIndex()]);
        trip.setDropoff_datetime(cols[TripsColumns.DROPOFF_DATETIME.getIndex()]);

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

    public abstract Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException;

    private Stream<Trip> parseRows(PairFiles files, Map<Integer, Zone> zones) throws IOException {
        return genericParseRows(files, zones, null);
    }

    private Stream<Trip> parseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        return genericParseRows(files, zones, borough);
    }

    private static Map<Integer, Zone> getZones(String zonesFilePath) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(zonesFilePath), StandardCharsets.UTF_8)) {
            return lines.skip(1) // skip header
                    .map(line -> line.split(DIVIDER))
                    .collect(Collectors.toMap(
                            cols -> Integer.parseInt(cols[0].trim()),
                            cols -> new Zone(
                                    Integer.parseInt(cols[0].trim()),
                                    cols[1].trim(),
                                    cols[2].trim()
                            )
                    ));
        }
    }

    public  <V> void run(DefaultParams params, IMap<String, V> iMap) throws IOException {
        PairFiles csvsPath = getFilesPath(params.getInPath());
        Map<Integer, Zone> zones = getZones(csvsPath.getzonesFiles());
        try (Stream<Trip> rows = parseRows(csvsPath, zones)) {
            final AtomicInteger auxKey = new AtomicInteger();
            rows.parallel().forEach(row -> {
                iMap.putAsync(String.valueOf(auxKey.getAndIncrement()), (V) row);
            });
        }
    }
}
