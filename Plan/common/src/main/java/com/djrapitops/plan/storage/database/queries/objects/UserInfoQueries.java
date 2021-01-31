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
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.utilities.java.Lists;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link UserInfo} objects.
 *
 * @author Rsl1122
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
    public static Query<Map<UUID, List<UserInfo>>> fetchAllUserInformation() {
        String sql = SELECT +
                UserInfoTable.REGISTERED + ',' +
                UserInfoTable.BANNED + ',' +
                UserInfoTable.OP + ',' +
                UserInfoTable.USER_UUID + ',' +
                UserInfoTable.SERVER_UUID +
                FROM + UserInfoTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, List<UserInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<UserInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<UserInfo>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(UserInfoTable.SERVER_UUID));
                    UUID uuid = UUID.fromString(set.getString(UserInfoTable.USER_UUID));

                    List<UserInfo> userInfos = serverMap.computeIfAbsent(serverUUID, Lists::create);

                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    boolean op = set.getBoolean(UserInfoTable.OP);

                    userInfos.add(new UserInfo(uuid, serverUUID, registered, op, banned));
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
    public static Query<List<UserInfo>> fetchUserInformationOfUser(UUID playerUUID) {
        String sql = SELECT +
                UserInfoTable.TABLE_NAME + '.' + UserInfoTable.REGISTERED + ',' +
                UserInfoTable.BANNED + ',' +
                UserInfoTable.OP + ',' +
                UserInfoTable.SERVER_UUID +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.USER_UUID + "=?";

        return new QueryStatement<List<UserInfo>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<UserInfo> processResults(ResultSet set) throws SQLException {
                List<UserInfo> userInformation = new ArrayList<>();
                while (set.next()) {
                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean op = set.getBoolean(UserInfoTable.OP);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    UUID serverUUID = UUID.fromString(set.getString(UserInfoTable.SERVER_UUID));
                    userInformation.add(new UserInfo(playerUUID, serverUUID, registered, op, banned));
                }
                return userInformation;
            }
        };
    }

    /**
     * Query database for all User information of a specific server.
     *
     * @param serverUUID UUID of the Plan server.
     * @return Map: Player UUID - user information
     */
    public static Query<Map<UUID, UserInfo>> fetchUserInformationOfServer(UUID serverUUID) {
        String sql = SELECT +
                UserInfoTable.REGISTERED + ',' +
                UserInfoTable.BANNED + ',' +
                UserInfoTable.OP + ',' +
                UserInfoTable.USER_UUID + ',' +
                UserInfoTable.SERVER_UUID +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.SERVER_UUID + "=?";
        return new QueryStatement<Map<UUID, UserInfo>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, UserInfo> processResults(ResultSet set) throws SQLException {
                Map<UUID, UserInfo> userInformation = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(UserInfoTable.SERVER_UUID));
                    UUID uuid = UUID.fromString(set.getString(UserInfoTable.USER_UUID));

                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    boolean op = set.getBoolean(UserInfoTable.OP);

                    userInformation.put(uuid, new UserInfo(uuid, serverUUID, registered, op, banned));
                }
                return userInformation;
            }
        };
    }

    public static Query<Map<UUID, Long>> fetchRegisterDates(long after, long before, UUID serverUUID) {
        String sql = SELECT +
                UserInfoTable.USER_UUID + ',' +
                UserInfoTable.REGISTERED +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.SERVER_UUID + "=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.REGISTERED + "<=?";

        return new QueryStatement<Map<UUID, Long>>(sql, 1000) {
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
                            UUID.fromString(set.getString(UserInfoTable.USER_UUID)),
                            set.getLong(UserInfoTable.REGISTERED)
                    );
                }
                return registerDates;
            }
        };
    }

    public static Query<Set<UUID>> uuidsOfOperators() {
        return getUUIDsForBooleanGroup(UserInfoTable.OP, true);
    }

    public static Query<Set<UUID>> getUUIDsForBooleanGroup(String column, boolean value) {
        String sql = SELECT + UserInfoTable.USER_UUID + FROM + UserInfoTable.TABLE_NAME +
                WHERE + column + "=?";
        return new QueryStatement<Set<UUID>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, value);
            }

            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> uuids = new HashSet<>();
                while (set.next()) {
                    uuids.add(UUID.fromString(set.getString(UserInfoTable.USER_UUID)));
                }
                return uuids;
            }
        };
    }

    public static Query<Set<UUID>> uuidsOfNonOperators() {
        return getUUIDsForBooleanGroup(UserInfoTable.OP, false);
    }

    public static Query<Set<UUID>> uuidsOfBanned() {
        return getUUIDsForBooleanGroup(UserInfoTable.BANNED, true);
    }

    public static Query<Set<UUID>> uuidsOfNotBanned() {
        return getUUIDsForBooleanGroup(UserInfoTable.BANNED, false);
    }
}