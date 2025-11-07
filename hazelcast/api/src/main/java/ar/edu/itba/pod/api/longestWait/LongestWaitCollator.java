package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.Collator;

import java.util.*;
import java.util.stream.Collectors;

public class LongestWaitCollator implements Collator<Map.Entry<Integer, LongestWaitReducerValue>, List<LongestWaitResultRow>> {

    @Override
    public List<LongestWaitResultRow> collate(Iterable<Map.Entry<Integer, LongestWaitReducerValue>> values) {
        List<LongestWaitResultRow> resultRows = new ArrayList<>();

        for (Map.Entry<Integer, LongestWaitReducerValue> entry : values) {
            LongestWaitReducerValue v = entry.getValue();

            resultRows.add(new LongestWaitResultRow(
                    v.puLocationName(),
                    v.doLocationName(),
                    v.waitTimeMillis() / 1000
            ));
        }

        return resultRows.stream()
                .sorted(Comparator.comparing(LongestWaitResultRow::getPickUpZone, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
