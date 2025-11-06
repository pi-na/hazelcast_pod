package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

//                                              LineaCSV                      pickUpLocationID
public class LongestWaitMapper implements Mapper<Long, LongestWaitTripData, Integer, LongestWaitReducerValue> {
    @Override
    public void map(Long keyIn, LongestWaitTripData longestWaitTripData, Context<Integer, LongestWaitReducerValue> context) {
        context.emit(longestWaitTripData.getPULocation(),
                new LongestWaitReducerValue(longestWaitTripData.getDOLocation(),
                        longestWaitTripData.waitMillis(longestWaitTripData.getRequest_datetime(), longestWaitTripData.getPickup_datetime()),
                        longestWaitTripData.getDropoffZone(),
                        longestWaitTripData.getPickUpZone()));
    }
}
