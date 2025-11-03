package ar.edu.itba.pod.api.AveragePrice;

import java.io.Serializable;

public class AveragePriceAccumulator implements Serializable {
    private double sum;
    private long count;

    public AveragePriceAccumulator() {}
    public AveragePriceAccumulator(double sum, long count) { this.sum = sum; this.count = count; }

    public void add(double v) { sum += v; count += 1; }
    public void merge(AveragePriceAccumulator o) { if (o != null) { sum += o.sum; count += o.count; } }
    public double getSum() { return sum; }
    public long getCount() { return count; }
}
