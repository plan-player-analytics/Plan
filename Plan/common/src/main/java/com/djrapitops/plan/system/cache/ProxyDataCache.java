/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.database.DBSystem;
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
    public ProxyDataCache(DBSystem dbSystem, ErrorHandler errorHandler) {
        super(dbSystem, errorHandler);
    }

    @Override
    public void endSession(UUID uuid, long time) {
        removeSessionFromCache(uuid);
        /* Proxy should not save sessions so session is not removed.. */
    }
}
