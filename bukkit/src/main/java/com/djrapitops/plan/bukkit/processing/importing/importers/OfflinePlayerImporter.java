/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bukkit.processing.importing.importers;

import com.djrapitops.plan.bukkit.PlanBukkit;
import com.djrapitops.plan.bukkit.processing.importing.UserImportRefiner;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.importing.ServerImportData;
import com.djrapitops.plan.system.processing.importing.UserImportData;
import com.djrapitops.plan.system.processing.importing.importers.Importer;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public class OfflinePlayerImporter extends Importer {

    @Override
    public List<String> getNames() {
        return Arrays.asList("offline", "offlineplayer");
    }

    @Override
    public ServerImportData getServerImportData() {
        return null;
    }

    @Override
    public List<UserImportData> getUserImportData() {
        List<UserImportData> dataList = new ArrayList<>();

        Set<OfflinePlayer> operators = Bukkit.getOperators();
        Set<OfflinePlayer> banned = Bukkit.getBannedPlayers();

        Arrays.stream(Bukkit.getOfflinePlayers()).parallel().forEach(player -> {
            UserImportData.UserImportDataBuilder builder = UserImportData.builder();
            builder.name(player.getName())
                    .uuid(player.getUniqueId())
                    .registered(player.getFirstPlayed());

            if (operators.contains(player)) {
                builder.op();
            }

            if (banned.contains(player)) {
                builder.banned();
            }

            dataList.add(builder.build());
        });

        return dataList;
    }

    @Override
    public void processUserData() {
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

        UserImportRefiner userImportRefiner = new UserImportRefiner(PlanBukkit.getInstance(), userImportData);
        userImportData = userImportRefiner.refineData();

        UUID serverUUID = ServerInfo.getServerUUID();
        Database db = Database.getActive();

        Set<UUID> existingUUIDs = db.fetch().getSavedUUIDs();
        Set<UUID> existingUserInfoTableUUIDs = db.fetch().getSavedUUIDs(serverUUID);

        Benchmark.start(insertDataIntoCollectionsBenchmarkName);

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
}
