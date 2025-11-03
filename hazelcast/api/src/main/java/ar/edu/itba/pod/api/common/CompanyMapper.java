package ar.edu.itba.pod.api.common;

public class CompanyMapper {
    public static String toCompanyName(String hvfhs) {
        if (hvfhs == null) return "Unknown";
        switch (hvfhs) {
            case "HV0003": return "Uber";
            case "HV0005": return "Lyft";
            case "HV0002": return "Juno";
            case "HV0004": return "Via";
            default: return hvfhs;
        }
    }
}
