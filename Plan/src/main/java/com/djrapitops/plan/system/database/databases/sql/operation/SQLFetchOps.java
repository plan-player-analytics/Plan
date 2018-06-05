package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.PerServerDataMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;

public class SQLFetchOps extends SQLOps implements FetchOperations {

    public SQLFetchOps(SQLDB db) {
        super(db);
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
            if (userInfo.isOpped()) {
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
    public PlayerProfile getPlayerProfile(UUID uuid) {
        if (!usersTable.isRegistered(uuid)) {
            return null;
        }

        String playerName = usersTable.getPlayerName(uuid);
        Optional<Long> registerDate = usersTable.getRegisterDate(uuid);

        if (!registerDate.isPresent()) {
            throw new IllegalStateException("User has been saved with null register date to a NOT NULL column");
        }

        PlayerProfile profile = new PlayerProfile(uuid, playerName, registerDate.get());
        profile.setTimesKicked(usersTable.getTimesKicked(uuid));

        Map<UUID, UserInfo> userInfo = userInfoTable.getAllUserInfo(uuid);
        addUserInfoToProfile(profile, userInfo);

        profile.setActions(actionsTable.getActions(uuid));
        profile.setNicknames(nicknamesTable.getAllNicknames(uuid));
        profile.setGeoInformation(geoInfoTable.getGeoInfo(uuid));

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(uuid);
        profile.setSessions(sessions);
        profile.calculateWorldTimesPerServer();
        profile.setTotalWorldTimes(worldTimesTable.getWorldTimesOfUser(uuid));

        return profile;
    }

    private void addUserInfoToProfile(PlayerProfile profile, Map<UUID, UserInfo> userInfo) {
        for (Map.Entry<UUID, UserInfo> entry : userInfo.entrySet()) {
            UUID serverUUID = entry.getKey();
            UserInfo info = entry.getValue();

            profile.setRegistered(serverUUID, info.getRegistered());
            if (info.isBanned()) {
                profile.bannedOnServer(serverUUID);
            }
            if (info.isOpped()) {
                profile.oppedOnServer(serverUUID);
            }
        }
    }

    @Override
    public PlayerContainer getPlayerContainer(UUID uuid) {
        PlayerContainer container = new PlayerContainer();
        container.putRawData(PlayerKeys.UUID, uuid);

        container.putAll(usersTable.getUserInformation(uuid));
        container.putSupplier(PlayerKeys.GEO_INFO, () -> geoInfoTable.getGeoInfo(uuid));
        container.putSupplier(PlayerKeys.NICKNAMES, () -> nicknamesTable.getNicknameInformation(uuid));
        container.putSupplier(PlayerKeys.PER_SERVER, () -> getPerServerData(uuid));

        container.putSupplier(PlayerKeys.BANNED,
                () -> new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).isBanned());
        container.putSupplier(PlayerKeys.OPERATOR,
                () -> new PerServerDataMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).isOperator());

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

        container.putSupplier(PlayerKeys.LAST_SEEN, () ->
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS)).toLastSeen());

        container.putSupplier(PlayerKeys.PLAYER_KILLS, () ->
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS)).toPlayerKillList());
        container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, () ->
                container.getUnsafe(PlayerKeys.PLAYER_KILLS).size());
        container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () ->
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS)).toMobKillCount());
        container.putSupplier(PlayerKeys.DEATH_COUNT, () ->
                new SessionsMutator(container.getUnsafe(PlayerKeys.SESSIONS)).toDeathCount());

        return container;
    }

    private PerServerContainer getPerServerData(UUID uuid) {
        PerServerContainer perServerContainer = new PerServerContainer();

        Map<UUID, UserInfo> allUserInfo = userInfoTable.getAllUserInfo(uuid);
        for (Map.Entry<UUID, UserInfo> entry : allUserInfo.entrySet()) {
            UUID serverUUID = entry.getKey();
            UserInfo info = entry.getValue();

            DataContainer perServer = perServerContainer.getOrDefault(serverUUID, new DataContainer());
            perServer.putRawData(PlayerKeys.REGISTERED, info.getRegistered());
            perServer.putRawData(PlayerKeys.BANNED, info.isBanned());
            perServer.putRawData(PlayerKeys.OPERATOR, info.isOpped());
            perServerContainer.put(serverUUID, perServer);
        }

        Map<UUID, List<Session>> sessions = sessionsTable.getSessions(uuid);
        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<Session> serverSessions = entry.getValue();

            DataContainer perServer = perServerContainer.getOrDefault(serverUUID, new DataContainer());
            perServer.putRawData(PerServerKeys.SESSIONS, serverSessions);

            perServer.putSupplier(PerServerKeys.LAST_SEEN, () ->
                    new SessionsMutator(perServer.getUnsafe(PerServerKeys.SESSIONS)).toLastSeen());

            perServer.putSupplier(PerServerKeys.WORLD_TIMES, () ->
                    new SessionsMutator(perServer.getUnsafe(PerServerKeys.SESSIONS)).toTotalWorldTimes());
            perServer.putSupplier(PerServerKeys.PLAYER_KILLS, () ->
                    new SessionsMutator(perServer.getUnsafe(PerServerKeys.SESSIONS)).toPlayerKillList());
            perServer.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, () ->
                    perServer.getUnsafe(PerServerKeys.PLAYER_KILLS).size());
            perServer.putSupplier(PerServerKeys.MOB_KILL_COUNT, () ->
                    new SessionsMutator(perServer.getUnsafe(PerServerKeys.SESSIONS)).toMobKillCount());
            perServer.putSupplier(PerServerKeys.DEATH_COUNT, () ->
                    new SessionsMutator(perServer.getUnsafe(PerServerKeys.SESSIONS)).toDeathCount());

            perServerContainer.put(serverUUID, perServer);
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
