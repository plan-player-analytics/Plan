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
import com.djrapitops.plan.storage.database.transactions.Executable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryAPIExecutable implements Executable {

    private final String sql;
    private final QueryService.ThrowingConsumer<PreparedStatement> statement;

    public QueryAPIExecutable(
            String sql,
            QueryService.ThrowingConsumer<PreparedStatement> statement
    ) {
        this.sql = sql;
        this.statement = statement;
    }

    @Override
    public boolean execute(Connection connection) {
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                statement.accept(preparedStatement);
                return true;
            }
        } catch (SQLException e) {
            throw DBOpException.forCause(sql, e);
        }
    }
}
