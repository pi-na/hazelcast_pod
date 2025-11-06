package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LongestTripCombinerFactory implements CombinerFactory<Integer, LongestTrip, LongestTrip> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Combiner<LongestTrip, LongestTrip> newCombiner(Integer key) {
        return new LongestTripCombiner();
    }

    private static class LongestTripCombiner extends Combiner<LongestTrip, LongestTrip> {
        private LongestTrip longest = null;

        @Override
        public void combine(LongestTrip value) {
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
        public LongestTrip finalizeChunk() {
            return longest;
        }

        @Override
        public void reset() {
            longest = null;
        }
    }
}


