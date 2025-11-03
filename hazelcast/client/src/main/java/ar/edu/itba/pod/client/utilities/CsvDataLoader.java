package ar.edu.itba.pod.client.utilities;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.client.params.DefaultParams;
import com.hazelcast.core.IMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvDataLoader {
    private static final String CSV_EXTENSION = ".csv";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DIVIDER = ";";
    private static final String TRIPS_IDENTIFIER = "trips";
    private static final String ZONES_IDENTIFIER = "zones";

    private CsvDataLoader() {}

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

    private static Trip parseTrip(String[] cols, Map<Integer, Zone> zones) {
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

    private static Stream<Trip> genericParseRows(PairFiles files, boolean filterByZone, Map<Integer, Zone> zones, String borough) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);
        Stream<Trip> tripStream= lines
                    .skip(1)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(DIVIDER))
                    .filter(cols -> cols.length >= 8)
                    //TODO esto se puede sacar a una funcion que se llame solo si me mandaron filterByZone
                    .filter(cols -> {
                        if (!filterByZone) return true;
                        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                        int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
                        return zones.containsKey(pu) && zones.containsKey(doL);
                    })
                    //TODO: esto es a modo de test, habria que ver como hacerlo ams eficiente con todos los 
                    .limit(1000)
                .map(pair -> parseTrip(pair, zones));
        return tripStream.onClose(lines::close);
    }

    private static Stream<Trip> parseRows(PairFiles files, boolean filterByZone, Map<Integer, Zone> zones) throws IOException {
        return genericParseRows(files, filterByZone, zones, null);
    }

    private static Stream<Trip> parseRows(PairFiles files, boolean filterByZone, Map<Integer, Zone> zones, String borough) throws IOException {
        return genericParseRows(files, filterByZone, zones, borough);
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

    public static <V> void run(DefaultParams params, boolean filterByZone, IMap<String, V> iMap) throws IOException {
        PairFiles csvsPath = getFilesPath(params.getInPath());
        Map<Integer, Zone> zones = getZones(csvsPath.getzonesFiles());
        try (Stream<Trip> rows = parseRows(csvsPath, filterByZone, zones)) {
            final AtomicInteger auxKey = new AtomicInteger();
            rows.parallel().forEach(row -> {
                iMap.putAsync(String.valueOf(auxKey.getAndIncrement()), (V) row);
            });
        }
    }
}
