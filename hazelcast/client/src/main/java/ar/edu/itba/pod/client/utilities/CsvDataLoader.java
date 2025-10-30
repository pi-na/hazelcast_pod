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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                if (!file.getName().contains(TRIPS_IDENTIFIER)) {
                    tripsFile = file.getAbsolutePath();
                }
                if (file.getName().contains(ZONES_IDENTIFIER)) {
                    zonesFile = file.getAbsolutePath();
                }
            }
        }

        if (tripsFile == null || zonesFile == null) {
            throw new IllegalArgumentException("");
        }

        return new PairFiles(tripsFile, zonesFile);
    }

    private static List<Trip> genericParseRows(PairFiles files, boolean filterByZone, Map<Integer, Zone> zones, String borough){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        try (Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8)) {
            return lines.skip(1)
                    .map(line -> line.split(DIVIDER))
                    .filter(cols -> !filterByZone ||
                            (zones.containsKey(Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim())) &&
                                    zones.containsKey(Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim()))))
                    .map(cols -> {
                        Trip trip = new Trip();
                        trip.setCompany(cols[TripsColumns.COMPANY.getIndex()].trim());
                        trip.setRequest_datetime(cols[TripsColumns.REQUEST_DATETIME.getIndex()].trim());
                        trip.setPickup_datetime(cols[TripsColumns.PICKUP_DATETIME.getIndex()].trim());
                        trip.setDropoff_datetime(cols[TripsColumns.DROPOFF_DATETIME.getIndex()].trim());
                        trip.setPULocation(Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim()));
                        trip.setDOLocation(Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim()));
                        trip.setTrip_miles(Double.parseDouble(cols[TripsColumns.TRIP_MILES.getIndex()].trim()));
                        trip.setBase_passenger_fare(Double.parseDouble(cols[TripsColumns.BASE_PASSENGER_FARE.getIndex()].trim()));
                        return trip;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading trips file: " + files.gettripsFile(), e);
        }
    }

    private static List<Trip> parseRows(PairFiles files, boolean filterByZone, Map<Integer, Zone> zones) throws IOException {
        return genericParseRows(files, filterByZone, zones, null);
    }

    private static List<Trip> parseRows(PairFiles files, boolean filterByZone, Map<Integer, Zone> zones, String borough) throws IOException {
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
        String borough = null; //TODO esto si DefaultParams es instance of BoroughParams deberia llamar a otro ParseRows
        Map<Integer, Zone> zones = getZones(csvsPath.getzonesFiles());
        List<Trip> rows = borough == null ? parseRows(csvsPath, filterByZone, zones) : parseRows(csvsPath, filterByZone, zones, borough);
        final AtomicInteger auxKey = new AtomicInteger();
        rows.forEach(row -> {
            V value = (V) row;
            iMap.put(String.valueOf(auxKey.getAndIncrement()), value);
        });
    }
}
