package ar.edu.itba.pod.client.longestTrip;

import ar.edu.itba.pod.api.common.RowParser;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.longestTrip.LongestTrip;

import java.util.Map;
import java.util.Optional;

public class LongestTripParser implements RowParser<LongestTrip> {
    private static final int OUTSIDE_NYC_LOCATION_ID = 265;

    @Override
    public Optional<LongestTrip> parseRow(String[] cols, Map<Integer, Zone> zones) {
        try {
            int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
            int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
            
            if (pu == OUTSIDE_NYC_LOCATION_ID || doL == OUTSIDE_NYC_LOCATION_ID
                    || !zones.containsKey(pu) || !zones.containsKey(doL)) {
                return Optional.empty();
            }

            String request = cols[TripsColumns.REQUEST_DATETIME.getIndex()].trim();
            double miles = Double.parseDouble(cols[TripsColumns.TRIP_MILES.getIndex()].trim());
            String company = cols[TripsColumns.COMPANY.getIndex()].trim();

            return Optional.of(new LongestTrip((short) pu, (short) doL, request, miles, company));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

