package ar.edu.itba.pod.api.ytdMiles;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

public class YtdMilesCombinerFactory implements CombinerFactory<CompanyYearMonth, Double, Double> {

    @Override
    public Combiner<Double, Double> newCombiner(CompanyYearMonth key) {
        return new YtdMilesCombiner();
    }

    private static class YtdMilesCombiner extends Combiner<Double, Double> {
        private double sum = 0.0;

        @Override
        public void combine(Double value) {
            sum += value;
        }

        @Override
        public Double finalizeChunk() {
            return sum;
        }

        @Override
        public void reset() {
            sum = 0.0;
        }
    }
}

