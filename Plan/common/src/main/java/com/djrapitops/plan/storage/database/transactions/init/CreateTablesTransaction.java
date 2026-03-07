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
package com.djrapitops.plan.storage.database.transactions.init;

import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;
import com.djrapitops.plan.storage.database.sql.tables.webuser.*;
import com.djrapitops.plan.storage.database.transactions.events.StoreJoinAddressTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.SecurityTableIdPatch;

/**
 * Transaction that creates the table schema of Plan database.
 *
 * @author AuroraLS3
 */
public class CreateTablesTransaction extends OperationCriticalTransaction {

    public static String[] tableNames() {
        return new String[]{
                ServerTable.TABLE_NAME,
                UsersTable.TABLE_NAME,
                UserInfoTable.TABLE_NAME,
                GeoInfoTable.TABLE_NAME,
                NicknamesTable.TABLE_NAME,
                SessionsTable.TABLE_NAME,
                KillsTable.TABLE_NAME,
                PingTable.TABLE_NAME,
                TPSTable.TABLE_NAME,
                JoinAddressTable.TABLE_NAME,
                WorldTable.TABLE_NAME,
                WorldTimesTable.TABLE_NAME,
                AccessLogTable.TABLE_NAME,
                WebGroupTable.TABLE_NAME,
                WebPermissionTable.TABLE_NAME,
                WebGroupToPermissionTable.TABLE_NAME,
                SecurityTable.TABLE_NAME,
                WebUserPreferencesTable.TABLE_NAME,
                PluginVersionTable.TABLE_NAME,
                AllowlistBounceTable.TABLE_NAME
        };
    }

    @Override
    protected void performOperations() {
        // DBType is required for SQL creation, as MySQL and SQLite primary key format differs.

        // Create statements are run in a specific order as some tables have foreign keys,
        // or had at some point in the past.
        execute(ServerTable.createTableSQL(dbType));
        execute(UsersTable.createTableSQL(dbType));
        execute(UserInfoTable.createTableSQL(dbType));
        execute(GeoInfoTable.createTableSQL(dbType));
        execute(NicknamesTable.createTableSQL(dbType));
        execute(JoinAddressTable.createTableSQL(dbType));
        executeOther(new StoreJoinAddressTransaction(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP));
        execute(SessionsTable.createTableSQL(dbType));
        execute(KillsTable.createTableSQL(dbType));
        execute(PingTable.createTableSQL(dbType));
        execute(TPSTable.createTableSQL(dbType));
        execute(WorldTable.createTableSQL(dbType));
        execute(WorldTimesTable.createTableSQL(dbType));
        execute(SettingsTable.createTableSQL(dbType));
        execute(CookieTable.createTableSQL(dbType));
        execute(AccessLogTable.createTableSql(dbType));
        execute(WebGroupTable.createTableSQL(dbType));
        execute(WebPermissionTable.createTableSQL(dbType));
        execute(WebGroupToPermissionTable.createTableSQL(dbType));
        execute(SecurityTable.createTableSQL(dbType));
        // Ensure plan_security has id column
        executeOther(new SecurityTableIdPatch());
        execute(WebUserPreferencesTable.createTableSQL(dbType));
        execute(PluginVersionTable.createTableSQL(dbType));
        execute(AllowlistBounceTable.createTableSQL(dbType));

        // DataExtension tables
        execute(ExtensionIconTable.createTableSQL(dbType));
        execute(ExtensionPluginTable.createTableSQL(dbType));
        execute(ExtensionTabTable.createTableSQL(dbType));
        execute(ExtensionProviderTable.createTableSQL(dbType));
        execute(ExtensionPlayerValueTable.createTableSQL(dbType));
        execute(ExtensionServerValueTable.createTableSQL(dbType));
        execute(ExtensionTableProviderTable.createTableSQL(dbType));
        execute(ExtensionPlayerTableValueTable.createTableSQL(dbType));
        execute(ExtensionServerTableValueTable.createTableSQL(dbType));
        execute(ExtensionGroupsTable.createTableSQL(dbType));
    }
}