package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.ErrorUtil;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
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
            if (allTimePeak.isPresent()) {
                TPS peak = allTimePeak.get();
                profile.setAllTimePeak(peak.getDate());
                profile.setAllTimePeakPlayers(peak.getPlayers());
            }
            Optional<TPS> lastPeak = tpsTable.getPeakPlayerCount(serverUUID, MiscUtils.getTime() - (TimeAmount.DAY.ms() * 2L));
            if (lastPeak.isPresent()) {
                TPS peak = lastPeak.get();
                profile.setLastPeakDate(peak.getDate());
                profile.setLastPeakPlayers(peak.getPlayers());
            }

            profile.setCommandUsage(commandUseTable.getCommandUse(serverUUID));
            profile.setServerWorldtimes(worldTimesTable.getWorldTimesOfServer(serverUUID));

            return profile;
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public List<PlayerProfile> getPlayers(UUID serverUUID) throws DBException {
        try {
            List<UserInfo> serverUserInfo = userInfoTable.getServerUserInfo(serverUUID);
            Map<UUID, Integer> timesKicked = usersTable.getAllTimesKicked();
            Map<UUID, List<Action>> actions = actionsTable.getServerActions(serverUUID);
            Map<UUID, List<GeoInfo>> geoInfo = ipsTable.getAllGeoInfo();

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
            throw ErrorUtil.getExceptionFor(e);
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
            profile.setGeoInformation(ipsTable.getGeoInfo(uuid));

            Map<UUID, List<Session>> sessions = sessionsTable.getSessions(uuid);
            profile.setSessions(sessions);
            profile.calculateWorldTimesPerServer();
            profile.setTotalWorldTimes(worldTimesTable.getWorldTimesOfUser(uuid));

            return profile;
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
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
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Set<UUID> getSavedUUIDs(UUID server) throws DBException {
        try {
            return userInfoTable.getSavedUUIDs(server);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Map<UUID, String> getServerNames() throws DBException {
        try {
            return serverTable.getServerNames();
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public Optional<UUID> getServerUUID(String serverName) throws DBException {
        try {
            return serverTable.getServerUUID(serverName);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public UUID getUuidOf(String playerName) throws DBException {
        try {
            return usersTable.getUuidOf(playerName);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }


    @Override
    public WebUser getWebUser(String username) throws DBException {
        try {
            return securityTable.getWebUser(username);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }


    @Override
    public List<TPS> getTPSData(UUID serverUUID) throws DBException {
        try {
            return tpsTable.getTPSData(serverUUID);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }
}
