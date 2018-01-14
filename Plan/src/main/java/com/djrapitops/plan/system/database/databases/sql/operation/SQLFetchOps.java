package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.database.databases.sql.ErrorUtil;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;
import java.util.*;

public class SQLFetchOps extends SQLOps implements FetchOperations {

    public SQLFetchOps(SQLDB db) {
        super(db);
    }

    @Override
    public ServerProfile getServerProfile(UUID serverUUID) {
        return null;
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
}
