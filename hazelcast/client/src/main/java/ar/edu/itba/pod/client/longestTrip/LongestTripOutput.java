package ar.edu.itba.pod.client.longestTrip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record LongestTripOutput(
        String pickUpZone,
        String dropOffZone,
        String datetime,
        double miles,
        String company
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public String toString() {
        LocalDateTime dt = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format(
                "%s;%s;%s;%.2f;%s",
                pickUpZone,
                dropOffZone,
                dt.format(FORMATTER),
                miles,
                company
        );
    }
}
