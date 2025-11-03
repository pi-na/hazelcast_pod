package ar.edu.itba.pod.api.AveragePrice;

import ar.edu.itba.pod.api.totalTrips.TotalKeyOut;
import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

import java.io.Serializable;


public class AveragePriceCombinerFactory implements CombinerFactory<AverageKeyOut, AveragePriceAccumulator, AveragePriceAccumulator> {

    @Override
    public Combiner<AveragePriceAccumulator, AveragePriceAccumulator> newCombiner(AverageKeyOut key) {
        return new AveragePriceCombiner();
    }

    private static class AveragePriceCombiner extends Combiner<AveragePriceAccumulator, AveragePriceAccumulator> {
        private double sum = 0.0;
        private long count = 0L;
        @Override
        public void combine(AveragePriceAccumulator value) {
            if (value != null) {
                sum += value.getSum();
                count += value.getCount();
            }
        }

        @Override
        public AveragePriceAccumulator finalizeChunk() {
            return new AveragePriceAccumulator(sum, count);
        }

        @Override
        public void reset() {
            sum = 0.0;
            count = 0L;
        }
    }
}
