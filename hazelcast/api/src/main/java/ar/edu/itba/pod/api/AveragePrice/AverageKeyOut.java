package ar.edu.itba.pod.api.AveragePrice;

import java.io.Serializable;
import java.util.Objects;

public class AverageKeyOut implements Serializable {
    private final String pickUpBorough;
    private final String company;

    public AverageKeyOut(String pickUpBorough, String company) {
        this.pickUpBorough = pickUpBorough;
        this.company = company;
    }
    public String getPickUpBorough() { return pickUpBorough; }
    public String getCompany() { return company; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AverageKeyOut)) return false;
        AverageKeyOut that = (AverageKeyOut) o;
        return Objects.equals(pickUpBorough, that.pickUpBorough) &&
                Objects.equals(company, that.company);
    }
    @Override public int hashCode() { return Objects.hash(pickUpBorough, company); }
}
