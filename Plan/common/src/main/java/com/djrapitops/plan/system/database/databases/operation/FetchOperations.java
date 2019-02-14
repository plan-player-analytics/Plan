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
import com.djrapitops.plan.system.settings.config.Config;

import java.util.*;

@Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    List<PlayerContainer> getAllPlayerContainers();

    /**
     * Used to get a PlayerContainer of a specific player.
     * <p>
     * Blocking methods are not called until DataContainer getter methods are called.
     *
     * @param uuid UUID of the player.
     * @return a new PlayerContainer.
     */
    @Deprecated
    PlayerContainer getPlayerContainer(UUID uuid);

    // UUIDs

    @Deprecated
    Set<UUID> getSavedUUIDs();

    @Deprecated
    Set<UUID> getSavedUUIDs(UUID server);

    @Deprecated
    Map<UUID, String> getServerNames();

    @Deprecated
    Optional<UUID> getServerUUID(String serverName);

    @Deprecated
    UUID getUuidOf(String playerName);

    // WebUsers
    @Deprecated
    WebUser getWebUser(String username);

    // Servers
    @Deprecated
    Optional<String> getServerName(UUID serverUUID);

    @Deprecated
    Optional<Server> getBungeeInformation();

    @Deprecated
    Optional<Integer> getServerID(UUID serverUUID);

    // Raw Data
    @Deprecated
    List<TPS> getTPSData(UUID serverUUID);

    @Deprecated
    Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras();

    /**
     * @deprecated It was not possible to keep this compatible so now empty map is returned.
     */
    @Deprecated
    Map<UUID, UserInfo> getUsers();

    /**
     * @deprecated Now empty map is returned.
     */
    @Deprecated
    Map<UUID, Long> getLastSeenForAllPlayers();

    @Deprecated
    Map<UUID, List<GeoInfo>> getAllGeoInfo();

    @Deprecated
    Map<UUID, String> getPlayerNames();

    @Deprecated
    String getPlayerName(UUID playerUUID);

    @Deprecated
    List<String> getNicknames(UUID uuid);

    @Deprecated
    Map<UUID, Server> getBukkitServers();

    @Deprecated
    List<WebUser> getWebUsers();

    @Deprecated
    List<Server> getServers();

    @Deprecated
    List<UUID> getServerUUIDs();

    @Deprecated
    Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers);

    @Deprecated
    Map<UUID, Integer> getPlayersRegisteredForServers(Collection<Server> servers);

    @Deprecated
    Optional<Config> getNewConfig(long updatedAfter, UUID serverUUID);
}
