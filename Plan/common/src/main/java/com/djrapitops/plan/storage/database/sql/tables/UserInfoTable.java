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
package com.djrapitops.plan.storage.database.sql.tables;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.UserIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.UserInfoOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Table information about 'plan_user_info'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link UserInfoOptimizationPatch}
 * {@link com.djrapitops.plan.storage.database.transactions.patches.RegisterDateMinimizationPatch}
 *
 * @author AuroraLS3
 */
public class UserInfoTable {

    public static final String TABLE_NAME = "plan_user_info";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String SERVER_ID = "server_id";
    public static final String REGISTERED = "registered";
    public static final String OP = "opped";
    public static final String BANNED = "banned";
    /**
     * @deprecated Join address is now stored in {@link JoinAddressTable}, this column may become unreliable in the future.
     */
    @Deprecated(since = "5.4 build 1722")
    public static final String JOIN_ADDRESS = "join_address";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
            USER_ID + ',' +
            REGISTERED + ',' +
            SERVER_ID + ',' +
            BANNED + ',' +
            JOIN_ADDRESS + ',' +
            OP +
            ") VALUES (" + UsersTable.SELECT_USER_ID + ", ?, " + ServerTable.SELECT_SERVER_ID + ", ?, ?, ?)";

    private UserInfoTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(JOIN_ADDRESS, Sql.varchar(JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH))
                .column(REGISTERED, Sql.LONG).notNull()
                .column(OP, Sql.BOOL).notNull().defaultValue(false)
                .column(BANNED, Sql.BOOL).notNull().defaultValue(false)
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }

    public static class Row implements UserIdentifiable, ServerIdentifiable {
        public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, USER_ID, SERVER_ID, JOIN_ADDRESS, REGISTERED, OP, BANNED);

        private int id;
        private int userId;
        private int serverId;
        private String joinAddress;
        private long registered;
        private boolean op;
        private boolean banned;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.userId = set.getInt(USER_ID);
            row.serverId = set.getInt(SERVER_ID);
            row.joinAddress = set.getString(JOIN_ADDRESS);
            row.registered = set.getLong(REGISTERED);
            row.op = set.getBoolean(OP);
            row.banned = set.getBoolean(BANNED);
            return row;
        }

        public int getId() {
            return id;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setInt(1, userId);
            statement.setInt(2, serverId);
            statement.setString(3, joinAddress);
            statement.setLong(4, registered);
            statement.setBoolean(5, op);
            statement.setBoolean(6, banned);
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public void setUserId(int userId) {
            this.userId = userId;
        }

        @Override
        public int getServerId() {
            return serverId;
        }

        @Override
        public void setServerId(int serverId) {
            this.serverId = serverId;
        }
    }

}
