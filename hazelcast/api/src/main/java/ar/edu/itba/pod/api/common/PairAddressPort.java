package ar.edu.itba.pod.api.common;

public class PairAddressPort {

    private final String address;
    private final String port;

    public PairAddressPort(String address, String port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

}