/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.database.processing;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class ExecStatement {

    private final String sql;

    public ExecStatement(String sql) {
        this.sql = sql;
//        Log.debug("Execute Statement: " + sql);
    }

    public boolean execute(PreparedStatement statement) throws SQLException {
        try {
            prepare(statement);
            return statement.executeUpdate() > 0;
        } finally {
            statement.close();
        }
    }

    public void executeBatch(PreparedStatement statement) throws SQLException {
        try {
            prepare(statement);
            statement.executeBatch();
        } finally {
            statement.close();
        }
    }

    public abstract void prepare(PreparedStatement statement) throws SQLException;

    public String getSql() {
        return sql;
    }
}