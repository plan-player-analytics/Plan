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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author AuroraLS3
 */
public class RemoveServerTransaction extends ThrowawayTransaction {

    private final ServerUUID serverUUID;

    public RemoveServerTransaction(ServerUUID serverUUID) {this.serverUUID = serverUUID;}

    @Override
    protected void performOperations() {
        deleteExtensionTables();

        deleteFromServerTable(NicknamesTable.TABLE_NAME);
        deleteFromServerTable(SettingsTable.TABLE_NAME);
        deleteFromServerTable(KillsTable.TABLE_NAME);
        deleteFromServerIdTable(WorldTimesTable.TABLE_NAME);
        deleteFromServerTable(WorldTable.TABLE_NAME);

        deleteFromServerIdTable(SessionsTable.TABLE_NAME);
        deleteFromServerIdTable(PluginVersionTable.TABLE_NAME);
        deleteFromServerIdTable(AllowlistBounceTable.TABLE_NAME);
        deleteFromServerIdTable(UserInfoTable.TABLE_NAME);
        deleteFromServerIdTable(TPSTable.TABLE_NAME);
        deleteFromServerIdTable(PingTable.TABLE_NAME);
        deleteServer();
    }

    private void deleteExtensionTables() {
        String selectProviderIdsOfServer = SELECT + "p." + ExtensionProviderTable.ID +
                FROM + ExtensionProviderTable.TABLE_NAME + " p" +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " pl on pl." + ExtensionPluginTable.ID + "=p." + ExtensionProviderTable.PLUGIN_ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?" + lockForUpdate();
        String selectTableIdsOfServer = SELECT + "p." + ExtensionTableProviderTable.ID +
                FROM + ExtensionTableProviderTable.TABLE_NAME + " p" +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " pl on pl." + ExtensionPluginTable.ID + "=p." + ExtensionTableProviderTable.PLUGIN_ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?" + lockForUpdate();
        String selectPluginIdOfServer = SELECT + "p." + ExtensionPluginTable.ID +
                FROM + ExtensionPluginTable.TABLE_NAME + " p" +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?" + lockForUpdate();

        // Provider values
        String in = " IN (";
        executeServerRemoval(DELETE_FROM + ExtensionGroupsTable.TABLE_NAME + WHERE + ExtensionGroupsTable.PROVIDER_ID + in + selectProviderIdsOfServer + ')');
        executeServerRemoval(DELETE_FROM + ExtensionServerValueTable.TABLE_NAME + WHERE + ExtensionServerValueTable.PROVIDER_ID + in + selectProviderIdsOfServer + ')');
        executeServerRemoval(DELETE_FROM + ExtensionPlayerValueTable.TABLE_NAME + WHERE + ExtensionPlayerValueTable.PROVIDER_ID + in + selectProviderIdsOfServer + ')');
        executeServerRemoval(DELETE_FROM + ExtensionServerTableValueTable.TABLE_NAME + WHERE + ExtensionServerTableValueTable.TABLE_ID + in + selectTableIdsOfServer + ')');
        executeServerRemoval(DELETE_FROM + ExtensionPlayerTableValueTable.TABLE_NAME + WHERE + ExtensionPlayerTableValueTable.TABLE_ID + in + selectTableIdsOfServer + ')');
        // Providers
        executeServerRemoval(DELETE_FROM + ExtensionProviderTable.TABLE_NAME + WHERE + ExtensionProviderTable.PLUGIN_ID + in + selectPluginIdOfServer + ')');
        executeServerRemoval(DELETE_FROM + ExtensionTableProviderTable.TABLE_NAME + WHERE + ExtensionTableProviderTable.PLUGIN_ID + in + selectPluginIdOfServer + ')');
        // Tabs
        executeServerRemoval(DELETE_FROM + ExtensionTabTable.TABLE_NAME + WHERE + ExtensionTabTable.PLUGIN_ID + in + selectPluginIdOfServer + ')');
        // Plugins
        executeServerRemoval(DELETE_FROM + ExtensionPluginTable.TABLE_NAME + WHERE + ExtensionPluginTable.SERVER_UUID + "=?");
    }

    private void executeServerRemoval(String sql) {
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }
        });
    }

    private void deleteServer() {
        executeServerRemoval(DELETE_FROM + ServerTable.TABLE_NAME + WHERE + ServerTable.SERVER_UUID + "=?");
    }

    private void deleteFromServerIdTable(String tableName) {
        executeServerRemoval(DELETE_FROM + tableName + WHERE + "server_id=" + ServerTable.SELECT_SERVER_ID);
    }

    private void deleteFromServerTable(String tableName) {
        executeServerRemoval(DELETE_FROM + tableName + WHERE + "server_uuid=?");
    }
}
