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
package com.djrapitops.plan.storage.database.sql.tables.webuser;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.objects.lookup.UserIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Represents plan_web_user_preferences.
 *
 * @author AuroraLS3
 */
public class WebUserPreferencesTable {

    public static final String TABLE_NAME = "plan_web_user_preferences";

    public static final String ID = "id";
    public static final String WEB_USER_ID = "web_user_id";
    public static final String PREFERENCES = "preferences";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" + PREFERENCES + ',' + WEB_USER_ID +
            ") VALUES (?, (" + SecurityTable.SELECT_ID_BY_USERNAME + "))";
    public static final String SELECT_BY_WEB_USERNAME = SELECT + PREFERENCES + FROM + TABLE_NAME +
            WHERE + WEB_USER_ID + "=(" + SecurityTable.SELECT_ID_BY_USERNAME + ")";
    public static final String DELETE_BY_WEB_USERNAME = DELETE_FROM + TABLE_NAME +
            WHERE + WEB_USER_ID + "=(" + SecurityTable.SELECT_ID_BY_USERNAME + ")";

    private WebUserPreferencesTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(PREFERENCES, "TEXT").notNull()
                .column(WEB_USER_ID, Sql.INT)
                .foreignKey(WEB_USER_ID, SecurityTable.TABLE_NAME, SecurityTable.ID)
                .toString();
    }

    public static Query<List<Row>> fetchRows(int currentId, int rowLimit) {
        return db -> db.queryList(Select.all(TABLE_NAME)
                .where(ID + '>' + currentId)
                .orderBy(ID)
                .limit(rowLimit)
                .toString(), Row::extract);
    }

    public static class Row implements UserIdentifiable {
        public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" + PREFERENCES + ',' + WEB_USER_ID + ") VALUES (?, ?)";

        public int id;
        public int webUserId;
        public String preferences;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.webUserId = set.getInt(WEB_USER_ID);
            row.preferences = set.getString(PREFERENCES);
            return row;
        }

        @Override
        public int getUserId() {
            return webUserId;
        }

        @Override
        public void setUserId(int webUserId) {
            this.webUserId = webUserId;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setString(1, preferences);
            statement.setInt(2, webUserId);
        }
    }

}
