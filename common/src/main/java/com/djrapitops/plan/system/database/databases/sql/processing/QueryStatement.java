/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.processing;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL query that closes proper elements.
 *
 * @author Rsl1122
 */
public abstract class QueryStatement<T> {

    private final String sql;
    private final int fetchSize;
    private boolean devMode;

    public QueryStatement(String sql) {
        this(sql, 10);
    }

    public QueryStatement(String sql, int fetchSize) {
        this.sql = sql;
        devMode = Settings.DEV_MODE.isTrue();
        this.fetchSize = fetchSize;
    }

    public T executeQuery(PreparedStatement statement) throws SQLException {
        Benchmark.start("SQL: " + sql);
        try {
            statement.setFetchSize(fetchSize);
            prepare(statement);
            try (ResultSet set = statement.executeQuery()) {
                return processResults(set);
            }
        } finally {
            statement.close();
            if (devMode) {
                Log.debug(Benchmark.stopAndFormat("SQL: " + sql));
            }
        }
    }

    public abstract void prepare(PreparedStatement statement) throws SQLException;

    public abstract T processResults(ResultSet set) throws SQLException;

    public String getSql() {
        return sql;
    }
}
