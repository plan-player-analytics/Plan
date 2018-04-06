/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.importing.importers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.importing.ServerImportData;
import com.djrapitops.plan.system.processing.importing.UserImportData;
import com.djrapitops.plan.system.processing.importing.UserImportRefiner;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.collect.ImmutableMap;

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

    public abstract List<String> getNames();

    public abstract ServerImportData getServerImportData();

    public abstract List<UserImportData> getUserImportData();

    public final void processImport() {
        String benchmarkName = "Import processing";
        String serverBenchmarkName = "Server Data processing";
        String userDataBenchmarkName = "User Data processing";

        Benchmark.start(benchmarkName);

        ExecutorService service = Executors.newCachedThreadPool();

        submitTo(service, () -> {
            Benchmark.start(serverBenchmarkName);
            processServerData();
            Benchmark.stop(serverBenchmarkName);
        });

        submitTo(service, () -> {
            Benchmark.start(userDataBenchmarkName);
            processUserData();
            Benchmark.stop(userDataBenchmarkName);
        });

        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Log.toLog(this.getClass(), e);
            Thread.currentThread().interrupt();
        }

        Benchmark.stop(benchmarkName);
    }

    private void processServerData() {
        String benchmarkName = "Processing Server Data";
        String getDataBenchmarkName = "Getting Server Data";
        String insertDataIntoDatabaseBenchmarkName = "Insert Server Data into Database";

        Benchmark.start(benchmarkName);
        Benchmark.start(getDataBenchmarkName);

        ServerImportData serverImportData = getServerImportData();

        Benchmark.stop(getDataBenchmarkName);

        if (serverImportData == null) {
            Log.debug("Server Import Data null, skipping");
            return;
        }

        UUID uuid = ServerInfo.getServerUUID();
        Database db = Database.getActive();

        ExecutorService service = Executors.newCachedThreadPool();

        Benchmark.start(insertDataIntoDatabaseBenchmarkName);

        SaveOperations save = db.save();
        submitTo(service, () -> save.insertTPS(ImmutableMap.of(uuid, serverImportData.getTpsData())));
        submitTo(service, () -> save.insertCommandUsage(ImmutableMap.of(uuid, serverImportData.getCommandUsages())));

        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.toLog(this.getClass(), e);
        }

        Benchmark.stop(insertDataIntoDatabaseBenchmarkName);
        Benchmark.stop(benchmarkName);
    }

    private void processUserData() throws DBException {
        String benchmarkName = "Processing User Data";
        String getDataBenchmarkName = "Getting User Data";
        String insertDataIntoCollectionsBenchmarkName = "Insert User Data into Collections";
        String insertDataIntoDatabaseBenchmarkName = "Insert User Data into Database";

        Benchmark.start(benchmarkName);
        Benchmark.start(getDataBenchmarkName);

        List<UserImportData> userImportData = getUserImportData();
        Benchmark.stop(getDataBenchmarkName);

        if (Verify.isEmpty(userImportData)) {
            Log.debug("User Import Data null or empty, skipping");
            return;
        }

        UserImportRefiner userImportRefiner = new UserImportRefiner(Plan.getInstance(), userImportData);
        userImportData = userImportRefiner.refineData();

        UUID serverUUID = ServerInfo.getServerUUID();
        Database db = Database.getActive();

        Set<UUID> existingUUIDs = db.fetch().getSavedUUIDs();
        Set<UUID> existingUserInfoTableUUIDs = db.fetch().getSavedUUIDs(serverUUID);

        Benchmark.start(insertDataIntoCollectionsBenchmarkName);

        Map<UUID, UserInfo> users = new HashMap<>();
        List<UserInfo> userInfo = new ArrayList<>();
        Map<UUID, List<String>> nickNames = new HashMap<>();
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

        Benchmark.stop(insertDataIntoCollectionsBenchmarkName);

        ExecutorService service = Executors.newCachedThreadPool();

        Benchmark.start(insertDataIntoDatabaseBenchmarkName);

        SaveOperations save = db.save();

        save.insertUsers(users);
        submitTo(service, () -> save.insertSessions(ImmutableMap.of(serverUUID, sessions), true));
        submitTo(service, () -> save.kickAmount(timesKicked));
        submitTo(service, () -> save.insertUserInfo(ImmutableMap.of(serverUUID, userInfo)));
        submitTo(service, () -> save.insertNicknames(ImmutableMap.of(serverUUID, nickNames)));
        submitTo(service, () -> save.insertAllGeoInfo(geoInfo));

        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.toLog(this.getClass(), e);
        }

        Benchmark.stop(insertDataIntoDatabaseBenchmarkName);
        Benchmark.stop(benchmarkName);
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

        Session session = new Session(0, 0L, 0L, mobKills, deaths, 0);

        session.setPlayerKills(userImportData.getKills());
        session.setWorldTimes(new WorldTimes(userImportData.getWorldTimes()));

        return session;
    }

    private List<GeoInfo> convertGeoInfo(UserImportData userImportData) {
        long date = MiscUtils.getTime();

        return userImportData.getIps().parallelStream()
                .map(ip -> {
                    String geoLoc = GeolocationCache.getCountry(ip);
                    return new GeoInfo(ip, geoLoc, date);
                }).collect(Collectors.toList());
    }

    private interface ImportExecutorHelper {
        void execute() throws DBException;

        default void submit(ExecutorService service) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        execute();
                    } catch (DBException e) {
                        Log.toLog(this.getClass(), e);
                    }
                }
            });
        }
    }
}
