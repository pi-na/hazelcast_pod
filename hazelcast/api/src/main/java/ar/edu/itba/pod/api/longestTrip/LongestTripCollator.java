package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.mapreduce.Collator;

import java.util.*;

public class LongestTripCollator implements Collator<Map.Entry<String, LongestTripData>, Map<String, LongestTripResult>> {

    @Override
    public Map<String, LongestTripResult> collate(Iterable<Map.Entry<String, LongestTripData>> values) {
        Map<String, LongestTripResult> resultsMap = new HashMap<>();

        for (Map.Entry<String, LongestTripData> entry : values) {
            String pickUpZone = entry.getKey();
            LongestTripData tripData = entry.getValue();

            LongestTripResult result = new LongestTripResult(
                pickUpZone,
                tripData.getDropOffZone(),
                tripData.getRequestDateTime(),
                tripData.getMiles(),
                tripData.getCompany()
            );

            resultsMap.put(pickUpZone, result);
        }

        return resultsMap;
    }
}


