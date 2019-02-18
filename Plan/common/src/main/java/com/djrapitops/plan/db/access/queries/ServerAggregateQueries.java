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
package com.djrapitops.plan.db.access.queries;

import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Static method class for queries that count how many entries of particular kinds there are for a server.
 *
 * @author Rsl1122
 */
public class ServerAggregateQueries {

    private ServerAggregateQueries() {
        /* Static method class */
    }

    /**
     * Count how many users are in the Plan database.
     *
     * @return Count of base users, all users in a network after Plan installation.
     */
    public static Query<Integer> baseUserCount() {
        String sql = "SELECT COUNT(1) as c FROM " + UsersTable.TABLE_NAME;
        return new QueryAllStatement<Integer>(sql) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 0;
            }
        };
    }

    /**
     * Count how many users are on a server in the network.
     *
     * @param serverUUID ServerUUID of the Plan server.
     * @return Count of users registered to that server after Plan installation.
     */
    public static Query<Integer> serverUserCount(UUID serverUUID) {
        String sql = "SELECT COUNT(1) as c FROM " + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.SERVER_UUID + "=?";
        return new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 0;
            }
        };
    }

    /**
     * Count how many users are on each server in the network.
     * <p>
     * Please note that counts can overlap as one user can join multiple servers.
     * Use {@link ServerAggregateQueries#baseUserCount()} if you want to count total number of users.
     *
     * @return Map: Server UUID - Count of users registered to that server
     */
    public static Query<Map<UUID, Integer>> serverUserCounts() {
        String sql = "SELECT COUNT(1) as c, " + UserInfoTable.SERVER_UUID + FROM + UserInfoTable.TABLE_NAME +
                GROUP_BY + UserInfoTable.SERVER_UUID;
        return new QueryAllStatement<Map<UUID, Integer>>(sql, 100) {
            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> ofServer = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(UserInfoTable.SERVER_UUID));
                    int count = set.getInt("c");
                    ofServer.put(serverUUID, count);
                }
                return ofServer;
            }
        };
    }

    /**
     * Count how many times commands have been used on a server.
     *
     * @param serverUUID Server UUID of the Plan server.
     * @return Map: Lowercase used command - Count of use times.
     */
    public static Query<Map<String, Integer>> commandUsageCounts(UUID serverUUID) {
        String sql = SELECT + CommandUseTable.COMMAND + ", " + CommandUseTable.TIMES_USED + FROM + CommandUseTable.TABLE_NAME +
                WHERE + CommandUseTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID;

        return new QueryStatement<Map<String, Integer>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> commandUse = new HashMap<>();
                while (set.next()) {
                    String cmd = set.getString(CommandUseTable.COMMAND).toLowerCase();
                    int amountUsed = set.getInt(CommandUseTable.TIMES_USED);
                    commandUse.put(cmd, amountUsed);
                }
                return commandUse;
            }
        };
    }

    public static Query<Map<String, Integer>> networkGeolocationCounts() {
        String subQuery1 = SELECT +
                GeoInfoTable.USER_UUID + ", " +
                GeoInfoTable.GEOLOCATION + ", " +
                GeoInfoTable.LAST_USED +
                FROM + GeoInfoTable.TABLE_NAME;
        String subQuery2 = SELECT +
                GeoInfoTable.USER_UUID + ", " +
                "MAX(" + GeoInfoTable.LAST_USED + ") as m" +
                FROM + GeoInfoTable.TABLE_NAME +
                GROUP_BY + GeoInfoTable.USER_UUID;
        String sql = SELECT + GeoInfoTable.GEOLOCATION + ", COUNT(1) as c FROM (" +
                "(" + subQuery1 + ") AS q1" +
                " INNER JOIN (" + subQuery2 + ") AS q2 ON q1.uuid = q2.uuid)" +
                WHERE + GeoInfoTable.LAST_USED + "=m" +
                GROUP_BY + GeoInfoTable.GEOLOCATION;

        return new QueryAllStatement<Map<String, Integer>>(sql) {
            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> geolocationCounts = new HashMap<>();
                while (set.next()) {
                    geolocationCounts.put(set.getString(GeoInfoTable.GEOLOCATION), set.getInt("c"));
                }
                return geolocationCounts;
            }
        };
    }
}
