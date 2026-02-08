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
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Replaces killer_id, victim_id and server_id foreign keys with respective uuid fields in kills table.
 * <p>
 * This was to "reduce the amount of joins when querying sessions".
 *
 * @author AuroraLS3
 */
public class KillsOptimizationPatch extends Patch {

    private final String tempTableName;
    private final String tableName;

    public KillsOptimizationPatch() {
        tableName = KillsTable.TABLE_NAME;
        tempTableName = "temp_kills";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, KillsTable.VICTIM_UUID)
                && hasColumn(tableName, KillsTable.KILLER_UUID)
                && hasColumn(tableName, KillsTable.SERVER_UUID)
                && !hasColumn(tableName, "killer_id")
                && !hasColumn(tableName, "victim_id")
                && !hasColumn(tableName, "server_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        try {
            if (hasTable(tempTableName) && hasColumn(tempTableName, KillsTable.VICTIM_UUID)) {
                // In this case a patch has made a table with almost correct schema to a temporary table.
                renameTable(tempTableName, tableName);
                return;
            } else if (hasColumn(tableName, KillsTable.VICTIM_UUID)) {
                // In this case a patch has made a table with almost correct schema, but something is not right.
                return;
            }

            tempOldTable();
            execute(KillsTable.createTableSQL(dbType));

            execute(INSERT_INTO + tableName + " (" +
                    KillsTable.VICTIM_UUID + ',' +
                    KillsTable.KILLER_UUID + ',' +
                    KillsTable.SERVER_UUID + ',' +
                    KillsTable.DATE + ',' +
                    KillsTable.WEAPON + ',' +
                    KillsTable.SESSION_ID +
                    ") SELECT " +
                    "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".victim_id LIMIT 1), " +
                    "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".killer_id LIMIT 1), " +
                    "(SELECT plan_servers.uuid FROM plan_servers WHERE plan_servers.id = " + tempTableName + ".server_id LIMIT 1), " +
                    KillsTable.DATE + ',' +
                    KillsTable.WEAPON + ',' +
                    KillsTable.SESSION_ID +
                    FROM + tempTableName
            );

            dropTable(tempTableName);
        } catch (Exception e) {
            throw new DBOpException(KillsOptimizationPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}
