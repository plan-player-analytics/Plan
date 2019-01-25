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
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.containers.*;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.PerServerMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.sql.queries.ContainerFetchQueries;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.db.sql.queries.OptionalFetchQueries;
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
        return db.query(ContainerFetchQueries.getServerContainer(serverUUID));
    }

    @Override
    public List<PlayerContainer> getPlayerContainers(UUID serverUUID) {
        List<PlayerContainer> containers = new ArrayList<>();

        List<UserInfo> serverUserInfo = userInfoTable.getServerUserInfo(serverUUID); // TODO Optimize and sort out
        Map<UUID, Integer> timesKicked = usersTable.getAllTimesKicked();  // TODO Optimize and sort out
        Map<UUID, List<GeoInfo>> geoInfo = db.query(LargeFetchQueries.fetchAllGeoInformation()); // TODO Optimize
        Map<UUID, List<Ping>> allPings = db.query(LargeFetchQueries.fetchAllPingData()); // TODO Optimize
        Map<UUID, List<Nickname>> allNicknames = db.query(LargeFetchQueries.fetchAllNicknameDataByPlayerUUIDs()); // TODO Optimize

        Map<UUID, List<Session>> sessions = sessionsTable.getSessionInfoOfServer(serverUUID);
        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        map.put(serverUUID, sessions);
        killsTable.addKillsToSessions(map); // TODO Optimize
        worldTimesTable.addWorldTimesToSessions(map); // TODO Optimize

        Map<UUID, List<UserInfo>> serverUserInfos = Collections.singletonMap(serverUUID, serverUserInfo);
        Map<UUID, Map<UUID, List<Session>>> serverSessions = Collections.singletonMap(serverUUID, sessions);
        Map<UUID, PerServerContainer> perServerInfo = getPerServerData(serverSessions, serverUserInfos, allPings);

        for (UserInfo userInfo : serverUserInfo) {
            PlayerContainer container = new PlayerContainer();
            UUID uuid = userInfo.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);

            container.putRawData(PlayerKeys.REGISTERED, userInfo.getRegistered());
            container.putRawData(PlayerKeys.NAME, userInfo.getName());
            container.putRawData(PlayerKeys.KICK_COUNT, timesKicked.get(uuid));
            container.putRawData(PlayerKeys.GEO_INFO, geoInfo.get(uuid));
            container.putRawData(PlayerKeys.PING, allPings.get(uuid));
            container.putRawData(PlayerKeys.NICKNAMES, allNicknames.get(uuid));
            container.putRawData(PlayerKeys.PER_SERVER, perServerInfo.get(uuid));

            container.putRawData(PlayerKeys.BANNED, userInfo.isBanned());
            container.putRawData(PlayerKeys.OPERATOR, userInfo.isOperator());

            container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                        List<Session> playerSessions = sessions.getOrDefault(uuid, new ArrayList<>());
                        container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            // Calculating getters
            container.putCachingSupplier(PlayerKeys.WORLD_TIMES, () -> {
                WorldTimes worldTimes = new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapWorldTimes();
                container.getValue(PlayerKeys.ACTIVE_SESSION)
                        .ifPresent(session -> worldTimes.add(
                                session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>())))
                        );
                return worldTimes;
            });

            container.putSupplier(PlayerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PlayerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
            container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PlayerKeys.PLAYER_KILLS).size());
            container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PlayerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

            containers.add(container);
        }
        return containers;
    }

    @Override
    public List<PlayerContainer> getAllPlayerContainers() {
        List<PlayerContainer> containers = new ArrayList<>();

        Collection<BaseUser> users = db.query(LargeFetchQueries.fetchAllCommonUserInformation());
        Map<UUID, List<GeoInfo>> geoInfo = db.query(LargeFetchQueries.fetchAllGeoInformation());
        Map<UUID, List<Ping>> allPings = db.query(LargeFetchQueries.fetchAllPingData());
        Map<UUID, List<Nickname>> allNicknames = db.query(LargeFetchQueries.fetchAllNicknameDataByPlayerUUIDs());

        Map<UUID, Map<UUID, List<Session>>> sessions = db.query(LargeFetchQueries.fetchAllSessionsWithoutKillOrWorldData());
        Map<UUID, List<UserInfo>> allUserInfo = db.query(LargeFetchQueries.fetchPerServerUserInformation());
        Map<UUID, PerServerContainer> perServerInfo = getPerServerData(sessions, allUserInfo, allPings);

        for (BaseUser baseUser : users) {
            PlayerContainer container = new PlayerContainer();
            UUID uuid = baseUser.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);

            container.putRawData(PlayerKeys.REGISTERED, baseUser.getRegistered());
            container.putRawData(PlayerKeys.NAME, baseUser.getName());
            container.putRawData(PlayerKeys.KICK_COUNT, baseUser.getTimesKicked());
            container.putRawData(PlayerKeys.GEO_INFO, geoInfo.get(uuid));
            container.putRawData(PlayerKeys.PING, allPings.get(uuid));
            container.putRawData(PlayerKeys.NICKNAMES, allNicknames.get(uuid));
            container.putRawData(PlayerKeys.PER_SERVER, perServerInfo.get(uuid));

            container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                        List<Session> playerSessions = PerServerMutator.forContainer(container).flatMapSessions();
                        container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            // Calculating getters
            container.putSupplier(PlayerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PlayerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

            containers.add(container);
        }
        return containers;
    }

    private Map<UUID, PerServerContainer> getPerServerData(
            Map<UUID, Map<UUID, List<Session>>> sessions,
            Map<UUID, List<UserInfo>> allUserInfo,
            Map<UUID, List<Ping>> allPings
    ) {
        Map<UUID, PerServerContainer> perServerContainers = new HashMap<>();

        for (Map.Entry<UUID, List<UserInfo>> entry : allUserInfo.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<UserInfo> serverUserInfo = entry.getValue();

            for (UserInfo userInfo : serverUserInfo) {
                UUID uuid = userInfo.getUuid();
                if (uuid == null) {
                    continue;
                }
                PerServerContainer perServerContainer = perServerContainers.getOrDefault(uuid, new PerServerContainer());
                DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());
                container.putRawData(PlayerKeys.REGISTERED, userInfo.getRegistered());
                container.putRawData(PlayerKeys.BANNED, userInfo.isBanned());
                container.putRawData(PlayerKeys.OPERATOR, userInfo.isOperator());
                perServerContainer.put(serverUUID, container);
                perServerContainers.put(uuid, perServerContainer);
            }
        }

        for (Map.Entry<UUID, Map<UUID, List<Session>>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            Map<UUID, List<Session>> serverUserSessions = entry.getValue();

            for (Map.Entry<UUID, List<Session>> sessionEntry : serverUserSessions.entrySet()) {
                UUID uuid = sessionEntry.getKey();
                PerServerContainer perServerContainer = perServerContainers.getOrDefault(uuid, new PerServerContainer());
                DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());

                List<Session> serverSessions = sessionEntry.getValue();
                container.putRawData(PerServerKeys.SESSIONS, serverSessions);

                container.putSupplier(PerServerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

                container.putSupplier(PerServerKeys.WORLD_TIMES, () -> SessionsMutator.forContainer(container).toTotalWorldTimes());
                container.putSupplier(PerServerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(container).toPlayerDeathList());
                container.putSupplier(PerServerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
                container.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PerServerKeys.PLAYER_KILLS).size());
                container.putSupplier(PerServerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
                container.putSupplier(PerServerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());
                perServerContainer.put(serverUUID, container);
                perServerContainers.put(uuid, perServerContainer);
            }
        }

        for (Map.Entry<UUID, List<Ping>> entry : allPings.entrySet()) {
            UUID uuid = entry.getKey();
            for (Ping ping : entry.getValue()) {
                UUID serverUUID = ping.getServerUUID();
                PerServerContainer perServerContainer = perServerContainers.getOrDefault(uuid, new PerServerContainer());
                DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());

                if (!container.supports(PerServerKeys.PING)) {
                    container.putRawData(PerServerKeys.PING, new ArrayList<>());
                }
                container.getUnsafe(PerServerKeys.PING).add(ping);

                perServerContainer.put(serverUUID, container);
                perServerContainers.put(uuid, perServerContainer);
            }
        }

        return perServerContainers;
    }

    @Override
    public PlayerContainer getPlayerContainer(UUID uuid) {
        PlayerContainer container = new PlayerContainer();
        container.putRawData(PlayerKeys.UUID, uuid);

        container.putAll(usersTable.getUserInformation(uuid));
        container.putCachingSupplier(PlayerKeys.GEO_INFO, () -> geoInfoTable.getGeoInfo(uuid));
        container.putCachingSupplier(PlayerKeys.PING, () -> pingTable.getPing(uuid));
        container.putCachingSupplier(PlayerKeys.NICKNAMES, () -> nicknamesTable.getNicknameInformation(uuid));
        container.putCachingSupplier(PlayerKeys.PER_SERVER, () -> getPerServerData(uuid));

        container.putSupplier(PlayerKeys.BANNED, () -> new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).isBanned());
        container.putSupplier(PlayerKeys.OPERATOR, () -> new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).isOperator());

        container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                    List<Session> sessions = new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapSessions();
                    container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(sessions::add);
                    return sessions;
                }
        );
        container.putCachingSupplier(PlayerKeys.WORLD_TIMES, () ->
        {
            WorldTimes worldTimes = new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapWorldTimes();
            container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(session -> worldTimes.add(
                    session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>())))
            );
            return worldTimes;
        });

        container.putSupplier(PlayerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

        container.putSupplier(PlayerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
        container.putSupplier(PlayerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(container).toPlayerDeathList());
        container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PlayerKeys.PLAYER_KILLS).size());
        container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
        container.putSupplier(PlayerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

        return container;
    }

    private PerServerContainer getPerServerData(UUID uuid) {
        PerServerContainer perServerContainer = new PerServerContainer();

        Map<UUID, UserInfo> allUserInfo = userInfoTable.getAllUserInfo(uuid);
        for (Map.Entry<UUID, UserInfo> entry : allUserInfo.entrySet()) {
            UUID serverUUID = entry.getKey();
            UserInfo info = entry.getValue();

            DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());
            container.putRawData(PlayerKeys.REGISTERED, info.getRegistered());
            container.putRawData(PlayerKeys.BANNED, info.isBanned());
            container.putRawData(PlayerKeys.OPERATOR, info.isOperator());
            perServerContainer.put(serverUUID, container);
        }

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(uuid);
        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<Session> serverSessions = entry.getValue();

            DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());
            container.putRawData(PerServerKeys.SESSIONS, serverSessions);

            container.putSupplier(PerServerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PerServerKeys.WORLD_TIMES, () -> SessionsMutator.forContainer(container).toTotalWorldTimes());
            container.putSupplier(PerServerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
            container.putSupplier(PerServerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(container).toPlayerDeathList());
            container.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PerServerKeys.PLAYER_KILLS).size());
            container.putSupplier(PerServerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PerServerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

            perServerContainer.put(serverUUID, container);
        }

        return perServerContainer;
    }

    @Override
    public Set<UUID> getSavedUUIDs() {
        return usersTable.getSavedUUIDs();
    }

    @Override
    public Set<UUID> getSavedUUIDs(UUID server) {
        return userInfoTable.getSavedUUIDs(server);
    }

    @Override
    public Map<UUID, String> getServerNames() {
        return serverTable.getServerNames();
    }

    @Override
    public Optional<UUID> getServerUUID(String serverName) {
        return serverTable.getServerUUID(serverName);
    }

    @Override
    public UUID getUuidOf(String playerName) {
        return usersTable.getUuidOf(playerName);
    }

    @Override
    public WebUser getWebUser(String username) {
        return db.query(OptionalFetchQueries.webUser(username)).orElse(null);
    }

    @Override
    public List<TPS> getTPSData(UUID serverUUID) {
        return tpsTable.getTPSData(serverUUID);
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras() {
        return db.query(LargeFetchQueries.fetchAllSessionsWithoutKillOrWorldData());
    }

    @Override
    public Map<UUID, UserInfo> getUsers() {
        return new HashMap<>();
    }

    @Override
    public Map<UUID, Long> getLastSeenForAllPlayers() {
        return sessionsTable.getLastSeenForAllPlayers();
    }

    @Override
    public Map<UUID, List<GeoInfo>> getAllGeoInfo() {
        return db.query(LargeFetchQueries.fetchAllGeoInformation());
    }

    @Override
    public Map<UUID, String> getPlayerNames() {
        return usersTable.getPlayerNames();
    }

    @Override
    public String getPlayerName(UUID playerUUID) {
        return usersTable.getPlayerName(playerUUID);
    }

    @Override
    public Optional<String> getServerName(UUID serverUUID) {
        return serverTable.getServerName(serverUUID);
    }

    @Override
    public List<String> getNicknames(UUID uuid) {
        return nicknamesTable.getNicknames(uuid);
    }

    @Override
    public Optional<Server> getBungeeInformation() {
        return db.query(OptionalFetchQueries.proxyServerInformation());
    }

    @Override
    public Optional<Integer> getServerID(UUID serverUUID) {
        return serverTable.getServerID(serverUUID);
    }

    @Override
    public Map<UUID, Server> getBukkitServers() {
        return db.query(LargeFetchQueries.fetchPlanServerInformation()).entrySet().stream()
                .filter(entry -> entry.getValue().isNotProxy())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<WebUser> getWebUsers() {
        return new ArrayList<>(db.query(LargeFetchQueries.fetchAllPlanWebUsers()));
    }

    @Override
    public List<Server> getServers() {
        List<Server> servers = new ArrayList<>(db.query(LargeFetchQueries.fetchPlanServerInformation()).values());
        Collections.sort(servers);
        return servers;
    }

    @Override
    public List<UUID> getServerUUIDs() {
        return serverTable.getServerUUIDs();
    }

    @Override
    public Map<Integer, List<TPS>> getPlayersOnlineForServers(Collection<Server> servers) {
        return tpsTable.getPlayersOnlineForServers(servers);
    }

    @Override
    public Map<UUID, Integer> getPlayersRegisteredForServers(Collection<Server> servers) {
        return userInfoTable.getPlayersRegisteredForServers(servers);
    }

    @Override
    public Optional<Config> getNewConfig(long updatedAfter, UUID serverUUID) {
        return settingsTable.fetchNewerConfig(updatedAfter, serverUUID);
    }
}
