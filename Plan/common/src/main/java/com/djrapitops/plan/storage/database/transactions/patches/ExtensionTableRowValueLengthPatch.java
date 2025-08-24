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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerTableValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable;

/**
 * Increases the length of Strings in extension tables to 250 to avoid cutoffs and exceptions.
 *
 * @author AuroraLS3
 */
public class ExtensionTableRowValueLengthPatch extends Patch {

    private final String playerTable;
    private final String serverTable;

    public ExtensionTableRowValueLengthPatch() {
        playerTable = ExtensionPlayerTableValueTable.TABLE_NAME;
        serverTable = ExtensionServerTableValueTable.TABLE_NAME;
    }

    @Override
    public boolean hasBeenApplied() {
        return dbType == DBType.SQLITE || // SQLite does not limit varchar lengths
                columnVarcharLength(playerTable, ExtensionPlayerTableValueTable.VALUE_4) >= 250
                        && columnVarcharLength(serverTable, ExtensionServerTableValueTable.VALUE_5) >= 250;
    }

    @Override
    protected void applyPatch() {
        increaseLength(playerTable, ExtensionPlayerTableValueTable.VALUE_1);
        increaseLength(playerTable, ExtensionPlayerTableValueTable.VALUE_2);
        increaseLength(playerTable, ExtensionPlayerTableValueTable.VALUE_3);
        increaseLength(playerTable, ExtensionPlayerTableValueTable.VALUE_4);

        increaseLength(serverTable, ExtensionServerTableValueTable.VALUE_1);
        increaseLength(serverTable, ExtensionServerTableValueTable.VALUE_2);
        increaseLength(serverTable, ExtensionServerTableValueTable.VALUE_3);
        increaseLength(serverTable, ExtensionServerTableValueTable.VALUE_4);
        increaseLength(serverTable, ExtensionServerTableValueTable.VALUE_5);
    }

    private void increaseLength(String table, String column) {
        execute("ALTER TABLE " + table + " MODIFY " + column + " " + Sql.varchar(250));
    }
}
