package ar.edu.itba.pod.client.params;

import ar.edu.itba.pod.api.common.PairAddressPort;

import java.util.Arrays;
import java.util.List;

public class DefaultParams {
    private final List<PairAddressPort> addresses;
    private final String inPath;
    private final String outPath;

    public DefaultParams(List<PairAddressPort> addresses, String inPath, String outPath) {
        this.addresses = addresses;
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public List<PairAddressPort> getAddresses() {
        return addresses;
    }

    public String getInPath() {
        return inPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public static DefaultParams getParams(String addresses, String inPath, String outPath) {
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("A server address must be provided.");
        }

        List<PairAddressPort> addressesList = Arrays.stream(addresses.split(";"))
                .map(part -> {
                    String[] addressAndPort = part.split(":");
                    return new PairAddressPort(addressAndPort[0], addressAndPort.length > 1 ? addressAndPort[1] : "5701");
                })
                .toList();
        return new DefaultParams(
                addressesList,
                inPath,
                outPath
        );
    }
}