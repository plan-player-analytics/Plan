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

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System that holds data caches of the plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class CacheSystem implements SubSystem {

    private final NicknameCache nicknameCache;
    private final GeolocationCache geolocationCache;

    @Inject
    public CacheSystem(NicknameCache nicknameCache, GeolocationCache geolocationCache) {
        this.nicknameCache = nicknameCache;
        this.geolocationCache = geolocationCache;
    }

    @Override
    public void enable() throws EnableException {
        nicknameCache.enable();
        geolocationCache.enable();
    }

    @Override
    public void disable() {
        geolocationCache.clearCache();
    }

    public NicknameCache getNicknameCache() {
        return nicknameCache;
    }

    public GeolocationCache getGeolocationCache() {
        return geolocationCache;
    }

}
