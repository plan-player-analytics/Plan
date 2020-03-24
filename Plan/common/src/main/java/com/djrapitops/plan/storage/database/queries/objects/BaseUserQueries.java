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
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link BaseUser} objects.
 *
 * @author Rsl1122
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

    /**
     * Query database for common user information for players that have played on a specific server.
     * <p>
     * Only one {@link BaseUser} per player exists unlike {@link UserInfo} which is available per server.
     * <p>
     * This will fetch BaseUsers for which UserInfo object also exists on the server.
     *
     * @param serverUUID UUID of the Plan server.
     * @return Collection: BaseUsers
     */
    public static Query<Collection<BaseUser>> fetchServerBaseUsers(UUID serverUUID) {
        String sql = SELECT +
                UsersTable.TABLE_NAME + '.' + UsersTable.USER_UUID + ',' +
                UsersTable.USER_NAME + ',' +
                UsersTable.TABLE_NAME + '.' + UsersTable.REGISTERED + ',' +
                UsersTable.TIMES_KICKED +
                FROM + UsersTable.TABLE_NAME +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " on " +
                UsersTable.TABLE_NAME + '.' + UsersTable.USER_UUID + "=" + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.USER_UUID +
                WHERE + UserInfoTable.SERVER_UUID + "=?";
        return new QueryStatement<Collection<BaseUser>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Collection<BaseUser> processResults(ResultSet set) throws SQLException {
                return extractBaseUsers(set);
            }
        };
    }

    public static Query<Set<UUID>> uuidsOfRegisteredBetween(long after, long before) {
        String sql = SELECT + DISTINCT + UsersTable.USER_UUID +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.REGISTERED + ">=?" +
                AND + UsersTable.REGISTERED + "<=?";
        return new QueryStatement<Set<UUID>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, after);
                statement.setLong(2, before);
            }

            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> uuids = new HashSet<>();
                while (set.next()) {
                    uuids.add(UUID.fromString(set.getString(UsersTable.USER_UUID)));
                }
                return uuids;
            }
        };
    }

    public static Query<Long> minimumRegisterDate() {
        String sql = SELECT + "MIN(" + UsersTable.REGISTERED + ") as min" +
                FROM + UsersTable.TABLE_NAME;
        return new QueryAllStatement<Long>(sql) {
            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("min") : -1;
            }
        };
    }

}