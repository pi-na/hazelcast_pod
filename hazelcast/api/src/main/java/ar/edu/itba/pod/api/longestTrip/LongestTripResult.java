package ar.edu.itba.pod.api.longestTrip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record LongestTripResult(
    String pickUpZone, 
    String longestDOZone, 
    String longestReqDateTime, 
    Double longestMiles, 
    String longestCompany
) {
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public String toString() {
        // Format date from input format to output format
        LocalDateTime dateTime = LocalDateTime.parse(longestReqDateTime, INPUT_FORMATTER);
        String formattedDate = dateTime.format(OUTPUT_FORMATTER);
        
        // Truncate miles to 2 decimals (not round)
        double truncatedMiles = Math.floor(longestMiles * 100) / 100.0;
        
        return String.format("%s;%s;%s;%.2f;%s", 
            pickUpZone, 
            longestDOZone, 
            formattedDate, 
            truncatedMiles, 
            longestCompany
        );
    }

    public String getKey() {
        return pickUpZone;
    }
}


