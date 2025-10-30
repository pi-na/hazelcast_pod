package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

public class TotalTripsCombinerFactory implements CombinerFactory<TotalKeyOut, Long, Long> {

    @Override
    public Combiner<Long, Long> newCombiner(TotalKeyOut key) {
        return new TotalTripsCombiner();
    }

    private static class TotalTripsCombiner extends Combiner<Long, Long> {
        private long sum = 0L;

        @Override
        public void combine(Long value) {
            sum += value;
        }

        @Override
        public Long finalizeChunk() {
            return sum;
        }

        @Override
        public void reset() {
            sum = 0L;
        }
    }
}
