package ar.edu.itba.pod.api.common;

import java.util.Map;
import java.util.Optional;

public interface RowParser<V extends ParsedRow> {
    Optional<V> parseRow(String[] cols, Map<Integer, Zone> zones);
}
