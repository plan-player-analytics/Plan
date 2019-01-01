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
package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.KillsTable;
import com.djrapitops.plan.system.database.databases.sql.tables.SessionsTable;
import com.djrapitops.plan.system.database.databases.sql.tables.SessionsTable.Col;
import com.djrapitops.plan.system.database.databases.sql.tables.WorldTimesTable;

public class SessionsOptimizationPatch extends Patch {

    private String tempTableName;
    private String tableName;

    public SessionsOptimizationPatch(SQLDB db) {
        super(db);
        tableName = SessionsTable.TABLE_NAME;
        tempTableName = "temp_sessions";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, Col.UUID.get())
                && hasColumn(tableName, Col.SERVER_UUID.get())
                && !hasColumn(tableName, "user_id")
                && !hasColumn(tableName, "server_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    public void apply() {
        try {
            dropForeignKey(WorldTimesTable.TABLE_NAME, WorldTimesTable.Col.SESSION_ID.get());
            dropForeignKey(KillsTable.TABLE_NAME, KillsTable.Col.SESSION_ID.get());

            tempOldTable();
            db.getSessionsTable().createTable();

            db.execute("INSERT INTO " + tableName + " (" +
                    Col.UUID + ", " +
                    Col.SERVER_UUID + ", " +
                    Col.ID + ", " +
                    Col.SESSION_START + ", " +
                    Col.SESSION_END + ", " +
                    Col.MOB_KILLS + ", " +
                    Col.DEATHS + ", " +
                    Col.AFK_TIME +
                    ") SELECT " +
                    "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".user_id LIMIT 1), " +
                    "(SELECT plan_servers.uuid FROM plan_servers WHERE plan_servers.id = " + tempTableName + ".server_id LIMIT 1), " +
                    Col.ID + ", " +
                    Col.SESSION_START + ", " +
                    Col.SESSION_END + ", " +
                    Col.MOB_KILLS + ", " +
                    Col.DEATHS + ", " +
                    Col.AFK_TIME +
                    " FROM " + tempTableName
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
