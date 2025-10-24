package ar.edu.itba.pod.model;
import java.io.Serializable;

public record Trip(
        String pickupZone,
        String dropoffZone,
        String company,
        double tripMiles,
        double tripSeconds
) implements Serializable {     // Implementa Serializable para usar en Mapper
    public Trip(String[] cols) {
        this(
                cols.length > 0 ? cols[0] : "",
                cols.length > 1 ? cols[1] : "",
                cols.length > 2 ? cols[2] : "",
                cols.length > 3 ? Double.parseDouble(cols[3]) : 0.0,
                cols.length > 4 ? Double.parseDouble(cols[4]) : 0.0
        );
    }
}
