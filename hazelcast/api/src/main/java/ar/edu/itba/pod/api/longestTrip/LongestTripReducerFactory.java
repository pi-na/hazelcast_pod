package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LongestTripReducerFactory implements ReducerFactory<String, LongestTripData, LongestTripData> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Reducer<LongestTripData, LongestTripData> newReducer(final String pickUpZone) {
        return new LongestReducer();
    }

    private class LongestReducer extends Reducer<LongestTripData, LongestTripData> {

        private LongestTripData longest = null;

        @Override
        public void reduce(final LongestTripData value) {
            if (longest == null) {
                longest = value;
            } else {
                // Compare by miles, if equal compare by most recent request datetime
                int comparison = Double.compare(value.getMiles(), longest.getMiles());
                if (comparison > 0) {
                    longest = value;
                } else if (comparison == 0) {
                    // Same miles, choose the most recent
                    LocalDateTime valueDate = LocalDateTime.parse(value.getRequestDateTime(), DATE_FORMATTER);
                    LocalDateTime longestDate = LocalDateTime.parse(longest.getRequestDateTime(), DATE_FORMATTER);
                    if (valueDate.isAfter(longestDate)) {
                        longest = value;
                    }
                }
            }
        }

        @Override
        public LongestTripData finalizeReduce() {
            return longest;
        }
    }
}


