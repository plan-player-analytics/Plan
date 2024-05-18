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
package com.djrapitops.plan.storage.database.transactions.commands;

import com.djrapitops.plan.storage.database.sql.tables.webuser.SecurityTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupToPermissionTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebUserPreferencesTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;

/**
 * Transaction that removes all web groups from the database.
 *
 * @author AuroraLS3
 */
public class RemoveWebGroupsTransaction extends Transaction {

    @Override
    protected void performOperations() {
        clearTable(WebUserPreferencesTable.TABLE_NAME);
        clearTable(SecurityTable.TABLE_NAME);
        clearTable(WebGroupToPermissionTable.TABLE_NAME);
        clearTable(WebGroupTable.TABLE_NAME);
    }

    private void clearTable(String tableName) {
        execute("DELETE FROM " + tableName);
    }
}