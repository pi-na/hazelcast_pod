package ar.edu.itba.pod.api.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class Trip implements DataSerializable {
    private String company;
    private String request_datetime;
    private String pickup_datetime;
    private String dropoff_datetime;
    private Integer PULocation;
    private Integer DOLocation;
    private Double trip_miles;
    private Double base_passenger_fare;

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(company);
        objectDataOutput.writeUTF(request_datetime);
        objectDataOutput.writeUTF(pickup_datetime);
        objectDataOutput.writeUTF(dropoff_datetime);
        objectDataOutput.writeInt(PULocation);
        objectDataOutput.writeInt(DOLocation);
        objectDataOutput.writeDouble(trip_miles);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        company = objectDataInput.readUTF();
        request_datetime = objectDataInput.readUTF();
        pickup_datetime = objectDataInput.readUTF();
        dropoff_datetime = objectDataInput.readUTF();
        PULocation = objectDataInput.readInt();
        DOLocation = objectDataInput.readInt();
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
    public void setTrip_miles(Double trip_miles) {
        this.trip_miles = trip_miles;
    }
    public void setBase_passenger_fare(Double base_passenger_fare) {
        this.base_passenger_fare = base_passenger_fare;
    }
}
