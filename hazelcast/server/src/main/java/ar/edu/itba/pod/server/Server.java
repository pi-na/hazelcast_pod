package ar.edu.itba.pod.server;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Map<String, String> datos = hz.getMap("materias");
        datos.put("72.42", "POD");

        System.out.println(String.format("%d Datos en el cluster",
                datos.size() ));

        for (String key : datos.keySet()) {
            System.out.println(String.format( "Datos con key %s= %s",
                    key, datos.get(key)));
            }
        }
}
