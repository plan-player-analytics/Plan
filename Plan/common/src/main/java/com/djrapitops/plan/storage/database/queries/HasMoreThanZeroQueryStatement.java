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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL query of a COUNT statement that closes proper elements.
 *
 * @author Rsl1122
 */
public abstract class HasMoreThanZeroQueryStatement extends QueryStatement<Boolean> {

    private String countColumnName = "c";

    protected HasMoreThanZeroQueryStatement(String sql) {
        super(sql);
    }

    protected HasMoreThanZeroQueryStatement(String sql, String countColumnName) {
        super(sql);
        this.countColumnName = countColumnName;
    }

    @Override
    public Boolean processResults(ResultSet set) throws SQLException {
        return set.next() && set.getInt(countColumnName) > 0;
    }
}