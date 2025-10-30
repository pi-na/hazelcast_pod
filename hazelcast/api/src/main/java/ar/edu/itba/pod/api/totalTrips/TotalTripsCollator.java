package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Collator;

import java.util.*;

public class TotalTripsCollator implements Collator<Map.Entry<TotalKeyOut, Long>, Map<String, TotalTripsResult>> {

    @Override
    public Map<String, TotalTripsResult> collate(Iterable<Map.Entry<TotalKeyOut, Long>> values) {
        Map<String, TotalTripsResult> resultsMap = new HashMap<>();

        for (Map.Entry<TotalKeyOut, Long> entry : values) {
            String pickUpZone = entry.getKey().getpickUpZone();
            String dropOffZone = entry.getKey().getdropOffZone();
            long totalTrips = entry.getValue();

            String key = pickUpZone + ";" + dropOffZone;

            TotalTripsResult result = new TotalTripsResult(pickUpZone, dropOffZone, (int) totalTrips);

            resultsMap.put(key, result);
        }

        return resultsMap;
    }

}