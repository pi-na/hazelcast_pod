package ar.edu.itba.pod.api.totalTrips;


import ar.edu.itba.pod.api.common.ParsedRow;

public interface TotalTrips extends ParsedRow {
    String getPickUpZone();
    String getDropoffZone();
    Double getBasePassengerFare();
    String getCompany();
    Double getBase_passenger_fare();
    String getPickupBorough();

}
