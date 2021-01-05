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

import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;

/**
 * Adds last_used field to the geolocation table.
 *
 * @author Rsl1122
 */
public class GeoInfoLastUsedPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(GeoInfoTable.TABLE_NAME, GeoInfoTable.LAST_USED);
    }

    @Override
    protected void applyPatch() {
        addColumn(GeoInfoTable.TABLE_NAME,
                GeoInfoTable.LAST_USED + " bigint NOT NULL DEFAULT 0"
        );
    }
}
