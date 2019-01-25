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
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.queries.LargeStoreQueries;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.config.Config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllTPSData(ofServers));
            }
        });
    }

    @Override
    public void insertCommandUsage(Map<UUID, Map<String, Integer>> ofServers) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllCommandUsageData(ofServers));
            }
        });
    }

    @Override
    public void insertUsers(Map<UUID, UserInfo> ofUsers) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                Collection<BaseUser> users = ofUsers.values().stream()
                        .map(user -> new BaseUser(user.getUuid(), user.getName(), user.getRegistered(), 0))
                        .collect(Collectors.toSet());
                execute(LargeStoreQueries.storeAllCommonUserInformation(users));
            }
        });
    }

    @Override
    public void insertSessions(Map<UUID, Map<UUID, List<Session>>> ofServers, boolean containsExtraData) {
        List<Session> sessions = ofServers.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(containsExtraData ? LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions)
                        : LargeStoreQueries.storeAllSessionsWithoutKillOrWorldData(sessions));
            }
        });
    }

    @Override
    public void kickAmount(Map<UUID, Integer> ofUsers) {
    }

    @Override
    public void insertUserInfo(Map<UUID, List<UserInfo>> ofServers) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storePerServerUserInformation(ofServers));
            }
        });
    }

    @Override
    public void insertNicknames(Map<UUID, Map<UUID, List<Nickname>>> ofServers) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllNicknameData(ofServers));
            }
        });
    }

    @Override
    public void insertAllGeoInfo(Map<UUID, List<GeoInfo>> ofUsers) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllGeoInformation(ofUsers));
            }
        });
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

    @Override
    public void setAsUninstalled(UUID serverUUID) {
        serverTable.setAsUninstalled(serverUUID);
    }

    @Override
    public void saveConfig(UUID serverUUID, Config config, long lastModified) {
        settingsTable.storeConfig(serverUUID, config, lastModified);
    }
}
