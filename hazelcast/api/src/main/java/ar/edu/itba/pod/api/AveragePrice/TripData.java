package ar.edu.itba.pod.api.AveragePrice;

import ar.edu.itba.pod.api.common.ParsedRow;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class TripData implements DataSerializable, ParsedRow {

    private String pickupBorough;
    private double basePassengerFare;
    private String company;

    public TripData() {
    }

    public TripData(String pickupBorough, double basePassengerFare, String company) {
        this.pickupBorough = pickupBorough;
        this.basePassengerFare = basePassengerFare;
        this.company = company;
    }

    public String getPickupBorough() {
        return pickupBorough;
    }

    public double getBasePassengerFare() {
        return basePassengerFare;
    }

    public String getCompany() {
        return company;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(pickupBorough);
        out.writeDouble(basePassengerFare);
        out.writeUTF(company);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickupBorough = in.readUTF();
        basePassengerFare = in.readDouble();
        company = in.readUTF();
    }
}
