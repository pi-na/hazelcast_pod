package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class LongestWaitReducerValue implements DataSerializable {
    private long doLocationID;
    private long waitTimeMillis;
    private String doLocationName;
    private String puLocationName;

    public LongestWaitReducerValue() {}

    public LongestWaitReducerValue(long doLocationID, long waitTimeMillis, String doLocationName, String puLocationName) {
        this.doLocationID = doLocationID;
        this.waitTimeMillis = waitTimeMillis;
        this.doLocationName = doLocationName;
        this.puLocationName = puLocationName;
    }

    public long doLocationID() {
        return doLocationID;
    }

    public long waitTimeMillis() {
        return waitTimeMillis;
    }

    public String doLocationName() {
        return doLocationName;
    }

    public String puLocationName() {
        return puLocationName;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(doLocationID);
        out.writeLong(waitTimeMillis);
        out.writeUTF(doLocationName);
        out.writeUTF(puLocationName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        doLocationID = in.readLong();
        waitTimeMillis = in.readLong();
        doLocationName = in.readUTF();
        puLocationName = in.readUTF();
    }
}
