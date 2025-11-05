package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class LongestWaitReducerFactory implements ReducerFactory<Integer, LongestWaitReducerValue, LongestWaitReducerValue> {

    // La key de este reducer es el pickUpLocationID
    // El valueIn y valueOut es (DROP OFF LOCATION ID, WAIT TIME IN MILLIS)
    // Voy a tener que averiguar cual es el max tiempo, y cual es la drop off con ese tiempo
    @Override
    public Reducer<LongestWaitReducerValue, LongestWaitReducerValue> newReducer(Integer puLocationId) {
        return new Reducer<>() {
            private LongestWaitReducerValue max = null;

            @Override
            public void reduce(LongestWaitReducerValue value) {
                if (max == null ||   // Desempata por orden alfabetico ascendiente (si la nueva tiene menor, la guardo)
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
