package ar.edu.itba.pod.api.AveragePrice;

import java.util.Locale;

public record AveragePriceOutput(String pickUpBorough, String company, double avgFare) {
    @Override
    public String toString() {
        return String.format(Locale.US, "%s;%s;%.2f", pickUpBorough, company, avgFare);
    }
}
