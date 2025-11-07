package ar.edu.itba.pod.client.ytdMiles;

import ar.edu.itba.pod.api.common.RowParser;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.ytdMiles.YtdMilesTrip;

import java.util.Map;
import java.util.Optional;

public class YtdMilesTripParser implements RowParser<YtdMilesTrip> {
    @Override
    public Optional<YtdMilesTrip> parseRow(String[] cols, Map<Integer, Zone> zones) {
        try {
            String company = cols[TripsColumns.COMPANY.getIndex()].trim();
            String request_datetime = cols[TripsColumns.REQUEST_DATETIME.getIndex()].trim();
            Double trip_miles = Double.parseDouble(cols[TripsColumns.TRIP_MILES.getIndex()].trim());

            return Optional.of(new YtdMilesTrip(company, request_datetime, trip_miles));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

