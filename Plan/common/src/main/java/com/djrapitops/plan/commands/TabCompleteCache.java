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
package com.djrapitops.plan.commands;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In charge of holding tab completion data for commands, as tab completion is done on server thread.
 *
 * @author AuroraLS3
 */
@Singleton
public class TabCompleteCache implements SubSystem {

    private final Processing processing;
    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final ServerSensor<?> serverSensor;

    private final Set<String> playerIdentifiers;
    private final Set<String> serverIdentifiers;
    private final Set<String> userIdentifiers;
    private final Set<String> backupFileNames;
    private final Set<String> webGroupIdentifiers;

    @Inject
    public TabCompleteCache(
            Processing processing,
            PlanFiles files,
            DBSystem dbSystem,
            ServerSensor<?> serverSensor
    ) {
        this.processing = processing;
        this.files = files;
        this.dbSystem = dbSystem;
        this.serverSensor = serverSensor;
        playerIdentifiers = new HashSet<>();
        serverIdentifiers = new HashSet<>();
        userIdentifiers = new HashSet<>();
        backupFileNames = new HashSet<>();
        webGroupIdentifiers = new HashSet<>();
    }

    @Override
    public void enable() {
        processing.submitNonCritical(() -> {
            refreshPlayerIdentifiers();
            refreshServerIdentifiers();
            refreshUserIdentifiers();
            refreshBackupFileNames();
            refreshWebGroupIdentifiers();
        });
    }

    private void refreshWebGroupIdentifiers() {
        webGroupIdentifiers.addAll(dbSystem.getDatabase().query(WebUserQueries.fetchGroupNames()));
    }

    private void refreshServerIdentifiers() {
        Map<ServerUUID, Server> serverNames = dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformation());
        for (Map.Entry<ServerUUID, Server> server : serverNames.entrySet()) {
            serverIdentifiers.add(server.getKey().toString());
            serverIdentifiers.add(server.getValue().getIdentifiableName());
            server.getValue().getId().ifPresent(id -> serverIdentifiers.add(Integer.toString(id)));
        }
    }

    private void refreshPlayerIdentifiers() {
        playerIdentifiers.addAll(dbSystem.getDatabase().query(UserIdentifierQueries.fetchAllPlayerNames()).values());
    }

    private void refreshUserIdentifiers() {
        userIdentifiers.addAll(dbSystem.getDatabase().query(WebUserQueries.fetchAllUsernames()));
    }

    private void refreshBackupFileNames() {
        Optional.ofNullable(files.getDataFolder().list()).stream()
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(fileName -> fileName.endsWith(".db")
                        && !fileName.equalsIgnoreCase("database.db"))
                .forEach(backupFileNames::add);
    }

    @Override
    public void disable() {
        playerIdentifiers.clear();
        serverIdentifiers.clear();
        userIdentifiers.clear();
        backupFileNames.clear();
    }

    public List<String> getMatchingServerIdentifiers(@Untrusted String searchFor) {
        return findMatches(serverIdentifiers, searchFor);
    }

    public List<String> getMatchingPlayerIdentifiers(@Untrusted String searchFor) {
        playerIdentifiers.addAll(serverSensor.getOnlinePlayerNames());
        return findMatches(playerIdentifiers, searchFor);
    }

    public List<String> getMatchingUserIdentifiers(@Untrusted String searchFor) {
        return findMatches(userIdentifiers, searchFor);
    }

    public List<String> getMatchingBackupFilenames(@Untrusted String searchFor) {
        return findMatches(backupFileNames, searchFor);
    }

    public List<String> getMatchingWebGroupNames(@Untrusted String searchFor) {
        return findMatches(webGroupIdentifiers, searchFor);
    }

    @NotNull
    List<String> findMatches(Collection<String> searchList, @Untrusted String searchFor) {
        List<String> filtered = searchList.stream()
                .filter(identifier -> searchFor == null || searchFor.isEmpty() || identifier.startsWith(searchFor))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        if (filtered.size() >= 100) {
            return Collections.emptyList();
        }
        return filtered;
    }
}
