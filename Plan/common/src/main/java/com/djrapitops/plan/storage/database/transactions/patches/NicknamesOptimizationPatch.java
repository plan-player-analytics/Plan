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
import com.djrapitops.plan.storage.database.sql.tables.NicknamesTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Replaces user_id and server_id foreign keys with respective uuid fields in nickname table.
 * <p>
 * This was to "reduce the amount of joins when querying sessions".
 *
 * @author AuroraLS3
 */
public class NicknamesOptimizationPatch extends Patch {

    private final String tempTableName;
    private final String tableName;

    public NicknamesOptimizationPatch() {
        tableName = NicknamesTable.TABLE_NAME;
        tempTableName = "temp_nicknames";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, NicknamesTable.USER_UUID)
                && hasColumn(tableName, NicknamesTable.SERVER_UUID)
                && !hasColumn(tableName, "user_id")
                && !hasColumn(tableName, "server_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        try {
            tempOldTable();
            execute(NicknamesTable.createTableSQL(dbType));

            execute(INSERT_INTO + tableName + " (" +
                    NicknamesTable.USER_UUID + ',' +
                    NicknamesTable.SERVER_UUID + ',' +
                    NicknamesTable.NICKNAME + ',' +
                    NicknamesTable.LAST_USED +
                    ") SELECT " +
                    "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".user_id LIMIT 1), " +
                    "(SELECT plan_servers.uuid FROM plan_servers WHERE plan_servers.id = " + tempTableName + ".server_id LIMIT 1), " +
                    NicknamesTable.NICKNAME + ',' +
                    NicknamesTable.LAST_USED +
                    FROM + tempTableName
            );

            dropTable(tempTableName);
        } catch (Exception e) {
            throw new DBOpException(NicknamesOptimizationPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}
