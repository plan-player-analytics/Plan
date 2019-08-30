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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.parsing.Sql.SELECT;

/**
 * Static method class for H2 Schema related queries.
 *
 * @author Rsl1122
 */
public class H2SchemaQueries {

    private H2SchemaQueries() {
        /* Static method class */
    }

    public static Query<Boolean> doesTableExist(String tableName) {
        String sql = SELECT + "COUNT(1) as c FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
            }
        };
    }

    public static Query<Boolean> doesColumnExist(String tableName, String columnName) {
        String sql = SELECT + "COUNT(1) as c FROM INFORMATION_SCHEMA.COLUMNS" +
                " WHERE TABLE_NAME=? AND COLUMN_NAME=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
                statement.setString(2, columnName);
            }
        };
    }
}