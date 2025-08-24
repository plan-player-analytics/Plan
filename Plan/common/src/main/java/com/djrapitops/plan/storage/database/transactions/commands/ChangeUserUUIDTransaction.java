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

import com.djrapitops.plan.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.storage.database.sql.tables.NicknamesTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerTableValueTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Intends to correct UUID of a user.
 *
 * @author AuroraLS3
 */
public class ChangeUserUUIDTransaction extends Transaction {

    protected final UUID oldUUID;
    protected final UUID newUUID;

    public ChangeUserUUIDTransaction(UUID oldUUID, UUID newUUID) {
        this.oldUUID = oldUUID;
        this.newUUID = newUUID;
    }

    @Override
    protected void performOperations() {
        execute(updateUUID(ExtensionGroupsTable.TABLE_NAME, ExtensionGroupsTable.USER_UUID));
        execute(updateUUID(ExtensionPlayerTableValueTable.TABLE_NAME, ExtensionPlayerTableValueTable.USER_UUID));
        execute(updateUUID(NicknamesTable.TABLE_NAME, NicknamesTable.USER_UUID));
        execute(updateUUID(UsersTable.TABLE_NAME, UsersTable.USER_UUID));
        execute(updateUUID(KillsTable.TABLE_NAME, KillsTable.VICTIM_UUID));
        execute(updateUUID(KillsTable.TABLE_NAME, KillsTable.KILLER_UUID));

        if (hasTable("plan_platforms")) execute(updateUUID("plan_platforms", "uuid"));
        if (hasTable("plan_tebex_payments")) execute(updateUUID("plan_tebex_payments", "uuid"));
        if (hasTable("plan_version_protocol")) execute(updateUUID("plan_version_protocol", "uuid"));
    }

    private Executable updateUUID(String tableName, String columnName) {
        return new ExecStatement("UPDATE " + tableName + " SET " + columnName + "=?" + WHERE + columnName + "=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, newUUID.toString());
                statement.setString(2, oldUUID.toString());
            }
        };
    }
}
