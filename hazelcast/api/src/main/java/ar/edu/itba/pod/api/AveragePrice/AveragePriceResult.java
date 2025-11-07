package ar.edu.itba.pod.api.AveragePrice;

import java.io.Serializable;

public class AveragePriceResult implements Serializable, Comparable<AveragePriceResult> {
    private final String pickUpBorough;
    private final String company;
    private final double avgFare;

    public AveragePriceResult(String pickUpBorough, String company, double avgFare) {
        this.pickUpBorough = pickUpBorough;
        this.company = company;
        this.avgFare = avgFare;
    }

    @Override
    public int compareTo(AveragePriceResult o) {
        int c = Double.compare(o.avgFare, this.avgFare);
        if (c != 0) return c;
        c = this.pickUpBorough.compareTo(o.pickUpBorough);
        if (c != 0) return c;
        return this.company.compareTo(o.company);
    }

    public String getPickUpBorough() {
        return pickUpBorough;
    }

    public String getCompany() {
        return company;
    }

    public double getAvgFare() {
        return avgFare;
    }

    @Override
    public String toString() {
        double truncatedFare = Math.floor(avgFare * 100) / 100.0;
        return String.format("%s;%s;%.2f", pickUpBorough, company, truncatedFare);
    }
}
