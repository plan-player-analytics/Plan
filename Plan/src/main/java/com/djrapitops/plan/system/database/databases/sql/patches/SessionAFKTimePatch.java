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
package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.SessionsTable;

public class SessionAFKTimePatch extends Patch {

    public SessionAFKTimePatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(SessionsTable.TABLE_NAME, SessionsTable.Col.AFK_TIME.get());
    }

    @Override
    public void apply() {
        addColumn(SessionsTable.TABLE_NAME,
                SessionsTable.Col.AFK_TIME + " bigint NOT NULL DEFAULT 0"
        );
    }
}
