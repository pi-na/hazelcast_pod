package ar.edu.itba.pod.api.common;

import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;

public class Zone implements DataSerializable {
    private int locationId;
    private String borough;
    private String zoneName;

    public Zone() {
    }

    public Zone(int locationId, String borough, String zoneName) {
        this.locationId = locationId;
        this.borough = borough;
        this.zoneName = zoneName;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getBorough() {
        return borough;
    }

    public String getZoneName() {
        return zoneName;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(locationId);
        out.writeUTF(borough);
        out.writeUTF(zoneName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        locationId = in.readInt();
        borough = in.readUTF();
        zoneName = in.readUTF();
    }
}
