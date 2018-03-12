package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.SQLException;
import java.util.*;

public class SQLFetchOps extends SQLOps implements FetchOperations {

    public SQLFetchOps(SQLDB db) {
        super(db);
    }

    @Override
    public ServerProfile getServerProfile(UUID serverUUID) throws DBException {
        try {
            ServerProfile profile = new ServerProfile(serverUUID);

            profile.setPlayers(getPlayers(serverUUID));
            profile.setTps(tpsTable.getTPSData(serverUUID));
            Optional<TPS> allTimePeak = tpsTable.getAllTimePeak(serverUUID);
            allTimePeak.ifPresent(peak -> {
                profile.setAllTimePeak(peak.getDate());
                profile.setAllTimePeakPlayers(peak.getPlayers());
            });
            Optional<TPS> lastPeak = tpsTable.getPeakPlayerCount(serverUUID, MiscUtils.getTime() - (TimeAmount.DAY.ms() * 2L));
            lastPeak.ifPresent(peak -> {
                profile.setLastPeakDate(peak.getDate());
                profile.setLastPeakPlayers(peak.getPlayers());
            });

            profile.setCommandUsage(commandUseTable.getCommandUse(serverUUID));
            profile.setServerWorldtimes(worldTimesTable.getWorldTimesOfServer(serverUUID));

            return profile;
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<PlayerProfile> getPlayers(UUID serverUUID) throws DBException {
        try {
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
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public PlayerProfile getPlayerProfile(UUID uuid) throws DBException {
        try {
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
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
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
    public Set<UUID> getSavedUUIDs() throws DBException {
        try {
            return usersTable.getSavedUUIDs();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Set<UUID> getSavedUUIDs(UUID server) throws DBException {
        try {
            return userInfoTable.getSavedUUIDs(server);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, String> getServerNames() throws DBException {
        try {
            return serverTable.getServerNames();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<UUID> getServerUUID(String serverName) throws DBException {
        try {
            return serverTable.getServerUUID(serverName);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public UUID getUuidOf(String playerName) throws DBException {
        try {
            return usersTable.getUuidOf(playerName);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public WebUser getWebUser(String username) throws DBException {
        try {
            return securityTable.getWebUser(username);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<TPS> getTPSData(UUID serverUUID) throws DBException {
        try {
            return tpsTable.getTPSData(serverUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<TPS> getNetworkOnlineData() throws DBException {
        try {
            return tpsTable.getNetworkOnlineData();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<Long> getRegisterDates() throws DBException {
        try {
            return usersTable.getRegisterDates();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<TPS> getAllTimePeak(UUID serverUUID) throws DBException {
        try {
            return tpsTable.getAllTimePeak(serverUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<TPS> getPeakPlayerCount(UUID serverUUID, long afterDate) throws DBException {
        try {
            return tpsTable.getPeakPlayerCount(serverUUID, afterDate);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsWithNoExtras() throws DBException {
        try {
            return sessionsTable.getAllSessions(false);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsAndExtras() throws DBException {
        try {
            return sessionsTable.getAllSessions(true);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, Map<UUID, List<Session>>> getSessionsInLastMonth() throws DBException {
        try {
            return sessionsTable.getSessionInLastMonth();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Set<String> getWorldNames(UUID serverUuid) throws DBException {
        try {
            return worldTable.getWorldNames(serverUuid);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<String> getNicknamesOfPlayerOnServer(UUID uuid, UUID serverUUID) throws DBException {
        try {
            return nicknamesTable.getNicknames(uuid, serverUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<Action> getActions(UUID uuid) throws DBException {
        try {
            return actionsTable.getActions(uuid);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, UserInfo> getUsers() throws DBException {
        try {
            return usersTable.getUsers();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, Long> getLastSeenForAllPlayers() throws DBException {
        try {
            return sessionsTable.getLastSeenForAllPlayers();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, List<GeoInfo>> getAllGeoInfo() throws DBException {
        try {
            return geoInfoTable.getAllGeoInfo();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, String> getPlayerNames() throws DBException {
        try {
            return usersTable.getPlayerNames();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public String getPlayerName(UUID playerUUID) throws DBException {
        try {
            return usersTable.getPlayerName(playerUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<String> getServerName(UUID serverUUID) throws DBException {
        try {
            return serverTable.getServerName(serverUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<String> getNicknames(UUID uuid) throws DBException {
        try {
            return nicknamesTable.getNicknames(uuid);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<Server> getBungeeInformation() throws DBException {
        try {
            return serverTable.getBungeeInfo();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<Integer> getServerID(UUID serverUUID) throws DBException {
        try {
            return serverTable.getServerID(serverUUID);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, Server> getBukkitServers() throws DBException {
        try {
            return serverTable.getBukkitServers();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<WebUser> getWebUsers() throws DBException {
        try {
            return securityTable.getUsers();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<Integer, String> getServerNamesByID() throws DBException {
        try {
            return serverTable.getServerNamesByID();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<Server> getServers() throws DBException {
        Map<UUID, Server> bukkitServers = getBukkitServers();
        Optional<Server> bungeeInformation = getBungeeInformation();

        List<Server> servers = new ArrayList<>(bukkitServers.values());
        bungeeInformation.ifPresent(servers::add);

        Collections.sort(servers);
        return servers;
    }

    @Override
    public List<UUID> getServerUUIDs() throws DBException {
        try {
            return serverTable.getServerUUIDs();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<String> getNetworkGeolocations() throws DBException {
        try {
            return geoInfoTable.getNetworkGeolocations();
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }
}
