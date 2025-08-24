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

import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Adds a is_proxy field to remove technical debt assuming name field "BungeeCord" being the proxy.
 * <p>
 * See <a href="https://github.com/plan-player-analytics/Plan/issues/1678">issue #1678</a> for more details
 *
 * @author AuroraLS3
 */
public class ServerIsProxyPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(ServerTable.TABLE_NAME, ServerTable.PROXY);
    }

    @Override
    protected void applyPatch() {
        addColumn(ServerTable.TABLE_NAME, ServerTable.PROXY + ' ' + Sql.BOOL + " DEFAULT 0");

        String populateFieldSql = "UPDATE " + ServerTable.TABLE_NAME + " SET " + ServerTable.PROXY + "=?" +
                WHERE + ServerTable.NAME + "=?";
        execute(new ExecStatement(populateFieldSql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
                statement.setString(2, "BungeeCord");
            }
        });
    }
}
