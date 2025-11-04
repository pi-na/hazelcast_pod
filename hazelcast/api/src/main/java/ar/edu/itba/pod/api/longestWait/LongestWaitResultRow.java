package ar.edu.itba.pod.api.longestWait;

public class LongestWaitResultRow implements Comparable<LongestWaitResultRow> {
    private final String puZoneName;
    private final String doZoneName;
    private final long waitTimeMillis;
    private final long puLocationID;
    private final long doLocationID;

    public LongestWaitResultRow(String puZoneName, String doZoneName, long waitTimeMillis,
                                long puLocationID, long doLocationID) {
        this.puZoneName = puZoneName;
        this.doZoneName = doZoneName;
        this.waitTimeMillis = waitTimeMillis;
        this.puLocationID = puLocationID;
        this.doLocationID = doLocationID;
    }

    public String getPuZoneName() { return puZoneName; }
    public String getDoZoneName() { return doZoneName; }
    public long getWaitTimeMillis() { return waitTimeMillis; }
    public long getPuLocationID() { return puLocationID; }
    public long getDoLocationID() { return doLocationID; }

    @Override
    public int compareTo(LongestWaitResultRow o) {
        return this.puZoneName.compareToIgnoreCase(o.puZoneName);
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%d;%d;%d",
                puZoneName, doZoneName, waitTimeMillis, puLocationID, doLocationID);
    }
}
