package ar.edu.itba.pod.api.AveragePrice;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class AverageKeyOut implements DataSerializable {
    private String pickUpBorough;
    private String company;

    public AverageKeyOut() {}

    public AverageKeyOut(String pickUpBorough, String company) {
        this.pickUpBorough = pickUpBorough;
        this.company = company;
    }

    public String getPickUpBorough() { return pickUpBorough; }
    public String getCompany() { return company; }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(pickUpBorough);
        out.writeUTF(company);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.pickUpBorough = in.readUTF();
        this.company = in.readUTF();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AverageKeyOut)) return false;
        AverageKeyOut that = (AverageKeyOut) o;
        return Objects.equals(pickUpBorough, that.pickUpBorough) &&
                Objects.equals(company, that.company);
    }

    @Override
    public int hashCode() { return Objects.hash(pickUpBorough, company); }
}
