package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class TotalTripsMapper implements Mapper<Long, MinimalTrip, TotalKeyOut, Long> {

    private static final long ONE = 1L;

    @Override
    public void map(Long key, MinimalTrip trip, Context<TotalKeyOut, Long> context) {
        context.emit(
                new TotalKeyOut(trip.pickUpZone, trip.dropOffZone), ONE
        );
    }
}