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

import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.builder.ComponentDataValue;
import com.djrapitops.plan.extension.implementation.builder.StringDataValue;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerValueTable.*;

/**
 * Transaction to store Extension String data for a server.
 *
 * @author AuroraLS3
 */
public class StoreServerStringResultTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final String providerName;

    private final boolean component;
    private final String value;

    public StoreServerStringResultTransaction(ProviderInformation information, Parameters parameters, String value) {
        this.pluginName = information.getPluginName();
        this.providerName = information.getName();
        this.serverUUID = parameters.getServerUUID();
        this.component = information.isComponent();
        this.value = StringUtils.truncate(value, component ? ComponentDataValue.MAX_LENGTH : StringDataValue.MAX_LENGTH);
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
                " SET " +
                (component ? COMPONENT_VALUE : STRING_VALUE) + "=?" +
                WHERE + PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, value);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable insertValue() {
        String sql = INSERT_INTO + TABLE_NAME + "(" +
                (component ? COMPONENT_VALUE : STRING_VALUE) + "," +
                PROVIDER_ID +
                ") VALUES (?," + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, value);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, providerName, pluginName, serverUUID);
            }
        };
    }
}