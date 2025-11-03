package ar.edu.itba.pod.api.ytdMiles;

import com.hazelcast.mapreduce.Collator;

import java.util.*;
import java.util.stream.Collectors;

public class YtdMilesCollator implements Collator<Map.Entry<CompanyYearMonth, Double>, List<YtdMilesResult>> {

    @Override
    public List<YtdMilesResult> collate(Iterable<Map.Entry<CompanyYearMonth, Double>> values) {
        // Group by company and year
        Map<String, Map<Integer, Map<Integer, Double>>> companyYearMonthMap = new HashMap<>();
        
        for (Map.Entry<CompanyYearMonth, Double> entry : values) {
            String company = entry.getKey().getCompany();
            int year = entry.getKey().getYear();
            int month = entry.getKey().getMonth();
            double miles = entry.getValue();
            
            companyYearMonthMap
                .computeIfAbsent(company, k -> new HashMap<>())
                .computeIfAbsent(year, k -> new HashMap<>())
                .put(month, miles);
        }
        
        List<YtdMilesResult> results = new ArrayList<>();
        
        // Calculate YTD for each company
        for (Map.Entry<String, Map<Integer, Map<Integer, Double>>> companyEntry : companyYearMonthMap.entrySet()) {
            String company = companyEntry.getKey();
            
            for (Map.Entry<Integer, Map<Integer, Double>> yearEntry : companyEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                Map<Integer, Double> monthMiles = yearEntry.getValue();
                
                // Sort months and calculate YTD
                List<Integer> sortedMonths = new ArrayList<>(monthMiles.keySet());
                Collections.sort(sortedMonths);
                
                double ytd = 0.0;
                for (int month : sortedMonths) {
                    ytd += monthMiles.get(month);
                    results.add(new YtdMilesResult(company, year, month, ytd));
                }
            }
        }
        
        // Sort results
        Collections.sort(results);
        
        return results;
    }
}

