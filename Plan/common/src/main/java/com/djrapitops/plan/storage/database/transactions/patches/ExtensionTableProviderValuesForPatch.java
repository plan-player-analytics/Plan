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
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTableProviderTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTableProviderTable.*;

/**
 * Adds values_for field to plan_extension_tables
 */
public class ExtensionTableProviderValuesForPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(
                TABLE_NAME,
                VALUES_FOR
        );
    }

    @Override
    protected void applyPatch() {
        addColumn(TABLE_NAME, VALUES_FOR + " " + Sql.INT + " DEFAULT " + VALUES_FOR_PLAYER);

        String selectServerTableIDs = SELECT + DISTINCT +
                ExtensionServerTableValueTable.TABLE_ID +
                FROM + ExtensionServerTableValueTable.TABLE_NAME;

        String updateSql = "UPDATE " + TABLE_NAME + " SET " + VALUES_FOR + "=" + VALUES_FOR_SERVER +
                WHERE + ExtensionTableProviderTable.ID + " IN (" + selectServerTableIDs + ")";

        execute(updateSql);
    }
}
