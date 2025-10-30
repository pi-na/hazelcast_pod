package ar.edu.itba.pod.api.enums;

public enum TripsColumns {
    COMPANY(0),
    REQUEST_DATETIME(1),
    PICKUP_DATETIME(2),
    DROPOFF_DATETIME(3),
    PULOCATIONID(4),
    DOLOCATIONID(5),
    TRIP_MILES(6),
    BASE_PASSENGER_FARE(7);

    private final int index;

    TripsColumns(final int index) { this.index = index; }

    public int getIndex() { return index; }
}
