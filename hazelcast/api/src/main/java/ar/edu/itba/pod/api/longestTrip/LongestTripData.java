package ar.edu.itba.pod.api.longestTrip;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class LongestTripData implements DataSerializable {
    private String pickUpZone;
    private String dropOffZone;
    private String company;
    private String requestDateTime;
    private Double miles;

    public LongestTripData() {
        // Default constructor for deserialization
    }

    public LongestTripData(String pickUpZone, String dropOffZone, String company, String requestDateTime, Double miles) {
        this.pickUpZone = pickUpZone;
        this.dropOffZone = dropOffZone;
        this.company = company;
        this.requestDateTime = requestDateTime;
        this.miles = miles;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(pickUpZone);
        out.writeUTF(dropOffZone);
        out.writeUTF(company);
        out.writeUTF(requestDateTime);
        out.writeDouble(miles);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickUpZone = in.readUTF();
        dropOffZone = in.readUTF();
        company = in.readUTF();
        requestDateTime = in.readUTF();
        miles = in.readDouble();
    }

    public String getPickUpZone() {
        return pickUpZone;
    }

    public String getDropOffZone() {
        return dropOffZone;
    }

    public String getCompany() {
        return company;
    }

    public String getRequestDateTime() {
        return requestDateTime;
    }

    public Double getMiles() {
        return miles;
    }
}


