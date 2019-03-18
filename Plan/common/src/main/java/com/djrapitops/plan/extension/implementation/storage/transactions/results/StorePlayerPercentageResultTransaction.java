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

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.ExtensionProviderTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.AND;
import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;
import static com.djrapitops.plan.db.sql.tables.ExtensionPlayerValueTable.*;

/**
 * Transaction to store method result of a {@link com.djrapitops.plan.extension.implementation.providers.PercentageDataProvider}.
 *
 * @author Rsl1122
 */
public class StorePlayerPercentageResultTransaction extends Transaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final String providerName;
    private final UUID playerUUID;

    private final double value;

    public StorePlayerPercentageResultTransaction(String pluginName, UUID serverUUID, String providerName, UUID playerUUID, double value) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.providerName = providerName;
        this.playerUUID = playerUUID;
        this.value = value;
    }

    @Override
    protected void performOperations() {
        execute(storeValue());
    }

    private Executable storeValue() {
        return connection -> {
            if (!updateValue().execute(connection)) {
                return insertValue().execute(connection);
            }
            return false;
        };
    }

    private Executable updateValue() {
        String sql = "UPDATE " + TABLE_NAME +
                " SET (" +
                PERCENTAGE_VALUE + "=?" +
                ")" + WHERE + USER_UUID + "=?" +
                AND + PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setDouble(1, value);
                statement.setString(2, playerUUID.toString());
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable insertValue() {
        String sql = "INSERT INTO " + TABLE_NAME + "(" +
                PERCENTAGE_VALUE + "," +
                USER_UUID + "," +
                PROVIDER_ID +
                ") VALUES (?,?," + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setDouble(1, value);
                statement.setString(2, playerUUID.toString());
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, providerName, pluginName, serverUUID);
            }
        };
    }
}