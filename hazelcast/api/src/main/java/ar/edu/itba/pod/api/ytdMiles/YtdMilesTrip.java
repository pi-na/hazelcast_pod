package ar.edu.itba.pod.api.ytdMiles;

import ar.edu.itba.pod.api.common.ParsedRow;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class YtdMilesTrip implements DataSerializable, ParsedRow {
    private String company;
    private String request_datetime;
    private double trip_miles;

    public YtdMilesTrip() {}

    public YtdMilesTrip(String company, String request_datetime, double trip_miles) {
        this.company = company;
        this.request_datetime = request_datetime;
        this.trip_miles = trip_miles;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(company);
        out.writeUTF(request_datetime);
        out.writeDouble(trip_miles);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        company = in.readUTF();
        request_datetime = in.readUTF();
        trip_miles = in.readDouble();
    }

    public String getCompany() {
        return company;
    }

    public String getRequest_datetime() {
        return request_datetime;
    }

    public double getTrip_miles() {
        return trip_miles;
    }
}

