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
        implements Mapper<String, CompanyTrips, AverageKeyOut, AveragePriceAccumulator> {

    private static final long ONE = 1L;

    @Override
    public void map(String key, CompanyTrips value, Context<AverageKeyOut, AveragePriceAccumulator> context) {
        context.emit(
                new AverageKeyOut(value.getBorough(), value.getCompany()), ONE
        );
    }
}
