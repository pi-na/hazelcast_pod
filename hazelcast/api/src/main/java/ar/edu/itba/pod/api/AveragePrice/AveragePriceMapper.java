package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class AveragePriceMapper
        implements Mapper<Long, TripData, AverageKeyOut, Double> {

    @Override
    public void map(Long key, TripData value, Context<AverageKeyOut, Double> context) {
        String borough = value.getPickupBorough();
        String company = value.getCompany();

        context.emit(
                new AverageKeyOut(borough, company),
                value.getBasePassengerFare()
        );
    }
}
