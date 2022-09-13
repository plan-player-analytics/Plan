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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.SQLDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * SQL query that closes proper elements.
 *
 * @author AuroraLS3
 */
public abstract class QueryStatement<T> implements Query<T> {

    private final String sql;
    private final int fetchSize;

    protected QueryStatement(String sql) {
        this(sql, 10);
    }

    protected QueryStatement(String sql, int fetchSize) {
        this.sql = sql;
        this.fetchSize = fetchSize;
    }

    @Override
    public T executeQuery(SQLDB db) {
        Connection connection = null;
        try {
            connection = db.getConnection();
            return executeWithConnection(connection);
        } catch (SQLException e) {
            throw DBOpException.forCause(sql, e);
        } finally {
            db.returnToPool(connection);
        }
    }

    public T executeWithConnection(Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return executeQuery(preparedStatement);
        } catch (SQLException e) {
            throw DBOpException.forCause(sql, e);
        }
    }

    public T executeQuery(PreparedStatement statement) throws SQLException {
        try (statement) {
            statement.setFetchSize(fetchSize);
            prepare(statement);
            try (ResultSet set = statement.executeQuery()) {
                return processResults(set);
            }
        }
    }

    public abstract void prepare(PreparedStatement statement) throws SQLException;

    public abstract T processResults(ResultSet set) throws SQLException;

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "Query (" + sql + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryStatement<?> that = (QueryStatement<?>) o;
        return Objects.equals(getSql(), that.getSql());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSql());
    }
}
