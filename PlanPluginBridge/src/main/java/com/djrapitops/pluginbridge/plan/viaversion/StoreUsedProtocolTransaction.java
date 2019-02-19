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
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;
import static com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable.*;

/**
 * Transaction to store used version protocol.
 *
 * @author Rsl1122
 */
public class StoreUsedProtocolTransaction extends Transaction {

    private final UUID playerUUID;
    private final int protocolVersion;

    public StoreUsedProtocolTransaction(UUID playerUUID, int protocolVersion) {
        this.playerUUID = playerUUID;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void performOperations() {
        execute(storeProtocol());
    }

    private Executable storeProtocol() {
        return connection -> {
            if (!updateProtocol().execute(connection)) {
                return insertProtocol().execute(connection);
            }
            return false;
        };
    }

    private Executable updateProtocol() {
        String sql = "UPDATE " + TABLE_NAME + " SET "
                + COL_PROTOCOL_VERSION + "=?"
                + WHERE + COL_UUID + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, protocolVersion);
                statement.setString(2, playerUUID.toString());
            }
        };
    }

    private Executable insertProtocol() {
        String sql = "INSERT INTO " + TABLE_NAME + " ("
                + COL_UUID + ", "
                + COL_PROTOCOL_VERSION
                + ") VALUES (?, ?)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setInt(2, protocolVersion);
            }
        };
    }
}