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

    Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers);

    Map<Integer, Integer> getPlayersRegisteredForServers(Collection<Server> servers);
}
