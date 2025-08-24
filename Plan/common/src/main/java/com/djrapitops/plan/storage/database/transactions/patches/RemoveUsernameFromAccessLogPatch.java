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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.AccessLogTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Sets username fields to NULL in plan_access_log table.
 *
 * @author AuroraLS3
 */
public class RemoveUsernameFromAccessLogPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        if (!hasColumn(AccessLogTable.TABLE_NAME, "username")) {
            return true;
        }
        return !hasUsernames();
    }

    private Boolean hasUsernames() {
        String sql = SELECT + "COUNT(*) as c" +
                FROM + AccessLogTable.TABLE_NAME +
                WHERE + "username" + IS_NOT_NULL;
        return query(new QueryAllStatement<>(sql) {
            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next() && set.getInt("c") > 0;
            }
        });
    }

    @Override
    protected void applyPatch() {
        execute(new ExecStatement("UPDATE " + AccessLogTable.TABLE_NAME + " SET username=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setNull(1, Types.VARCHAR);
            }
        });
    }
}
