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

import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link BaseUser} objects.
 *
 * @author AuroraLS3
 */
public class BaseUserQueries {

    private BaseUserQueries() {
        /* Static method class */
    }

    /**
     * Query database for common user information.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link UserInfo} which is available per server.
     *
     * @return Map: Player UUID - BaseUser
     */
    public static Query<Collection<BaseUser>> fetchAllBaseUsers() {
        String sql = Select.all(UsersTable.TABLE_NAME).toString();

        return new QueryAllStatement<Collection<BaseUser>>(sql, 20000) {
            @Override
            public Collection<BaseUser> processResults(ResultSet set) throws SQLException {
                return extractBaseUsers(set);
            }
        };
    }

    private static Collection<BaseUser> extractBaseUsers(ResultSet set) throws SQLException {
        Collection<BaseUser> users = new HashSet<>();
        while (set.next()) {
            UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
            String name = set.getString(UsersTable.USER_NAME);
            long registered = set.getLong(UsersTable.REGISTERED);
            int kicked = set.getInt(UsersTable.TIMES_KICKED);

            users.add(new BaseUser(playerUUID, name, registered, kicked));
        }
        return users;
    }

    /**
     * Query database for common user information of a player.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link UserInfo} which is available per server.
     *
     * @param playerUUID UUID of the Player.
     * @return Optional: BaseUser if found, empty if not.
     */
    public static Query<Optional<BaseUser>> fetchBaseUserOfPlayer(UUID playerUUID) {
        String sql = Select.all(UsersTable.TABLE_NAME).where(UsersTable.USER_UUID + "=?").toString();

        return new QueryStatement<Optional<BaseUser>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<BaseUser> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    String name = set.getString(UsersTable.USER_NAME);
                    long registered = set.getLong(UsersTable.REGISTERED);
                    int kicked = set.getInt(UsersTable.TIMES_KICKED);

                    return Optional.of(new BaseUser(playerUUID, name, registered, kicked));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Set<Integer>> userIdsOfRegisteredBetween(long after, long before) {
        String sql = SELECT + DISTINCT + UsersTable.ID +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.REGISTERED + ">=?" +
                AND + UsersTable.REGISTERED + "<=?";
        return new QueryStatement<Set<Integer>>(sql) {
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

    public static Query<Optional<Long>> minimumRegisterDate() {
        String sql = SELECT + min(UsersTable.REGISTERED) + " as min" +
                FROM + UsersTable.TABLE_NAME;
        return new QueryAllStatement<Optional<Long>>(sql) {
            @Override
            public Optional<Long> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    long min = set.getLong("min");
                    if (!set.wasNull()) return Optional.of(min);
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Integer> fetchUserId(UUID playerUUID) {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.ID)
                .where(UsersTable.USER_UUID + "=?")
                .toString();

        return new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt(UsersTable.ID) : -1;
            }
        };
    }
}