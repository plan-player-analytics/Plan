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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.building.Update;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Transaction to update a player's ban status.
 *
 * @author AuroraLS3
 */
public class BatchBanStatusTransaction extends Transaction {

    private final List<UUID> bannedPlayerUUIDs;
    private final List<UUID> unbannedPlayerUUIDs;
    private final ServerUUID serverUUID;

    public BatchBanStatusTransaction(List<UUID> bannedPlayerUUIDs, List<UUID> unbannedPlayerUUIDs, ServerUUID serverUUID) {
        this.bannedPlayerUUIDs = bannedPlayerUUIDs;
        this.unbannedPlayerUUIDs = unbannedPlayerUUIDs;
        this.serverUUID = serverUUID;
    }

    @Override
    protected void performOperations() {
        execute(updateBanStatus());
    }

    private Executable updateBanStatus() {
        String sql = Update.values(UserInfoTable.TABLE_NAME, UserInfoTable.BANNED)
                .where(UserInfoTable.USER_ID + "=" + UsersTable.SELECT_USER_ID)
                .and(UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID)
                .toString();

        return new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
                for (UUID bannedPlayerUUID : bannedPlayerUUIDs) {
                    statement.setString(2, bannedPlayerUUID.toString());
                    statement.setString(3, serverUUID.toString());
                    statement.addBatch();
                }
                statement.setBoolean(1, false);
                for (UUID unbannedPlayerUUID : unbannedPlayerUUIDs) {
                    statement.setString(2, unbannedPlayerUUID.toString());
                    statement.setString(3, serverUUID.toString());
                    statement.addBatch();
                }
            }
        };
    }
}