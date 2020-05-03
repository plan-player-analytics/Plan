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
import com.djrapitops.plan.storage.database.sql.tables.SecurityTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for web user objects.
 *
 * @author Rsl1122
 */
public class WebUserQueries {

    private WebUserQueries() {
        /* Static method class */
    }

    /**
     * Query database for all Plan WebUsers.
     *
     * @return List of Plan WebUsers.
     */
    @Deprecated
    public static Query<List<WebUser>> fetchAllPlanWebUsers() {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME + ORDER_BY + SecurityTable.PERMISSION_LEVEL + " ASC";

        return new QueryAllStatement<List<WebUser>>(sql, 5000) {
            @Override
            public List<WebUser> processResults(ResultSet set) throws SQLException {
                List<WebUser> list = new ArrayList<>();
                while (set.next()) {
                    String user = set.getString(SecurityTable.USERNAME);
                    String saltedPassHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    WebUser info = new WebUser(user, saltedPassHash, permissionLevel);
                    list.add(info);
                }
                return list;
            }
        };
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
                    List<String> permissions = WebUser.getPermissionsForLevel(set.getInt(SecurityTable.PERMISSION_LEVEL));
                    return Optional.of(new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissions));
                }
                return Optional.empty();
            }
        };
    }

    @Deprecated
    public static Query<Optional<WebUser>> fetchWebUser(String called) {
        String sql = SELECT + '*' + FROM + SecurityTable.TABLE_NAME +
                WHERE + SecurityTable.USERNAME + "=? LIMIT 1";
        return new QueryStatement<Optional<WebUser>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, called);
            }

            @Override
            public Optional<WebUser> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String saltedPassHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    return Optional.of(new WebUser(called, saltedPassHash, permissionLevel));
                }
                return Optional.empty();
            }
        };
    }
}