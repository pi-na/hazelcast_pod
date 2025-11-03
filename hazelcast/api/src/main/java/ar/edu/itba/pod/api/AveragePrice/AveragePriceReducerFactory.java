package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;


public class AveragePriceReducerFactory
        implements ReducerFactory<AverageKeyOut, AveragePriceAccumulator, AveragePriceAccumulator> {

    @Override
    public Reducer<AveragePriceAccumulator, AveragePriceAccumulator> newReducer(final AverageKeyOut AverageKeyOut) {
        return new ComplaintReducer();
    }

    private class ComplaintReducer extends Reducer<AveragePriceAccumulator, AveragePriceAccumulator> {

        private double sum;
        private long count;

        @Override
        public void beginReduce() { sum = 0d; count = 0L; }

        @Override
        public void reduce(AveragePriceAccumulator value) {
            if (value != null) {
                sum += value.getSum();
                count += value.getCount();
            }
        }

        @Override
        public AveragePriceAccumulator finalizeReduce() {
            return new AveragePriceAccumulator(sum, count);
        }
    }
}
