package ar.edu.itba.pod.api.totalTrips;

import ar.edu.itba.pod.api.common.ParsedRow;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

// Tamaño en memoria ≈ 8 bytes + overhead de Hazelcast.
public class MinimalTrip implements DataSerializable, ParsedRow {
    public short pickUpZone;
    public short dropOffZone;

    public MinimalTrip() {}
    public MinimalTrip(short pu, short doL) {
        this.pickUpZone = pu;
        this.dropOffZone = doL;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeShort(pickUpZone);
        out.writeShort(dropOffZone);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickUpZone = in.readShort();
        dropOffZone = in.readShort();
    }
}
