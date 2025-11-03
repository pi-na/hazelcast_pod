package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.mapreduce.Collator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AveragePriceCollator
        implements Collator<Map.Entry<AverageKeyOut, AveragePriceAccumulator>, Map<String, AveragePriceResult>>, Serializable {

    @Override
    public Map<String, AveragePriceResult> collate(Iterable<Map.Entry<AverageKeyOut, AveragePriceAccumulator>> values) {
        Map<String, AveragePriceResult> results = new HashMap<>();

        for (Map.Entry<AverageKeyOut, AveragePriceAccumulator> entry : values) {
            String companyKey = entry.getKey().getCompany();
            String boroughKey = entry.getKey().getPickUpBorough();
            AveragePriceAccumulator acc = entry.getValue();

            String key = companyKey + ";" + boroughKey;

            double avgFare = 0d;
            if (acc.getCount() > 0) {
                avgFare = acc.getSum() / acc.getCount();
                avgFare = Math.floor(avgFare * 100d) / 100d;
            }

            AveragePriceResult result = new AveragePriceResult(boroughKey, companyKey, avgFare);
            results.put(key, result);
        }
        System.out.println("results: " + results);
        return results;
    }
}
