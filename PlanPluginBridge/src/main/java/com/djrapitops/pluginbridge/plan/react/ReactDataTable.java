package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
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
public class ReactDataTable extends Table {

    public static final String TABLE_NAME = "plan_react_data";

    public ReactDataTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(TABLE_NAME)
                .primaryKey(usingMySQL, Col.ID)
                .column(Col.DATE, Sql.LONG)
                .column(Col.SAMPLED_TYPE, Sql.varchar(30))
                .column(Col.MINUTE_AVERAGE, Sql.DOUBLE)
                .primaryKeyIDColumn(usingMySQL, Col.ID)
                .toString());
    }

    public void clean() throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + Col.DATE + "<?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, System.currentTimeMillis() - TimeAmount.MONTH.ms());
            }
        });
    }

    public void addData(ReactValue value) throws SQLException {
        String sql = "INSERT INTO " + tableName + " (" +
                Col.SAMPLED_TYPE + ", " +
                Col.DATE + ", " +
                Col.MINUTE_AVERAGE +
                ") VALUES (?, ?, ?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, value.getType().name());
                statement.setLong(2, value.getDate());
                statement.setDouble(3, value.getDataValue());
            }
        });
    }

    public Map<SampledType, List<ReactValue>> getAllData() throws SQLException {
        String sql = Select.all(tableName).toString();

        return query(new QueryAllStatement<Map<SampledType, List<ReactValue>>>(sql, 50000) {
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
        });
    }

    public enum Col implements Column {
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