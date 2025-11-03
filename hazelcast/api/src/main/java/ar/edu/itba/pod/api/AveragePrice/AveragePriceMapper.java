package ar.edu.itba.pod.api.AveragePrice;

import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.totalTrips.TotalKeyOut;
import ar.edu.itba.pod.api.totalTrips.TotalTrips;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import java.io.Serializable;

public class AveragePriceMapper
        implements Mapper<Long, TotalTrips, AverageKeyOut, AveragePriceAccumulator> {

    private static final long ONE = 1L;

    @Override
    public void map(Long key, TotalTrips value, Context<AverageKeyOut, AveragePriceAccumulator> context) {
        if (value == null) return;

//        String borough = value.getBorough();
        String company = value.getCompany();
        Double fare = value.getBasePassengerFare();

        if (borough == null || company == null || fare == null) return;
        if ("Outside of NYC".equalsIgnoreCase(borough)) return;

        context.emit(
                new AverageKeyOut(borough, company),
                new AveragePriceAccumulator(fare, 1)
        );
    }
}
