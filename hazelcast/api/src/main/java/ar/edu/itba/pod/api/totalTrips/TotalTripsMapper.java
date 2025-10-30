package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class TotalTripsMapper implements Mapper<String, TotalTrips, TotalKeyOut, Long> {

    private static final long ONE = 1L;

    @Override
    public void map(String key, TotalTrips value, Context<TotalKeyOut, Long> context) {
        context.emit(
                new TotalKeyOut(value.getPickUpZone(), value.getDropoffZone()), ONE
        );
    }

}