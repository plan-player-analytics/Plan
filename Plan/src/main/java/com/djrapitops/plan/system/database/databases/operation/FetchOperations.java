package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public interface FetchOperations {

    ServerProfile getServerProfile(UUID serverUUID) throws SQLException;

    PlayerProfile getPlayerProfile(UUID uuid) throws SQLException;

    Set<UUID> getSavedUUIDs() throws SQLException;

    Set<UUID> getSavedUUIDs(UUID server) throws SQLException;
}
