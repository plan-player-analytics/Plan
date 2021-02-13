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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;

/**
 * Replaces user_id and server_id foreign keys with respective uuid fields in user info table.
 *
 * @author AuroraLS3
 */
public class UserInfoOptimizationPatch extends Patch {

    private final String tempTableName;
    private final String tableName;

    public UserInfoOptimizationPatch() {
        tableName = UserInfoTable.TABLE_NAME;
        tempTableName = "temp_user_info";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, UserInfoTable.USER_UUID)
                && hasColumn(tableName, UserInfoTable.SERVER_UUID)
                && !hasColumn(tableName, "user_id")
                && !hasColumn(tableName, "server_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        try {
            tempOldTable();
            execute(UserInfoTable.createTableSQL(dbType));

            execute("INSERT INTO " + tableName + " (" +
                    UserInfoTable.USER_UUID + ',' +
                    UserInfoTable.SERVER_UUID + ',' +
                    UserInfoTable.REGISTERED + ',' +
                    UserInfoTable.BANNED + ',' +
                    UserInfoTable.OP +
                    ") SELECT " +
                    "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".user_id LIMIT 1), " +
                    "(SELECT plan_servers.uuid FROM plan_servers WHERE plan_servers.id = " + tempTableName + ".server_id LIMIT 1), " +
                    UserInfoTable.REGISTERED + ',' +
                    UserInfoTable.BANNED + ',' +
                    UserInfoTable.OP +
                    FROM + tempTableName
            );

            dropTable(tempTableName);
        } catch (Exception e) {
            throw new DBOpException(UserInfoOptimizationPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}
