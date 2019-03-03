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
package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plugin.api.TimeAmount;
import com.volmit.react.api.SampledType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Database Table in charge of storing data from React.
 *
 * @author Rsl1122
 */
public class ReactDataTable {

    private static final String TABLE_NAME = "plan_react_data";

    public void createTable() throws DBInitException {
//        createTable(TableSqlParser.createTable(TABLE_NAME)
//                .primaryKey(supportsMySQLQueries, Col.ID)
//                .column(Col.DATE, Sql.LONG)
//                .column(Col.SAMPLED_TYPE, Sql.varchar(30))
//                .column(Col.MINUTE_AVERAGE, Sql.DOUBLE)
//                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
//                .toString());
    }

    public Executable clean() {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + Col.DATE + "<?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, System.currentTimeMillis() - TimeAmount.MONTH.toMillis(1L));
            }
        };
    }

    public Executable addData(ReactValue value) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                Col.SAMPLED_TYPE + ", " +
                Col.DATE + ", " +
                Col.MINUTE_AVERAGE +
                ") VALUES (?, ?, ?)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, value.getType().name());
                statement.setLong(2, value.getDate());
                statement.setDouble(3, value.getDataValue());
            }
        };
    }

    public Query<Map<SampledType, List<ReactValue>>> getAllData() {
        String sql = Select.all(TABLE_NAME).toString();

        return new QueryAllStatement<Map<SampledType, List<ReactValue>>>(sql, 50000) {
            @Override
            public Map<SampledType, List<ReactValue>> processResults(ResultSet set) throws SQLException {
                Map<SampledType, List<ReactValue>> results = new EnumMap<>(SampledType.class);
                while (set.next()) {
                    try {
                        SampledType type = SampledType.valueOf(set.getString(Col.SAMPLED_TYPE.get()));
                        long date = set.getLong(Col.DATE.get());
                        double average = set.getDouble(Col.MINUTE_AVERAGE.get());

                        ReactValue value = new ReactValue(type, date, average);

                        List<ReactValue> values = results.getOrDefault(type, new ArrayList<>());
                        values.add(value);
                        results.put(type, values);
                    } catch (NoSuchFieldError ignore) {
                        /* Ignored, field has been removed and is no longer supported */
                    }
                }
                return results;
            }
        };
    }

    public enum Col {
        ID("id"),
        SAMPLED_TYPE("sampled_type"),
        DATE("date"),
        MINUTE_AVERAGE("minute_average");

        private final String columnName;

        Col(String columnName) {
            this.columnName = columnName;
        }

        public String get() {
            return columnName;
        }

        @Override
        public String toString() {
            return columnName;
        }
    }
}