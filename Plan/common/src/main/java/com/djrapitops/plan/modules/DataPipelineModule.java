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
import com.djrapitops.plan.gathering.cache.JoinAddressCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.gathering.domain.event.MobKill;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.storage.database.transactions.events.SessionEndTransaction;
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

    @Provides
    @Singleton
    @IntoSet
    DataService.Pipeline joinAddress(JoinAddressCache joinAddressCache) {
        return service -> service
                .registerSink(UUID.class, JoinAddress.class, joinAddressCache::put)
                .registerOptionalPullSource(UUID.class, JoinAddress.class, joinAddressCache::get)
                .registerSink(UUID.class, PlayerLeave.class, joinAddressCache::remove);
    }

    @Provides
    @Singleton
    @IntoSet
    DataService.Pipeline duringSession() {
        return service -> service
                .registerOptionalPullSource(UUID.class, ActiveSession.class, SessionCache::getCachedSession)
                .registerOptionalPullSource(UUID.class, WorldTimes.class, uuid ->
                        service.pull(ActiveSession.class, uuid)
                                .map(ActiveSession::getExtraData)
                                .flatMap(extra -> extra.get(WorldTimes.class)))
                .registerOptionalPullSource(UUID.class, MobKillCounter.class, uuid ->
                        service.pull(ActiveSession.class, uuid)
                                .map(ActiveSession::getExtraData)
                                .flatMap(extra -> extra.get(MobKillCounter.class)))
                .registerOptionalPullSource(UUID.class, DeathCounter.class, uuid ->
                        service.pull(ActiveSession.class, uuid)
                                .map(ActiveSession::getExtraData)
                                .flatMap(extra -> extra.get(DeathCounter.class)))
                .registerOptionalPullSource(UUID.class, PlayerKills.class, uuid ->
                        service.pull(ActiveSession.class, uuid)
                                .map(ActiveSession::getExtraData)
                                .flatMap(extra -> extra.get(PlayerKills.class)))
                .registerSink(UUID.class, MobKill.class, (uuid, kill) -> {
                    service.pull(MobKillCounter.class, uuid).ifPresent(Counter::add);
                })
                .registerSink(UUID.class, PlayerKill.class, (uuid, kill) -> {
                    service.pull(PlayerKills.class, kill.getKiller().getUuid()).ifPresent(playerKills -> playerKills.add(kill));
                    service.pull(DeathCounter.class, kill.getVictim().getUuid()).ifPresent(Counter::add);
                });
    }

    @Provides
    @Singleton
    @IntoSet
    DataService.Pipeline playerLeaveToSession(SessionCache sessionCache) {
        return service -> service
                .registerOptionalMapper(UUID.class, PlayerLeave.class, FinishedSession.class, sessionCache::endSession)
                .registerDatabaseSink(UUID.class, FinishedSession.class, SessionEndTransaction::new);
    }

}
