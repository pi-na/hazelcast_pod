package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Collator;

import java.util.*;

public class LongestTripCollator implements Collator<Map.Entry<Integer, LongestTrip>, Map<String, LongestTripResult>> {

    @Override
    public Map<String, LongestTripResult> collate(Iterable<Map.Entry<Integer, LongestTrip>> values) {
        Map<String, LongestTripResult> resultsMap = new HashMap<>();

        for (Map.Entry<Integer, LongestTrip> entry : values) {
            Integer pickUpZone = entry.getKey();
            LongestTrip tripData = entry.getValue();

            LongestTripResult result = new LongestTripResult(
                tripData.getPickUpZone(),
                tripData.dropOffZone,
                tripData.getRequestDateTime(),
                tripData.getMiles(),
                tripData.getCompany()
            );

            resultsMap.put(pickUpZone.toString(), result);
        }

        return resultsMap;
    }
}


