package ar.edu.itba.pod.client;

import ar.edu.itba.pod.model.Trip;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static final String COMMA_DELIMITER = ";";
    private static List<List<String>> records;
    private static final String CSV_FILE = "/Users/tomaspinausig/code/hazelcast_pod/trips.csv";

    public static void main(String[] args) throws InterruptedException {
        logger.info("hazelcast Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        List<Trip> trips;
        try (Stream<String> lines = Files.lines(Paths.get(CSV_FILE))) {
            trips = lines
                    .skip(1)
                    .map(line -> line.split(";"))
                    .map(Trip::new)
                    .toList();

            // DEBUG
            trips.forEach(t -> {logger.info(t.toString());});
        } catch(IOException e){
            logger.error(e.getMessage());
        }


        try {
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
