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
package com.djrapitops.plan.gathering.importing.importers;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.gathering.importing.data.BukkitUserImportRefiner;
import com.djrapitops.plan.gathering.importing.data.ServerImportData;
import com.djrapitops.plan.gathering.importing.data.UserImportData;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generic importer for user data into Plan on the Bukkit platform.
 *
 * @author Fuzzlemann
 */
public abstract class BukkitImporter implements Importer {

    protected final Supplier<ServerUUID> serverUUID;
    private final GeolocationCache geolocationCache;
    private final DBSystem dbSystem;
    private final String name;

    protected BukkitImporter(
            GeolocationCache geolocationCache,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            String name
    ) {
        this.geolocationCache = geolocationCache;
        this.dbSystem = dbSystem;
        this.serverUUID = serverInfo::getServerUUID;

        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract ServerImportData getServerImportData();

    public abstract List<UserImportData> getUserImportData();

    @Override
    public final void processImport() {
        ExecutorService service = Executors.newCachedThreadPool();

        try {
            service.submit(this::processServerData);
            service.submit(this::processUserData);
        } finally {
            shutdownService(service);
        }
    }

    private void processServerData() {
        ServerImportData serverImportData = getServerImportData();

        if (serverImportData == null) {
            return;
        }

        dbSystem.getDatabase().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllTPSData(Collections.singletonMap(serverUUID.get(), serverImportData.getTpsData())));
            }
        });
    }

    private void processUserData() {
        List<UserImportData> userImportData = getUserImportData();

        if (userImportData == null || userImportData.isEmpty()) {
            return;
        }

        BukkitUserImportRefiner userImportRefiner = new BukkitUserImportRefiner(userImportData);
        userImportData = userImportRefiner.refineData();

        Database db = dbSystem.getDatabase();

        Set<UUID> existingUUIDs = db.query(UserIdentifierQueries.fetchAllPlayerUUIDs());
        Set<UUID> existingUserInfoTableUUIDs = db.query(UserIdentifierQueries.fetchPlayerUUIDsOfServer(serverUUID.get()));

        Map<UUID, BaseUser> users = new HashMap<>();
        List<UserInfo> userInfo = new ArrayList<>();
        Map<UUID, List<Nickname>> nickNames = new HashMap<>();
        List<FinishedSession> sessions = new ArrayList<>();
        Map<UUID, List<GeoInfo>> geoInfo = new HashMap<>();

        userImportData.parallelStream().forEach(data -> {
            UUID uuid = data.getUuid();

            if (!existingUUIDs.contains(uuid)) {
                users.put(uuid, toBaseUser(data));
            }

            if (!existingUserInfoTableUUIDs.contains(uuid)) {
                userInfo.add(toUserInfo(data));
            }

            nickNames.put(uuid, data.getNicknames());
            geoInfo.put(uuid, convertGeoInfo(data));
            sessions.add(toSession(data));
        });

        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.insertBaseUsers(users.values()));
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
                Map<ServerUUID, List<UserInfo>> userInformation = Collections.singletonMap(serverUUID.get(), userInfo);
                execute(LargeStoreQueries.storePerServerUserInformation(userInformation));
                execute(LargeStoreQueries.storeAllNicknameData(Collections.singletonMap(serverUUID.get(), nickNames)));
                execute(LargeStoreQueries.storeAllGeoInformation(geoInfo));
            }
        });
    }

    private void shutdownService(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(20, TimeUnit.MINUTES)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private BaseUser toBaseUser(UserImportData userImportData) {
        UUID playerUUID = userImportData.getUuid();
        String playerName = userImportData.getName();
        long registered = userImportData.getRegistered();
        int timesKicked = userImportData.getTimesKicked();
        return new BaseUser(playerUUID, playerName, registered, timesKicked);
    }

    private UserInfo toUserInfo(UserImportData userImportData) {
        UUID uuid = userImportData.getUuid();
        long registered = userImportData.getRegistered();
        boolean op = userImportData.isOp();
        boolean banned = userImportData.isBanned();
        String joinAddress = userImportData.getJoinAddress();

        return new UserInfo(uuid, serverUUID.get(), registered, op, joinAddress, banned);
    }

    private FinishedSession toSession(UserImportData userImportData) {
        int mobKills = userImportData.getMobKills();
        int deaths = userImportData.getDeaths();

        DataMap extraData = new DataMap();
        extraData.put(MobKillCounter.class, new MobKillCounter(mobKills));
        extraData.put(DeathCounter.class, new DeathCounter(deaths));
        extraData.put(WorldTimes.class, new WorldTimes(userImportData.getWorldTimes()));
        extraData.put(PlayerKills.class, new PlayerKills(userImportData.getKills()));

        return new FinishedSession(userImportData.getUuid(), serverUUID.get(), 0L, 0L, 0, extraData);
    }

    private List<GeoInfo> convertGeoInfo(UserImportData userImportData) {
        long date = System.currentTimeMillis();

        return userImportData.getIps().stream()
                .map(geolocationCache::getCountry)
                .filter(Objects::nonNull)
                .map(geoLocation -> new GeoInfo(geoLocation, date))
                .collect(Collectors.toList());
    }
}
