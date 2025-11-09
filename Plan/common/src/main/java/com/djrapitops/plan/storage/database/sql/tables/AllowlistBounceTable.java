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
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Represents plan_allowlist_bounce table.
 *
 * @author AuroraLS3
 */
public class AllowlistBounceTable {

    public static final String TABLE_NAME = "plan_allowlist_bounce";

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String USER_NAME = "name";
    public static final String SERVER_ID = "server_id";
    public static final String TIMES = "times";
    public static final String LAST_BOUNCE = "last_bounce";

    @Language("SQL")
    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            UUID + ',' +
            USER_NAME + ',' +
            SERVER_ID + ',' +
            TIMES + ',' +
            LAST_BOUNCE +
            ") VALUES (?,?," + ServerTable.SELECT_SERVER_ID + ",?,?)";

    @Language("SQL")
    public static final String INCREMENT_TIMES_STATEMENT = "UPDATE " + TABLE_NAME +
            " SET " + TIMES + "=" + TIMES + "+1, " + LAST_BOUNCE + "=?" +
            " WHERE " + UUID + "=?" +
            " AND " + SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

    public static final String UPSERT_MYSQL = INSERT_INTO + TABLE_NAME + " (" + UUID + ',' + USER_NAME + ',' + TIMES + ',' + LAST_BOUNCE + ',' + SERVER_ID + ')' +
            " VALUES (?, ?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE " +
            TIMES + "=" + TIMES + "+VALUES(" + TIMES + ")," +
            LAST_BOUNCE + "=GREATEST(VALUES(" + LAST_BOUNCE + ")," + LAST_BOUNCE + ")";
    public static final String UPSERT_SQLITE = INSERT_INTO + TABLE_NAME + " (" + UUID + ',' + USER_NAME + ',' + TIMES + ',' + LAST_BOUNCE + ',' + SERVER_ID + ')' +
            " VALUES (?, ?, ?, ?, ?)" +
            " ON CONFLICT(uuid, server_id) DO UPDATE SET " +
            TIMES + "=" + TABLE_NAME + '.' + TIMES + "+excluded." + TIMES + "," +
            LAST_BOUNCE + "=MAX(excluded." + LAST_BOUNCE + "," + TABLE_NAME + '.' + LAST_BOUNCE + ")";

    private AllowlistBounceTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(UUID, Sql.varchar(36)).notNull().unique()
                .column(USER_NAME, Sql.varchar(36)).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(TIMES, Sql.INT).notNull().defaultValue("0")
                .column(LAST_BOUNCE, Sql.LONG).notNull()
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }

    public static Query<List<Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(TABLE_NAME)
                .where(ID + '>' + currentId)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, Row::extract);
    }

    public static class Row implements ServerIdentifiable {
        public int id;
        public String uuid;
        public String userName;
        public int serverId;
        public int times;
        public long lastBounce;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.uuid = set.getString(UUID);
            row.userName = set.getString(USER_NAME);
            row.serverId = set.getInt(SERVER_ID);
            row.times = set.getInt(TIMES);
            row.lastBounce = set.getLong(LAST_BOUNCE);
            return row;
        }

        @Override
        public int getServerId() {
            return serverId;
        }

        @Override
        public void setServerId(int id) {
            serverId = id;
        }

        public void upsert(PreparedStatement statement) throws SQLException {
            statement.setString(1, uuid);
            statement.setString(2, userName);
            statement.setInt(3, times);
            statement.setInt(4, serverId);
            statement.setLong(5, lastBounce);
        }
    }
}
