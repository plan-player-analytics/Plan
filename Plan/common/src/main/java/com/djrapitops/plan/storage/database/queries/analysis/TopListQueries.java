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
package com.djrapitops.plan.storage.database.queries.analysis;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class TopListQueries {

    private TopListQueries() {
        // Static query generation class
    }

    public static Query<Optional<TopListEntry<Long>>> fetchNthTop10PlaytimePlayerOn(ServerUUID serverUUID, int n, long after, long before) {
        String sql = SELECT + UsersTable.USER_NAME + ", " +
                "SUM(" + SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + ") as playtime" +
                FROM + SessionsTable.TABLE_NAME + " s" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + "=s." + SessionsTable.USER_ID +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + SessionsTable.SESSION_START + ">?" +
                AND + SessionsTable.SESSION_END + "<?" +
                GROUP_BY + "name" +
                ORDER_BY + "playtime DESC" +
                LIMIT + "10" +
                OFFSET + "?";

        return new QueryStatement<Optional<TopListEntry<Long>>>(sql, 10) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
                statement.setInt(4, n - 1);
            }

            @Override
            public Optional<TopListEntry<Long>> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(
                            new TopListEntry<>(set.getString(UsersTable.USER_NAME), set.getLong("playtime"))
                    );
                }
                return Optional.empty();
            }
        };

    }

    public static Query<Optional<TopListEntry<Long>>> fetchNthTop10ActivePlaytimePlayerOn(ServerUUID serverUUID, int n, long after, long before) {
        String sql = SELECT + UsersTable.USER_NAME + ", " +
                "SUM(" + SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + '-' + SessionsTable.AFK_TIME + ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME + " s" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + "=s." + SessionsTable.USER_ID +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + SessionsTable.SESSION_START + ">?" +
                AND + SessionsTable.SESSION_END + "<?" +
                GROUP_BY + "name" +
                ORDER_BY + "active_playtime DESC" +
                LIMIT + "10" +
                OFFSET + "?";

        return new QueryStatement<Optional<TopListEntry<Long>>>(sql, 10) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
                statement.setInt(4, n - 1);
            }

            @Override
            public Optional<TopListEntry<Long>> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(
                            new TopListEntry<>(set.getString(UsersTable.USER_NAME), set.getLong("active_playtime"))
                    );
                }
                return Optional.empty();
            }
        };
    }

    public static class TopListEntry<T> {
        private final String playerName;
        private final T value;

        public TopListEntry(String playerName, T value) {
            this.playerName = playerName;
            this.value = value;
        }

        public String getPlayerName() {
            return playerName;
        }

        public T getValue() {
            return value;
        }
    }
}
