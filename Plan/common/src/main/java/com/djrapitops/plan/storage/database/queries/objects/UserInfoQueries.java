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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.java.Lists;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link UserInfo} objects.
 *
 * @author AuroraLS3
 */
public class UserInfoQueries {

    private UserInfoQueries() {
        /* Static method class */
    }

    /**
     * Query database for user information.
     * <p>
     * The user information does not contain player names.
     *
     * @return Map: Server UUID - List of user information
     */
    public static Query<Map<ServerUUID, List<UserInfo>>> fetchAllUserInformation() {
        String sql = SELECT +
                "ux." + UserInfoTable.REGISTERED + ',' +
                UserInfoTable.BANNED + ',' +
                UserInfoTable.OP + ',' +
                "u." + UsersTable.USER_UUID + ',' +
                "s." + ServerTable.SERVER_UUID + " as server_uuid," +
                UserInfoTable.JOIN_ADDRESS +
                FROM + UserInfoTable.TABLE_NAME + " ux" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + '=' + "ux." + UserInfoTable.USER_ID +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.ID + '=' + "ux." + UserInfoTable.SERVER_ID;

        return new QueryAllStatement<>(sql, 50000) {
            @Override
            public Map<ServerUUID, List<UserInfo>> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, List<UserInfo>> serverMap = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString("server_uuid"));
                    UUID uuid = UUID.fromString(set.getString(UsersTable.USER_UUID));

                    List<UserInfo> userInfos = serverMap.computeIfAbsent(serverUUID, Lists::create);

                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    boolean op = set.getBoolean(UserInfoTable.OP);
                    String joinAddress = set.getString(UserInfoTable.JOIN_ADDRESS);

                    userInfos.add(new UserInfo(uuid, serverUUID, registered, op, joinAddress, banned));
                }
                return serverMap;
            }
        };
    }

    /**
     * Query database for User information of a specific player.
     *
     * @param playerUUID UUID of the player.
     * @return List of UserInfo objects, one for each server where the player has played.
     */
    public static Query<Set<UserInfo>> fetchUserInformationOfUser(UUID playerUUID) {
        String sql = SELECT +
                UserInfoTable.TABLE_NAME + '.' + UserInfoTable.REGISTERED + ',' +
                UserInfoTable.BANNED + ',' +
                UserInfoTable.OP + ',' +
                ServerTable.SERVER_UUID + ',' +
                UserInfoTable.JOIN_ADDRESS +
                FROM + UserInfoTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.ID + '=' + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.SERVER_ID +
                WHERE + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.USER_ID + "=" + UsersTable.SELECT_USER_ID;

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Set<UserInfo> processResults(ResultSet set) throws SQLException {
                Set<UserInfo> userInformation = new HashSet<>();
                while (set.next()) {
                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean op = set.getBoolean(UserInfoTable.OP);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    String joinAddress = set.getString(UserInfoTable.JOIN_ADDRESS);

                    userInformation.add(new UserInfo(playerUUID, serverUUID, registered, op, joinAddress, banned));
                }
                return userInformation;
            }
        };
    }

    public static Query<Map<UUID, Long>> fetchRegisterDates(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT +
                UsersTable.USER_UUID + ',' +
                "ux." + UserInfoTable.REGISTERED +
                FROM + UserInfoTable.TABLE_NAME + " ux" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + '=' + "ux." + UserInfoTable.USER_ID +
                WHERE + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + "ux." + UserInfoTable.REGISTERED + ">=?" +
                AND + "ux." + UserInfoTable.REGISTERED + "<=?";

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public Map<UUID, Long> processResults(ResultSet set) throws SQLException {
                Map<UUID, Long> registerDates = new HashMap<>();
                while (set.next()) {
                    registerDates.put(
                            UUID.fromString(set.getString(UsersTable.USER_UUID)),
                            set.getLong(UserInfoTable.REGISTERED)
                    );
                }
                return registerDates;
            }
        };
    }

    public static Query<Set<Integer>> userIdsOfOperators() {
        return getUserIdsForBooleanGroup(UserInfoTable.OP, true);
    }

    public static Query<Set<Integer>> getUserIdsForBooleanGroup(String column, boolean value) {
        String sql = SELECT + "u." + UsersTable.ID +
                FROM + UserInfoTable.TABLE_NAME +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + '=' + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.USER_ID +
                WHERE + column + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, value);
            }

            @Override
            public Set<Integer> processResults(ResultSet set) throws SQLException {
                return extractUserIds(set);
            }
        };
    }

    public static Set<Integer> extractUserIds(ResultSet set) throws SQLException {
        return extractUserIds(set, UsersTable.ID);
    }

    public static Set<Integer> extractUserIds(ResultSet set, String column) throws SQLException {
        Set<Integer> userIds = new HashSet<>();
        while (set.next()) {
            userIds.add(set.getInt(column));
        }
        return userIds;
    }

    public static Query<Set<Integer>> userIdsOfNonOperators() {
        return getUserIdsForBooleanGroup(UserInfoTable.OP, false);
    }

    public static Query<Set<Integer>> userIdsOfBanned() {
        return getUserIdsForBooleanGroup(UserInfoTable.BANNED, true);
    }

    public static Query<Set<Integer>> userIdsOfNotBanned() {
        return getUserIdsForBooleanGroup(UserInfoTable.BANNED, false);
    }

    public static Query<Set<Integer>> userIdsOfRegisteredBetween(long after, long before, List<ServerUUID> serverUUIDs) {
        String selectServerIds = SELECT + ServerTable.ID +
                FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.SERVER_UUID + " IN ('" + new TextStringBuilder().appendWithSeparators(serverUUIDs, "','") + "')";

        String sql = SELECT + DISTINCT + "u." + UsersTable.ID +
                FROM + UserInfoTable.TABLE_NAME + " ux" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + "=ux." + UserInfoTable.USER_ID +
                INNER_JOIN + "(" + selectServerIds + ") sel_server on sel_server." + ServerTable.ID + "=ux." + UserInfoTable.SERVER_ID +
                WHERE + "ux." + UserInfoTable.REGISTERED + ">=?" +
                AND + "ux." + UserInfoTable.REGISTERED + "<=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, after);
                statement.setLong(2, before);
            }

            @Override
            public Set<Integer> processResults(ResultSet set) throws SQLException {
                Set<Integer> userIds = new HashSet<>();
                while (set.next()) {
                    userIds.add(set.getInt(UsersTable.ID));
                }
                return userIds;
            }
        };
    }

    public static Query<List<UserInfoTable.Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(UserInfoTable.TABLE_NAME)
                .where(UserInfoTable.ID + '>' + currentId)
                .orderBy(UserInfoTable.ID)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, UserInfoTable.Row::extract);
    }
}