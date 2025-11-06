package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.mapreduce.Collator;
import java.util.*;

public class TotalTripsCollator implements Collator<Map.Entry<TotalKeyOut, Long>, Map<String, TotalTripsResult>> {
    @Override
    public Map<String, TotalTripsResult> collate(Iterable<Map.Entry<TotalKeyOut, Long>> entries) {
        Map<String, TotalTripsResult> map = new HashMap<>();
        for (Map.Entry<TotalKeyOut, Long> e : entries) {
            TotalKeyOut k = e.getKey();
            map.put(k.getPickUpZone() + ";" + k.getDropOffZone(),
                    new TotalTripsResult(Short.toString(k.getPickUpZone()),
                            Short.toString(k.getDropOffZone()),
                            e.getValue()));
        }
        return map;
    }
}
