/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.database.processing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class QueryAllStatement<T> extends QueryStatement<T> {
    public QueryAllStatement(String sql) {
        super(sql);
    }

    public QueryAllStatement(String sql, int fetchSize) {
        super(sql, fetchSize);
    }

    @Override
    public void prepare(PreparedStatement statement) throws SQLException {
        /* None Required */
    }

    @Override
    public abstract T processResults(ResultSet set) throws SQLException;
}