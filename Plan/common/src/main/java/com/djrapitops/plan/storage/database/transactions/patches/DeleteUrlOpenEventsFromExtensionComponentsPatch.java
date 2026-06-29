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

import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerValueTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author AuroraLS3
 */
public class DeleteUrlOpenEventsFromExtensionComponentsPatch extends Patch {

    String[] invalidStrings = new String[]{
            "javascript", "clickEvent", "hoverEvent", "open_url", "copy_to_clipboard", "\"action\"", "&#", "\\"
    };

    @Override
    public boolean hasBeenApplied() {
        for (String invalidString : invalidStrings) {
            String playerSql = SELECT + ExtensionPlayerValueTable.COMPONENT_VALUE +
                    FROM + ExtensionPlayerValueTable.TABLE_NAME +
                    WHERE + ExtensionPlayerValueTable.COMPONENT_VALUE + " LIKE '%" + invalidString + "%'";
            if (query(db -> db.queryOptional(playerSql, row -> row.getString(1)))
                    .isPresent()) {
                return false;
            }
            String serverSql = SELECT + ExtensionServerValueTable.COMPONENT_VALUE +
                    FROM + ExtensionServerValueTable.TABLE_NAME +
                    WHERE + ExtensionServerValueTable.COMPONENT_VALUE + " LIKE '%" + invalidString + "%'";
            if (query(db -> db.queryOptional(serverSql, row -> row.getString(1)))
                    .isPresent()) {
                return false;
            }
            String nullCharSql = SELECT + ExtensionPlayerValueTable.COMPONENT_VALUE +
                    FROM + ExtensionPlayerValueTable.TABLE_NAME +
                    WHERE + "INSTR(" + ExtensionPlayerValueTable.COMPONENT_VALUE + ", CHAR(0))";
            if (query(db -> db.queryOptional(nullCharSql, row -> row.getString(1)))
                    .isPresent()) {
                return false;
            }
            String nullCharServerSql = SELECT + ExtensionServerValueTable.COMPONENT_VALUE +
                    FROM + ExtensionPlayerValueTable.TABLE_NAME +
                    WHERE + "INSTR(" + ExtensionServerValueTable.COMPONENT_VALUE + ", CHAR(0))";
            if (query(db -> db.queryOptional(nullCharServerSql, row -> row.getString(1)))
                    .isPresent()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void applyPatch() {
        for (String invalidString : invalidStrings) {
            execute("DELETE FROM " + ExtensionPlayerValueTable.TABLE_NAME +
                    WHERE + ExtensionPlayerValueTable.COMPONENT_VALUE + " LIKE '%" + invalidString + "%'" +
                    OR + "INSTR(" + ExtensionPlayerValueTable.COMPONENT_VALUE + ", CHAR(0))");
            execute("DELETE FROM " + ExtensionServerValueTable.TABLE_NAME +
                    WHERE + ExtensionServerValueTable.COMPONENT_VALUE + " LIKE '%" + invalidString + "%'" +
                    OR + "INSTR(" + ExtensionServerValueTable.COMPONENT_VALUE + ", CHAR(0))");
        }
    }
}
