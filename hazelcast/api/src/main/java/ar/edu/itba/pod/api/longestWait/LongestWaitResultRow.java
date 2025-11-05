package ar.edu.itba.pod.api.longestWait;

public class LongestWaitResultRow implements Comparable<LongestWaitResultRow> {
    private final String pickUpZone;
    private final String dropOffZone;
    private final long delayInSeconds;

    public LongestWaitResultRow(String pickUpZone, String dropOffZone, long delayInSeconds) {
        this.pickUpZone = pickUpZone;
        this.dropOffZone = dropOffZone;
        this.delayInSeconds = delayInSeconds;
    }

    public String getPickUpZone() { return pickUpZone; }
    public String getDropOffZone() { return dropOffZone; }
    public long getDelayInSeconds() { return delayInSeconds; }

    @Override
    public int compareTo(LongestWaitResultRow o) {
        return this.pickUpZone.compareToIgnoreCase(o.pickUpZone);
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%d", pickUpZone, dropOffZone, delayInSeconds);
    }
}
