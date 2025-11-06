package ar.edu.itba.pod.api.common;

import ar.edu.itba.pod.api.AveragePrice.CompanyTrips;
import ar.edu.itba.pod.api.longestTrip.LongestTrip;
import ar.edu.itba.pod.api.longestWait.LongestWaitMapperValueIn;
import ar.edu.itba.pod.api.totalTrips.TotalTrips;
import ar.edu.itba.pod.api.ytdMiles.YtdMilesTrip;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class Trip implements ParsedRow, DataSerializable, TotalTrips, YtdMilesTrip, CompanyTrips, LongestWaitMapperValueIn {
    private String company;
    private String request_datetime;
    private String pickup_datetime;
    private String dropoff_datetime;
    private Integer PULocation;
    private Integer DOLocation;
    private String  pickup_borough;
    private String  pickup_location;
    private String dropoff_location;
    private Double trip_miles;
    private Double base_passenger_fare;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(company);
        out.writeUTF(request_datetime);
        out.writeUTF(pickup_datetime);
        out.writeUTF(dropoff_datetime);
        out.writeInt(PULocation);
        out.writeInt(DOLocation);
        out.writeUTF(pickup_location);
        out.writeUTF(pickup_borough);
        out.writeUTF(dropoff_location);
        out.writeDouble(trip_miles);
        out.writeDouble(base_passenger_fare);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        company = in.readUTF();
        request_datetime = in.readUTF();
        pickup_datetime = in.readUTF();
        dropoff_datetime = in.readUTF();
        PULocation = in.readInt();
        DOLocation = in.readInt();
        pickup_location = in.readUTF();
        pickup_borough = in.readUTF();
        dropoff_location = in.readUTF();
        trip_miles = in.readDouble();
        base_passenger_fare = in.readDouble();
    }

    public void setCompany(String company) {
        this.company = company;
    }
    public void setRequest_datetime(String request_datetime) {
        this.request_datetime = request_datetime;
    }
    public void setPickup_datetime(String pickup_datetime) {
        this.pickup_datetime = pickup_datetime;
    }
    public void setDropoff_datetime(String dropoff_datetime) {
        this.dropoff_datetime = dropoff_datetime;
    }
    public void setPULocation(Integer PULocation) {
        this.PULocation = PULocation;
    }
    public void setDOLocation(Integer DOLocation) {
        this.DOLocation = DOLocation;
    }
    public void setPickup_location(String pickup_location) {
        this.pickup_location = pickup_location;
    }
    public void setPickup_borough(String pickup_borough) {
        this.pickup_borough = pickup_borough;
    }
    public void setDropoff_location(String dropoff_location) {
        this.dropoff_location = dropoff_location;
    }
    public void setTrip_miles(Double trip_miles) {
        this.trip_miles = trip_miles;
    }
    public void setBase_passenger_fare(Double base_passenger_fare) {
        this.base_passenger_fare = base_passenger_fare;
    }

    @Override
    public String getPickUpZone() {
        return pickup_location;
    }

    @Override
    public String getDropoffZone() {
        return dropoff_location;
    }

    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public String getPickupBorough() {
        return pickup_borough;
    }

    @Override
    public Double getBase_passenger_fare() {
        return base_passenger_fare;
    }

    public Double getBasePassengerFare() {
        return base_passenger_fare;
    }

    public String getRequest_datetime() {
        return request_datetime;
    }

    public String getPickup_datetime() {
        return pickup_datetime;
    }

    public Double getTrip_miles() {
        return trip_miles;
    }

    public Integer getPULocation() {
        return PULocation;
    }

    public Integer getDOLocation() {
        return DOLocation;
    }
}
