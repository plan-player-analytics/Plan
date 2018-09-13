package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Bungee specific DataCache.
 * <p>
 * Used for overriding {@link SessionCache#endSession(UUID, long)}.
 *
 * @author Rsl1122
 */
@Singleton
public class BungeeDataCache extends DataCache {

    @Inject
    public BungeeDataCache(Database database, ErrorHandler errorHandler) {
        super(database, errorHandler);
    }

    @Override
    public void endSession(UUID uuid, long time) {
        removeSessionFromCache(uuid);
        /* Bungee should not save sessions so session is not removed.. */
    }
}
