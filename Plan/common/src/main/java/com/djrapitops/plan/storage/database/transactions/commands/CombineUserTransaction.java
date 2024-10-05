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

import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionGraphQueries;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Intends to correct UUID of a user.
 *
 * @author AuroraLS3
 */
public class CombineUserTransaction extends ChangeUserUUIDTransaction {

    public CombineUserTransaction(UUID oldUUID, UUID newUUID) {
        super(oldUUID, newUUID);
    }

    @Override
    protected void performOperations() {
        Optional<Integer> foundOldId = query(BaseUserQueries.fetchUserId(oldUUID));
        Optional<Integer> foundNewId = query(BaseUserQueries.fetchUserId(newUUID));
        if (foundOldId.isEmpty() || foundNewId.isEmpty()) return;

        Integer oldId = foundOldId.get();
        Integer newId = foundNewId.get();

        execute(updateUserId(GeoInfoTable.TABLE_NAME, GeoInfoTable.USER_ID, oldId, newId));
        execute(updateUserId(PingTable.TABLE_NAME, PingTable.USER_ID, oldId, newId));
        execute(updateUserId(SessionsTable.TABLE_NAME, SessionsTable.USER_ID, oldId, newId));
        execute(updateUserId(WorldTimesTable.TABLE_NAME, WorldTimesTable.USER_ID, oldId, newId));
        query(ExtensionGraphQueries.findGraphTableNames(ExtensionGraphMetadataTable.TableType.PLAYER))
                .forEach(tableName -> execute(updateUserId(tableName, "user_id", oldId, newId)));

        execute(updateUserInfo(newId, oldId));
        execute(DELETE_FROM + UserInfoTable.TABLE_NAME + WHERE + UserInfoTable.USER_ID + "=" + oldId);
        execute(DELETE_FROM + UsersTable.TABLE_NAME + WHERE + UsersTable.ID + "=" + oldId);

        super.performOperations(); // Change UUID fields to match where user_id is not used
    }

    private Executable updateUserInfo(Integer newId, Integer oldId) {
        String sql = "UPDATE " + UserInfoTable.TABLE_NAME +
                " SET " + UserInfoTable.USER_ID + "=?" +
                WHERE + UserInfoTable.USER_ID + "=?" +
                AND + UserInfoTable.SERVER_ID + " NOT IN (" +
                SELECT + UserInfoTable.SERVER_ID + FROM + UserInfoTable.TABLE_NAME + WHERE + UserInfoTable.USER_ID + "=?)";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, newId);
                statement.setInt(2, oldId);
                statement.setInt(3, newId);
            }
        };
    }

    private Executable updateUserId(String tableName, String columnName, Integer oldId, Integer newId) {
        return new ExecStatement("UPDATE " + tableName + " SET " + columnName + "=?" + WHERE + columnName + "=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, newId);
                statement.setInt(2, oldId);
            }
        };
    }
}
