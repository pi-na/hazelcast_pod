package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LongestTripReducerFactory implements ReducerFactory<Integer, LongestTrip, LongestTrip> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Reducer<LongestTrip, LongestTrip> newReducer(final Integer pickUpZone) {
        return new LongestReducer();
    }

    private class LongestReducer extends Reducer<LongestTrip, LongestTrip> {

        private LongestTrip longest = null;

        @Override
        public void reduce(final LongestTrip value) {
            if (longest == null) {
                longest = value;
            } else {
                int comparison = Double.compare(value.getMiles(), longest.getMiles());
                if (comparison > 0) {
                    longest = value;
                } else if (comparison == 0) {
                    LocalDateTime valueDate = LocalDateTime.parse(value.getRequestDateTime(), DATE_FORMATTER);
                    LocalDateTime longestDate = LocalDateTime.parse(longest.getRequestDateTime(), DATE_FORMATTER);
                    if (valueDate.isAfter(longestDate)) {
                        longest = value;
                    }
                }
            }
        }

        @Override
        public LongestTrip finalizeReduce() {
            return longest;
        }
    }
}


