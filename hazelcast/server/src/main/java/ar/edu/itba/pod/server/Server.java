package ar.edu.itba.pod.server;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        logger.info(" Server Starting ...");

        Config config = new Config();

        GroupConfig groupConfig = new GroupConfig().setName("g5").setPassword("g5");
        config.setGroupConfig(groupConfig);

        MulticastConfig multicastConfig = new MulticastConfig();

        JoinConfig joinConfig = new JoinConfig().setMulticastConfig(multicastConfig);

        InterfacesConfig interfacesConfig = new InterfacesConfig()
                //TODO: adaptarlo a pampero
                .setInterfaces(Collections.singletonList("127.0.0.*")).setEnabled(true);

        NetworkConfig networkConfig = new NetworkConfig().setInterfaces(interfacesConfig).setJoin(joinConfig);

        config.setNetworkConfig(networkConfig);

        // === Map configuration ===
        MapConfig mapConfig = new MapConfig("g5");
        mapConfig.setInMemoryFormat(InMemoryFormat.BINARY);     // clave! guarda binario y no objetos. lo deserializa solo al pedirlo.
        mapConfig.setBackupCount(0);                            // sin replicas. Tenemos poca memoria!!!
        mapConfig.setMaxSizeConfig(
                new MaxSizeConfig()
                        .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.PER_NODE)
        );
//        mapConfig.setEvictionPolicy(EvictionPolicy.NONE);     // por default, elimina entradas si se queda sin heap.
//         mapConfig.setMaxSizeConfig(new MaxSizeConfig(85, MaxSizeConfig.MaxSizePolicy.USED_HEAP_PERCENTAGE)); // limita por heap
        config.addMapConfig(mapConfig);

        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.FINE);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.FINE);
        }

        Hazelcast.newHazelcastInstance(config);
    }
}
