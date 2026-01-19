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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.icon.IconAccessor;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTabTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.building.Sql.TEXT;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable.*;

/**
 * Transaction to store information about a simple {@link DataProvider}.
 *
 * @author AuroraLS3
 */
public class StoreProviderTransaction extends ThrowawayTransaction {

    private final ServerUUID serverUUID;
    private final ProviderInformation info;

    public StoreProviderTransaction(DataProvider<?> provider, ServerUUID serverUUID) {
        this(provider.getProviderInformation(), serverUUID);
    }

    public StoreProviderTransaction(ProviderInformation info, Parameters parameters) {
        this(info, parameters.getServerUUID());
    }

    public StoreProviderTransaction(ProviderInformation info, ServerUUID serverUUID) {
        this.serverUUID = serverUUID;
        this.info = info;
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
        String sql = "UPDATE " + TABLE_NAME +
                " SET " +
                TEXT + "=?," +
                DESCRIPTION + "=?," +
                PRIORITY + "=?," +
                CONDITION + "=?," +
                ICON_ID + "=?," +
                TAB_ID + '=' + ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
                SHOW_IN_PLAYERS_TABLE + "=?," +
                HIDDEN + "=?," +
                PROVIDED_CONDITION + "=?," +
                FORMAT_TYPE + "=?," +
                IS_PLAYER_NAME + "=?" +
                WHERE + PLUGIN_ID + '=' + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID +
                AND + PROVIDER_NAME + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Found for all providers
                statement.setString(1, info.getText());
                Sql.setStringOrNull(statement, 2, info.getDescription().orElse(null));
                statement.setInt(3, info.getPriority());
                Sql.setStringOrNull(statement, 4, info.getCondition().orElse(null));
                statement.setInt(5, IconAccessor.getId(info.getIcon()));
                ExtensionTabTable.set3TabValuesToStatement(statement, 6, info.getTab().orElse(null), info.getPluginName(), serverUUID);
                statement.setBoolean(9, info.isShownInPlayersTable());

                // Specific provider cases
                statement.setBoolean(10, info.isHidden());
                Sql.setStringOrNull(statement, 11, info.getProvidedCondition());
                Sql.setStringOrNull(statement, 12, info.getFormatType().map(FormatType::name).orElse(null));
                statement.setBoolean(13, info.isPlayerName());

                // Find appropriate provider
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 14, info.getPluginName(), serverUUID);
                statement.setString(16, info.getName());
            }
        };
    }

    private Executable insertProvider() {
        String sql = INSERT_INTO + TABLE_NAME + '(' +
                PROVIDER_NAME + ',' +
                TEXT + ',' +
                DESCRIPTION + ',' +
                PRIORITY + ',' +
                CONDITION + ',' +
                SHOW_IN_PLAYERS_TABLE + ',' +
                HIDDEN + ',' +
                PROVIDED_CONDITION + ',' +
                FORMAT_TYPE + ',' +
                IS_PLAYER_NAME + ',' +
                TAB_ID + ',' +
                ICON_ID + ',' +
                PLUGIN_ID +
                ") VALUES (?,?,?,?,?,?,?,?,?,?," +
                ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
                "?," +
                ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ')';
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Found for all providers
                statement.setString(1, info.getName());
                statement.setString(2, info.getText());
                Sql.setStringOrNull(statement, 3, info.getDescription().orElse(null));
                statement.setInt(4, info.getPriority());
                Sql.setStringOrNull(statement, 5, info.getCondition().orElse(null));
                statement.setBoolean(6, info.isShownInPlayersTable());

                // Specific provider cases
                statement.setBoolean(7, info.isHidden());
                Sql.setStringOrNull(statement, 8, info.getProvidedCondition());
                Sql.setStringOrNull(statement, 9, info.getFormatType().map(FormatType::name).orElse(null));
                statement.setBoolean(10, info.isPlayerName());

                // Found for all providers
                ExtensionTabTable.set3TabValuesToStatement(statement, 11, info.getTab().orElse(null), info.getPluginName(), serverUUID);
                statement.setInt(14, IconAccessor.getId(info.getIcon()));
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 15, info.getPluginName(), serverUUID);
            }
        };
    }
}