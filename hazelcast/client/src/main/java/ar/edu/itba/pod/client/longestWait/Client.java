package ar.edu.itba.pod.client.longestWait;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
import ar.edu.itba.pod.api.enums.TripsColumns;
import ar.edu.itba.pod.api.longestWait.LongestWaitMapperValueIn;
import ar.edu.itba.pod.api.totalTrips.TotalTrips;
import ar.edu.itba.pod.client.QueryCLient;
import ar.edu.itba.pod.client.params.DefaultParams;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Client extends QueryCLient {
    private static final String QUERY4_CSV = "query4.csv";
    private static final String QUERY4_CSV_HEADERS = "request_datetime;pickup_datetime;PULocationID;DOLocationID";
    private static final String DIVIDER = ";";

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client() throws IOException, ExecutionException, InterruptedException {super();}

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException{
        QueryCLient queryCLient = new Client();
    }

    @Override
    public void finishQuery(IMap<String, TotalTrips> iMap, HazelcastInstance hazelcastInstance, DefaultParams params) throws IOException, ExecutionException, InterruptedException {
        KeyValueSource<Long, LongestWaitMapperValueIn>
    }

    // TODO: En la clase Trip, los datetime son String. Deberian ser LocalDateTime?
    @Override
    public Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);

        Stream<Trip> tripStream = lines
                .skip(1)
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .map(line -> line.split(DIVIDER)) // -1 para no perder vacíos
                .filter(cols -> {
                    try {
                        int pu = Integer.parseInt(cols[TripsColumns.PULOCATIONID.getIndex()].trim());
                        Zone z = zones.get(pu);
                        return z != null && z.getBorough().equalsIgnoreCase(borough);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(cols -> parseTrip(cols, zones));

        return tripStream.onClose(lines::close);
    }
}

}
