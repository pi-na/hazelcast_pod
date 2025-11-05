package ar.edu.itba.pod.api.longestWait;

import ar.edu.itba.pod.api.common.Trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface LongestWaitMapperValueIn{
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    Integer getPULocation();
    Integer getDOLocation();
    String getPickUpZone();
    String getDropoffZone();
    String getRequest_datetime();
    String getPickup_datetime();

    default long waitMillis(String requestTime, String pickUpTime) {
       LocalDateTime requestTimeParsed = LocalDateTime.parse(requestTime.trim(), FORMATTER);
       LocalDateTime pickUpTimeParsed = LocalDateTime.parse(pickUpTime.trim(), FORMATTER);
       return java.time.Duration.between(requestTimeParsed, pickUpTimeParsed).toMillis();
    }

}
