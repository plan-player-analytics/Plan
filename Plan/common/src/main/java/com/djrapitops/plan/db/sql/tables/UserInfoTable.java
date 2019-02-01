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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.UserInfoOptimizationPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.db.sql.parsing.Update;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing server specific player data.
 * <p>
 * Table Name: plan_user_info
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link UserInfoOptimizationPatch}
 *
 * @author Rsl1122
 */
public class UserInfoTable extends Table {

    public static final String TABLE_NAME = "plan_user_info";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String REGISTERED = "registered";
    public static final String OP = "opped";
    public static final String BANNED = "banned";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_UUID + ", " +
            REGISTERED + ", " +
            SERVER_UUID + ", " +
            BANNED + ", " +
            OP +
            ") VALUES (?, ?, ?, ?, ?)";

    public UserInfoTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(REGISTERED, Sql.LONG).notNull()
                .column(OP, Sql.BOOL).notNull().defaultValue(false)
                .column(BANNED, Sql.BOOL).notNull().defaultValue(false)
                .toString();
    }

    public void registerUserInfo(UUID uuid, long registered) {
        execute(new ExecStatement(INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setLong(2, registered);
                statement.setString(3, getServerUUID().toString());
                statement.setBoolean(4, false);
                statement.setBoolean(5, false);
            }
        });
    }

    public boolean isRegistered(UUID uuid, UUID serverUUID) {
        String sql = Select.from(TABLE_NAME, "COUNT(" + USER_UUID + ") as c")
                .where(USER_UUID + "=?")
                .and(SERVER_UUID + "=?")
                .toString();

        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next() && set.getInt("c") >= 1;
            }
        });
    }

    public boolean isRegistered(UUID uuid) {
        return isRegistered(uuid, getServerUUID());
    }

    public void updateOpStatus(UUID uuid, boolean op) {
        String sql = Update.values(TABLE_NAME, OP)
                .where(USER_UUID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, op);
                statement.setString(2, uuid.toString());
            }
        });
    }

    public void updateBanStatus(UUID uuid, boolean banned) {
        String sql = Update.values(TABLE_NAME, BANNED)
                .where(USER_UUID + "=?")
                .toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, banned);
                statement.setString(2, uuid.toString());
            }
        });
    }

    public List<UserInfo> getServerUserInfo(UUID serverUUID) {
        String sql = "SELECT " +
                TABLE_NAME + "." + REGISTERED + ", " +
                BANNED + ", " +
                OP + ", " +
                TABLE_NAME + "." + USER_UUID +
                " FROM " + TABLE_NAME +
                " WHERE " + SERVER_UUID + "=?";

        return query(new QueryStatement<List<UserInfo>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<UserInfo> processResults(ResultSet set) throws SQLException {
                List<UserInfo> userInfo = new ArrayList<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(USER_UUID));
                    long registered = set.getLong(REGISTERED);
                    boolean op = set.getBoolean(OP);
                    boolean banned = set.getBoolean(BANNED);

                    UserInfo info = new UserInfo(uuid, serverUUID, registered, op, banned);
                    if (!userInfo.contains(info)) {
                        userInfo.add(info);
                    }
                }
                return userInfo;
            }
        });
    }

    /**
     * Used for getting info of all users on THIS server.
     *
     * @return List of UserInfo objects.
     */
    public List<UserInfo> getServerUserInfo() {
        return getServerUserInfo(getServerUUID());
    }

    public boolean isRegisteredOnThisServer(UUID player) {
        return isRegistered(player, getServerUUID());
    }

    public Set<UUID> getSavedUUIDs(UUID serverUUID) {
        String sql = "SELECT " + USER_UUID + " FROM " + TABLE_NAME + " WHERE " + SERVER_UUID + "=?";

        return query(new QueryStatement<Set<UUID>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> uuids = new HashSet<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(USER_UUID));
                    uuids.add(uuid);
                }
                return uuids;
            }
        });
    }
}
