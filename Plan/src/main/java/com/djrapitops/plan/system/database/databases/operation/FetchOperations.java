package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;

import java.util.*;

public interface FetchOperations {

    ServerProfile getServerProfile(UUID serverUUID) throws DBException;

    List<PlayerProfile> getPlayers(UUID serverUUID) throws DBException;

    PlayerProfile getPlayerProfile(UUID uuid) throws DBException;

    Set<UUID> getSavedUUIDs() throws DBException;

    Set<UUID> getSavedUUIDs(UUID server) throws DBException;

    Map<UUID, String> getServerNames() throws DBException;

    Optional<UUID> getServerUUID(String serverName) throws DBException;
}
