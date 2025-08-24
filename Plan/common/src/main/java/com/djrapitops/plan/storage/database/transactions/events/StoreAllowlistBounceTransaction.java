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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.AllowlistBounceTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Stores a bounced allowlist login.
 *
 * @author AuroraLS3
 */
public class StoreAllowlistBounceTransaction extends Transaction {

    private final UUID playerUUID;
    @Untrusted
    private final String playerName;
    private final ServerUUID serverUUID;
    private final long time;

    public StoreAllowlistBounceTransaction(UUID playerUUID, @Untrusted String playerName, ServerUUID serverUUID, long time) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.serverUUID = serverUUID;
        this.time = time;
    }

    @Override
    protected void performOperations() {
        boolean updated = update();
        if (!updated) {
            try {
                insert();
            } catch (DBOpException e) {
                if (e.isDuplicateKeyViolation()) {
                    update();
                    return;
                }
                throw e;
            }
        }
    }

    private void insert() {
        execute(new ExecStatement(AllowlistBounceTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, playerName);
                statement.setString(3, serverUUID.toString());
                statement.setInt(4, 1);
                statement.setLong(5, time);
            }
        });
    }

    private boolean update() {
        return execute(new ExecStatement(AllowlistBounceTable.INCREMENT_TIMES_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, time);
                statement.setString(2, playerUUID.toString());
                statement.setString(3, serverUUID.toString());
            }
        });
    }
}
