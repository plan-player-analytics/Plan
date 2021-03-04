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
import com.djrapitops.plan.storage.database.sql.tables.WorldTimesTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;

/**
 * Replaces server_id foreign keys with server_uuid field in world times table.
 * <p>
 * This was to "reduce the amount of joins when querying sessions".
 *
 * @author Rsl1122
 */
public class WorldTimesOptimizationPatch extends Patch {

    private final String tempTableName;
    private final String tableName;

    public WorldTimesOptimizationPatch() {
        tableName = WorldTimesTable.TABLE_NAME;
        tempTableName = "temp_world_times";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, WorldTimesTable.ID)
                && hasColumn(tableName, WorldTimesTable.USER_UUID)
                && hasColumn(tableName, WorldTimesTable.SERVER_UUID)
                && !hasColumn(tableName, "user_id")
                && !hasColumn(tableName, "server_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        try {
            tempOldTable();
            execute(WorldTimesTable.createTableSQL(dbType));

            execute("INSERT INTO " + tableName + " (" +
                    WorldTimesTable.USER_UUID + ',' +
                    WorldTimesTable.SERVER_UUID + ',' +
                    WorldTimesTable.ADVENTURE + ',' +
                    WorldTimesTable.CREATIVE + ',' +
                    WorldTimesTable.SURVIVAL + ',' +
                    WorldTimesTable.SPECTATOR + ',' +
                    WorldTimesTable.SESSION_ID + ',' +
                    WorldTimesTable.WORLD_ID +
                    ") SELECT " +
                    "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".user_id LIMIT 1), " +
                    "(SELECT plan_servers.uuid FROM plan_servers WHERE plan_servers.id = " + tempTableName + ".server_id LIMIT 1), " +
                    WorldTimesTable.ADVENTURE + ',' +
                    WorldTimesTable.CREATIVE + ',' +
                    WorldTimesTable.SURVIVAL + ',' +
                    WorldTimesTable.SPECTATOR + ',' +
                    WorldTimesTable.SESSION_ID + ',' +
                    WorldTimesTable.WORLD_ID +
                    FROM + tempTableName
            );

            dropTable(tempTableName);
        } catch (Exception e) {
            throw new DBOpException(WorldTimesOptimizationPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}
