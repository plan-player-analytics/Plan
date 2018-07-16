/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.info.server.Server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SaveOperations implementation for SQL databases.
 *
 * @author Rsl1122
 */
public class SQLSaveOps extends SQLOps implements SaveOperations {

    public SQLSaveOps(SQLDB db) {
        super(db);
    }

    @Override
    public void insertTPS(Map<UUID, List<TPS>> ofServers) {
        tpsTable.insertAllTPS(ofServers);
    }

    @Override
    public void insertCommandUsage(Map<UUID, Map<String, Integer>> ofServers) {
        commandUseTable.insertCommandUsage(ofServers);
    }

    @Override
    public void insertUsers(Map<UUID, UserInfo> ofServers) {
        usersTable.insertUsers(ofServers);
    }

    @Override
    public void insertSessions(Map<UUID, Map<UUID, List<Session>>> ofServers, boolean containsExtraData) {
        sessionsTable.insertSessions(ofServers, containsExtraData);
    }

    @Override
    public void kickAmount(Map<UUID, Integer> ofUsers) {
        usersTable.updateKicked(ofUsers);
    }

    @Override
    public void insertUserInfo(Map<UUID, List<UserInfo>> ofServers) {
        userInfoTable.insertUserInfo(ofServers);
    }

    @Override
    public void insertNicknames(Map<UUID, Map<UUID, List<Nickname>>> ofServers) {
        nicknamesTable.insertNicknames(ofServers);
    }

    @Override
    public void insertAllGeoInfo(Map<UUID, List<GeoInfo>> ofUsers) {
        geoInfoTable.insertAllGeoInfo(ofUsers);
    }

    @Override
    public void banStatus(UUID uuid, boolean banned) {
        userInfoTable.updateBanStatus(uuid, banned);
    }

    @Override
    public void opStatus(UUID uuid, boolean op) {
        userInfoTable.updateOpStatus(uuid, op);
    }

    @Override
    public void registerNewUser(UUID uuid, long registered, String name) {
        usersTable.registerUser(uuid, registered, name);
    }

    @Override
    public void geoInfo(UUID uuid, GeoInfo geoInfo) {
        geoInfoTable.saveGeoInfo(uuid, geoInfo);
    }

    @Override
    public void playerWasKicked(UUID uuid) {
        usersTable.kicked(uuid);
    }

    @Override
    public void playerName(UUID uuid, String playerName) {
        usersTable.updateName(uuid, playerName);
    }

    @Override
    public void playerDisplayName(UUID uuid, Nickname nickname) {
        nicknamesTable.saveUserName(uuid, nickname);
    }

    @Override
    public void registerNewUserOnThisServer(UUID uuid, long registered) {
        userInfoTable.registerUserInfo(uuid, registered);
    }

    @Override
    public void commandUsed(String commandName) {
        commandUseTable.commandUsed(commandName);
    }

    @Override
    public void insertTPSforThisServer(TPS tps) {
        tpsTable.insertTPS(tps);
    }

    @Override
    public void session(UUID uuid, Session session) {
        sessionsTable.saveSession(uuid, session);
    }

    @Override
    public void serverInfoForThisServer(Server server) {
        serverTable.saveCurrentServerInfo(server);
    }

    @Override
    public void webUser(WebUser webUser) {
        securityTable.addNewUser(webUser);
    }

    @Override
    public void ping(UUID uuid, Ping ping) {
        pingTable.insertPing(uuid, ping);
    }
}
