package ar.edu.itba.pod.api.longestWait;

import ar.edu.itba.pod.api.common.Trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LongestWaitMapperValueIn extends Trip {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Integer puLocationId() {
        return getPULocation();
    }

    public Integer doLocationId() {
        return getDOLocation();
    }

    public long waitMillis() {
       LocalDateTime requestTime = LocalDateTime.parse(getRequest_datetime().trim(), FORMATTER);
       LocalDateTime pickUpTime = LocalDateTime.parse(pickUpDa().trim(), FORMATTER);
         return java.time.Duration.between(requestTime, pickUpTime).toMillis();
    }

    public String zoneName() {
    }
}
