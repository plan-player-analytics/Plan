/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bukkit.processing.importing;

import com.djrapitops.plan.bukkit.PlanBukkit;
import com.djrapitops.plan.system.processing.importing.UserImportData;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.UUIDFetcher;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A class for refining the {@link UserImportData}s
 * so no {@code null} is left in any field.
 * It also removes invalid data.
 *
 * @author Fuzzlemann
 * @since 4.0.0
 */
public class UserImportRefiner {

    private final PlanBukkit plugin;
    private final boolean onlineMode;

    private final List<UserImportData> importers = new ArrayList<>();

    private final Map<String, Boolean> worlds = new HashMap<>();

    private final Map<UserImportData, String> uuidsMissing = new HashMap<>();
    private final Map<UserImportData, String> namesMissing = new HashMap<>();

    private final Map<UserImportData, String> foundUUIDs = new HashMap<>();
    private final Map<UserImportData, String> foundNames = new HashMap<>();

    public UserImportRefiner(PlanBukkit plugin, List<UserImportData> importers) {
        this.plugin = plugin;
        this.importers.addAll(importers);

        onlineMode = plugin.getServer().getOnlineMode();
    }

    public List<UserImportData> refineData() {
        String benchmarkName = "Refining UserImportData";

        Benchmark.start(benchmarkName);
        processMissingIdentifiers();
        processOldWorlds();
        Benchmark.stop(benchmarkName);

        return importers;
    }

    private void processOldWorlds() {
        String benchmarkName = "Processing old worlds";

        Benchmark.start(benchmarkName);

        importers.parallelStream()
                .flatMap(importer -> importer.getWorldTimes().keySet().stream())
                .forEach(this::checkOldWorld);

        if (!worlds.containsValue(true)) {
            return;
        }

        worlds.values().removeIf(old -> false);

        importers.parallelStream()
                .forEach(importer -> importer.getWorldTimes().keySet().removeAll(worlds.keySet()));

        Benchmark.stop(benchmarkName);
    }

    private void checkOldWorld(String worldName) {
        if (worlds.containsKey(worldName)) {
            return;
        }

        World world = plugin.getServer().getWorld(worldName);
        boolean old = world == null;

        worlds.put(worldName, old);
    }

    private void processMissingIdentifiers() {
        String benchmarkName = "Processing missing identifiers";

        Benchmark.start(benchmarkName);

        List<UserImportData> invalidData = new ArrayList<>();

        importers.parallelStream().forEach(importer -> {
            String name = importer.getName();
            UUID uuid = importer.getUuid();

            boolean nameNull = name == null;
            boolean uuidNull = uuid == null;

            if (nameNull && uuidNull) {
                invalidData.add(importer);
            } else if (nameNull) {
                namesMissing.put(importer, uuid.toString());
            } else if (uuidNull) {
                uuidsMissing.put(importer, name);
            }
        });

        importers.removeAll(invalidData);

        processMissingUUIDs();
        processMissingNames();

        Benchmark.stop(benchmarkName);
    }

    private void processMissingUUIDs() {
        String benchmarkName = "Processing missing UUIDs";

        Benchmark.start(benchmarkName);

        if (onlineMode) {
            addMissingUUIDsOverFetcher();
            addMissingUUIDsOverOfflinePlayer();
        } else {
            addMissingUUIDsOverOfflinePlayer();
            addMissingUUIDsOverFetcher();
        }

        foundUUIDs.entrySet().parallelStream()
                .forEach(entry -> {
                    UserImportData userImportData = entry.getKey();
                    UUID uuid = UUID.fromString(entry.getValue());

                    userImportData.setUuid(uuid);
                });

        importers.removeAll(uuidsMissing.keySet());

        Benchmark.stop(benchmarkName);
    }

    private void addMissingUUIDsOverFetcher() {
        UUIDFetcher uuidFetcher = new UUIDFetcher(new ArrayList<>(uuidsMissing.values()));

        Map<String, String> result;

        try {
            result = uuidFetcher.call().entrySet().parallelStream()
                    .collect(Collectors.toMap(entry -> entry.getValue().toString(), Map.Entry::getKey));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
            return;
        }

        addFoundUUIDs(result);
    }

    private void addMissingUUIDsOverOfflinePlayer() {
        Map<String, String> result = new HashMap<>();

        for (String name : uuidsMissing.values()) {
            String uuid = getUuidByOfflinePlayer(name);

            if (uuid == null) {
                continue;
            }

            result.put(name, uuid);
        }

        addFoundUUIDs(result);
    }

    private void addFoundUUIDs(Map<String, String> foundUUIDs) {
        List<UserImportData> found = new ArrayList<>();

        uuidsMissing.entrySet().parallelStream().forEach((entry) -> {
            UserImportData importer = entry.getKey();
            String name = entry.getValue();

            String uuid = foundUUIDs.get(name);

            this.foundUUIDs.put(importer, uuid);
            found.add(importer);
        });

        uuidsMissing.keySet().removeAll(found);
    }

    @SuppressWarnings("deprecation")
    private String getUuidByOfflinePlayer(String name) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);

        if (!player.hasPlayedBefore()) {
            return null;
        }

        return player.getUniqueId().toString();
    }

    private void processMissingNames() {
        String benchmarkNames = "Processing missing names";

        Benchmark.start(benchmarkNames);

        addMissingNames();

        foundNames.entrySet().parallelStream().forEach(entry -> entry.getKey().setName(entry.getValue()));

        importers.removeAll(namesMissing.keySet());

        Benchmark.stop(benchmarkNames);
    }

    private void addMissingNames() {
        Map<String, String> result = new HashMap<>();

        namesMissing.values().parallelStream().forEach(uuid -> {
            String name = getNameByOfflinePlayer(uuid);

            result.put(uuid, name);
        });

        addFoundNames(result);
    }

    private void addFoundNames(Map<String, String> foundNames) {
        List<UserImportData> found = new ArrayList<>();

        namesMissing.entrySet().parallelStream().forEach(entry -> {
            UserImportData importer = entry.getKey();
            String uuid = entry.getValue();

            String name = foundNames.get(uuid);

            this.foundNames.put(importer, name);
            found.add(importer);
        });

        namesMissing.keySet().removeAll(found);
    }

    private String getNameByOfflinePlayer(String uuid) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));

        if (!player.hasPlayedBefore()) {
            return null;
        }

        return player.getName();
    }
}
