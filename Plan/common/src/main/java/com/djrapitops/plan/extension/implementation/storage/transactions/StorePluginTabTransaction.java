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
package com.djrapitops.plan.extension.implementation.storage.transactions;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.db.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.db.sql.tables.ExtensionTabTable;
import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.implementation.PluginTab;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.AND;
import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;

/**
 * Transaction for storing {@link com.djrapitops.plan.extension.implementation.PluginTab}s.
 *
 * @author Rsl1122
 */
public class StorePluginTabTransaction extends Transaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final PluginTab pluginTab;

    public StorePluginTabTransaction(String pluginName, UUID serverUUID, PluginTab pluginTab) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.pluginTab = pluginTab;
    }

    @Override
    protected void performOperations() {
        execute(storeTab());
    }

    private Executable storeTab() {
        return connection -> {
            if (!updateTab().execute(connection)) {
                return insertTab().execute(connection);
            }
            return false;
        };
    }

    private Executable updateTab() {
        String sql = "UPDATE " + ExtensionTabTable.TABLE_NAME +
                " SET (" +
                ExtensionTabTable.TAB_PRIORITY + "=?," +
                ExtensionTabTable.ELEMENT_ORDER + "=?," +
                ExtensionTabTable.ICON_ID + "=" + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + "," +
                ")" + WHERE + ExtensionTabTable.PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID +
                AND + ExtensionTabTable.TAB_NAME + "=?";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, pluginTab.getTabPriority());
                statement.setString(2, pluginTab.getTabElementOrder().map(ElementOrder::serialize).orElse(null));
                ExtensionIconTable.setIconValuesToStatement(statement, 3, pluginTab.getTabIcon());
                ExtensionPluginTable.setPluginValuesToStatement(statement, 6, pluginName, serverUUID);
                statement.setString(8, pluginTab.getTabName());
            }
        };
    }

    private Executable insertTab() {
        String sql = "INSERT INFO " + ExtensionTabTable.TABLE_NAME + "(" +
                ExtensionTabTable.TAB_NAME + "," +
                ExtensionTabTable.ELEMENT_ORDER + "," +
                ExtensionTabTable.TAB_PRIORITY + "," +
                ExtensionTabTable.ICON_ID +
                ExtensionTabTable.PLUGIN_ID +
                ") VALUES (?,?,?," + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + "," + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, pluginTab.getTabName());
                statement.setString(2, pluginTab.getTabElementOrder().map(ElementOrder::serialize).orElse(null));
                statement.setInt(3, pluginTab.getTabPriority());
                ExtensionIconTable.setIconValuesToStatement(statement, 4, pluginTab.getTabIcon());
                ExtensionPluginTable.setPluginValuesToStatement(statement, 7, pluginName, serverUUID);
            }
        };
    }
}