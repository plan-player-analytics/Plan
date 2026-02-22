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

import com.djrapitops.plan.extension.icon.IconAccessor;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTabTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction for storing {@link TabInformation}s.
 *
 * @author AuroraLS3
 */
public class StoreTabInformationTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final TabInformation tabInformation;

    public StoreTabInformationTransaction(String pluginName, ServerUUID serverUUID, TabInformation tabInformation) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.tabInformation = tabInformation;
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
                " SET " +
                ExtensionTabTable.TAB_PRIORITY + "=?," +
                ExtensionTabTable.ELEMENT_ORDER + "=?," +
                ExtensionTabTable.ICON_ID + "=?" +
                WHERE + ExtensionTabTable.PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID +
                AND + ExtensionTabTable.TAB_NAME + "=?";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tabInformation.getTabPriority());
                statement.setString(2, tabInformation.getSerializedTabElementOrder());
                statement.setInt(3, IconAccessor.getId(tabInformation.getTabIcon()));
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 4, pluginName, serverUUID);
                statement.setString(6, tabInformation.getTabName());
            }
        };
    }

    private Executable insertTab() {
        String sql = INSERT_INTO + ExtensionTabTable.TABLE_NAME + "(" +
                ExtensionTabTable.TAB_NAME + "," +
                ExtensionTabTable.ELEMENT_ORDER + "," +
                ExtensionTabTable.TAB_PRIORITY + "," +
                ExtensionTabTable.ICON_ID + "," +
                ExtensionTabTable.PLUGIN_ID +
                ") VALUES (?,?,?,?," + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tabInformation.getTabName());
                statement.setString(2, tabInformation.getSerializedTabElementOrder());
                statement.setInt(3, tabInformation.getTabPriority());
                statement.setInt(4, IconAccessor.getId(tabInformation.getTabIcon()));
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 5, pluginName, serverUUID);
            }
        };
    }
}