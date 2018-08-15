package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Cache to aid Bungee in case SQL is causing cpu thread starvation, leading to mysql connection drops.
 *
 * @author Rsl1122
 */
public class DataContainerCache extends DataContainer {

    public DataContainerCache() {
        super(TimeAmount.SECOND.ms() * 10L);
    }

    public PlayerContainer getPlayerContainer(UUID uuid) {
        return getAndCacheSupplier(Keys.playerContainer(uuid), Suppliers.playerContainer(uuid));
    }

    public AnalysisContainer getAnalysisContainer(UUID serverUUID) {
        return getAndCacheSupplier(Keys.analysisContainer(serverUUID), Suppliers.analysisContainer(serverUUID));
    }

    private <T> T getAndCacheSupplier(Key<T> key, Supplier<T> ifNotPresent) {
        if (!supports(key)) {
            putSupplier(key, ifNotPresent);
        }
        return getUnsafe(key);
    }

    private static class Keys {
        static Key<AnalysisContainer> analysisContainer(UUID serverUUID) {
            return new Key<>(AnalysisContainer.class, "ANALYSIS_CONTAINER:" + serverUUID);
        }

        static Key<PlayerContainer> playerContainer(UUID uuid) {
            return new Key<>(PlayerContainer.class, "PLAYER_CONTAINER:" + uuid);
        }

        private Keys() {
            // Static utility class
        }
    }

    private static class Suppliers {
        static Supplier<AnalysisContainer> analysisContainer(UUID serverUUID) {
            return () -> new AnalysisContainer(Database.getActive().fetch().getServerContainer(serverUUID));
        }

        static Supplier<PlayerContainer> playerContainer(UUID uuid) {
            return () -> Database.getActive().fetch().getPlayerContainer(uuid);
        }

        private Suppliers() {
            // Static utility class
        }
    }

}