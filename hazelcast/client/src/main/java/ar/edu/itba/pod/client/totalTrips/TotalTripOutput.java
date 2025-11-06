package ar.edu.itba.pod.client.totalTrips;

public record TotalTripOutput(String pickUpZone, String dropOffZone, long total) {
    @Override
    public String toString() {
        return String.format("%s;%s;%d", pickUpZone, dropOffZone, total);
    }
}
