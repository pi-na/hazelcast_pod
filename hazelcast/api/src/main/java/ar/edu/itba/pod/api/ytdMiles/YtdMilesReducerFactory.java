package ar.edu.itba.pod.api.ytdMiles;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class YtdMilesReducerFactory implements ReducerFactory<CompanyYearMonth, Double, Double> {

    @Override
    public Reducer<Double, Double> newReducer(final CompanyYearMonth companyYearMonth) {
        return new MilesReducer();
    }

    private class MilesReducer extends Reducer<Double, Double> {

        private double sum = 0.0;

        @Override
        public void reduce(final Double value) {
            sum += value;
        }

        @Override
        public Double finalizeReduce() {
            return sum;
        }
    }
}

