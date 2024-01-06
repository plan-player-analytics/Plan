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

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Patch to fix incorrect register dates for nukkit.
 * <a href="https://github.com/plan-player-analytics/Plan/issues/1320">Related issue</a>
 *
 * @author AuroraLS3
 */
public class BadNukkitRegisterValuePatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasNoWrongRegisterDates(UserInfoTable.TABLE_NAME, UserInfoTable.REGISTERED)
                && hasNoWrongRegisterDates(UsersTable.TABLE_NAME, UsersTable.REGISTERED);
    }

    public boolean hasNoWrongRegisterDates(String tableName, String registered) {
        String sql = SELECT + "COUNT(*) as c" + FROM + tableName + WHERE + registered + "<?";
        Boolean foundWrongRegisterDates = query(new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, System.currentTimeMillis() / 1000L);
            }
        });
        return !foundWrongRegisterDates;
    }

    @Override
    protected void applyPatch() {
        multiplyInTable(UserInfoTable.TABLE_NAME, UserInfoTable.REGISTERED);
        multiplyInTable(UsersTable.TABLE_NAME, UsersTable.REGISTERED);
    }

    private void multiplyInTable(String tableName, String registered) {
        String sql = "UPDATE " + tableName + " SET " +
                registered + "=" + registered + "*1000" +
                WHERE + registered + "<?" +
                AND + registered + ">?";
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, System.currentTimeMillis() / 1000L);
                statement.setLong(2, 0L);
            }
        });
    }
}
