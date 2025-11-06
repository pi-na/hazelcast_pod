package ar.edu.itba.pod.api.totalTrips;

public record TotalTripsResult(Integer pickUpZone, Integer dropOffZone, long total) {
    public TotalTripsResult(short pickUpZone, short dropOffZone, Long value) {
        this((int) pickUpZone, (int) dropOffZone, value);
    }

    public TotalTripsResult {
    }

    @Override
    public String toString() {
        return String.format("%d;%d;%d", pickUpZone, dropOffZone, total);
    }

    public String getKey() {
        return String.format("%s;%s", pickUpZone, dropOffZone);
    }

}
