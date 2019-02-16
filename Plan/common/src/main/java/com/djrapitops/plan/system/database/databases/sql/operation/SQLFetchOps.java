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
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.queries.ServerAggregateQueries;
import com.djrapitops.plan.db.access.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.db.access.queries.objects.*;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.config.Config;

import java.util.*;
import java.util.stream.Collectors;

public class SQLFetchOps extends SQLOps implements FetchOperations {

    public SQLFetchOps(SQLDB db) {
        super(db);
    }

    @Override
    public NetworkContainer getNetworkContainer() {
        return db.query(ContainerFetchQueries.fetchNetworkContainer());
    }

    @Override
    public ServerContainer getServerContainer(UUID serverUUID) {
        return db.query(ContainerFetchQueries.fetchServerContainer(serverUUID));
    }

    @Override
    public List<PlayerContainer> getAllPlayerContainers() {
        return db.query(ContainerFetchQueries.fetchAllPlayerContainers());
    }

    @Override
    public PlayerContainer getPlayerContainer(UUID uuid) {
        return db.query(ContainerFetchQueries.fetchPlayerContainer(uuid));
    }

    @Override
    public Set<UUID> getSavedUUIDs() {
        return db.query(UserIdentifierQueries.fetchAllPlayerUUIDs());
    }

    @Override
    public Set<UUID> getSavedUUIDs(UUID serverUUID) {
        return db.query(UserIdentifierQueries.fetchPlayerUUIDsOfServer(serverUUID));
    }

    @Override
    public Map<UUID, String> getServerNames() {
        return db.query(ServerQueries.fetchServerNames());
    }

    @Override
    public Optional<UUID> getServerUUID(String serverName) {
        return db.query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                .map(Server::getUuid);
    }

    @Override
    public UUID getUuidOf(String playerName) {
        return db.query(UserIdentifierQueries.fetchPlayerUUIDOf(playerName)).orElse(null);
    }

    @Override
    public WebUser getWebUser(String username) {
        return db.query(WebUserQueries.fetchWebUser(username)).orElse(null);
    }

    @Override
    public List<TPS> getTPSData(UUID serverUUID) {
        return tpsTable.getTPSData(serverUUID);
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras() {
        return db.query(SessionQueries.fetchAllSessionsWithoutKillOrWorldData());
    }

    @Override
    public Map<UUID, UserInfo> getUsers() {
        return new HashMap<>();
    }

    @Override
    public Map<UUID, Long> getLastSeenForAllPlayers() {
        return new HashMap<>();
    }

    @Override
    public Map<UUID, List<GeoInfo>> getAllGeoInfo() {
        return db.query(GeoInfoQueries.fetchAllGeoInformation());
    }

    @Override
    public Map<UUID, String> getPlayerNames() {
        return db.query(UserIdentifierQueries.fetchAllPlayerNames());
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return db.query(UserIdentifierQueries.fetchPlayerNameOf(playerUUID)).orElse(null);
    }

    @Override
    public Optional<String> getServerName(UUID serverUUID) {
        return db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)).map(Server::getName);
    }

    @Override
    public List<String> getNicknames(UUID playerUUID) {
        return db.query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID)).stream()
                .map(Nickname::getName).collect(Collectors.toList());
    }

    @Override
    public Optional<Server> getBungeeInformation() {
        return db.query(ServerQueries.fetchProxyServerInformation());
    }

    @Override
    public Optional<Integer> getServerID(UUID serverUUID) {
        return db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)).map(Server::getId);
    }

    @Override
    public Map<UUID, Server> getBukkitServers() {
        return db.query(ServerQueries.fetchPlanServerInformation()).entrySet().stream()
                .filter(entry -> entry.getValue().isNotProxy())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<WebUser> getWebUsers() {
        return new ArrayList<>(db.query(WebUserQueries.fetchAllPlanWebUsers()));
    }

    @Override
    public List<Server> getServers() {
        List<Server> servers = new ArrayList<>(db.query(ServerQueries.fetchPlanServerInformation()).values());
        Collections.sort(servers);
        return servers;
    }

    @Override
    public List<UUID> getServerUUIDs() {
        return new ArrayList<>(db.query(ServerQueries.fetchPlanServerInformation()).keySet());
    }

    @Override
    public Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers) {
        return tpsTable.getPlayersOnlineForServers(servers);
    }

    @Override
    public Map<UUID, Integer> getPlayersRegisteredForServers(Collection<Server> servers) {
        return db.query(ServerAggregateQueries.serverUserCounts());
    }

    @Override
    public Optional<Config> getNewConfig(long updatedAfter, UUID serverUUID) {
        return db.query(new NewerConfigQuery(serverUUID, updatedAfter));
    }
}
