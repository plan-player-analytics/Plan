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
import com.djrapitops.plan.query.QueryService;
import com.djrapitops.plan.storage.database.SQLDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryAPIQuery<T> implements Query<T> {

    private final QueryService.ThrowingFunction<PreparedStatement, T> performQuery;
    private final String sql;

    public QueryAPIQuery(
            String sql,
            QueryService.ThrowingFunction<PreparedStatement, T> performQuery
    ) {
        this.sql = sql;
        this.performQuery = performQuery;
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
            return performQuery.apply(preparedStatement);
        } catch (SQLException e) {
            throw DBOpException.forCause(sql, e);
        }
    }
}
