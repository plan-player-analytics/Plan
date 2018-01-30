package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.system.info.server.Server;

import java.util.*;

public interface FetchOperations {

    // Profiles

    ServerProfile getServerProfile(UUID serverUUID) throws DBException;

    List<PlayerProfile> getPlayers(UUID serverUUID) throws DBException;

    PlayerProfile getPlayerProfile(UUID uuid) throws DBException;

    // UUIDs

    Set<UUID> getSavedUUIDs() throws DBException;

    Set<UUID> getSavedUUIDs(UUID server) throws DBException;

    Map<UUID, String> getServerNames() throws DBException;

    Optional<UUID> getServerUUID(String serverName) throws DBException;

    UUID getUuidOf(String playerName) throws DBException;

    // WebUsers

    WebUser getWebUser(String username) throws DBException;

    // Servers

    Optional<String> getServerName(UUID serverUUID) throws DBException;

    Optional<Server> getBungeeInformation() throws DBException;

    Optional<Integer> getServerID(UUID serverUUID) throws DBException;

    // Raw Data

    List<TPS> getTPSData(UUID serverUUID) throws DBException;

    List<TPS> getNetworkOnlineData() throws DBException;

    List<Long> getRegisterDates() throws DBException;

    Optional<TPS> getAllTimePeak(UUID serverUUID) throws DBException;

    Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate) throws DBException;

    Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras() throws DBException;

    Map<UUID, Map<UUID, List<Session>>> getSessionsAndExtras() throws DBException;

    Set<String> getWorldNames(UUID serverUuid) throws DBException;

    List<String> getNicknamesOfPlayerOnServer(UUID uuid, UUID serverUUID) throws DBException;

    List<Action> getActions(UUID uuid) throws DBException;

    Map<UUID, UserInfo> getUsers() throws DBException;

    Map<UUID, Long> getLastSeenForAllPlayers() throws DBException;

    Map<UUID, List<GeoInfo>> getAllGeoInfo() throws DBException;

    Map<UUID, String> getPlayerNames() throws DBException;

    String getPlayerName(UUID playerUUID) throws DBException;

    List<String> getNicknames(UUID uuid) throws DBException;

    Map<UUID, Server> getBukkitServers() throws DBException;

    List<WebUser> getWebUsers() throws DBException;

    Map<Integer, String> getServerNamesByID() throws DBException;

    Map<UUID, Map<UUID, List<Session>>> getSessionsInLastMonth() throws DBException;
}
