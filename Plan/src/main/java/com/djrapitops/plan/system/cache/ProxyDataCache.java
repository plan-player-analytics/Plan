package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Proxy server specific DataCache.
 * <p>
 * Used for overriding {@link SessionCache#endSession(UUID, long)}.
 *
 * @author Rsl1122
 */
@Singleton
public class ProxyDataCache extends DataCache {

    @Inject
    public ProxyDataCache(Database database, ErrorHandler errorHandler) {
        super(database, errorHandler);
    }

    @Override
    public void endSession(UUID uuid, long time) {
        removeSessionFromCache(uuid);
        /* Proxy should not save sessions so session is not removed.. */
    }
}
