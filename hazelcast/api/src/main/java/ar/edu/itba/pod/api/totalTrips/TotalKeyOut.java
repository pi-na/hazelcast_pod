package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class TotalKeyOut implements DataSerializable {
    
    private String pickUpZone;
    private String dropOffZone;

    public TotalKeyOut(String pickUpZone, String dropOffZone) {
        this.pickUpZone = pickUpZone;
        this.dropOffZone = dropOffZone;
    }

    public TotalKeyOut() {
        // Default constructor for deserialization
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(pickUpZone);
        out.writeUTF(dropOffZone);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickUpZone = in.readUTF();
        dropOffZone = in.readUTF();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TotalKeyOut TotalKeyOut &&
                pickUpZone.equals(TotalKeyOut.pickUpZone) &&
                dropOffZone.equals(TotalKeyOut.dropOffZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pickUpZone, dropOffZone);
    }

    public String getpickUpZone() {
        return pickUpZone;
    }

    public String getdropOffZone() {
        return dropOffZone;
    }
}
