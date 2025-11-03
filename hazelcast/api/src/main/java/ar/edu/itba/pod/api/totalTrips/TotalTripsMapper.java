package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class TotalTripsMapper implements Mapper<Long, TotalTrips, TotalKeyOut, Long> {

    private static final long ONE = 1L;

    @Override
    public void map(Long aLong, TotalTrips totalTrips, Context<TotalKeyOut, Long> context) {
        context.emit(
                new TotalKeyOut(totalTrips.getPickUpZone(), totalTrips.getDropoffZone()), ONE
        );
    }
}