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
                .setInterfaces(Collections.singletonList("10.6.1.*")).setEnabled(true);

        NetworkConfig networkConfig = new NetworkConfig().setInterfaces(interfacesConfig).setJoin(joinConfig);

        config.setNetworkConfig(networkConfig);

        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.FINE);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.FINE);
        }

        Hazelcast.newHazelcastInstance(config);
    }
}
