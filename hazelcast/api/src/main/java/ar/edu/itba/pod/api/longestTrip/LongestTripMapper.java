package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class LongestTripMapper implements Mapper<String, LongestTrip, String, LongestTripData> {

    private static final int OUTSIDE_NYC_LOCATION_ID = 265;

    @Override
    public void map(String key, LongestTrip value, Context<String, LongestTripData> context) {
        // Filter: only trips that start and end within NYC (not Outside of NYC)
        if (value.getPULocation() != OUTSIDE_NYC_LOCATION_ID && 
            value.getDOLocation() != OUTSIDE_NYC_LOCATION_ID) {
            
            LongestTripData tripData = new LongestTripData(
                value.getPickUpZone(),
                value.getDropoffZone(),
                value.getCompany(),
                value.getRequest_datetime(),
                value.getTrip_miles()
            );
            
            // Emit with pickUpZone as key
            context.emit(value.getPickUpZone(), tripData);
        }
    }
}


