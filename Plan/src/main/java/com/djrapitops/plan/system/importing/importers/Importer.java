/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.importing.importers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.databases.Database;
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
import java.util.stream.Collectors;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public abstract class Importer {

    private final GeolocationCache geolocationCache;
    private final Database database;
    private final UUID serverUUID;

    private final String name;

    protected Importer(
            GeolocationCache geolocationCache,
            Database database,
            ServerInfo serverInfo,
            String name
    ) {
        this.geolocationCache = geolocationCache;
        this.database = database;
        this.serverUUID = serverInfo.getServerUUID();

        this.name = name;
    }

    @Deprecated
    public List<String> getNames() {
        return new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public abstract ServerImportData getServerImportData();

    public abstract List<UserImportData> getUserImportData();

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

        SaveOperations save = database.save();
        submitTo(service, () -> save.insertTPS(ImmutableMap.of(serverUUID, serverImportData.getTpsData())));
        submitTo(service, () -> save.insertCommandUsage(ImmutableMap.of(serverUUID, serverImportData.getCommandUsages())));

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

        UserImportRefiner userImportRefiner = new UserImportRefiner(Plan.getInstance(), userImportData);
        userImportData = userImportRefiner.refineData();

        Set<UUID> existingUUIDs = database.fetch().getSavedUUIDs();
        Set<UUID> existingUserInfoTableUUIDs = database.fetch().getSavedUUIDs(serverUUID);

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

        SaveOperations save = database.save();

        save.insertUsers(users);
        submitTo(service, () -> save.insertSessions(ImmutableMap.of(serverUUID, sessions), true));
        submitTo(service, () -> save.kickAmount(timesKicked));
        submitTo(service, () -> save.insertUserInfo(ImmutableMap.of(serverUUID, userInfo)));
        submitTo(service, () -> save.insertNicknames(ImmutableMap.of(serverUUID, nickNames)));
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

    private UserInfo toUserInfo(UserImportData userImportData) {
        UUID uuid = userImportData.getUuid();
        String name = userImportData.getName();
        long registered = userImportData.getRegistered();
        boolean op = userImportData.isOp();
        boolean banned = userImportData.isBanned();

        return new UserInfo(uuid, name, registered, op, banned);
    }

    private Session toSession(UserImportData userImportData) {
        int mobKills = userImportData.getMobKills();
        int deaths = userImportData.getDeaths();

        Session session = new Session(0, userImportData.getUuid(), serverUUID, 0L, 0L, mobKills, deaths, 0);

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
