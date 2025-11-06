package ar.edu.itba.pod.client.longestWait;

import ar.edu.itba.pod.api.common.RowParser;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.longestWait.LongestWaitTripData;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LongestWaitParser implements RowParser<LongestWaitTripData> {
    private final String borough;
    private final Set<Integer> allowedPuIds; // PULocationIDs del borough indicado

    /**
     * @param zones   mapa de zones (locationId -> Zone) cargado desde zones.csv
     * @param borough borough pasado por parámetro del cliente (-Dborough=...)
     */
    public LongestWaitParser(Map<Integer, Zone> zones, String borough) {
        if (borough == null || borough.isBlank()) {
            throw new IllegalArgumentException("El parámetro 'borough' es obligatorio.");
        }
        this.borough = borough.trim();

        // Precomputamos qué PU locationIds pertenecen al borough indicado
        this.allowedPuIds = zones.entrySet().stream()
                .filter(e -> this.borough.equalsIgnoreCase(e.getValue().getBorough()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (this.allowedPuIds.isEmpty()) {
            throw new IllegalStateException("No se encontraron zonas para el borough: " + this.borough);
        }
    }

    @Override
    public Optional<LongestWaitTripData> parseRow(String[] cols, Map<Integer, Zone> zones) {
        // Columns necesarias: PU/DO ids + request_datetime + pickup_datetime
        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
        if (!allowedPuIds.contains(pu)) return Optional.empty();       // sólo PU del borough
        if (!zones.containsKey(pu)) return Optional.empty();           // seguridad: PU debe estar en zones

        int dl = Integer.parseInt(cols[TripsColumns.DOLOCATIONID.getIndex()].trim());
        if (!zones.containsKey(dl)) return Optional.empty();           // DO debe estar en zones

        String request = cols[TripsColumns.REQUEST_DATETIME.getIndex()].trim();
        String pickup  = cols[TripsColumns.PICKUP_DATETIME.getIndex()].trim();

        String puName = zones.get(pu).getZoneName();
        String doName = zones.get(dl).getZoneName();

        return Optional.of(new LongestWaitTripData(
                pu, dl, puName, doName, request, pickup
        ));
    }
}
