package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.system.info.server.Server;

import java.util.*;

public interface FetchOperations {

    // Profiles

    ServerContainer getServerContainer(UUID serverUUID);

    // UUIDs

    PlayerContainer getPlayerContainer(UUID uuid);

    Set<UUID> getSavedUUIDs();

    Set<UUID> getSavedUUIDs(UUID server);

    Map<UUID, String> getServerNames();

    Optional<UUID> getServerUUID(String serverName);

    UUID getUuidOf(String playerName);

    // WebUsers

    WebUser getWebUser(String username);

    // Servers

    Optional<String> getServerName(UUID serverUUID);

    Optional<Server> getBungeeInformation();

    Optional<Integer> getServerID(UUID serverUUID);

    // Raw Data

    List<TPS> getTPSData(UUID serverUUID);

    List<TPS> getNetworkOnlineData();

    List<Long> getRegisterDates();

    Optional<TPS> getAllTimePeak(UUID serverUUID);

    Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate);

    Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras();

    Map<UUID, Map<UUID, List<Session>>> getSessionsAndExtras();

    Set<String> getWorldNames(UUID serverUuid);

    List<String> getNicknamesOfPlayerOnServer(UUID uuid, UUID serverUUID);

    Map<UUID, UserInfo> getUsers();

    Map<UUID, Long> getLastSeenForAllPlayers();

    Map<UUID, List<GeoInfo>> getAllGeoInfo();

    Map<UUID, String> getPlayerNames();

    String getPlayerName(UUID playerUUID);

    List<String> getNicknames(UUID uuid);

    Map<UUID, Server> getBukkitServers();

    List<WebUser> getWebUsers();

    Map<Integer, String> getServerNamesByID();

    Map<UUID, Map<UUID, List<Session>>> getSessionsInLastMonth();

    List<Server> getServers();

    List<UUID> getServerUUIDs();

    List<String> getNetworkGeolocations();
}
