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

import com.djrapitops.plan.extension.implementation.builder.StringDataValue;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerValueTable;

/**
 * Increases the length of Strings in extension strings to 250 to avoid cutoffs and exceptions.
 *
 * @author AuroraLS3
 */
public class ExtensionStringValueLengthPatch extends Patch {

    private final String playerTable;
    private final String serverTable;

    public ExtensionStringValueLengthPatch() {
        playerTable = ExtensionPlayerValueTable.TABLE_NAME;
        serverTable = ExtensionServerValueTable.TABLE_NAME;
    }

    @Override
    public boolean hasBeenApplied() {
        return dbType == DBType.SQLITE || // SQLite does not limit varchar lengths
                columnVarcharLength(playerTable, ExtensionPlayerValueTable.STRING_VALUE) >= StringDataValue.MAX_LENGTH
                        && columnVarcharLength(serverTable, ExtensionServerValueTable.STRING_VALUE) >= StringDataValue.MAX_LENGTH;
    }

    @Override
    protected void applyPatch() {
        increaseLength(playerTable, ExtensionPlayerValueTable.STRING_VALUE);
        increaseLength(serverTable, ExtensionServerValueTable.STRING_VALUE);
    }

    private void increaseLength(String table, String column) {
        execute("ALTER TABLE " + table + " MODIFY " + column + " " + Sql.varchar(StringDataValue.MAX_LENGTH));
    }
}
