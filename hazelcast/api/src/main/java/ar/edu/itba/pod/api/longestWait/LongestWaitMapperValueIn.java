package ar.edu.itba.pod.api.longestWait;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record LongestWaitMapperValueIn(LocalDateTime requestDatetime,
                                       LocalDateTime pickupDatetime,
                                       Long puLocationId,
                                       Long doLocationId,
                                       String zoneName) implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // TODO Lo arme para q reciba todas strings esta bien? Lo voy a usar asi desde el client??
    public LongestWaitMapperValueIn(String requestDatetimeStr, String pickupDatetimeStr,
                                    String puLocationIdStr, String doLocationIdStr, String zoneName) {
        this(
                LocalDateTime.parse(requestDatetimeStr.trim(), FORMATTER),
                LocalDateTime.parse(pickupDatetimeStr.trim(), FORMATTER),
                Long.parseLong(puLocationIdStr.trim()),
                Long.parseLong(doLocationIdStr.trim()),
                zoneName
        );
    }

    /** Devuelve la espera en milisegundos */
    public long waitMillis() {
        return java.time.Duration.between(requestDatetime, pickupDatetime).toMillis();
    }
}

