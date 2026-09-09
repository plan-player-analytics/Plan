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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Patch that removes player IPs that were gathered as join address on Fabric.
 *
 * @author AuroraLS3
 */
public class BadFabricJoinAddressValuePatch extends Patch {

    private final ServerUUID serverUUID;

    public BadFabricJoinAddressValuePatch(ServerUUID serverUUID) {this.serverUUID = serverUUID;}

    @Override
    public boolean hasBeenApplied() {
        return false; // There is no good way to detect this, so this patch has to be applied manually
    }

    @Override
    protected void applyPatch() {
        execute(new ExecStatement("UPDATE " + UserInfoTable.TABLE_NAME + " SET " + UserInfoTable.JOIN_ADDRESS + "=?" +
                WHERE + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setNull(1, Types.VARCHAR);
                statement.setString(2, serverUUID.toString());
            }
        });
        execute(new ExecStatement("UPDATE " + SessionsTable.TABLE_NAME +
                " SET " + SessionsTable.JOIN_ADDRESS_ID + "=" + JoinAddressTable.SELECT_ID +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
                statement.setString(2, serverUUID.toString());
            }
        });
        execute("DELETE FROM " + JoinAddressTable.TABLE_NAME +
                WHERE + JoinAddressTable.ID + " NOT IN (" +
                SELECT + DISTINCT + SessionsTable.JOIN_ADDRESS_ID + FROM + SessionsTable.TABLE_NAME + lockForUpdate() +
                ")");
    }
}
