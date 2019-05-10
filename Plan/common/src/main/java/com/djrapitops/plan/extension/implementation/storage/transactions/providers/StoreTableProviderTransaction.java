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
package com.djrapitops.plan.extension.implementation.storage.transactions.providers;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.db.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.db.sql.tables.ExtensionTabTable;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.table.Table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.AND;
import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;
import static com.djrapitops.plan.db.sql.tables.ExtensionTableProviderTable.*;

/**
 * Transaction to store information about a {@link com.djrapitops.plan.extension.implementation.providers.TableDataProvider}.
 *
 * @author Rsl1122
 */
public class StoreTableProviderTransaction extends Transaction {

    private final UUID serverUUID;
    private final ProviderInformation providerInformation;
    private final Color tableColor;
    private final Table table;

    public StoreTableProviderTransaction(UUID serverUUID, ProviderInformation providerInformation, Color tableColor, Table table) {
        this.providerInformation = providerInformation;
        this.tableColor = tableColor;
        this.table = table;
        this.serverUUID = serverUUID;
    }

    @Override
    protected void performOperations() {
        execute(storeProvider());
    }

    private Executable storeProvider() {
        return connection -> {
            if (!updateProvider().execute(connection)) {
                return insertProvider().execute(connection);
            }
            return false;
        };
    }

    private Executable updateProvider() {
        String[] columns = table.getColumns();
        Icon[] icons = table.getIcons();

        String sql = "UPDATE " + TABLE_NAME + " SET " +
                COLOR + "=?," +
                COL_1 + "=?," +
                COL_2 + "=?," +
                COL_3 + "=?," +
                COL_4 + "=?," +
                COL_5 + "=?," +
                CONDITION + "=?," +
                TAB_ID + '=' + ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
                ICON_1_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_2_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_3_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_4_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_5_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID +
                WHERE + PROVIDER_NAME + "=?" +
                AND + PLUGIN_ID + '=' + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableColor.name());
                setStringOrNull(statement, 2, columns[0]);
                setStringOrNull(statement, 3, columns[1]);
                setStringOrNull(statement, 4, columns[2]);
                setStringOrNull(statement, 5, columns[3]);
                setStringOrNull(statement, 6, columns[4]);
                setStringOrNull(statement, 7, providerInformation.getCondition().orElse(null));
                ExtensionTabTable.set3TabValuesToStatement(statement, 8, providerInformation.getTab().orElse("No Tab"), providerInformation.getPluginName(), serverUUID);
                ExtensionIconTable.set3IconValuesToStatement(statement, 11, icons[0]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 14, icons[1]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 17, icons[2]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 20, icons[3]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 23, icons[4]);
                statement.setString(26, providerInformation.getName());
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 27, providerInformation.getPluginName(), serverUUID);
            }
        };
    }

    private Executable insertProvider() {
        String[] columns = table.getColumns();
        Icon[] icons = table.getIcons();

        String sql = "INSERT INTO " + TABLE_NAME + '(' +
                PROVIDER_NAME + ',' +
                COLOR + ',' +
                COL_1 + ',' +
                COL_2 + ',' +
                COL_3 + ',' +
                COL_4 + ',' +
                COL_5 + ',' +
                CONDITION + ',' +
                TAB_ID + ',' +
                PLUGIN_ID + ',' +
                ICON_1_ID + ',' +
                ICON_2_ID + ',' +
                ICON_3_ID + ',' +
                ICON_4_ID + ',' +
                ICON_5_ID +
                ") VALUES (?,?,?,?,?,?,?,?," +
                ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
                ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ')';

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, providerInformation.getName());
                statement.setString(2, tableColor.name());
                setStringOrNull(statement, 3, columns[0]);
                setStringOrNull(statement, 4, columns[1]);
                setStringOrNull(statement, 5, columns[2]);
                setStringOrNull(statement, 6, columns[3]);
                setStringOrNull(statement, 7, columns[4]);
                setStringOrNull(statement, 8, providerInformation.getCondition().orElse(null));
                ExtensionTabTable.set3TabValuesToStatement(statement, 9, providerInformation.getTab().orElse("No Tab"), providerInformation.getPluginName(), serverUUID);
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 12, providerInformation.getPluginName(), serverUUID);
                ExtensionIconTable.set3IconValuesToStatement(statement, 14, icons[0]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 17, icons[1]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 20, icons[2]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 23, icons[3]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 26, icons[4]);
            }
        };
    }

    private void setStringOrNull(PreparedStatement statement, int index, String value) throws SQLException {
        if (value != null) {
            statement.setString(index, value);
        } else {
            statement.setNull(index, Types.VARCHAR);
        }
    }
}