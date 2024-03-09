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
import com.djrapitops.plan.storage.database.sql.tables.webuser.SecurityTable;

/**
 * Adds linked_to_uuid field to plan_security table that stores web users.
 * <p>
 * This patch allows web users to have a username other than the minecraft username.
 *
 * @author AuroraLS3
 * @see LinkUsersToPlayersSecurityTablePatch for the patch that populates the field afterwards.
 */
public class LinkedToSecurityTablePatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(SecurityTable.TABLE_NAME, SecurityTable.LINKED_TO);
    }

    @Override
    protected void applyPatch() {
        addColumn(SecurityTable.TABLE_NAME, SecurityTable.LINKED_TO + ' ' + Sql.varchar(36) + " DEFAULT ''");
    }
}
