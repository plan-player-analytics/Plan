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
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerValueTable;

public class ComponentColumnToExtensionDataPatch extends Patch {

    private static final String SERVER_TABLE = ExtensionServerValueTable.TABLE_NAME;
    private static final String SERVER_COLUMN = ExtensionServerValueTable.COMPONENT_VALUE;
    private static final String PLAYER_TABLE = ExtensionPlayerValueTable.TABLE_NAME;
    private static final String PLAYER_COLUMN = ExtensionPlayerValueTable.COMPONENT_VALUE;
    private static final int LENGTH = 500;

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(SERVER_TABLE, SERVER_COLUMN)
                && hasColumn(PLAYER_TABLE, PLAYER_COLUMN);
    }

    @Override
    protected void applyPatch() {
        addColumn(SERVER_TABLE, SERVER_COLUMN + " " + Sql.varchar(LENGTH));
        addColumn(PLAYER_TABLE, PLAYER_COLUMN + " " + Sql.varchar(LENGTH));
    }
}
