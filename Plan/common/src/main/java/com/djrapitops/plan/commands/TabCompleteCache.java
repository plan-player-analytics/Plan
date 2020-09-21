package com.djrapitops.plan.commands;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.file.PlanFiles;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In charge of holding tab completion data for commands, as tab completion is done on server thread.
 *
 * @author Rsl1122
 */
@Singleton
public class TabCompleteCache implements SubSystem {

    private final PlanFiles files;
    private final DBSystem dbSystem;

    private final List<String> playerIdentifiers;
    private final List<String> serverIdentifiers;
    private final List<String> userIdentifiers;
    private final List<String> backupFileNames;

    @Inject
    public TabCompleteCache(
            PlanFiles files,
            DBSystem dbSystem
    ) {
        this.files = files;
        this.dbSystem = dbSystem;
        playerIdentifiers = new ArrayList<>();
        serverIdentifiers = new ArrayList<>();
        userIdentifiers = new ArrayList<>();
        backupFileNames = new ArrayList<>();
    }

    @Override
    public void enable() {
        refreshPlayerIdentifiers();
        refreshServerIdentifiers();
        refreshUserIdentifiers();
        refreshBackupFileNames();

        Collections.sort(playerIdentifiers);
        Collections.sort(serverIdentifiers);
        Collections.sort(userIdentifiers);
        Collections.sort(backupFileNames);
    }

    private void refreshServerIdentifiers() {
        Map<UUID, Server> serverNames = dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformation());
        for (Map.Entry<UUID, Server> server : serverNames.entrySet()) {
            serverIdentifiers.add(server.getKey().toString());
            serverIdentifiers.add(server.getValue().getIdentifiableName());
            serverIdentifiers.add(Integer.toString(server.getValue().getId()));
        }
    }

    private void refreshPlayerIdentifiers() {
        playerIdentifiers.addAll(dbSystem.getDatabase().query(UserIdentifierQueries.fetchAllPlayerNames()).values());
    }

    private void refreshUserIdentifiers() {
        dbSystem.getDatabase().query(WebUserQueries.fetchAllUsers()).stream()
                .map(User::getUsername)
                .forEach(userIdentifiers::add);
    }

    private void refreshBackupFileNames() {
        Arrays.stream(files.getDataFolder().list())
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

    public List<String> getMatchingServerIdentifiers(String searchFor) {
        if (searchFor == null || searchFor.isEmpty()) return serverIdentifiers;
        return serverIdentifiers.stream().filter(identifier -> identifier.startsWith(searchFor)).collect(Collectors.toList());
    }

    public List<String> getMatchingPlayerIdentifiers(String searchFor) {
        if (searchFor == null || searchFor.isEmpty()) return playerIdentifiers;
        return playerIdentifiers.stream().filter(identifier -> identifier.startsWith(searchFor)).collect(Collectors.toList());
    }

    public List<String> getMatchingUserIdentifiers(String searchFor) {
        if (searchFor == null || searchFor.isEmpty()) return userIdentifiers;
        return userIdentifiers.stream().filter(identifier -> identifier.startsWith(searchFor)).collect(Collectors.toList());
    }

    public List<String> getMatchingBackupFilenames(String searchFor) {
        if (searchFor == null || searchFor.isEmpty()) return backupFileNames;
        return backupFileNames.stream().filter(identifier -> identifier.startsWith(searchFor)).collect(Collectors.toList());
    }
}
