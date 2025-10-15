package ar.edu.itba.pod.server;

import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();

        InterfacesConfig interfacesConfig = new InterfacesConfig()
                // TODO: CAMBIA CADA VEZ Q CAMBIAS DE RED
                .setInterfaces(Collections.singletonList("10.9.64.*"))
                .setEnabled(true);
        networkConfig.setInterfaces(interfacesConfig);

        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig()
                .setUrl("http://localhost:8080/mancenter")
                .setEnabled(true);
        config.setManagementCenterConfig(managementCenterConfig);

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
