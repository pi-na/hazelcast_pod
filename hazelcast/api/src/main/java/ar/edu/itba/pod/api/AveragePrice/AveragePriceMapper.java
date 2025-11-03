package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class AveragePriceMapper
        implements Mapper<String, CompanyTrips, AverageKeyOut, AveragePriceAccumulator> {

    @Override
    public void map(String key, CompanyTrips value, Context<AverageKeyOut, AveragePriceAccumulator> context) {
        if (value == null) return;

        String borough = value.getPickupBorough();
        String company = value.getCompany();
        Double fare = value.getBase_passenger_fare();

        if (borough == null || company == null || fare == null) return;
        if ("Outside of NYC".equalsIgnoreCase(borough)) return;

        context.emit(
                new AverageKeyOut(borough, company),
                new AveragePriceAccumulator(fare, 1)
        );
    }
}
