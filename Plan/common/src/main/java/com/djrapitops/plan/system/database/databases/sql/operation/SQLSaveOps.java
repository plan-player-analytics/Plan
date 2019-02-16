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
package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.queries.DataStoreQueries;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;

import java.util.UUID;

/**
 * SaveOperations implementation for SQL databases.
 *
 * @author Rsl1122
 */
public class SQLSaveOps extends SQLOps implements SaveOperations {

    public SQLSaveOps(SQLDB db) {
        super(db);
    }

    @Override
    public void banStatus(UUID uuid, boolean banned) {
        userInfoTable.updateBanStatus(uuid, banned);
    }

    @Override
    public void opStatus(UUID uuid, boolean op) {
        userInfoTable.updateOpStatus(uuid, op);
    }

    @Override
    public void session(UUID uuid, Session session) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.storeSession(session));
            }
        });
    }
}
