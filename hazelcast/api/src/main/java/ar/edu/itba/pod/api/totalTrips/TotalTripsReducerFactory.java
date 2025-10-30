package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class TotalTripsReducerFactory implements ReducerFactory<TotalKeyOut, Long, Long> {

    @Override
    public Reducer<Long, Long> newReducer(final TotalKeyOut TotalKeyOut) {
        return new ComplaintReducer();
    }

    private class ComplaintReducer extends Reducer<Long, Long> {

        private Long count = 0L;

        @Override
        public void reduce(final Long value) {
            count += value;
        }

        @Override
        public Long finalizeReduce() {
            return count;
        }
    }

}