package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.PerServerDataMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;
import java.util.stream.Collectors;

public class SQLFetchOps extends SQLOps implements FetchOperations {

    public SQLFetchOps(SQLDB db) {
        super(db);
    }

    public ServerContainer getServerContainer(UUID serverUUID) {
        ServerContainer container = new ServerContainer();

        container.putSupplier(ServerKeys.PLAYERS, () -> getPlayerContainers(serverUUID));
        container.putSupplier(ServerKeys.PLAYER_COUNT, container.getUnsafe(ServerKeys.PLAYERS)::size);

        container.putSupplier(ServerKeys.TPS, () -> tpsTable.getTPSData(serverUUID));
        container.putSupplier(ServerKeys.ALL_TIME_PEAK_PLAYERS, () -> {
            Optional<TPS> allTimePeak = tpsTable.getAllTimePeak(serverUUID);
            if (allTimePeak.isPresent()) {
                TPS peak = allTimePeak.get();
                return new DateObj<>(peak.getDate(), peak.getPlayers());
            }
            return null;
        });
        container.putSupplier(ServerKeys.RECENT_PEAK_PLAYERS, () -> {
            long twoDaysAgo = System.currentTimeMillis() - (TimeAmount.DAY.ms() * 2L);
            Optional<TPS> lastPeak = tpsTable.getPeakPlayerCount(serverUUID, twoDaysAgo);
            if (lastPeak.isPresent()) {
                TPS peak = lastPeak.get();
                return new DateObj<>(peak.getDate(), peak.getPlayers());
            }
            return null;
        });

        container.putSupplier(ServerKeys.COMMAND_USAGE, () -> commandUseTable.getCommandUse(serverUUID));
        container.putSupplier(ServerKeys.WORLD_TIMES, () -> worldTimesTable.getWorldTimesOfServer(serverUUID));

        // Calculating getters
        container.putSupplier(ServerKeys.OPERATORS, () -> container.getUnsafe(ServerKeys.PLAYERS).stream()
                .filter(player -> player.getValue(PlayerKeys.OPERATOR).orElse(false)).collect(Collectors.toList()));
        container.putSupplier(ServerKeys.PLAYER_KILLS,
                new SessionsMutator(container.getUnsafe(ServerKeys.SESSIONS))::toPlayerKillList);
        container.putSupplier(ServerKeys.PLAYER_KILL_COUNT, container.getUnsafe(ServerKeys.PLAYER_KILLS)::size);
        container.putSupplier(ServerKeys.MOB_KILL_COUNT,
                new SessionsMutator(container.getUnsafe(ServerKeys.SESSIONS))::toMobKillCount);
        container.putSupplier(ServerKeys.DEATH_COUNT,
                new SessionsMutator(container.getUnsafe(ServerKeys.SESSIONS))::toDeathCount);

        return container;
    }

    private List<PlayerContainer> getPlayerContainers(UUID serverUUID) {
        List<PlayerContainer> containers = new ArrayList<>();

        List<UserInfo> serverUserInfo = userInfoTable.getServerUserInfo(serverUUID);
        Map<UUID, Integer> timesKicked = usersTable.getAllTimesKicked();
        Map<UUID, List<GeoInfo>> geoInfo = geoInfoTable.getAllGeoInfo();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessionInfoOfServer(serverUUID);
        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        map.put(serverUUID, sessions);
        killsTable.addKillsToSessions(map);
        worldTimesTable.addWorldTimesToSessions(map);

        for (UserInfo userInfo : serverUserInfo) {
            PlayerContainer container = new PlayerContainer();
            UUID uuid = userInfo.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);

            container.putRawData(PlayerKeys.REGISTERED, userInfo.getRegistered());
            container.putRawData(PlayerKeys.NAME, userInfo.getName());
            container.putRawData(PlayerKeys.KICK_COUNT, timesKicked.get(uuid));
            container.putSupplier(PlayerKeys.GEO_INFO, () -> geoInfo.get(uuid));
            container.putSupplier(PlayerKeys.NICKNAMES, () -> nicknamesTable.getNicknameInformation(uuid));
            container.putSupplier(PlayerKeys.PER_SERVER, () -> getPerServerData(uuid));

            container.putRawData(PlayerKeys.BANNED, userInfo.isBanned());
            container.putRawData(PlayerKeys.OPERATOR, userInfo.isOperator());

            container.putSupplier(PlayerKeys.SESSIONS, () -> {
                        List<Session> playerSessions = sessions.getOrDefault(uuid, new ArrayList<>());
                        container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            // Calculating getters
            container.putSupplier(PlayerKeys.WORLD_TIMES, () -> {
                WorldTimes worldTimes = new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapWorldTimes();
                container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(session -> worldTimes.add(session.getWorldTimes()));
                return worldTimes;
            });

            container.putSupplier(PlayerKeys.LAST_SEEN,
                    new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toLastSeen);

            container.putSupplier(PlayerKeys.PLAYER_KILLS,
                    new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toPlayerKillList);
            container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, container.getUnsafe(PlayerKeys.PLAYER_KILLS)::size);
            container.putSupplier(PlayerKeys.MOB_KILL_COUNT,
                    new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toMobKillCount);
            container.putSupplier(PlayerKeys.DEATH_COUNT,
                    new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toDeathCount);
        }

        return containers;
    }

    @Override
    public ServerProfile getServerProfile(UUID serverUUID) {
        ServerProfile profile = new ServerProfile(serverUUID);

        profile.setPlayers(getPlayers(serverUUID));
        profile.setTps(tpsTable.getTPSData(serverUUID));
        Optional<TPS> allTimePeak = tpsTable.getAllTimePeak(serverUUID);
        allTimePeak.ifPresent(peak -> {
            profile.setAllTimePeak(peak.getDate());
            profile.setAllTimePeakPlayers(peak.getPlayers());
        });
        Optional<TPS> lastPeak = tpsTable.getPeakPlayerCount(serverUUID, System.currentTimeMillis() - (TimeAmount.DAY.ms() * 2L));
        lastPeak.ifPresent(peak -> {
            profile.setLastPeakDate(peak.getDate());
            profile.setLastPeakPlayers(peak.getPlayers());
        });

        profile.setCommandUsage(commandUseTable.getCommandUse(serverUUID));
        profile.setServerWorldtimes(worldTimesTable.getWorldTimesOfServer(serverUUID));

        return profile;
    }

    @Override
    public List<PlayerProfile> getPlayers(UUID serverUUID) {
        List<UserInfo> serverUserInfo = userInfoTable.getServerUserInfo(serverUUID);
        Map<UUID, Integer> timesKicked = usersTable.getAllTimesKicked();
        Map<UUID, List<Action>> actions = actionsTable.getServerActions(serverUUID);
        Map<UUID, List<GeoInfo>> geoInfo = geoInfoTable.getAllGeoInfo();

        Map<UUID, List<Session>> sessions = sessionsTable.getSessionInfoOfServer(serverUUID);
        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        map.put(serverUUID, sessions);
        killsTable.addKillsToSessions(map);
        worldTimesTable.addWorldTimesToSessions(map);

        List<PlayerProfile> players = new ArrayList<>();

        for (UserInfo userInfo : serverUserInfo) {
            UUID uuid = userInfo.getUuid();
            PlayerProfile profile = new PlayerProfile(uuid, userInfo.getName(), userInfo.getRegistered());
            profile.setTimesKicked(timesKicked.getOrDefault(uuid, 0));
            if (userInfo.isBanned()) {
                profile.bannedOnServer(serverUUID);
            }
            if (userInfo.isOperator()) {
                profile.oppedOnServer(serverUUID);
            }
            profile.setActions(actions.getOrDefault(uuid, new ArrayList<>()));
            profile.setGeoInformation(geoInfo.getOrDefault(uuid, new ArrayList<>()));
            profile.setSessions(serverUUID, sessions.getOrDefault(uuid, new ArrayList<>()));

            players.add(profile);
        }
        return players;
    }

    @Override
    public PlayerContainer getPlayerContainer(UUID uuid) {
        PlayerContainer container = new PlayerContainer();
        container.putRawData(PlayerKeys.UUID, uuid);

        container.putAll(usersTable.getUserInformation(uuid));
        container.putSupplier(PlayerKeys.GEO_INFO, () -> geoInfoTable.getGeoInfo(uuid));
        container.putSupplier(PlayerKeys.NICKNAMES, () -> nicknamesTable.getNicknameInformation(uuid));
        container.putSupplier(PlayerKeys.PER_SERVER, () -> getPerServerData(uuid));

        container.putSupplier(PlayerKeys.BANNED, new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER))::isBanned);
        container.putSupplier(PlayerKeys.OPERATOR, new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER))::isOperator);

        container.putSupplier(PlayerKeys.SESSIONS, () -> {
                    List<Session> sessions = new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapSessions();
                    container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(sessions::add);
                    return sessions;
                }
        );
        container.putSupplier(PlayerKeys.WORLD_TIMES, () ->
        {
            WorldTimes worldTimes = new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapWorldTimes();
            container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(session -> worldTimes.add(session.getWorldTimes()));
            return worldTimes;
        });

        container.putSupplier(PlayerKeys.LAST_SEEN,
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toLastSeen);

        container.putSupplier(PlayerKeys.PLAYER_KILLS,
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toPlayerKillList);
        container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, container.getUnsafe(PlayerKeys.PLAYER_KILLS)::size);
        container.putSupplier(PlayerKeys.MOB_KILL_COUNT,
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toMobKillCount);
        container.putSupplier(PlayerKeys.DEATH_COUNT,
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS))::toDeathCount);

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

            container.putSupplier(PerServerKeys.LAST_SEEN,
                    new SessionsMutator(container.getUnsafe(PerServerKeys.SESSIONS))::toLastSeen);

            container.putSupplier(PerServerKeys.WORLD_TIMES,
                    new SessionsMutator(container.getUnsafe(PerServerKeys.SESSIONS))::toTotalWorldTimes);
            container.putSupplier(PerServerKeys.PLAYER_KILLS,
                    new SessionsMutator(container.getUnsafe(PerServerKeys.SESSIONS))::toPlayerKillList);
            container.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, container.getUnsafe(PerServerKeys.PLAYER_KILLS)::size);
            container.putSupplier(PerServerKeys.MOB_KILL_COUNT,
                    new SessionsMutator(container.getUnsafe(PerServerKeys.SESSIONS))::toMobKillCount);
            container.putSupplier(PerServerKeys.DEATH_COUNT,
                    new SessionsMutator(container.getUnsafe(PerServerKeys.SESSIONS))::toDeathCount);

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
        return securityTable.getWebUser(username);
    }

    @Override
    public List<TPS> getTPSData(UUID serverUUID) {
        return tpsTable.getTPSData(serverUUID);
    }

    @Override
    public List<TPS> getNetworkOnlineData() {
        return tpsTable.getNetworkOnlineData();
    }

    @Override
    public List<Long> getRegisterDates() {
        return usersTable.getRegisterDates();
    }

    @Override
    public Optional<TPS> getAllTimePeak(UUID serverUUID) {
        return tpsTable.getAllTimePeak(serverUUID);
    }

    @Override
    public Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate) {
        return tpsTable.getPeakPlayerCount(serverUUID, afterDate);
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras() {
        return sessionsTable.getAllSessions(false);
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsAndExtras() {
        return sessionsTable.getAllSessions(true);
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsInLastMonth() {
        return sessionsTable.getSessionInLastMonth();
    }

    @Override
    public Set<String> getWorldNames(UUID serverUuid) {
        return worldTable.getWorldNames(serverUuid);
    }

    @Override
    public List<String> getNicknamesOfPlayerOnServer(UUID uuid, UUID serverUUID) {
        return nicknamesTable.getNicknames(uuid, serverUUID);
    }

    @Override
    public List<Action> getActions(UUID uuid) {
        return actionsTable.getActions(uuid);
    }

    @Override
    public Map<UUID, UserInfo> getUsers() {
        return usersTable.getUsers();
    }

    @Override
    public Map<UUID, Long> getLastSeenForAllPlayers() {
        return sessionsTable.getLastSeenForAllPlayers();
    }

    @Override
    public Map<UUID, List<GeoInfo>> getAllGeoInfo() {
        return geoInfoTable.getAllGeoInfo();
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
        return serverTable.getBungeeInfo();
    }

    @Override
    public Optional<Integer> getServerID(UUID serverUUID) {
        return serverTable.getServerID(serverUUID);
    }

    @Override
    public Map<UUID, Server> getBukkitServers() {
        return serverTable.getBukkitServers();
    }

    @Override
    public List<WebUser> getWebUsers() {
        return securityTable.getUsers();
    }

    @Override
    public Map<Integer, String> getServerNamesByID() {
        return serverTable.getServerNamesByID();
    }

    @Override
    public List<Server> getServers() {
        Map<UUID, Server> bukkitServers = getBukkitServers();
        Optional<Server> bungeeInformation = getBungeeInformation();

        List<Server> servers = new ArrayList<>(bukkitServers.values());
        bungeeInformation.ifPresent(servers::add);

        Collections.sort(servers);
        return servers;
    }

    @Override
    public List<UUID> getServerUUIDs() {
        return serverTable.getServerUUIDs();
    }

    @Override
    public List<String> getNetworkGeolocations() {
        return geoInfoTable.getNetworkGeolocations();
    }
}
