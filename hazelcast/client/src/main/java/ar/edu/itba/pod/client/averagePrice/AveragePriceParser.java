package ar.edu.itba.pod.client.averagePrice;

import ar.edu.itba.pod.api.AveragePrice.TripData;
import ar.edu.itba.pod.api.common.RowParser;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;

import java.util.Map;
import java.util.Optional;

public class AveragePriceParser implements RowParser<TripData> {
    private static final int OUTSIDE_OF_NYC_ZONE_ID = 265;
    private int outsideOfNycZoneId;

    public AveragePriceParser(Map<Integer, Zone> zones) {
        for (Map.Entry<Integer, Zone> e : zones.entrySet()) {
            String name = e.getValue().getZoneName();
            String bor  = e.getValue().getBorough();
            if ("outside of nyc".equalsIgnoreCase(name) || "outside of nyc".equalsIgnoreCase(bor)) {
                this.outsideOfNycZoneId = e.getKey();
                return;
            }
        }
        throw new IllegalStateException("'Outside of NYC' no encontrado en zones.csv");
    }

    @Override
    public Optional<TripData> parseRow(String[] cols, Map<Integer, Zone> zones) {
        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
        int doL = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
        // Solo se deben considerar los viajes que inician en una zona distinta a "Outside of NYC"
        // Solo se deben listar las zonas presentes en el archivo de zonas
        if(pu == outsideOfNycZoneId || !zones.containsKey(pu) || !zones.containsKey(doL)) return Optional.empty();

        String pickupBorough = zones.get(pu).getBorough();
        double basePassengerFare = Double.parseDouble(cols[TripsColumns.BASE_PASSENGER_FARE.getIndex()].trim());
        String company = cols[TripsColumns.COMPANY.getIndex()].trim();

        return Optional.of(new TripData(pickupBorough, basePassengerFare, company));
    }
}
