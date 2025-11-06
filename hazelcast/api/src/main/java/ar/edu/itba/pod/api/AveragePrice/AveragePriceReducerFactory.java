package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;


public class AveragePriceReducerFactory
        implements ReducerFactory<AverageKeyOut, Double, Double> {

    @Override
    public Reducer<Double, Double> newReducer(final AverageKeyOut AverageKeyOut) {
        return new AveragePriceReducer();
    }

    private class AveragePriceReducer extends Reducer<Double, Double> {
        private double sum;
        private long count;

        @Override
        public void beginReduce() { sum = 0.0; count = 0L; }

        @Override
        public void reduce(Double price) {
            sum += price;
            count++;
        }

        @Override
        public Double finalizeReduce() {
            if(count == 0) {
                return 0.0;
            }
            double avg = sum / count;
            return Math.floor(avg * 100) / 100.0;
        }
    }
}
