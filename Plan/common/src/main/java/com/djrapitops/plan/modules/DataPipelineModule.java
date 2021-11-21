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
package com.djrapitops.plan.modules;

import com.djrapitops.plan.DataService;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

import javax.inject.Singleton;
import java.util.UUID;

@Module // NOTE: not yet in use
public class DataPipelineModule {

    @Provides
    @Singleton
    @IntoSet
    DataService.Pipeline playerJoinToSession(SessionCache sessionCache) {
        return service -> service
                .registerMapper(UUID.class, PlayerJoin.class, ActiveSession.class, ActiveSession::fromPlayerJoin)
                .registerSink(UUID.class, ActiveSession.class, sessionCache::cacheSession);
    }

}
