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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL query that doesn't require preparing that closes proper elements.
 *
 * @author AuroraLS3
 */
public abstract class QueryAllStatement<T> extends QueryStatement<T> {
    protected QueryAllStatement(String sql) {
        super(sql);
    }

    protected QueryAllStatement(String sql, int fetchSize) {
        super(sql, fetchSize);
    }

    @Override
    public void prepare(PreparedStatement statement) throws SQLException {
        /* None Required */
    }

    @Override
    public abstract T processResults(ResultSet set) throws SQLException;
}
