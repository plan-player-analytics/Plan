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
package com.djrapitops.plan.storage.database.queries.schema;

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Static method class for SQLite Schema related queries.
 *
 * @author AuroraLS3
 */
public class SQLiteSchemaQueries {

    private SQLiteSchemaQueries() {
        /* Static method class */
    }

    public static Query<Boolean> doesTableExist(String tableName) {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + "sqlite_master" + WHERE + "tbl_name=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
            }
        };
    }

    public static Query<Boolean> doesColumnExist(String tableName, String columnName) {
        return new QueryAllStatement<Boolean>("PRAGMA table_info(" + tableName + ')') {
            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    if (columnName.equals(set.getString("name"))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}