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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.domain.datatransfer.preferences.Preferences;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.CookieMetadata;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.CookieTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.*;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.intellij.lang.annotations.Language;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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

    public static Query<Optional<User>> fetchUser(@Untrusted String username) {
        String sql = SELECT +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME + ',' +
                "GROUP_CONCAT(" + WebPermissionTable.PERMISSION + ",',') as user_permissions" +
                FROM + SecurityTable.TABLE_NAME + " s" +
                INNER_JOIN + WebGroupTable.TABLE_NAME + " g on g." + WebGroupTable.ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gtp on gtp." + WebGroupToPermissionTable.GROUP_ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebPermissionTable.TABLE_NAME + " p on gtp." + WebGroupToPermissionTable.PERMISSION_ID + "=p." + WebPermissionTable.ID +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + SecurityTable.USERNAME + "=?" +
                GROUP_BY +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME
                + LIMIT + "1";
        return db -> db.queryOptional(sql, WebUserQueries::extractUser, username);
    }

    public static Query<Optional<User>> fetchUser(UUID linkedToUUID) {
        String sql = SELECT +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME + ',' +
                "GROUP_CONCAT(" + WebPermissionTable.PERMISSION + ",',') as user_permissions" +
                FROM + SecurityTable.TABLE_NAME + " s" +
                INNER_JOIN + WebGroupTable.TABLE_NAME + " g on g." + WebGroupTable.ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gtp on gtp." + WebGroupToPermissionTable.GROUP_ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebPermissionTable.TABLE_NAME + " p on gtp." + WebGroupToPermissionTable.PERMISSION_ID + "=p." + WebPermissionTable.ID +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + SecurityTable.LINKED_TO + "=?" +
                GROUP_BY +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME
                + LIMIT + "1";
        return db -> db.queryOptional(sql, WebUserQueries::extractUser, linkedToUUID);
    }

    public static Query<List<User>> fetchAllUsers() {
        String sql = SELECT +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME + ',' +
                "GROUP_CONCAT(" + WebPermissionTable.PERMISSION + ",',') as user_permissions" +
                FROM + SecurityTable.TABLE_NAME + " s" +
                INNER_JOIN + WebGroupTable.TABLE_NAME + " g on g." + WebGroupTable.ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gtp on gtp." + WebGroupToPermissionTable.GROUP_ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebPermissionTable.TABLE_NAME + " p on gtp." + WebGroupToPermissionTable.PERMISSION_ID + "=p." + WebPermissionTable.ID +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                GROUP_BY +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME;
        return db -> db.queryList(sql, WebUserQueries::extractUser);
    }

    public static Query<Map<String, CookieMetadata>> fetchActiveCookies() {
        String sql = SELECT +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME + ',' +
                CookieTable.COOKIE + ',' +
                CookieTable.EXPIRES + ',' +
                CookieTable.IP_ADDRESS + ',' +
                "GROUP_CONCAT(" + WebPermissionTable.PERMISSION + ",',') as user_permissions" +
                FROM + CookieTable.TABLE_NAME + " c" +
                INNER_JOIN + SecurityTable.TABLE_NAME + " s on c." + CookieTable.WEB_USERNAME + "=s." + SecurityTable.USERNAME +
                INNER_JOIN + WebGroupTable.TABLE_NAME + " g on g." + WebGroupTable.ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gtp on gtp." + WebGroupToPermissionTable.GROUP_ID + "=s." + SecurityTable.GROUP_ID +
                LEFT_JOIN + WebPermissionTable.TABLE_NAME + " p on gtp." + WebGroupToPermissionTable.PERMISSION_ID + "=p." + WebPermissionTable.ID +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + SecurityTable.LINKED_TO + "=" + UsersTable.USER_UUID +
                WHERE + CookieTable.EXPIRES + ">?" +
                GROUP_BY +
                SecurityTable.USERNAME + ',' +
                UsersTable.USER_NAME + ',' +
                SecurityTable.LINKED_TO + ',' +
                SecurityTable.SALT_PASSWORD_HASH + ',' +
                WebGroupTable.NAME + ',' +
                CookieTable.COOKIE + ',' +
                CookieTable.EXPIRES + ',' +
                CookieTable.IP_ADDRESS;

        return db -> db.queryMap(sql, (set, byCookie) -> byCookie.put(set.getString(CookieTable.COOKIE), extractCookieMetadata(set)),
                System.currentTimeMillis());
    }

    private static CookieMetadata extractCookieMetadata(ResultSet set) throws SQLException {
        return new CookieMetadata(
                extractUser(set),
                set.getLong(CookieTable.EXPIRES),
                set.getString(CookieTable.IP_ADDRESS)
        );
    }

    private static User extractUser(ResultSet set) throws SQLException {
        String username = set.getString(SecurityTable.USERNAME);
        String linkedTo = set.getString(UsersTable.USER_NAME);
        UUID linkedToUUID = linkedTo != null ? UUID.fromString(set.getString(SecurityTable.LINKED_TO)) : null;
        String passwordHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
        String permissionGroup = set.getString(WebGroupTable.NAME);
        String userPermissions = set.getString("user_permissions");
        List<String> permissions = userPermissions != null ? Arrays.stream(userPermissions.split(","))
                .filter(permission -> !permission.isEmpty())
                .collect(Collectors.toList()) : List.of();
        return new User(username, linkedTo != null ? linkedTo : "console", linkedToUUID, passwordHash, permissionGroup, new HashSet<>(permissions));
    }

    public static Query<List<String>> fetchGroupNames() {
        String sql = SELECT + WebGroupTable.NAME + FROM + WebGroupTable.TABLE_NAME;
        return db -> db.queryList(sql, row -> row.getString(WebGroupTable.NAME));
    }

    public static Query<List<String>> fetchGroupPermissions(@Untrusted String group) {
        String sql = SELECT + WebPermissionTable.PERMISSION +
                FROM + WebGroupTable.TABLE_NAME + " g" +
                INNER_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gtp ON g." + WebGroupTable.ID + "=gtp." + WebGroupToPermissionTable.GROUP_ID +
                INNER_JOIN + WebPermissionTable.TABLE_NAME + " p ON p." + WebPermissionTable.ID + "=gtp." + WebGroupToPermissionTable.PERMISSION_ID +
                WHERE + WebGroupTable.NAME + "=?";
        return db -> db.queryList(sql, row -> row.getString(WebPermissionTable.PERMISSION), group);
    }

    public static Query<List<String>> fetchAvailablePermissions() {
        String sql = SELECT + WebPermissionTable.PERMISSION + FROM + WebPermissionTable.TABLE_NAME;
        return db -> db.queryList(sql, row -> row.getString(WebPermissionTable.PERMISSION));
    }

    public static Query<Optional<Integer>> fetchGroupId(@Untrusted String name) {
        return db -> db.queryOptional(WebGroupTable.SELECT_GROUP_ID, row -> row.getInt(WebGroupTable.ID), name);
    }

    public static Query<List<Integer>> fetchPermissionIds(@Untrusted Collection<String> permissions) {
        String sql = SELECT + WebPermissionTable.ID +
                FROM + WebPermissionTable.TABLE_NAME +
                WHERE + WebPermissionTable.PERMISSION + " IN (" + Sql.nParameters(permissions.size()) + ")";
        return db -> {
            if (permissions.isEmpty()) return Collections.emptyList();
            return db.queryList(sql, row -> row.getInt(WebPermissionTable.ID), permissions);
        };
    }

    public static Query<List<String>> fetchAllUsernames() {
        return db -> db.queryList(SELECT + SecurityTable.USERNAME + FROM + SecurityTable.TABLE_NAME, row -> row.getString(SecurityTable.USERNAME));
    }

    public static Query<List<String>> fetchGroupNamesWithPermission(String permission) {
        @Language("SQL")
        String sql = SELECT + WebGroupTable.NAME +
                FROM + WebGroupTable.TABLE_NAME + " g" +
                INNER_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gp ON gp." + WebGroupToPermissionTable.GROUP_ID + "=g." + WebGroupTable.ID +
                INNER_JOIN + WebPermissionTable.TABLE_NAME + " p ON gp." + WebGroupToPermissionTable.PERMISSION_ID + "=p." + WebPermissionTable.ID +
                WHERE + WebPermissionTable.PERMISSION + "=?";
        return db -> db.queryList(sql, row -> row.getString(WebGroupTable.NAME), permission);
    }

    public static Query<Optional<Integer>> fetchPermissionId(String permission) {
        String sql = SELECT + WebPermissionTable.ID + FROM + WebPermissionTable.TABLE_NAME + WHERE + WebPermissionTable.PERMISSION + "=?";
        return db -> db.queryOptional(sql, row -> row.getInt(WebPermissionTable.ID), permission);
    }

    public static Query<List<Integer>> fetchGroupIds(List<String> groups) {
        String sql = SELECT + WebGroupTable.ID +
                FROM + WebGroupTable.TABLE_NAME +
                WHERE + WebGroupTable.NAME + " IN (" + Sql.nParameters(groups.size()) + ')';
        return db -> db.queryList(sql, row -> row.getInt(WebGroupTable.ID), groups);
    }

    public static Query<Map<String, List<String>>> fetchAllGroupPermissions() {
        String sql = SELECT + WebGroupTable.NAME + ',' + WebPermissionTable.PERMISSION +
                FROM + WebGroupTable.TABLE_NAME + " g" +
                INNER_JOIN + WebGroupToPermissionTable.TABLE_NAME + " gtp ON g." + WebGroupTable.ID + "=gtp." + WebGroupToPermissionTable.GROUP_ID +
                INNER_JOIN + WebPermissionTable.TABLE_NAME + " p ON p." + WebPermissionTable.ID + "=gtp." + WebGroupToPermissionTable.PERMISSION_ID;
        return new QueryAllStatement<>(sql, 100) {
            @Override
            public Map<String, List<String>> processResults(ResultSet set) throws SQLException {
                Map<String, List<String>> groupMap = new HashMap<>();
                while (set.next()) {
                    String group = set.getString(WebGroupTable.NAME);
                    String permission = set.getString(WebPermissionTable.PERMISSION);

                    List<String> permissionList = groupMap.computeIfAbsent(group, Lists::create);

                    permissionList.add(permission);
                }
                return groupMap;
            }
        };
    }

    public static Query<Optional<Preferences>> fetchPreferences(@Untrusted WebUser user) {
        return db -> db.queryOptional(WebUserPreferencesTable.SELECT_BY_WEB_USERNAME, WebUserQueries::extractPreferences, user.getUsername());
    }

    private static Preferences extractPreferences(ResultSet set) throws SQLException {
        try {
            String preferences = set.getString(WebUserPreferencesTable.PREFERENCES);
            return new Gson().fromJson(preferences, Preferences.class);
        } catch (JsonSyntaxException jsonChangedIncompatibly) {
            return null;
        }
    }

    public static Query<Map<String, String>> fetchAllPreferences() {
        String sql = SELECT + WebUserPreferencesTable.PREFERENCES + "," + SecurityTable.USERNAME +
                FROM + WebUserPreferencesTable.TABLE_NAME + " p" +
                INNER_JOIN + SecurityTable.TABLE_NAME + " s ON s." + SecurityTable.ID + "=p." + WebUserPreferencesTable.WEB_USER_ID;
        return db -> db.queryMap(sql, (results, to) ->
                to.put(results.getString(SecurityTable.USERNAME), results.getString(WebUserPreferencesTable.PREFERENCES)));
    }
}