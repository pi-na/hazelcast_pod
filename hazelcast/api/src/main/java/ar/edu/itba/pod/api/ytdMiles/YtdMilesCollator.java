package ar.edu.itba.pod.api.ytdMiles;

import com.hazelcast.mapreduce.Collator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class YtdMilesCollator implements Collator<Map.Entry<CompanyYearMonth, Double>, List<YtdMilesResult>> {

    @Override
    public List<YtdMilesResult> collate(Iterable<Map.Entry<CompanyYearMonth, Double>> values) {
        Map<String, Map<Integer, Map<Integer, Double>>> companyYearMonthMap = new HashMap<>();

        for (Map.Entry<CompanyYearMonth, Double> entry : values) {
            String company = entry.getKey().getCompany();
            int year = entry.getKey().getYear();
            int month = entry.getKey().getMonth();
            double miles = entry.getValue();

            companyYearMonthMap
                .computeIfAbsent(company, k -> new HashMap<>())
                .computeIfAbsent(year, k -> new HashMap<>())
                .merge(month, miles, Double::sum);
        }

        List<YtdMilesResult> results = new ArrayList<>();

        for (Map.Entry<String, Map<Integer, Map<Integer, Double>>> companyEntry : companyYearMonthMap.entrySet()) {
            String company = companyEntry.getKey();

            for (Map.Entry<Integer, Map<Integer, Double>> yearEntry : companyEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                Map<Integer, Double> monthMiles = yearEntry.getValue();

                List<Integer> sortedMonths = new ArrayList<>(monthMiles.keySet());
                Collections.sort(sortedMonths);

                double ytd = 0.0;
                for (int month : sortedMonths) {
                    ytd += monthMiles.get(month);
                    double truncated = BigDecimal.valueOf(ytd)
                            .setScale(2, RoundingMode.DOWN)
                            .doubleValue();
                    results.add(new YtdMilesResult(company, year, month, ytd));
                }
            }
        }

        results.sort(Comparator
                .comparing(YtdMilesResult::company)
                .thenComparing(YtdMilesResult::year)
                .thenComparing(YtdMilesResult::month));

        return results;
    }
}

