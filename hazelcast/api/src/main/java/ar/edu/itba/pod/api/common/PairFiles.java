package ar.edu.itba.pod.api.common;

public class PairFiles {
    private String tripsFile;
    private String zonesFiles;

    public PairFiles(final String tripsFile, final String zonesFiles) {
        this.tripsFile = tripsFile;
        this.zonesFiles = zonesFiles;
    }

    public String gettripsFile() {
        return tripsFile;
    }

    public String getzonesFiles() {
        return zonesFiles;
    }

}
