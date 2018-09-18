package com.djrapitops.plan.system.importing;

import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.importing.importers.OfflinePlayerImporter;
import com.djrapitops.plan.system.info.server.ServerInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ImportSystem implementation for Bukkit.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitImportSystem extends ImportSystem {

    private final GeolocationCache geolocationCache;
    private final Database database;
    private final ServerInfo serverInfo;

    @Inject
    public BukkitImportSystem(
            GeolocationCache geolocationCache,
            Database database,
            ServerInfo serverInfo
    ) {
        this.geolocationCache = geolocationCache;
        this.database = database;
        this.serverInfo = serverInfo;
    }

    @Override
    void registerImporters() {
        registerImporter(new OfflinePlayerImporter(geolocationCache, database, serverInfo));
    }
}