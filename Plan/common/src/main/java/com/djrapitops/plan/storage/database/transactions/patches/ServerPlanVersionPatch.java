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

import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;

public class ServerPlanVersionPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(ServerTable.TABLE_NAME, ServerTable.PLAN_VERSION);
    }

    @Override
    protected void applyPatch() {
        addColumn(ServerTable.TABLE_NAME, ServerTable.PLAN_VERSION + " " + Sql.varchar(18) + " DEFAULT 'Old'");
    }
}
