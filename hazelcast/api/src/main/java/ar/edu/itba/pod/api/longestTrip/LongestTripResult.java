package ar.edu.itba.pod.api.longestTrip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record LongestTripResult(
    short pickUpZone,
    short longestDOZone,
    String longestReqDateTime, 
    double longestMiles,
    String longestCompany
) {
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public String toString() {
        LocalDateTime dateTime = LocalDateTime.parse(longestReqDateTime, INPUT_FORMATTER);
        String formattedDate = dateTime.format(OUTPUT_FORMATTER);
        
        double truncatedMiles = Math.floor(longestMiles * 100) / 100.0;
        
        return String.format("%s;%s;%s;%.2f;%s", 
            pickUpZone, 
            longestDOZone, 
            formattedDate, 
            truncatedMiles, 
            longestCompany
        );
    }

    public short getKey() {
        return pickUpZone;
    }
    public short getLongestDOZone() {
        return longestDOZone;
    }
    public String getLongestReqDateTime() {
        return longestReqDateTime;
    }
    public double getLongestMiles() {
        return longestMiles;
    }
    public String getLongestCompany() {
        return longestCompany;
    }
}


