package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.MiscUtils;

/**
 * DataContainer for the whole network.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.NetworkKeys for Key objects
 * @see com.djrapitops.plan.data.store.PlaceholderKey for placeholder information
 */
public class NetworkContainer extends DataContainer {

    private final ServerContainer bungeeContainer;

    public NetworkContainer(ServerContainer bungeeContainer) {
        this.bungeeContainer = bungeeContainer;
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(NetworkKeys.REFRESH_TIME, now);
        putSupplier(NetworkKeys.REFRESH_TIME_F, () -> Formatters.second().apply(() -> getUnsafe(NetworkKeys.REFRESH_TIME)));

        putRawData(NetworkKeys.VERSION, PlanPlugin.getInstance().getVersion());
        putSupplier(NetworkKeys.TIME_ZONE, MiscUtils::getTimeZoneOffsetHours);

        putSupplier(NetworkKeys.NETWORK_NAME, () -> bungeeContainer.getValue(ServerKeys.NAME).orElse("Plan"));
        putSupplier(NetworkKeys.PLAYERS_ONLINE, ServerInfo.getServerProperties()::getOnlinePlayers);
    }

}