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
import com.djrapitops.plan.storage.database.queries.objects.lookup.UserIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.GeoInfoLastUsedPatch;
import com.djrapitops.plan.storage.database.transactions.patches.GeoInfoOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_ips'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link GeoInfoLastUsedPatch}
 * {@link GeoInfoOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class GeoInfoTable {

    public static final String TABLE_NAME = "plan_geolocations";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String GEOLOCATION = "geolocation";
    public static final String LAST_USED = "last_used";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " ("
            + USER_ID + ','
            + GEOLOCATION + ','
            + LAST_USED
            + ") VALUES (" + UsersTable.SELECT_USER_ID + ", ?, ?)";

    public static final String UPDATE_STATEMENT = UPDATE + TABLE_NAME + SET +
            LAST_USED + "=?" +
            WHERE + USER_ID + "=" + UsersTable.SELECT_USER_ID +
            AND + GEOLOCATION + "=?";

    public static final String UPSERT_STATEMENT_MYSQL = INSERT_INTO + TABLE_NAME + " (" + USER_ID + ", " + GEOLOCATION + ", " + LAST_USED + ") " +
            "VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            LAST_USED + " = GREATEST(VALUES(" + LAST_USED + "), " + LAST_USED + ");";
    public static final String UPSERT_STATEMENT_SQLITE = INSERT_INTO + TABLE_NAME + " (" + USER_ID + ", " + GEOLOCATION + ", " + LAST_USED + ") " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT(" + USER_ID + ',' + GEOLOCATION + ") DO UPDATE SET " +
            LAST_USED + " = MAX(excluded." + LAST_USED + ", " + TABLE_NAME + "." + LAST_USED + ");";

    private GeoInfoTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(GEOLOCATION, Sql.varchar(50)).notNull()
                .column(LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .toString();
    }

    public static Query<List<Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(TABLE_NAME)
                .where(ID + '>' + currentId)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, Row::extract);
    }

    public static class Row implements UserIdentifiable {
        public int id;
        public int userId;
        public String geolocation;
        public long lastUsed;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.userId = set.getInt(USER_ID);
            row.geolocation = set.getString(GEOLOCATION);
            row.lastUsed = set.getLong(LAST_USED);
            return row;
        }

        public void upsert(PreparedStatement statement) throws SQLException {
            statement.setInt(1, userId);
            statement.setString(2, geolocation);
            statement.setLong(3, lastUsed);
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
}
