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

/**
 * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
 */
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
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.queries.containers.NetworkContainerQuery}
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
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.queries.containers.ServerContainerQuery}.
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
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.queries.containers.AllPlayerContainersQuery}.
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
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.queries.containers.PlayerContainerQuery}.
     */
    @Deprecated
    PlayerContainer getPlayerContainer(UUID uuid);

    // UUIDs

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Set<UUID> getSavedUUIDs();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Set<UUID> getSavedUUIDs(UUID server);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Map<UUID, String> getServerNames();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Optional<UUID> getServerUUID(String serverName);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    UUID getUuidOf(String playerName);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    WebUser getWebUser(String username);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Optional<String> getServerName(UUID serverUUID);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Optional<Server> getBungeeInformation();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Optional<Integer> getServerID(UUID serverUUID);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    List<TPS> getTPSData(UUID serverUUID);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
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

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Map<UUID, List<GeoInfo>> getAllGeoInfo();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Map<UUID, String> getPlayerNames();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    String getPlayerName(UUID playerUUID);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    List<String> getNicknames(UUID uuid);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Map<UUID, Server> getBukkitServers();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    List<WebUser> getWebUsers();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    List<Server> getServers();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    List<UUID> getServerUUIDs();

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Map<UUID, Integer> getPlayersRegisteredForServers(Collection<Server> servers);

    /**
     * @deprecated Bad API, replaced with {@link com.djrapitops.plan.db.access.Query} objects.
     */
    @Deprecated
    Optional<Config> getNewConfig(long updatedAfter, UUID serverUUID);
}
