/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.processing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL query that closes proper elements.
 *
 * @author Rsl1122
 */
public abstract class QueryStatement<T> extends AbstractSQLStatement {

    private final int fetchSize;

    public QueryStatement(String sql) {
        this(sql, 10);
    }

    public QueryStatement(String sql, int fetchSize) {
        super(sql);
        this.fetchSize = fetchSize;
    }

    public T executeQuery(PreparedStatement statement) throws SQLException {
        startBenchmark();
        try {
            statement.setFetchSize(fetchSize);
            prepare(statement);
            try (ResultSet set = statement.executeQuery()) {
                return processResults(set);
            }
        } finally {
            statement.close();
            stopBenchmark();
        }
    }

    public abstract void prepare(PreparedStatement statement) throws SQLException;

    public abstract T processResults(ResultSet set) throws SQLException;

    public String getSql() {
        return sql;
    }
}
