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
import com.djrapitops.plan.storage.database.transactions.patches.PingOptimizationPatch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Table information about 'plan_ping'.
 * <p>
 * Patches related to this table:
 * {@link PingOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class PingTable {

    public static final String TABLE_NAME = "plan_ping";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String SERVER_ID = "server_id";
    public static final String DATE = "date";
    public static final String MAX_PING = "max_ping";
    public static final String AVG_PING = "avg_ping";
    public static final String MIN_PING = "min_ping";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_ID + ',' +
            SERVER_ID + ',' +
            DATE + ',' +
            MIN_PING + ',' +
            MAX_PING + ',' +
            AVG_PING +
            ") VALUES (" + UsersTable.SELECT_USER_ID + ',' + ServerTable.SELECT_SERVER_ID + ", ?, ?, ?, ?)";

    private PingTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(MAX_PING, Sql.INT).notNull()
                .column(MIN_PING, Sql.INT).notNull()
                .column(AVG_PING, Sql.DOUBLE).notNull()
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }

    public static class Row implements UserIdentifiable, ServerIdentifiable {
        public static String INSERT_STATEMENT = Insert.values(TABLE_NAME, USER_ID, SERVER_ID, DATE, MAX_PING, MIN_PING, AVG_PING);

        public int id;
        public int userId;
        public int serverId;
        public long date;
        public int maxPing;
        public int minPing;
        public double avgPing;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.userId = set.getInt(USER_ID);
            row.serverId = set.getInt(SERVER_ID);
            row.date = set.getLong(DATE);
            row.maxPing = set.getInt(MAX_PING);
            row.minPing = set.getInt(MIN_PING);
            row.avgPing = set.getDouble(AVG_PING);
            return row;
        }

        public static void insert(PreparedStatement statement, Row row) throws SQLException {
            statement.setInt(1, row.userId);
            statement.setInt(2, row.serverId);
            statement.setLong(3, row.date);
            statement.setInt(4, row.maxPing);
            statement.setInt(5, row.minPing);
            statement.setDouble(6, row.avgPing);
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
