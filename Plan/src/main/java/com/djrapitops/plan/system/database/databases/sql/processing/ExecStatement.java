/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.processing;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQL executing statement that closes appropriate elements.
 *
 * @author Rsl1122
 */
public abstract class ExecStatement {

    private final String sql;
    private final boolean devMode;

    public ExecStatement(String sql) {
        this.sql = sql;
        devMode = Settings.DEV_MODE.isTrue();
    }

    public boolean execute(PreparedStatement statement) throws SQLException {
        Benchmark.start("SQL: " + sql);
        try {
            prepare(statement);
            return statement.executeUpdate() > 0;
        } finally {
            statement.close();
            if (devMode) {
                Log.debug(Benchmark.stopAndFormat("SQL: " + sql));
            }
        }
    }

    public void executeBatch(PreparedStatement statement) throws SQLException {
        Benchmark.start("SQL: " + sql + " (Batch)");
        try {
            prepare(statement);
            statement.executeBatch();
        } finally {
            statement.close();
            if (devMode) {
                Log.debug(Benchmark.stopAndFormat("SQL: " + sql + " (Batch)"));
            }
        }
    }

    public abstract void prepare(PreparedStatement statement) throws SQLException;

    public String getSql() {
        return sql;
    }
}
