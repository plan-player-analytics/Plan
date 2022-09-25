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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Static method class for queries that count how many entries of particular kinds there are for a server.
 *
 * @author AuroraLS3
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
        String sql = SELECT + "COUNT(1) as c FROM " + UsersTable.TABLE_NAME;
        return new QueryAllStatement<>(sql) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 0;
            }
        };
    }

    /**
     * Count how many users are on a server in the network.
     *
     * @param serverUUID Server UUID of the Plan server.
     * @return Count of users registered to that server after Plan installation.
     */
    public static Query<Integer> serverUserCount(ServerUUID serverUUID) {
        String sql = SELECT + "COUNT(1) as c FROM " + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;
        return new QueryStatement<>(sql) {
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
    public static Query<Map<ServerUUID, Integer>> serverUserCounts() {
        String sql = SELECT + "COUNT(1) as c, " + ServerTable.SERVER_UUID +
                FROM + UserInfoTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.ID + '=' + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.SERVER_ID +
                GROUP_BY + ServerTable.SERVER_UUID;

        return new QueryAllStatement<>(sql, 100) {
            @Override
            public Map<ServerUUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, Integer> ofServer = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    int count = set.getInt("c");
                    ofServer.put(serverUUID, count);
                }
                return ofServer;
            }
        };
    }

}
