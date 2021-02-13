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
package com.djrapitops.plan.storage.database.transactions.commands;

import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Transaction for telling Plan that Plan has been uninstalled from the server.
 *
 * @author AuroraLS3
 */
public class SetServerAsUninstalledTransaction extends ThrowawayTransaction {

    private final UUID serverUUID;

    public SetServerAsUninstalledTransaction(UUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    protected void performOperations() {
        execute(updateServerAsUninstalled());
    }

    private Executable updateServerAsUninstalled() {
        String sql = "UPDATE " + ServerTable.TABLE_NAME + " SET " + ServerTable.INSTALLED + "=?" +
                WHERE + ServerTable.SERVER_UUID + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, false);
                statement.setString(2, serverUUID.toString());
            }
        };
    }
}