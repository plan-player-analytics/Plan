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

import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;
import com.djrapitops.plan.storage.database.sql.tables.webuser.*;
import com.djrapitops.plan.storage.database.transactions.events.StoreJoinAddressTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;

/**
 * Transaction that removes everything from the database.
 *
 * @author AuroraLS3
 */
public class RemoveEverythingTransaction extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return false;
    }

    @Override
    protected void applyPatch() {
        clearTable(SettingsTable.TABLE_NAME);
        clearTable(GeoInfoTable.TABLE_NAME);
        clearTable(NicknamesTable.TABLE_NAME);
        clearTable(KillsTable.TABLE_NAME);
        clearTable(WorldTimesTable.TABLE_NAME);
        clearTable(SessionsTable.TABLE_NAME);
        clearTable(JoinAddressTable.TABLE_NAME);
        clearTable(AllowlistBounceTable.TABLE_NAME);
        clearTable(WorldTable.TABLE_NAME);
        clearTable(PingTable.TABLE_NAME);
        clearTable(UserInfoTable.TABLE_NAME);
        clearTable(UsersTable.TABLE_NAME);
        clearTable(TPSTable.TABLE_NAME);
        clearTable(WebGroupToPermissionTable.TABLE_NAME);
        clearTable(WebPermissionTable.TABLE_NAME);
        clearTable(WebGroupTable.TABLE_NAME);
        clearTable(WebUserPreferencesTable.TABLE_NAME);
        clearTable(SecurityTable.TABLE_NAME);
        clearTable(ServerTable.TABLE_NAME);
        clearTable(CookieTable.TABLE_NAME);
        clearTable(ExtensionPlayerValueTable.TABLE_NAME);
        clearTable(ExtensionServerValueTable.TABLE_NAME);
        clearTable(ExtensionGroupsTable.TABLE_NAME);
        clearTable(ExtensionProviderTable.TABLE_NAME);
        clearTable(ExtensionPlayerTableValueTable.TABLE_NAME);
        clearTable(ExtensionServerTableValueTable.TABLE_NAME);
        clearTable(ExtensionTableProviderTable.TABLE_NAME);
        clearTable(ExtensionTabTable.TABLE_NAME);
        clearTable(ExtensionPluginTable.TABLE_NAME);
        clearTable(ExtensionIconTable.TABLE_NAME);

        executeOther(new StoreJoinAddressTransaction(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP));
    }

    private void clearTable(String tableName) {
        execute("DELETE FROM " + tableName);
    }
}