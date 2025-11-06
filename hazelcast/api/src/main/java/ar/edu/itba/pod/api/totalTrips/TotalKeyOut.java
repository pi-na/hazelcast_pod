package ar.edu.itba.pod.api.totalTrips;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Objects;

// Ocupa 8 bytes en total (2 ints) más overhead mínimo de serialización.
public class TotalKeyOut implements DataSerializable, Comparable<TotalKeyOut> {
    // ASUMIMOS QUE LOS ZONAS SON ENTEROS MENORES a 32.767 (MAX VALUE DE SHORT)
    private short pickUpZone;   // enteros menores a 1000 → cabe en short
    private short dropOffZone;

    public TotalKeyOut() {} // requerido por Hazelcast
    public TotalKeyOut(short pickUpZone, short dropOffZone) {
        this.pickUpZone = pickUpZone;
        this.dropOffZone = dropOffZone;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TotalKeyOut)) return false;
        TotalKeyOut o = (TotalKeyOut) obj;
        return pickUpZone == o.pickUpZone && dropOffZone == o.dropOffZone;
    }

    @Override
    public int hashCode() {
        return (pickUpZone << 16) ^ dropOffZone;
    }

    @Override
    public int compareTo(TotalKeyOut o) {
        int cmp = Short.compare(pickUpZone, o.pickUpZone);
        return (cmp != 0) ? cmp : Short.compare(dropOffZone, o.dropOffZone);
    }

    public short getPickUpZone() { return pickUpZone; }
    public short getDropOffZone() { return dropOffZone; }
}
