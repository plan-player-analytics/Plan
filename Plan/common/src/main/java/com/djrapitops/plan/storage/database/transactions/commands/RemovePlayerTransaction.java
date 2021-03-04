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

import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction for removing a player's data from the database.
 *
 * @author AuroraLS3
 */
public class RemovePlayerTransaction extends ThrowawayTransaction {

    private final UUID playerUUID;

    public RemovePlayerTransaction(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return playerUUID != null;
    }

    @Override
    protected void performOperations() {
        query(PlayerFetchQueries.playerUserName(playerUUID)).ifPresent(this::deleteWebUser);

        deleteFromTable(GeoInfoTable.TABLE_NAME);
        deleteFromTable(NicknamesTable.TABLE_NAME);
        deleteFromKillsTable();
        deleteFromTable(WorldTimesTable.TABLE_NAME);
        deleteFromTable(SessionsTable.TABLE_NAME);
        deleteFromTable(PingTable.TABLE_NAME);
        deleteFromTable(UserInfoTable.TABLE_NAME);
        deleteFromTable(UsersTable.TABLE_NAME);

        deleteFromTable(ExtensionPlayerTableValueTable.TABLE_NAME);
        deleteFromTable(ExtensionPlayerValueTable.TABLE_NAME);
        deleteFromTable(ExtensionGroupsTable.TABLE_NAME);
    }

    private void deleteWebUser(String username) {
        executeOther(new RemoveWebUserTransaction(username));
    }

    private void deleteFromTable(String tableName) {
        execute(new ExecStatement(DELETE_FROM + tableName + WHERE + "uuid=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }
        });
    }

    private void deleteFromKillsTable() {
        String sql = DELETE_FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.KILLER_UUID + "=?" +
                OR + KillsTable.VICTIM_UUID + "=?";
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, playerUUID.toString());
            }
        });
    }
}