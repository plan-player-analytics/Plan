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
package com.djrapitops.plan.gathering.importing.data;

import net.playeranalytics.plugin.player.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UserImportRefiner attempts to find any crucial information that is missing.
 * <p>
 * - Finds UUIDs if only name is present.
 * - Finds Names if only UUID is present.
 * - Removes any importers that do not have any identifiers.
 *
 * @author Fuzzlemann
 */
public class BukkitUserImportRefiner {

    private final boolean onlineMode;

    private final List<UserImportData> importers = new ArrayList<>();

    private final Map<UserImportData, String> missingUUIDs = new HashMap<>();
    private final Map<UserImportData, String> missingNames = new HashMap<>();

    private final Map<UserImportData, String> foundUUIDs = new HashMap<>();
    private final Map<UserImportData, String> foundNames = new HashMap<>();

    public BukkitUserImportRefiner(List<UserImportData> importers) {
        this.importers.addAll(importers);

        onlineMode = Bukkit.getOnlineMode();
    }

    public List<UserImportData> refineData() {
        processMissingIdentifiers();
        return importers;
    }

    private void processMissingIdentifiers() {
        List<UserImportData> invalidData = new ArrayList<>();

        importers.parallelStream().forEach(importer -> {
            String name = importer.getName();
            UUID uuid = importer.getUuid();

            boolean nameNull = name == null;
            boolean uuidNull = uuid == null;

            if (nameNull && uuidNull) {
                invalidData.add(importer);
            } else if (nameNull) {
                missingNames.put(importer, uuid.toString());
            } else if (uuidNull) {
                missingUUIDs.put(importer, name);
            }
        });

        importers.removeAll(invalidData);

        processMissingUUIDs();
        processMissingNames();
    }

    private void processMissingUUIDs() {
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

        importers.removeAll(missingUUIDs.keySet());
    }

    private void addMissingUUIDsOverFetcher() {
        UUIDFetcher uuidFetcher = new UUIDFetcher(new ArrayList<>(missingUUIDs.values()));

        Map<String, String> result;

        try {
            result = uuidFetcher.call().entrySet().parallelStream()
                    .collect(Collectors.toMap(entry -> entry.getValue().toString(), Map.Entry::getKey));
        } catch (Exception e) {
            return;
        }

        addFoundUUIDs(result);
    }

    private void addMissingUUIDsOverOfflinePlayer() {
        Map<String, String> result = new HashMap<>();

        for (String name : missingUUIDs.values()) {
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

        missingUUIDs.entrySet().parallelStream().forEach(entry -> {
            UserImportData importer = entry.getKey();
            String name = entry.getValue();

            String uuid = foundUUIDs.get(name);

            this.foundUUIDs.put(importer, uuid);
            found.add(importer);
        });

        missingUUIDs.keySet().removeAll(found);
    }

    @SuppressWarnings("deprecation")
    private String getUuidByOfflinePlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (!player.hasPlayedBefore()) {
            return null;
        }

        return player.getUniqueId().toString();
    }

    private void processMissingNames() {
        findMissingNames();

        foundNames.entrySet().parallelStream().forEach(entry -> entry.getKey().setName(entry.getValue()));

        importers.removeAll(missingNames.keySet());
    }

    private void findMissingNames() {
        Map<String, String> result = new HashMap<>();

        missingNames.values().parallelStream().forEach(uuid -> {
            String name = getNameByOfflinePlayer(uuid);

            result.put(uuid, name);
        });

        addFoundNames(result);
    }

    private void addFoundNames(Map<String, String> foundNames) {
        List<UserImportData> found = new ArrayList<>();

        missingNames.entrySet().parallelStream().forEach(entry -> {
            UserImportData importer = entry.getKey();
            String uuid = entry.getValue();

            String name = foundNames.get(uuid);

            this.foundNames.put(importer, name);
            found.add(importer);
        });

        missingNames.keySet().removeAll(found);
    }

    private String getNameByOfflinePlayer(String uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

        if (!player.hasPlayedBefore()) {
            return null;
        }

        return player.getName();
    }
}
