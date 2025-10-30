package ar.edu.itba.pod.api.totalTrips;

public record TotalTripsResult(String pickUpZone, String dropOffZone, long total) {
    @Override
    public String toString() {
        return String.format("%s;%s;%d", pickUpZone, dropOffZone, total);
    }

    public String getKey() {
        return String.format("%s;%s", pickUpZone, dropOffZone);
    }

}
