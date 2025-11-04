package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.CombinerFactory;

public class LongestWaitCombinerFactory implements CombinerFactory<Long, LongestWaitReducerValue, LongestWaitReducerValue> {
    @Override
    public com.hazelcast.mapreduce.Combiner<LongestWaitReducerValue, LongestWaitReducerValue> newCombiner(Long key) {
        return new LongestWaitCombiner();
    }

    private static class LongestWaitCombiner extends com.hazelcast.mapreduce.Combiner<LongestWaitReducerValue, LongestWaitReducerValue> {
        private LongestWaitReducerValue currentMax = null;

        @Override
        public void reset() {
            currentMax = null;
        }

        @Override
        public void combine(LongestWaitReducerValue value) {
            if (currentMax == null ||   // Desempata por orden alfabetico ascendiente (si la nueva tiene menor, la guardo)
                    value.waitTimeMillis() > currentMax.waitTimeMillis() ||
                    value.waitTimeMillis().equals(currentMax.waitTimeMillis()) && value.doLocationName().compareTo(currentMax.doLocationName()) < 0) {
                currentMax = value;
            }
        }

        @Override
        public LongestWaitReducerValue finalizeChunk() {
            return currentMax;
        }
    }
}
