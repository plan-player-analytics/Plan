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
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class MetadataTable {

    public static final String TABLE_NAME = "plan_database_metadata";

    public static final String KEY = "id";
    public static final String VALUE = "uuid";
    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            KEY + ',' +
            VALUE +
            ") VALUES (?, ?)";

    public static final String UPDATE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " + VALUE + "=?" +
            WHERE + KEY + "=?";

    public static final String SELECT_VALUE_OF_KEY = SELECT + VALUE + FROM + TABLE_NAME + WHERE + KEY + "=?";

    private MetadataTable() {
        /* Static information class */
    }

    public static Executable insertValue(String key, String value) {
        return new ExecStatement(MetadataTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, key);
                statement.setString(2, value);
            }
        };
    }

    public static Query<String> getValueOrNull(String key) {
        return new QueryStatement<String>(MetadataTable.SELECT_VALUE_OF_KEY) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, key);
            }

            @Override
            public String processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getString(MetadataTable.VALUE) : null;
            }
        };
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(KEY, Sql.varchar(36)).notNull()
                .column(VALUE, Sql.varchar(75)).notNull()
                .toString();
    }
}
