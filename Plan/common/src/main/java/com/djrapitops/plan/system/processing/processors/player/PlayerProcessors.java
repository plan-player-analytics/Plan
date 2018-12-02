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
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/**
 * Factory for creating Runnables related to Player data to run with {@link com.djrapitops.plan.system.processing.Processing}.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerProcessors {

    private final Lazy<Processing> processing;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<DBSystem> dbSystem;
    private final Lazy<DataCache> dataCache;
    private final Lazy<GeolocationCache> geolocationCache;

    @Inject
    public PlayerProcessors(
            Lazy<Processing> processing,
            Lazy<ServerInfo> serverInfo,
            Lazy<DBSystem> dbSystem,
            Lazy<DataCache> dataCache,
            Lazy<GeolocationCache> geolocationCache
    ) {
        this.processing = processing;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.dataCache = dataCache;
        this.geolocationCache = geolocationCache;
    }

    public BanAndOpProcessor banAndOpProcessor(UUID uuid, BooleanSupplier banned, boolean op) {
        return new BanAndOpProcessor(uuid, banned, op, dbSystem.get().getDatabase());
    }

    public ProxyRegisterProcessor proxyRegisterProcessor(UUID uuid, String name, long registered, Runnable... afterProcess) {
        return new ProxyRegisterProcessor(uuid, name, registered, processing.get(), dbSystem.get().getDatabase(), afterProcess);
    }

    public EndSessionProcessor endSessionProcessor(UUID uuid, long time) {
        return new EndSessionProcessor(uuid, time, dataCache.get());
    }

    public IPUpdateProcessor ipUpdateProcessor(UUID uuid, InetAddress ip, long time) {
        return new IPUpdateProcessor(uuid, ip, time, dbSystem.get().getDatabase(), geolocationCache.get());
    }

    public KickProcessor kickProcessor(UUID uuid) {
        return new KickProcessor(uuid, dbSystem.get().getDatabase());
    }

    public NameProcessor nameProcessor(UUID uuid, String playerName, String displayName) {
        Nickname nickname = new Nickname(displayName, System.currentTimeMillis(), serverInfo.get().getServerUUID());
        return new NameProcessor(uuid, playerName, nickname, dbSystem.get().getDatabase(), dataCache.get());
    }

    public PingInsertProcessor pingInsertProcessor(UUID uuid, List<DateObj<Integer>> pingList) {
        return new PingInsertProcessor(uuid, serverInfo.get().getServerUUID(), pingList, dbSystem.get().getDatabase());
    }

    public RegisterProcessor registerProcessor(UUID uuid, LongSupplier registered, String name, Runnable... afterProcess) {
        return new RegisterProcessor(uuid, registered, name, processing.get(), dbSystem.get().getDatabase(), afterProcess);
    }

}