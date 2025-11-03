package ar.edu.itba.pod.client.longestWait;

import ar.edu.itba.pod.api.common.PairFiles;
import ar.edu.itba.pod.api.common.Trip;
import ar.edu.itba.pod.api.common.Zone;
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

    }

    // me mandan un borough por terminal -> TODO: Conseguir el borough de parametro de terminal
    // tengo que conseguir un mapa de barrios que estan en esa borough
    // tengo que filtrar las lineas de zone para que esten en esa borough
    @Override
    public Stream<Trip> genericParseRows(PairFiles files, Map<Integer, Zone> zones, String borough) throws IOException {
        Stream<String> lines = Files.lines(Paths.get(files.gettripsFile()), StandardCharsets.UTF_8);
        Stream<Trip> tripStream = lines
                .skip(1)
                .map(String::trim)
                .map(line -> line.split(DIVIDER))
                .filter()
    }
}
