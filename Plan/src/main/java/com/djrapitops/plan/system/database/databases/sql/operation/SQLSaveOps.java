/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.database.databases.sql.ErrorUtil;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SQLSaveOps extends SQLOps implements SaveOperations {

    public SQLSaveOps(SQLDB db) {
        super(db);
    }

    @Override
    public void insertTPS(Map<UUID, List<TPS>> ofServers) throws DBException {
        try {
            tpsTable.insertAllTPS(ofServers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertCommandUsage(Map<UUID, Map<String, Integer>> ofServers) throws DBException {
        try {
            commandUseTable.insertCommandUsage(ofServers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertUsers(Map<UUID, UserInfo> ofServers) throws DBException {
        try {
            usersTable.insertUsers(ofServers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertSessions(Map<UUID, Map<UUID, List<Session>>> ofServers, boolean containsExtraData) throws DBException {
        try {
            sessionsTable.insertSessions(ofServers, containsExtraData);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void kickAmount(Map<UUID, Integer> ofUsers) throws DBException {
        try {
            usersTable.updateKicked(ofUsers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertUserInfo(Map<UUID, List<UserInfo>> ofServers) throws DBException {
        try {
            userInfoTable.insertUserInfo(ofServers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertNicknames(Map<UUID, Map<UUID, List<String>>> ofServers) throws DBException {
        try {
            nicknamesTable.insertNicknames(ofServers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertAllGeoInfo(Map<UUID, List<GeoInfo>> ofUsers) throws DBException {
        try {
            geoInfoTable.insertAllGeoInfo(ofUsers);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void banStatus(UUID uuid, boolean banned) throws DBException {
        try {
            userInfoTable.updateBanStatus(uuid, banned);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void opStatus(UUID uuid, boolean op) throws DBException {
        try {
            userInfoTable.updateOpStatus(uuid, op);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void registerNewUser(UUID uuid, long registered, String name) throws DBException {
        try {
            usersTable.registerUser(uuid, registered, name);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void action(UUID uuid, Action action) throws DBException {
        try {
            actionsTable.insertAction(uuid, action);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void geoInfo(UUID uuid, GeoInfo geoInfo) throws DBException {
        try {
            geoInfoTable.saveGeoInfo(uuid, geoInfo);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void playerWasKicked(UUID uuid) throws DBException {
        try {
            usersTable.kicked(uuid);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void playerName(UUID uuid, String playerName) throws DBException {
        try {
            usersTable.updateName(uuid, playerName);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void playerDisplayName(UUID uuid, String displayName) throws DBException {
        try {
            nicknamesTable.saveUserName(uuid, displayName);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void registerNewUserOnThisServer(UUID uuid, long registered) throws DBException {
        try {
            userInfoTable.registerUserInfo(uuid, registered);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void commandUsed(String commandName) throws DBException {
        try {
            commandUseTable.commandUsed(commandName);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }

    @Override
    public void insertTPSforThisServer(TPS tps) throws DBException {
        try {
            tpsTable.insertTPS(tps);
        } catch (SQLException e) {
            throw ErrorUtil.getExceptionFor(e);
        }
    }
}