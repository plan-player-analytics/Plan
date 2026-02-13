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
import com.djrapitops.plan.delivery.domain.World;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.objects.JoinAddressQueries;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.webuser.*;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.KillsTable.*;

/**
 * Static method class for large storage queries.
 *
 * @author AuroraLS3
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
        if (ofUsers == null || ofUsers.isEmpty()) return Executable.empty();

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
    public static Executable storeAllNicknameData(Map<ServerUUID, Map<UUID, List<Nickname>>> ofServersAndUsers) {
        if (ofServersAndUsers == null || ofServersAndUsers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(NicknamesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<ServerUUID, Map<UUID, List<Nickname>>> serverEntry : ofServersAndUsers.entrySet()) {
                    ServerUUID serverUUID = serverEntry.getKey();
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
        if (users == null || users.isEmpty()) return Executable.empty();

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
                    statement.setString(4, user.getPermissionGroup());
                    statement.addBatch();
                }
            }
        };
    }

    /**
     * Execute a big batch of server information insert statements.
     *
     * @param servers Collection of Plan Servers.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeAllPlanServerInformation(Collection<Server> servers) {
        if (servers == null || servers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(ServerTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Server server : servers) {
                    ServerUUID serverUUID = server.getUuid();
                    if (serverUUID == null) {
                        continue;
                    }

                    statement.setString(1, serverUUID.toString());
                    statement.setString(2, server.getName());
                    statement.setString(3, server.getWebAddress());
                    statement.setBoolean(4, true);
                    statement.setBoolean(5, server.isProxy());
                    statement.setString(6, server.getPlanVersion());
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
    public static Executable storeAllTPSData(Map<ServerUUID, List<TPS>> ofServers) {
        if (ofServers == null || ofServers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(TPSTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<ServerUUID, List<TPS>> entry : ofServers.entrySet()) {
                    ServerUUID serverUUID = entry.getKey();
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
                        Sql.setDoubleOrNull(statement, 10, tps.getMsptAverage());
                        Sql.setDoubleOrNull(statement, 11, tps.getMspt95thPercentile());
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
    public static Executable storePerServerUserInformation(Map<ServerUUID, List<UserInfo>> ofServers) {
        if (ofServers == null || ofServers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(UserInfoTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (Map.Entry<ServerUUID, List<UserInfo>> entry : ofServers.entrySet()) {
                    ServerUUID serverUUID = entry.getKey();
                    // Every User
                    for (UserInfo user : entry.getValue()) {
                        statement.setString(1, user.getPlayerUuid().toString());
                        statement.setLong(2, user.getRegistered());
                        statement.setString(3, serverUUID.toString());
                        statement.setBoolean(4, user.isBanned());
                        statement.setString(5, StringUtils.truncate(user.getJoinAddress(), JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH));
                        statement.setBoolean(6, user.isOperator());
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
    public static Executable storeAllWorldNames(Map<ServerUUID, Collection<String>> ofServers) {
        if (ofServers == null || ofServers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<ServerUUID, Collection<String>> entry : ofServers.entrySet()) {
                    ServerUUID serverUUID = entry.getKey();
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
    public static Executable insertBaseUsers(Collection<BaseUser> ofUsers) {
        if (ofUsers == null || ofUsers.isEmpty()) return Executable.empty();

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

    public static Executable storeAllSessionsWithoutKillOrWorldData(Collection<FinishedSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(SessionsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (FinishedSession session : sessions) {
                    statement.setString(1, session.getPlayerUUID().toString());
                    statement.setLong(2, session.getStart());
                    statement.setLong(3, session.getEnd());
                    statement.setInt(4, session.getDeathCount());
                    statement.setInt(5, session.getMobKillCount());
                    statement.setLong(6, session.getAfkTime());
                    statement.setString(7, session.getServerUUID().toString());
                    statement.setString(8, StringUtils.truncate(session.getExtraData(JoinAddress.class)
                            .map(JoinAddress::getAddress).orElse(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP), JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH));
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable storeAllSessionsWithKillAndWorldData(Collection<FinishedSession> sessions) {
        return connection -> {
            Set<World> existingWorlds = WorldTimesQueries.fetchWorlds().executeWithConnection(connection);
            storeAllJoinAddresses(sessions, connection);
            storeAllWorldNames(sessions, existingWorlds).execute(connection);
            storeAllSessionsWithoutKillOrWorldData(sessions).execute(connection);
            storeSessionKillData(sessions).execute(connection);
            return storeSessionWorldTimeData(sessions).execute(connection);
        };
    }

    private static void storeAllJoinAddresses(Collection<FinishedSession> sessions, Connection connection) {
        List<String> existingJoinAddresses = JoinAddressQueries.allJoinAddresses().executeWithConnection(connection);
        storeAllJoinAddresses(sessions, existingJoinAddresses).execute(connection);
    }

    private static Executable storeAllJoinAddresses(Collection<FinishedSession> sessions, List<String> existingJoinAddresses) {
        return new ExecBatchStatement(JoinAddressTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) {
                sessions.stream()
                        .map(FinishedSession::getExtraData)
                        .map(extraData -> extraData.get(JoinAddress.class))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(JoinAddress::getAddress)
                        .map(joinAddress -> StringUtils.truncate(joinAddress, JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH))
                        .distinct()
                        .filter(address -> !existingJoinAddresses.contains(address))
                        .forEach(address -> {
                            try {
                                statement.setString(1, address);
                                statement.addBatch();
                            } catch (SQLException e) {
                                throw DBOpException.forCause(JoinAddressTable.INSERT_STATEMENT, e);
                            }
                        });
            }
        };
    }

    private static Executable storeAllWorldNames(Collection<FinishedSession> sessions, Set<World> existingWorlds) {
        Set<World> worlds = sessions.stream().flatMap(session -> {
                    ServerUUID serverUUID = session.getServerUUID();
                    return session.getExtraData(WorldTimes.class)
                            .map(WorldTimes::getWorldTimes)
                            .map(Map::keySet)
                            .orElseGet(Collections::emptySet)
                            .stream()
                            .map(worldName -> new World(worldName, serverUUID));
                }).filter(world -> !existingWorlds.contains(world))
                .collect(Collectors.toSet());

        if (worlds.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (World world : worlds) {
                    statement.setString(1, world.getWorldName());
                    statement.setString(2, world.getServerUUID().toString());
                    statement.addBatch();
                }
            }
        };
    }

    private static Executable storeSessionKillData(Collection<FinishedSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(KillsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (FinishedSession session : sessions) {
                    KillsTable.addSessionKillsToBatch(statement, session);
                }
            }
        };
    }

    private static Executable storeSessionWorldTimeData(Collection<FinishedSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(WorldTimesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                for (FinishedSession session : sessions) {
                    WorldTimesTable.addSessionWorldTimesToBatch(statement, session, gms);
                }
            }
        };
    }

    public static Executable storeAllPingData(Map<UUID, List<Ping>> ofUsers) {
        if (ofUsers == null || ofUsers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(PingTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, List<Ping>> entry : ofUsers.entrySet()) {
                    UUID uuid = entry.getKey();
                    List<Ping> pings = entry.getValue();
                    for (Ping ping : pings) {
                        ServerUUID serverUUID = ping.getServerUUID();
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

    public static Executable storeGroupNames(List<String> groups) {
        if (groups == null || groups.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(WebGroupTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String group : groups) {
                    statement.setString(1, group);
                    statement.addBatch();
                }
            }
        };
    }


    public static Executable storePermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(WebPermissionTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String permission : permissions) {
                    statement.setString(1, permission);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable storeGroupPermissionRelations(Map<String, List<String>> groupPermissions) {
        if (groupPermissions == null || groupPermissions.isEmpty()) return Executable.empty();

        @Language("SQL")
        String sql = INSERT_INTO + WebGroupToPermissionTable.TABLE_NAME + " (" +
                WebGroupToPermissionTable.GROUP_ID + ',' + WebGroupToPermissionTable.PERMISSION_ID +
                ") VALUES ((" +
                WebGroupTable.SELECT_GROUP_ID + "),(" + WebPermissionTable.SELECT_PERMISSION_ID +
                "))";

        return new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (var permissionsOfGroup : groupPermissions.entrySet()) {
                    String group = permissionsOfGroup.getKey();
                    for (String permission : permissionsOfGroup.getValue()) {
                        statement.setString(1, group);
                        statement.setString(2, permission);
                        statement.addBatch();
                    }
                }
            }
        };
    }

    public static Executable storeGroupPermissionIdRelations(Map<Integer, List<Integer>> groupPermissions) {
        if (groupPermissions == null || groupPermissions.isEmpty()) return Executable.empty();

        @Language("SQL")
        String sql = INSERT_INTO + WebGroupToPermissionTable.TABLE_NAME + " (" +
                WebGroupToPermissionTable.GROUP_ID + ',' + WebGroupToPermissionTable.PERMISSION_ID +
                ") VALUES (?, ?)";

        return new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (var permissionsOfGroup : groupPermissions.entrySet()) {
                    Integer groupId = permissionsOfGroup.getKey();
                    for (Integer permissionId : permissionsOfGroup.getValue()) {
                        statement.setInt(1, groupId);
                        statement.setInt(2, permissionId);
                        statement.addBatch();
                    }
                }
            }
        };
    }

    public static Executable storeAllPreferences(Map<String, String> preferencesByUsername) {
        if (preferencesByUsername.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(WebUserPreferencesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (var entry : preferencesByUsername.entrySet()) {
                    String username = entry.getKey();
                    String preferences = entry.getValue();
                    statement.setString(1, preferences);
                    statement.setString(2, username);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable mergeBaseUsers(List<BaseUser> existingUsers, LookupTable<UUID> playerLookupTable) {
        if (existingUsers.isEmpty()) return Executable.empty();

        return new ExecBatchStatement(UsersTable.UPDATE_MERGE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (BaseUser user : existingUsers) {
                    Optional<Integer> playerId = playerLookupTable.find(user.getUuid());
                    if (playerId.isPresent()) {
                        statement.setInt(1, user.getTimesKicked());
                        statement.setLong(2, user.getRegistered());
                        statement.setLong(3, user.getRegistered());
                        statement.setInt(4, playerId.get());
                        statement.addBatch();
                    }
                }
            }
        };
    }

    public static Executable insertUserInfo(List<UserInfoTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(UserInfoTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (UserInfoTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable insertJoinAddresses(List<JoinAddressTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(JoinAddressTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (JoinAddressTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable insertPing(List<PingTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(PingTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (PingTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable insertTps(List<TPSTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(TPSTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (TPSTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable insertPluginVersions(List<PluginVersionTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(PluginVersionTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (PluginVersionTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable insertSessionsWithOldIds(List<SessionsTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(SessionsTable.Row.INSERT_STATEMENT_WITH_OLD_ID) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (SessionsTable.Row row : rows) {
                    row.insert(statement, true);
                    statement.addBatch();
                }
            }
        };
    }

    public static Transaction insertWorldTimesWithOldSessionIds(List<WorldTimesTable.Row> rows) {
        return new Transaction() {
            @Override
            protected void performOperations() {
                String batchTableName = "plan_world_times_batch";
                execute(CreateTableBuilder.create(batchTableName, dbType)
                        .column(WorldTimesTable.USER_ID, Sql.INT).notNull()
                        .column(WorldTimesTable.WORLD_ID, Sql.INT).notNull()
                        .column(WorldTimesTable.SERVER_ID, Sql.INT).notNull()
                        .column(WorldTimesTable.SESSION_ID, Sql.INT).notNull()
                        .column(WorldTimesTable.SURVIVAL, Sql.LONG).notNull().defaultValue("0")
                        .column(WorldTimesTable.CREATIVE, Sql.LONG).notNull().defaultValue("0")
                        .column(WorldTimesTable.ADVENTURE, Sql.LONG).notNull().defaultValue("0")
                        .column(WorldTimesTable.SPECTATOR, Sql.LONG).notNull().defaultValue("0")
                        .build());

                String insertSql = Insert.values(batchTableName,
                        WorldTimesTable.USER_ID,
                        WorldTimesTable.WORLD_ID,
                        WorldTimesTable.SERVER_ID,
                        WorldTimesTable.SESSION_ID,
                        WorldTimesTable.SURVIVAL,
                        WorldTimesTable.CREATIVE,
                        WorldTimesTable.ADVENTURE,
                        WorldTimesTable.SPECTATOR
                );

                execute(new ExecBatchStatement(insertSql) {
                    @Override
                    public void prepare(PreparedStatement statement) throws SQLException {
                        for (WorldTimesTable.Row row : rows) {
                            row.insert(statement);
                            statement.addBatch();
                        }
                    }
                });

                String batchCopyStatement = INSERT_INTO + WorldTimesTable.TABLE_NAME + " (" +
                        WorldTimesTable.USER_ID + ", " +
                        WorldTimesTable.WORLD_ID + ", " +
                        WorldTimesTable.SERVER_ID + ", " +
                        WorldTimesTable.SESSION_ID + ", " +
                        WorldTimesTable.SURVIVAL + ", " +
                        WorldTimesTable.CREATIVE + ", " +
                        WorldTimesTable.ADVENTURE + ", " +
                        WorldTimesTable.SPECTATOR +
                        ")" + SELECT +
                        "a." + WorldTimesTable.USER_ID + ',' + WorldTimesTable.WORLD_ID + ",a." + WorldTimesTable.SERVER_ID + ',' +
                        "s." + SessionsTable.ID + ',' +
                        WorldTimesTable.SURVIVAL + ',' + WorldTimesTable.CREATIVE + ',' +
                        WorldTimesTable.ADVENTURE + ',' + WorldTimesTable.SPECTATOR +
                        FROM + batchTableName + " a" +
                        INNER_JOIN + SessionsTable.TABLE_NAME + " s ON a." + WorldTimesTable.SESSION_ID + "=s." + SessionsTable.Row.OLD_ID;
                execute(batchCopyStatement);

                execute(DELETE_FROM + batchTableName);
            }
        };
    }

    public static Executable insertWorlds(List<WorldTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (WorldTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Transaction insertKillsWithOldSessionIds(List<KillsTable.Row> rows) {
        return new Transaction() {
            @Override
            protected void performOperations() {
                String batchTableName = "plan_kills_batch";
                execute(CreateTableBuilder.create(batchTableName, dbType)
                        .column(KILLER_UUID, Sql.varchar(36)).notNull()
                        .column(VICTIM_UUID, Sql.varchar(36)).notNull()
                        .column(SERVER_UUID, Sql.varchar(36)).notNull()
                        .column(WEAPON, Sql.varchar(WEAPON_COLUMN_LENGTH)).notNull()
                        .column(DATE, Sql.LONG).notNull()
                        .column(SESSION_ID, Sql.INT).notNull()
                        .build());

                String insertSql = Insert.values(batchTableName,
                        KILLER_UUID,
                        VICTIM_UUID,
                        SERVER_UUID,
                        DATE,
                        SESSION_ID,
                        WEAPON
                );

                execute(new ExecBatchStatement(insertSql) {
                    @Override
                    public void prepare(PreparedStatement statement) throws SQLException {
                        for (KillsTable.Row row : rows) {
                            row.insert(statement);
                            statement.addBatch();
                        }
                    }
                });

                String batchCopyStatement = INSERT_INTO + TABLE_NAME + " (" +
                        SESSION_ID + ", " +
                        KILLER_UUID + ", " +
                        VICTIM_UUID + ", " +
                        SERVER_UUID + ", " +
                        WEAPON + ", " +
                        DATE +
                        ")" + SELECT +
                        "s." + SessionsTable.ID + ',' +
                        KILLER_UUID + ',' +
                        VICTIM_UUID + ',' +
                        SERVER_UUID + ',' +
                        WEAPON + ',' +
                        DATE +
                        FROM + batchTableName + " a" +
                        INNER_JOIN + SessionsTable.TABLE_NAME + " s ON a." + SESSION_ID + "=s." + SessionsTable.Row.OLD_ID;
                execute(batchCopyStatement);

                execute(DELETE_FROM + batchTableName);
            }
        };
    }

    public static Executable insertAccessLog(List<AccessLogTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(AccessLogTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (AccessLogTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable upsertGeoInfo(List<GeoInfoTable.Row> rows, DBType dbType) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(dbType == DBType.MYSQL ? GeoInfoTable.UPSERT_STATEMENT_MYSQL : GeoInfoTable.UPSERT_STATEMENT_SQLITE) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (GeoInfoTable.Row row : rows) {
                    row.upsert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable upsertNicknames(List<NicknamesTable.Row> rows, DBType dbType) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(dbType == DBType.MYSQL ? NicknamesTable.UPSERT_STATEMENT_MYSQL : NicknamesTable.UPSERT_STATEMENT_SQLITE) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (NicknamesTable.Row row : rows) {
                    row.upsert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable upsertAllowlistBounces(List<AllowlistBounceTable.Row> rows, DBType dbType) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(dbType == DBType.MYSQL ? AllowlistBounceTable.UPSERT_MYSQL : AllowlistBounceTable.UPSERT_SQLITE) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (AllowlistBounceTable.Row row : rows) {
                    row.upsert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable storeUsers(List<SecurityTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(SecurityTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (SecurityTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }

    public static Executable insertPreferences(List<WebUserPreferencesTable.Row> rows) {
        if (rows.isEmpty()) return Executable.empty();
        return new ExecBatchStatement(WebUserPreferencesTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (WebUserPreferencesTable.Row row : rows) {
                    row.insert(statement);
                    statement.addBatch();
                }
            }
        };
    }
}