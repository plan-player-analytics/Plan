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
package com.djrapitops.plan.system.importing.importers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.BaseUser;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.importing.data.ServerImportData;
import com.djrapitops.plan.system.importing.data.UserImportData;
import com.djrapitops.plan.system.importing.data.UserImportRefiner;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.SHA256Hash;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.collect.ImmutableMap;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Fuzzlemann
 */
public abstract class BukkitImporter implements Importer {

    protected final Supplier<UUID> serverUUID;
    private final GeolocationCache geolocationCache;
    private final DBSystem dbSystem;
    private final String name;
    private final Plan plugin;

    protected BukkitImporter(
            Plan plugin,
            GeolocationCache geolocationCache,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            String name
    ) {
        this.geolocationCache = geolocationCache;
        this.dbSystem = dbSystem;
        this.serverUUID = serverInfo::getServerUUID;

        this.name = name;
        this.plugin = plugin;
    }

    @Deprecated
    public List<String> getNames() {
        return new ArrayList<>();
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

        submitTo(service, this::processServerData);
        submitTo(service, this::processUserData);

        service.shutdown();
        try {
            service.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    private void processServerData() {
        ServerImportData serverImportData = getServerImportData();

        if (serverImportData == null) {
            return;
        }

        ExecutorService service = Executors.newCachedThreadPool();

        SaveOperations save = dbSystem.getDatabase().save();
        submitTo(service, () -> save.insertTPS(ImmutableMap.of(serverUUID.get(), serverImportData.getTpsData())));
        submitTo(service, () -> save.insertCommandUsage(ImmutableMap.of(serverUUID.get(), serverImportData.getCommandUsages())));

        service.shutdown();
        try {
            service.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void processUserData() {
        List<UserImportData> userImportData = getUserImportData();

        if (Verify.isEmpty(userImportData)) {
            return;
        }

        UserImportRefiner userImportRefiner = new UserImportRefiner(plugin, userImportData);
        userImportData = userImportRefiner.refineData();

        FetchOperations fetch = dbSystem.getDatabase().fetch();
        Set<UUID> existingUUIDs = fetch.getSavedUUIDs();
        Set<UUID> existingUserInfoTableUUIDs = fetch.getSavedUUIDs(serverUUID.get());

        Map<UUID, UserInfo> users = new HashMap<>();
        List<UserInfo> userInfo = new ArrayList<>();
        Map<UUID, List<Nickname>> nickNames = new HashMap<>();
        Map<UUID, List<Session>> sessions = new HashMap<>();
        Map<UUID, List<GeoInfo>> geoInfo = new HashMap<>();
        Map<UUID, Integer> timesKicked = new HashMap<>();

        userImportData.parallelStream().forEach(data -> {
            UUID uuid = data.getUuid();
            UserInfo info = toUserInfo(data);

            if (!existingUUIDs.contains(uuid)) {
                users.put(uuid, info);
            }

            if (!existingUserInfoTableUUIDs.contains(uuid)) {
                userInfo.add(info);
            }

            nickNames.put(uuid, data.getNicknames());
            geoInfo.put(uuid, convertGeoInfo(data));
            timesKicked.put(uuid, data.getTimesKicked());
            sessions.put(uuid, Collections.singletonList(toSession(data)));
        });

        ExecutorService service = Executors.newCachedThreadPool();

        SaveOperations save = dbSystem.getDatabase().save();

        save.insertUsers(users);
        submitTo(service, () -> save.insertSessions(ImmutableMap.of(serverUUID.get(), sessions), true));
        submitTo(service, () -> save.kickAmount(timesKicked));
        submitTo(service, () -> save.insertUserInfo(ImmutableMap.of(serverUUID.get(), userInfo)));
        submitTo(service, () -> save.insertNicknames(ImmutableMap.of(serverUUID.get(), nickNames)));
        submitTo(service, () -> save.insertAllGeoInfo(geoInfo));

        service.shutdown();
        try {
            service.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void submitTo(ExecutorService service, ImportExecutorHelper helper) {
        helper.submit(service);
    }

    // TODO Refactor parts to use the base user
    private BaseUser toBaseUser(UserImportData userImportData) {
        UUID uuid = userImportData.getUuid();
        String name = userImportData.getName();
        long registered = userImportData.getRegistered();
        int timesKicked = userImportData.getTimesKicked();
        return new BaseUser(uuid, name, registered, timesKicked);
    }

    private UserInfo toUserInfo(UserImportData userImportData) {
        UUID uuid = userImportData.getUuid();
        long registered = userImportData.getRegistered();
        boolean op = userImportData.isOp();
        boolean banned = userImportData.isBanned();

        return new UserInfo(uuid, serverUUID.get(), registered, op, banned);
    }

    private Session toSession(UserImportData userImportData) {
        int mobKills = userImportData.getMobKills();
        int deaths = userImportData.getDeaths();

        Session session = new Session(0, userImportData.getUuid(), serverUUID.get(), 0L, 0L, mobKills, deaths, 0);

        session.setPlayerKills(userImportData.getKills());
        session.setWorldTimes(new WorldTimes(userImportData.getWorldTimes()));

        return session;
    }

    private List<GeoInfo> convertGeoInfo(UserImportData userImportData) {
        long date = System.currentTimeMillis();

        return userImportData.getIps().parallelStream()
                .map(ip -> {
                    String geoLoc = geolocationCache.getCountry(ip);
                    try {
                        return new GeoInfo(ip, geoLoc, date, new SHA256Hash(ip).create());
                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalArgumentException(e);
                    }
                }).collect(Collectors.toList());
    }

    private interface ImportExecutorHelper {
        void execute() throws DBException;

        default void submit(ExecutorService service) {
            service.submit(() -> {
                try {
                    execute();
                } catch (DBException e) {
                    throw new DBOpException("Import Execution failed", e);
                }
            });
        }
    }
}
