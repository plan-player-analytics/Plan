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

import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionTabTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.parsing.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.parsing.Sql.WHERE;
import static com.djrapitops.plan.storage.database.sql.tables.ExtensionProviderTable.*;

/**
 * Transaction to store information about a {@link com.djrapitops.plan.extension.implementation.providers.GroupDataProvider}.
 *
 * @author Rsl1122
 */
public class StoreGroupProviderTransaction extends ThrowawayTransaction {

    private final UUID serverUUID;
    private ProviderInformation providerInformation;

    public StoreGroupProviderTransaction(DataProvider<String[]> provider, UUID serverUUID) {
        this.serverUUID = serverUUID;
        providerInformation = provider.getProviderInformation();
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
                CONDITION + "=?," +
                TAB_ID + "=" + ExtensionTabTable.STATEMENT_SELECT_TAB_ID + "," +
                ICON_ID + "=" + ExtensionIconTable.STATEMENT_SELECT_ICON_ID +
                WHERE + PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID +
                AND + PROVIDER_NAME + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, providerInformation.getText());
                Optional<String> condition = providerInformation.getCondition();
                if (condition.isPresent()) {
                    statement.setString(2, condition.get());
                } else {
                    statement.setNull(2, Types.VARCHAR);
                }
                ExtensionTabTable.set3TabValuesToStatement(statement, 3, providerInformation.getTab().orElse("No Tab"), providerInformation.getPluginName(), serverUUID);
                ExtensionIconTable.set3IconValuesToStatement(statement, 6, providerInformation.getIcon());
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 9, providerInformation.getPluginName(), serverUUID);
                statement.setString(11, providerInformation.getName());
            }
        };
    }

    private Executable insertProvider() {
        String sql = "INSERT INTO " + TABLE_NAME + "(" +
                PROVIDER_NAME + "," +
                TEXT + "," +
                CONDITION + "," +
                SHOW_IN_PLAYERS_TABLE + "," +
                TAB_ID + "," +
                ICON_ID + "," +
                PLUGIN_ID +
                ") VALUES (?,?,?,?," +
                ExtensionTabTable.STATEMENT_SELECT_TAB_ID + "," +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + "," +
                ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, providerInformation.getName());
                statement.setString(2, providerInformation.getText());
                Optional<String> condition = providerInformation.getCondition();
                if (condition.isPresent()) {
                    statement.setString(3, condition.get());
                } else {
                    statement.setNull(3, Types.VARCHAR);
                }
                statement.setBoolean(4, providerInformation.isShownInPlayersTable());
                ExtensionTabTable.set3TabValuesToStatement(statement, 5, providerInformation.getTab().orElse("No Tab"), providerInformation.getPluginName(), serverUUID);
                ExtensionIconTable.set3IconValuesToStatement(statement, 8, providerInformation.getIcon());
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 11, providerInformation.getPluginName(), serverUUID);
            }
        };
    }
}