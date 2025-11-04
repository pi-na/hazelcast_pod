package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LongestWaitMapperValueInFUTURO implements DataSerializable {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime requestDatetime;
    private LocalDateTime pickupDatetime;
    private long puLocationId;
    private long doLocationId;
    private String zoneName;

    // Constructor vacío obligatorio para Hazelcast
    public LongestWaitMapperValueIn() {}

    public LongestWaitMapperValueIn(LocalDateTime requestDatetime, LocalDateTime pickupDatetime,
                                    long puLocationId, long doLocationId, String zoneName) {
        this.requestDatetime = requestDatetime;
        this.pickupDatetime = pickupDatetime;
        this.puLocationId = puLocationId;
        this.doLocationId = doLocationId;
        this.zoneName = zoneName;
    }

    // Constructor auxiliar que parsea Strings directamente (para usar desde el client)
    public LongestWaitMapperValueIn(String requestDatetimeStr, String pickupDatetimeStr,
                                    String puLocationIdStr, String doLocationIdStr, String zoneName) {
        this(
                LocalDateTime.parse(requestDatetimeStr.trim(), FORMATTER),
                LocalDateTime.parse(pickupDatetimeStr.trim(), FORMATTER),
                Long.parseLong(puLocationIdStr.trim()),
                Long.parseLong(doLocationIdStr.trim()),
                zoneName
        );
    }

    public LocalDateTime requestDatetime() {
        return requestDatetime;
    }

    public LocalDateTime pickupDatetime() {
        return pickupDatetime;
    }

    public long puLocationId() {
        return puLocationId;
    }

    public long doLocationId() {
        return doLocationId;
    }

    public String zoneName() {
        return zoneName;
    }

    public long waitMillis() {
        return java.time.Duration.between(requestDatetime, pickupDatetime).toMillis();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(requestDatetime.toString());
        out.writeUTF(pickupDatetime.toString());
        out.writeLong(puLocationId);
        out.writeLong(doLocationId);
        out.writeUTF(zoneName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        requestDatetime = LocalDateTime.parse(in.readUTF());
        pickupDatetime = LocalDateTime.parse(in.readUTF());
        puLocationId = in.readLong();
        doLocationId = in.readLong();
        zoneName = in.readUTF();
    }
}
