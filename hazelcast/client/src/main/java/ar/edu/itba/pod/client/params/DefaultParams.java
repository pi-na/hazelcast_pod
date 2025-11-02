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

    public static DefaultParams getParams(String addressesKey, String inPathKey, String outPathKey) {
        String addresses = System.getProperty(addressesKey);
        String inPath = System.getProperty(inPathKey);
        String outPath = System.getProperty(outPathKey);

        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalArgumentException("A server address must be provided. Use -Daddresses=<ip:port;ip:port>.");
        }

        if (inPath == null || inPath.isEmpty()) {
            throw new IllegalArgumentException("An input path must be provided. Use -DinPath=<path_to_csv_directory>.");
        }

        if (outPath == null || outPath.isEmpty()) {
            throw new IllegalArgumentException("An output path must be provided. Use -DoutPath=<output_directory>.");
        }

        List<PairAddressPort> addressesList = Arrays.stream(addresses.split(";"))
                .map(part -> {
                    String[] addressAndPort = part.split(":");
                    return new PairAddressPort(addressAndPort[0], addressAndPort.length > 1 ? addressAndPort[1] : "5701");
                })
                .toList();

        return new DefaultParams(addressesList, inPath, outPath);
    }
}