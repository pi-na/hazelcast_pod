package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.mapreduce.Collator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AveragePriceCollator implements Collator<
        Map.Entry<AverageKeyOut, AveragePriceAccumulator>,
        Map<String, AveragePriceResult>> {

    @Override
    public Map<String, AveragePriceResult> collate(
            Iterable<Map.Entry<AverageKeyOut, AveragePriceAccumulator>> values) {
        Map<String, AveragePriceResult> result = new HashMap<>();

        for (Map.Entry<AverageKeyOut, AveragePriceAccumulator> e : values) {
            AverageKeyOut key = e.getKey();
            AveragePriceAccumulator acc = e.getValue();
            if (key == null || acc == null) continue;

            double avg = acc.getCount() == 0 ? 0 : acc.getSum() / acc.getCount();
            result.put(key.getPickUpBorough()+ ";" + key.getCompany(),
                    new AveragePriceResult(key.getPickUpBorough(), key.getCompany(), avg));
        }

        System.out.println("[Collator] Entradas procesadas: " + result.size());
        return result;
    }
}

