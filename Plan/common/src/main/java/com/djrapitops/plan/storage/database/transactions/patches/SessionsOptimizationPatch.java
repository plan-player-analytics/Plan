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
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Replaces user_id and server_id foreign keys with respective uuid fields in sessions table.
 * <p>
 * This was to "reduce the amount of joins when querying sessions".
 *
 * @author AuroraLS3
 */
public class SessionsOptimizationPatch extends Patch {

    private final String tempTableName;
    private final String tableName;

    public SessionsOptimizationPatch() {
        tableName = SessionsTable.TABLE_NAME;
        tempTableName = "temp_sessions";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, SessionsTable.USER_ID)
                && hasColumn(tableName, SessionsTable.SERVER_ID)
                && !hasColumn(tableName, "uuid")
                && !hasColumn(tableName, "server_uuid")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        try {
            dropForeignKeys(tableName);
            ensureNoForeignKeyConstraints(tableName);

            tempOldTable();

            execute(SessionsTable.createTableSQL(dbType));

            execute(INSERT_INTO + tableName + " (" +
                    SessionsTable.USER_ID + ',' +
                    SessionsTable.SERVER_ID + ',' +
                    SessionsTable.ID + ',' +
                    SessionsTable.SESSION_START + ',' +
                    SessionsTable.SESSION_END + ',' +
                    SessionsTable.MOB_KILLS + ',' +
                    SessionsTable.DEATHS + ',' +
                    SessionsTable.AFK_TIME +
                    ") SELECT " +
                    "(SELECT plan_users.id FROM plan_users WHERE plan_users.uuid = " + tempTableName + ".uuid LIMIT 1), " +
                    "(SELECT plan_servers.id FROM plan_servers WHERE plan_servers.uuid = " + tempTableName + ".server_uuid LIMIT 1), " +
                    SessionsTable.ID + ',' +
                    SessionsTable.SESSION_START + ',' +
                    SessionsTable.SESSION_END + ',' +
                    SessionsTable.MOB_KILLS + ',' +
                    SessionsTable.DEATHS + ',' +
                    SessionsTable.AFK_TIME +
                    FROM + tempTableName
            );

            dropTable(tempTableName);
        } catch (Exception e) {
            throw new DBOpException(SessionsOptimizationPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}
