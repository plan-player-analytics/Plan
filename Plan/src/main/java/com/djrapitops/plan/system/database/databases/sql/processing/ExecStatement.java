/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.processing;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQL executing statement that closes appropriate elements.
 *
 * @author Rsl1122
 */
public abstract class ExecStatement extends AbstractSQLStatement {

    public ExecStatement(String sql) {
        super(sql);
    }

    public boolean execute(PreparedStatement statement) throws SQLException {
        startBenchmark();
        try {
            prepare(statement);
            return statement.executeUpdate() > 0;
        } finally {
            statement.close();
            stopBenchmark();
        }
    }

    public void executeBatch(PreparedStatement statement) throws SQLException {
        startBatchBenchmark();
        try {
            prepare(statement);
            statement.executeBatch();
        } finally {
            statement.close();
            stopBatchBenchmark();
        }
    }

    public abstract void prepare(PreparedStatement statement) throws SQLException;

    public String getSql() {
        return sql;
    }
}
