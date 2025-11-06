package ar.edu.itba.pod.client.totalTrips;

import ar.edu.itba.pod.api.common.RowParser;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.totalTrips.MinimalTrip;

import java.util.Map;
import java.util.Optional;

public class MinimalTripParser implements RowParser<MinimalTrip> {
    @Override
    public Optional<MinimalTrip> parseRow(String[] cols, Map<Integer, Zone> zones) {
        //QUERY1 pide que solo se consideren los que salen y llegan a zonas difrentes
        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
        int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
        if(pu == doL || !zones.containsKey(pu) || !zones.containsKey(doL)) return Optional.empty();

        return Optional.of(new MinimalTrip((short)pu, (short)doL));
    }
}
