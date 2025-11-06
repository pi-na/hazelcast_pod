package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class LongestWaitReducerFactory implements ReducerFactory<Integer, LongestWaitReducerValue, LongestWaitReducerValue> {
    @Override
    public Reducer<LongestWaitReducerValue, LongestWaitReducerValue> newReducer(Integer puLocationId) {
        return new Reducer<>() {
            private LongestWaitReducerValue max = null;

            @Override
            public void reduce(LongestWaitReducerValue value) {
                if (max == null ||
                        value.waitTimeMillis() > max.waitTimeMillis() ||
                        value.waitTimeMillis() == max.waitTimeMillis() && value.doLocationName().compareTo(max.doLocationName()) < 0) {
                    max = value;
                }
            }

            @Override
            public LongestWaitReducerValue finalizeReduce() {
                return max;
            }
        };
    }
}
