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
package com.djrapitops.plan.extension.implementation.storage.transactions.results;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to remove method results that correspond to {@link com.djrapitops.plan.extension.annotation.InvalidateMethod} annotations.
 *
 * @author AuroraLS3
 */
public class RemoveInvalidResultsTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final Collection<String> invalidatedMethods;

    public RemoveInvalidResultsTransaction(String pluginName, ServerUUID serverUUID, Collection<String> invalidatedMethods) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.invalidatedMethods = invalidatedMethods;
    }

    @Override
    protected void performOperations() {
        for (String invalidatedMethod : invalidatedMethods) {
            execute(deleteInvalidPlayerMethodResults(invalidatedMethod));
            execute(deleteInvalidServerMethodResults(invalidatedMethod));
            execute(deleteInvalidMethodProvider(invalidatedMethod));

            execute(deleteInvalidPlayerTableResults(invalidatedMethod));
            execute(deleteInvalidServerTableResults(invalidatedMethod));
            execute(deleteInvalidTableProvider(invalidatedMethod));
        }
    }

    private Executable deleteInvalidPlayerMethodResults(String invalidMethod) {
        String sql = DELETE_FROM + ExtensionPlayerValueTable.TABLE_NAME +
                WHERE + ExtensionPlayerValueTable.PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, invalidMethod, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteInvalidServerMethodResults(String invalidMethod) {
        String sql = DELETE_FROM + ExtensionServerValueTable.TABLE_NAME +
                WHERE + ExtensionServerValueTable.PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, invalidMethod, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteInvalidPlayerTableResults(String invalidMethod) {
        String sql = DELETE_FROM + ExtensionPlayerTableValueTable.TABLE_NAME +
                WHERE + ExtensionPlayerTableValueTable.TABLE_ID + "=" + ExtensionTableProviderTable.STATEMENT_SELECT_TABLE_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionTableProviderTable.set3PluginValuesToStatement(statement, 1, invalidMethod, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteInvalidServerTableResults(String invalidMethod) {
        String sql = DELETE_FROM + ExtensionServerTableValueTable.TABLE_NAME +
                WHERE + ExtensionServerTableValueTable.TABLE_ID + "=" + ExtensionTableProviderTable.STATEMENT_SELECT_TABLE_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionTableProviderTable.set3PluginValuesToStatement(statement, 1, invalidMethod, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteInvalidMethodProvider(String invalidMethod) {
        String sql = DELETE_FROM + ExtensionProviderTable.TABLE_NAME +
                WHERE + ExtensionProviderTable.PROVIDER_NAME + "=?" +
                AND + ExtensionProviderTable.PLUGIN_ID + '=' + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, invalidMethod);
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 2, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteInvalidTableProvider(String invalidMethod) {
        String sql = DELETE_FROM + ExtensionTableProviderTable.TABLE_NAME +
                WHERE + ExtensionTableProviderTable.PROVIDER_NAME + "=?" +
                AND + ExtensionTableProviderTable.PLUGIN_ID + '=' + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, invalidMethod);
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 2, pluginName, serverUUID);
            }
        };
    }
}