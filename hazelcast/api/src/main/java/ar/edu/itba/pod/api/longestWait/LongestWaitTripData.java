package ar.edu.itba.pod.api.longestWait;

import ar.edu.itba.pod.api.common.ParsedRow;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;
import java.time.LocalDateTime;

public class LongestWaitTripData implements DataSerializable, ParsedRow {
    private static final java.time.format.DateTimeFormatter FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Integer puLocation;
    private Integer doLocation;
    private String pickUpZone;
    private String dropOffZone;
    private String requestDatetime;
    private String pickupDatetime;

    public LongestWaitTripData() {}

    public LongestWaitTripData(Integer puLocation,
                               Integer doLocation,
                               String pickUpZone,
                               String dropOffZone,
                               String requestDatetime,
                               String pickupDatetime) {
        this.puLocation = puLocation;
        this.doLocation = doLocation;
        this.pickUpZone = pickUpZone;
        this.dropOffZone = dropOffZone;
        this.requestDatetime = requestDatetime;
        this.pickupDatetime = pickupDatetime;
    }

    public Integer getPULocation() {
        return puLocation;
    }

    public Integer getDOLocation() {
        return doLocation;
    }

    public String getPickUpZone() {
        return pickUpZone;
    }

    public String getDropoffZone() {
        return dropOffZone;
    }

    public String getRequest_datetime() {
        return requestDatetime;
    }

    public String getPickup_datetime() {
        return pickupDatetime;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(puLocation);
        out.writeInt(doLocation);

        out.writeUTF(pickUpZone);
        out.writeUTF(dropOffZone);

        out.writeUTF(requestDatetime);
        out.writeUTF(pickupDatetime);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.puLocation = in.readInt();
        this.doLocation = in.readInt();

        this.pickUpZone = in.readUTF();
        this.dropOffZone = in.readUTF();

        this.requestDatetime = in.readUTF();
        this.pickupDatetime = in.readUTF();
    }

    @Override
    public String toString() {
        return "LongestWaitTripData{" +
                "puLocation=" + puLocation +
                ", doLocation=" + doLocation +
                ", pickUpZone='" + pickUpZone + '\'' +
                ", dropOffZone='" + dropOffZone + '\'' +
                ", requestDatetime='" + requestDatetime + '\'' +
                ", pickupDatetime='" + pickupDatetime + '\'' +
                '}';
    }

    public long waitMillis(String requestDatetime, String pickupDatetime) {
        LocalDateTime request = LocalDateTime.parse(requestDatetime.trim(), FORMATTER);
        LocalDateTime pickup = LocalDateTime.parse(pickupDatetime.trim(), FORMATTER);
        return java.time.Duration.between(request, pickup).toMillis();
    }
}
