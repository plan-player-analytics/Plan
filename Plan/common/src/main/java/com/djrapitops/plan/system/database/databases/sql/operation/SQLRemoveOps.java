/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.RemoveOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
import com.djrapitops.plan.system.database.databases.sql.tables.UserIDTable;

import java.util.UUID;

public class SQLRemoveOps extends SQLOps implements RemoveOperations {

    public SQLRemoveOps(SQLDB db) {
        super(db);
    }

    @Override
    public void player(UUID uuid) {
        if (uuid == null) {
            return;
        }

        String webUser = usersTable.getPlayerName(uuid);

        for (Table t : db.getAllTablesInRemoveOrder()) {
            if (!(t instanceof UserIDTable)) {
                continue;
            }

            UserIDTable table = (UserIDTable) t;
            table.removeUser(uuid);
        }

        securityTable.removeUser(webUser);
    }

    @Override
    public void everything() {
        for (Table table : db.getAllTablesInRemoveOrder()) {
            table.removeAllData();
        }
    }

    @Override
    public void webUser(String userName) {
        securityTable.removeUser(userName);
    }
}
