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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Transaction to store information in the database when a player is kicked from the server.
 *
 * @author AuroraLS3
 */
public class KickStoreTransaction extends ThrowawayTransaction {

    private final UUID playerUUID;

    public KickStoreTransaction(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    protected void performOperations() {
        String sql = "UPDATE " + UsersTable.TABLE_NAME + " SET "
                + UsersTable.TIMES_KICKED + "=" + UsersTable.TIMES_KICKED + "+ 1" +
                WHERE + UsersTable.USER_UUID + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }
        });
    }
}