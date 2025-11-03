package ar.edu.itba.pod.api.AveragePrice;

import ar.edu.itba.pod.api.totalTrips.TotalTrips;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class AveragePriceMapper
        implements Mapper<Long, TotalTrips, AverageKeyOut, AveragePriceAccumulator> {

    @Override
    public void map(Long key, TotalTrips value, Context<AverageKeyOut, AveragePriceAccumulator> context) {
        if (value == null) return;

        String borough = value.getPickupBorough();
        String company = value.getCompany();
        Double fare = value.getBase_passenger_fare();
        System.out.println("Borough: " + borough);
        if (borough == null || company == null || fare == null) return;
        if ("Outside of NYC".equalsIgnoreCase(borough)) return;

        context.emit(
                new AverageKeyOut(borough, company),
                new AveragePriceAccumulator(fare, 1)
        );
    }
}
