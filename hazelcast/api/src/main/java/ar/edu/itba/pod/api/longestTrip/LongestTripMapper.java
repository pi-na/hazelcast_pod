package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class LongestTripMapper implements Mapper<Long, LongestTrip, Integer, LongestTrip> {

    @Override
    public void map(Long key, LongestTrip value, Context<Integer, LongestTrip> context) {
        context.emit((int) value.getPickUpZone(), value);
    }
}
