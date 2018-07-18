package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.system.info.server.Server;

import java.util.*;

public interface FetchOperations {

    /**
     * Used to get a NetworkContainer, some limitations apply to values returned by DataContainer keys.
     * <p>
     * Limitations:
     * - Bungee ServerContainer does not support: ServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_DEATHS, PLAYER_KILL_COUNT
     * - Bungee ServerContainer ServerKeys.TPS only contains playersOnline values
     * - NetworkKeys.PLAYERS PlayerContainers:
     * - do not support: PlayerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_DEATHS, PLAYER_KILL_COUNT
     * - PlayerKeys.PER_SERVER does not support: PerServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_DEATHS, PLAYER_KILL_COUNT
     * <p>
     * Blocking methods are not called until DataContainer getter methods are called.
     *
     * @return a new NetworkContainer.
     */
    NetworkContainer getNetworkContainer();

    /**
     * Used to get a ServerContainer, some limitations apply to values returned by DataContainer keys.
     * <p>
     * Limitations:
     * - ServerKeys.PLAYERS PlayerContainers PlayerKeys.PER_SERVER only contains information about the queried server.
     * <p>
     * Blocking methods are not called until DataContainer getter methods are called.
     *
     * @param serverUUID UUID of the Server.
     * @return a new ServerContainer.
     */
    ServerContainer getServerContainer(UUID serverUUID);

    /**
     * Used to get PlayerContainers of all players on the network, some limitations apply to DataContainer keys.
     * <p>
     * Limitations:
     * - PlayerContainers do not support: PlayerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_KILL_COUNT
     * - PlayerContainers PlayerKeys.PER_SERVER does not support: PerServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_KILL_COUNT
     * <p>
     * Blocking methods are not called until DataContainer getter methods are called.
     *
     * @return a list of PlayerContainers in Plan database.
     */
    List<PlayerContainer> getAllPlayerContainers();

    /**
     * Used to get a PlayerContainer of a specific player.
     * <p>
     * Blocking methods are not called until DataContainer getter methods are called.
     *
     * @param uuid UUID of the player.
     * @return a new PlayerContainer.
     */
    PlayerContainer getPlayerContainer(UUID uuid);

    // UUIDs

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

    Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras();

    Map<UUID, UserInfo> getUsers();

    Map<UUID, Long> getLastSeenForAllPlayers();

    Map<UUID, List<GeoInfo>> getAllGeoInfo();

    Map<UUID, String> getPlayerNames();

    String getPlayerName(UUID playerUUID);

    List<String> getNicknames(UUID uuid);

    Map<UUID, Server> getBukkitServers();

    List<WebUser> getWebUsers();

    List<Server> getServers();

    List<UUID> getServerUUIDs();

}
