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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.sql.tables.UsersTable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Static method class for queries that count how many entries of particular kinds there are.
 *
 * @author Rsl1122
 */
public class AggregateQueries {

    private AggregateQueries() {
        /* Static method class */
    }

    /**
     * Count how many users are in the Plan database.
     *
     * @return Count of base users, all users in a network.
     */
    public static Query<Integer> baseUserCount() {
        String sql = "SELECT COUNT(1) as c FROM " + UsersTable.TABLE_NAME;
        return new QueryAllStatement<Integer>(sql) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 0;
            }
        };
    }
}