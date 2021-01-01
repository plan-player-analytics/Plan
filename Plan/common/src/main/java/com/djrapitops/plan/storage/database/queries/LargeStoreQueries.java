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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Static method class for large storage queries.
 *
 * @author Rsl1122
 */
public class LargeStoreQueries {

    private LargeStoreQueries() {
        /* Static method class */
    }

    /**
     * Execute a big batch of GeoInfo insert statements.
     *
     * @param ofUsers Map: Player UUID - List of GeoInfo
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllGeoInformation(Map<UUID, List<GeoInfo>> ofUsers) {
        if (Verify.isEmpty(ofUsers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(GeoInfoTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every User
                for (Map.Entry<UUID, List<GeoInfo>> playerEntry : ofUsers.entrySet()) {
                    UUID playerUUID = playerEntry.getKey();
                    // Every GeoInfo
                    for (GeoInfo info : playerEntry.getValue()) {
                        String geoLocation = info.getGeolocation();
                        long lastUsed = info.getDate();

                        statement.setString(1, playerUUID.toString());
                        statement.setString(2, geoLocation);
                        statement.setLong(3, lastUsed);

                        statement.addBatch();
                    }
                }
            }
        };
    }

    /**
     * Execute a big batch of nickname insert statements.
     *
     * @param ofServersAndUsers Multimap: Server UUID - (Player UUID - List of nicknames)
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllNicknameData(Map<UUID, Map<UUID, List<Nickname>>> ofServersAndUsers) {
        if (Verify.isEmpty(ofServersAndUsers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(NicknamesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<UUID, Map<UUID, List<Nickname>>> serverEntry : ofServersAndUsers.entrySet()) {
                    UUID serverUUID = serverEntry.getKey();
                    // Every User
                    for (Map.Entry<UUID, List<Nickname>> entry : serverEntry.getValue().entrySet()) {
                        UUID uuid = entry.getKey();
                        // Every Nickname
                        List<Nickname> nicknames = entry.getValue();
                        for (Nickname nickname : nicknames) {
                            statement.setString(1, uuid.toString());
                            statement.setString(2, serverUUID.toString());
                            statement.setString(3, nickname.getName());
                            statement.setLong(4, nickname.getDate());
                            statement.addBatch();
                        }
                    }
                }
            }
        };
    }

    public static Executable storeAllPlanWebUsers(Collection<User> users) {
        if (Verify.isEmpty(users)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(SecurityTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (User user : users) {
                    statement.setString(1, user.getUsername());
                    if (user.getLinkedToUUID() == null) {
                        statement.setNull(2, Types.VARCHAR);
                    } else {
                        statement.setString(2, user.getLinkedToUUID().toString());
                    }
                    statement.setString(3, user.getPasswordHash());
                    statement.setInt(4, user.getPermissionLevel());
                    statement.addBatch();
                }
            }
        };
    }

    /**
     * Execute a big batch of server infromation insert statements.
     *
     * @param servers Collection of Plan Servers.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllPlanServerInformation(Collection<Server> servers) {
        if (Verify.isEmpty(servers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(ServerTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Server server : servers) {
                    UUID uuid = server.getUuid();
                    if (uuid == null) {
                        continue;
                    }

                    statement.setString(1, uuid.toString());
                    statement.setString(2, server.getName());
                    statement.setString(3, server.getWebAddress());
                    statement.setBoolean(4, true);
                    statement.setBoolean(5, server.isProxy());
                    statement.addBatch();
                }
            }
        };
    }

    /**
     * Execute a big batch of TPS insert statements.
     *
     * @param ofServers Map: Server UUID - List of TPS data
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllTPSData(Map<UUID, List<TPS>> ofServers) {
        if (Verify.isEmpty(ofServers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(TPSTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<UUID, List<TPS>> entry : ofServers.entrySet()) {
                    UUID serverUUID = entry.getKey();
                    // Every TPS Data point
                    List<TPS> tpsList = entry.getValue();
                    for (TPS tps : tpsList) {
                        statement.setString(1, serverUUID.toString());
                        statement.setLong(2, tps.getDate());
                        statement.setDouble(3, tps.getTicksPerSecond());
                        statement.setInt(4, tps.getPlayers());
                        statement.setDouble(5, tps.getCPUUsage());
                        statement.setLong(6, tps.getUsedMemory());
                        statement.setDouble(7, tps.getEntityCount());
                        statement.setDouble(8, tps.getChunksLoaded());
                        statement.setLong(9, tps.getFreeDiskSpace());
                        statement.addBatch();
                    }
                }
            }
        };
    }

    /**
     * Execute a big batch of Per server UserInfo insert statements.
     *
     * @param ofServers Map: Server UUID - List of user information
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storePerServerUserInformation(Map<UUID, List<UserInfo>> ofServers) {
        if (Verify.isEmpty(ofServers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(UserInfoTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<UUID, List<UserInfo>> entry : ofServers.entrySet()) {
                    UUID serverUUID = entry.getKey();
                    // Every User
                    for (UserInfo user : entry.getValue()) {
                        statement.setString(1, user.getPlayerUuid().toString());
                        statement.setLong(2, user.getRegistered());
                        statement.setString(3, serverUUID.toString());
                        statement.setBoolean(4, user.isBanned());
                        statement.setBoolean(5, user.isOperator());
                        statement.addBatch();
                    }
                }
            }
        };
    }

    /**
     * Execute a big batch of world name insert statements.
     *
     * @param ofServers Map: Server UUID - Collection of world names
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllWorldNames(Map<UUID, Collection<String>> ofServers) {
        if (Verify.isEmpty(ofServers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, Collection<String>> entry : ofServers.entrySet()) {
                    UUID serverUUID = entry.getKey();
                    for (String world : entry.getValue()) {
                        statement.setString(1, StringUtils.truncate(world, 100));
                        statement.setString(2, serverUUID.toString());
                        statement.addBatch();
                    }
                }
            }
        };
    }

    /**
     * Execute a big batch of user information insert statements.
     *
     * @param ofUsers Collection of BaseUsers
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllCommonUserInformation(Collection<BaseUser> ofUsers) {
        if (Verify.isEmpty(ofUsers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(UsersTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (BaseUser user : ofUsers) {
                    statement.setString(1, user.getUuid().toString());
                    statement.setString(2, user.getName());
                    statement.setLong(3, user.getRegistered());
                    statement.setInt(4, user.getTimesKicked());
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable storeAllSessionsWithoutKillOrWorldData(Collection<Session> sessions) {
        if (Verify.isEmpty(sessions)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(SessionsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Session session : sessions) {
                    statement.setString(1, session.getUnsafe(SessionKeys.UUID).toString());
                    statement.setLong(2, session.getUnsafe(SessionKeys.START));
                    statement.setLong(3, session.getUnsafe(SessionKeys.END));
                    statement.setInt(4, session.getValue(SessionKeys.DEATH_COUNT).orElse(0));
                    statement.setInt(5, session.getValue(SessionKeys.MOB_KILL_COUNT).orElse(0));
                    statement.setLong(6, session.getValue(SessionKeys.AFK_TIME).orElse(0L));
                    statement.setString(7, session.getUnsafe(SessionKeys.SERVER_UUID).toString());
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable storeAllSessionsWithKillAndWorldData(Collection<Session> sessions) {
        return connection -> {
            storeAllSessionsWithoutKillOrWorldData(sessions).execute(connection);
            storeSessionKillData(sessions).execute(connection);
            return storeSessionWorldTimeData(sessions).execute(connection);
        };
    }

    private static Executable storeSessionKillData(Collection<Session> sessions) {
        if (Verify.isEmpty(sessions)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(KillsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Session session : sessions) {
                    KillsTable.addSessionKillsToBatch(statement, session);
                }
            }
        };
    }

    private static Executable storeSessionWorldTimeData(Collection<Session> sessions) {
        if (Verify.isEmpty(sessions)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(WorldTimesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                for (Session session : sessions) {
                    WorldTimesTable.addSessionWorldTimesToBatch(statement, session, gms);
                }
            }
        };
    }

    public static Executable storeAllPingData(Map<UUID, List<Ping>> ofUsers) {
        if (Verify.isEmpty(ofUsers)) {
            return Executable.empty();
        }

        return new ExecBatchStatement(PingTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, List<Ping>> entry : ofUsers.entrySet()) {
                    UUID uuid = entry.getKey();
                    List<Ping> pings = entry.getValue();
                    for (Ping ping : pings) {
                        UUID serverUUID = ping.getServerUUID();
                        long date = ping.getDate();
                        int minPing = ping.getMin();
                        int maxPing = ping.getMax();
                        double avgPing = ping.getAverage();

                        statement.setString(1, uuid.toString());
                        statement.setString(2, serverUUID.toString());
                        statement.setLong(3, date);
                        statement.setInt(4, minPing);
                        statement.setInt(5, maxPing);
                        statement.setDouble(6, avgPing);
                        statement.addBatch();
                    }
                }
            }
        };
    }
}