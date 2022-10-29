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
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPlayerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionServerValueTable;

public class ComponentColumnToExtensionDataPatch extends Patch {

    private static final String serverTable = ExtensionServerValueTable.TABLE_NAME;
    private static final String serverColumn = ExtensionServerValueTable.COMPONENT_VALUE;
    private static final String playerTable = ExtensionPlayerValueTable.TABLE_NAME;
    private static final String playerColumn = ExtensionPlayerValueTable.COMPONENT_VALUE;
    private static final int length = 500;

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(serverTable, serverColumn)
                && hasColumn(playerTable, playerColumn);
    }

    @Override
    protected void applyPatch() {
        addColumn(serverTable, serverColumn + " " + Sql.varchar(length));
        addColumn(playerTable, playerColumn + " " + Sql.varchar(length));
    }
}
