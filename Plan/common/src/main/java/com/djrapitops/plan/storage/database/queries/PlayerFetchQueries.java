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
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Static method class for queries that return information related to a single player.
 *
 * @author AuroraLS3
 */
public class PlayerFetchQueries {

    private PlayerFetchQueries() {
        /* static method class */
    }

    /**
     * Query Player's name by player's UUID.
     *
     * @param playerUUID UUID of the player.
     * @return Optional, Name if found.
     */
    public static Query<Optional<String>> playerUserName(UUID playerUUID) {
        String sql = SELECT + UsersTable.USER_NAME +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.USER_UUID + "=?";
        return new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(UsersTable.USER_NAME));
                }
                return Optional.empty();
            }
        };
    }

    /**
     * Check if the player's BaseUser is registered.
     *
     * @param playerUUID UUID of the player.
     * @return True if the player's BaseUser is found
     */
    public static Query<Boolean> isPlayerRegistered(UUID playerUUID) {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.USER_UUID + "=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }
        };
    }

    /**
     * Check if the player's UserInfo is registered.
     *
     * @param playerUUID UUID of the player.
     * @param serverUUID UUID of the Plan server.
     * @return True if the player's UserInfo is found
     */
    public static Query<Boolean> isPlayerRegisteredOnServer(UUID playerUUID, ServerUUID serverUUID) {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, serverUUID.toString());
            }
        };
    }

    public static Query<Optional<Long>> fetchRegisterDate(UUID playerUUID) {
        String sql = SELECT + UsersTable.REGISTERED +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.USER_UUID + "=? LIMIT 1";

        return new QueryStatement<Optional<Long>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<Long> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getLong(UsersTable.REGISTERED));
                }
                return Optional.empty();
            }
        };
    }
}