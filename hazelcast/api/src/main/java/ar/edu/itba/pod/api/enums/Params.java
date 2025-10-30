package ar.edu.itba.pod.api.enums;

public enum Params {
    QUERYID("queryId"),
    ADDRESSES("addresses"),
    INPATH("inPath"),
    OUTPATH("outPath"),
    BOROUGH("borough"),;

    private final String param;

    Params(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
