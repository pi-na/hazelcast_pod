package ar.edu.itba.pod.api.longestTrip;

import ar.edu.itba.pod.api.common.ParsedRow;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class LongestTrip implements DataSerializable, ParsedRow {
    public short pickUpZone;
    public short dropOffZone;
    public String  requestDateTime;
    public double tripMile;
    public String  company;


    public LongestTrip() {}
    public LongestTrip(short pu, short doL, String  dt, double mile, String company) {
        this.pickUpZone = pu;
        this.dropOffZone = doL;
        this.requestDateTime = dt;
        this.tripMile = mile;
        this.company = company;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeShort(pickUpZone);
        out.writeShort(dropOffZone);
        out.writeUTF(requestDateTime);
        out.writeDouble(tripMile);
        out.writeUTF(company);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickUpZone = in.readShort();
        dropOffZone = in.readShort();
        requestDateTime = in.readUTF();
        tripMile = in.readDouble();
        company = in.readUTF();
    }

    public short getPickUpZone() {
        return pickUpZone;
    }

    public short getDropOffZone() {
        return dropOffZone;
    }

    public String getCompany() {
        return company;
    }

    public String getRequestDateTime() {
        return requestDateTime;
    }

    public double getMiles() {
        return tripMile;
    }
}


