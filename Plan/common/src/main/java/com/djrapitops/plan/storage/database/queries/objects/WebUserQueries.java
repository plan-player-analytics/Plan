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

import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.CookieTable;
import com.djrapitops.plan.storage.database.sql.tables.SecurityTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for web user objects.
 *
 * @author AuroraLS3
 */
public class WebUserQueries {

    private WebUserQueries() {
        /* Static method class */
    }

    public static Query<Optional<User>> fetchUser(String username) {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + SecurityTable.USERNAME + "=? LIMIT 1";
        return new QueryStatement<Optional<User>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, username);
            }

            @Override
            public Optional<User> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String linkedTo = set.getString(UsersTable.USER_NAME);
                    UUID linkedToUUID = linkedTo != null ? UUID.fromString(set.getString(SecurityTable.LINKED_TO)) : null;
                    String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    List<String> permissions = WebUser.getPermissionsForLevel(permissionLevel);
                    return Optional.of(new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionLevel, permissions));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<User>> fetchUserLinkedTo(String playerName) {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + UsersTable.USER_NAME + "=? LIMIT 1";
        return new QueryStatement<Optional<User>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerName);
            }

            @Override
            public Optional<User> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String linkedTo = set.getString(UsersTable.USER_NAME);
                    UUID linkedToUUID = linkedTo != null ? UUID.fromString(set.getString(SecurityTable.LINKED_TO)) : null;
                    String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    List<String> permissions = WebUser.getPermissionsForLevel(permissionLevel);
                    return Optional.of(new User(playerName, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionLevel, permissions));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<User>> fetchUser(UUID linkedToUUID) {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + SecurityTable.LINKED_TO + "=? LIMIT 1";
        return new QueryStatement<Optional<User>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                if (linkedToUUID == null) {
                    statement.setNull(1, Types.VARCHAR);
                } else {
                    statement.setString(1, linkedToUUID.toString());
                }
            }

            @Override
            public Optional<User> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String username = set.getString(SecurityTable.USERNAME);
                    String linkedTo = set.getString(UsersTable.USER_NAME);
                    String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    List<String> permissions = WebUser.getPermissionsForLevel(permissionLevel);
                    return Optional.of(new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionLevel, permissions));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<List<User>> fetchAllUsers() {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID;
        return new QueryAllStatement<List<User>>(sql) {

            @Override
            public List<User> processResults(ResultSet set) throws SQLException {
                List<User> users = new ArrayList<>();
                while (set.next()) {
                    String username = set.getString(SecurityTable.USERNAME);
                    String linkedTo = set.getString(UsersTable.USER_NAME);
                    UUID linkedToUUID = linkedTo != null ? UUID.fromString(set.getString(SecurityTable.LINKED_TO)) : null;
                    String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    List<String> permissions = WebUser.getPermissionsForLevel(permissionLevel);
                    users.add(new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionLevel, permissions));
                }
                return users;
            }
        };
    }

    public static Query<List<User>> matchUsers(String partOfUsername) {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + "LOWER(" + SecurityTable.USERNAME + ") LIKE LOWER(?)";
        return new QueryStatement<List<User>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, '%' + partOfUsername + '%');
            }

            @Override
            public List<User> processResults(ResultSet set) throws SQLException {
                List<User> users = new ArrayList<>();
                while (set.next()) {
                    String username = set.getString(SecurityTable.USERNAME);
                    String linkedTo = set.getString(UsersTable.USER_NAME);
                    UUID linkedToUUID = linkedTo != null ? UUID.fromString(set.getString(SecurityTable.LINKED_TO)) : null;
                    String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    List<String> permissions = WebUser.getPermissionsForLevel(permissionLevel);
                    users.add(new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionLevel, permissions));
                }
                return users;
            }
        };
    }

    public static Query<Map<String, User>> fetchActiveCookies() {
        String sql = SELECT + '*' + FROM + CookieTable.TABLE_NAME +
                INNER_JOIN + SecurityTable.TABLE_NAME + " on " + CookieTable.TABLE_NAME + '.' + CookieTable.WEB_USERNAME + '=' + SecurityTable.TABLE_NAME + '.' + SecurityTable.USERNAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + CookieTable.EXPIRES + ">?";

        return new QueryStatement<Map<String, User>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, System.currentTimeMillis());
            }

            @Override
            public Map<String, User> processResults(ResultSet set) throws SQLException {
                Map<String, User> usersByCookie = new HashMap<>();
                while (set.next()) {
                    String cookie = set.getString(CookieTable.COOKIE);
                    User user = extractUser(set);
                    usersByCookie.put(cookie, user);
                }
                return usersByCookie;
            }
        };
    }

    private static User extractUser(ResultSet set) throws SQLException {
        String username = set.getString(SecurityTable.USERNAME);
        String linkedTo = set.getString(UsersTable.USER_NAME);
        UUID linkedToUUID = linkedTo != null ? UUID.fromString(set.getString(SecurityTable.LINKED_TO)) : null;
        String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
        int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
        List<String> permissions = WebUser.getPermissionsForLevel(permissionLevel);
        return new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionLevel, permissions);
    }

    public static Query<Map<String, Long>> getCookieExpiryTimes() {
        return new QueryAllStatement<Map<String, Long>>(SELECT + CookieTable.COOKIE + ',' + CookieTable.EXPIRES + FROM + CookieTable.TABLE_NAME) {
            @Override
            public Map<String, Long> processResults(ResultSet set) throws SQLException {
                HashMap<String, Long> expiryTimes = new HashMap<>();
                while (set.next()) expiryTimes.put(set.getString(CookieTable.COOKIE), set.getLong(CookieTable.EXPIRES));
                return expiryTimes;
            }
        };
    }
}