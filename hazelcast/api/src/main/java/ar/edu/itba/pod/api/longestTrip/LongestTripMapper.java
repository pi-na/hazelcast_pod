package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class LongestTripMapper implements Mapper<Long, LongestTrip, String, LongestTripData> {

    @Override
    public void map(Long key, LongestTrip value, Context<String, LongestTripData> context) {
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
