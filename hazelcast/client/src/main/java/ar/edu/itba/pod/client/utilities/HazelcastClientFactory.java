package ar.edu.itba.pod.client.utilities;

import ar.edu.itba.pod.api.common.PairAddressPort;
import ar.edu.itba.pod.client.params.DefaultParams;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;

public final class HazelcastClientFactory {
    public static HazelcastInstance newHazelcastClient(DefaultParams params) {
        GroupConfig groupConfig = new GroupConfig().setName("g5").setPassword("g5");
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();

        for (PairAddressPort addressAndPort : params.getAddresses()) {
            clientNetworkConfig.addAddress(addressAndPort.getAddress() + ":" + addressAndPort.getPort());
        }
        ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
