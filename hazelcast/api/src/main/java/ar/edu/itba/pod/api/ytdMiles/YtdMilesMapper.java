package ar.edu.itba.pod.api.ytdMiles;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class YtdMilesMapper implements Mapper<String, YtdMilesTrip, CompanyYearMonth, Double> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void map(String key, YtdMilesTrip value, Context<CompanyYearMonth, Double> context) {
        // Parse request_datetime to extract year and month
        LocalDateTime requestDate = LocalDateTime.parse(value.getRequest_datetime(), DATE_FORMATTER);
        
        int year = requestDate.getYear();
        int month = requestDate.getMonthValue();
        
        CompanyYearMonth companyYearMonth = new CompanyYearMonth(
            value.getCompany(),
            year,
            month
        );
        
        context.emit(companyYearMonth, value.getTrip_miles());
    }
}

