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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionTableProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.ExtensionServerTableValueTable.*;

/**
 * Transaction to store method result of a {@link com.djrapitops.plan.extension.implementation.providers.TableDataProvider}.
 *
 * @author AuroraLS3
 */
public class StoreServerTableResultTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final String providerName;

    private final Table table;

    public StoreServerTableResultTransaction(String pluginName, ServerUUID serverUUID, String providerName, Table table) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.providerName = providerName;
        this.table = table;
    }

    @Override
    protected void performOperations() {
        execute(storeValue());
    }

    private Executable storeValue() {
        return connection -> {
            int maxColumnSize = table.getMaxColumnSize();
            if (maxColumnSize == 0) {
                return false;
            }

            Integer tableID = query(tableID());
            deleteOldValues(tableID).execute(connection);
            insertNewValues(tableID).execute(connection);
            return false;
        };
    }

    private Executable deleteOldValues(int tableID) {
        String sql = DELETE_FROM + TABLE_NAME +
                WHERE + TABLE_ID + "=?" +
                AND + SERVER_UUID + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tableID);
                statement.setString(2, serverUUID.toString());
            }
        };
    }

    private Executable insertNewValues(int tableID) {
        String sql = "INSERT INTO " + TABLE_NAME + '(' +
                TABLE_ID + ',' +
                SERVER_UUID + ',' +
                VALUE_1 + ',' +
                VALUE_2 + ',' +
                VALUE_3 + ',' +
                VALUE_4 + ',' +
                VALUE_5 +
                ") VALUES (?,?,?,?,?,?, ?)";

        return new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                int maxColumnSize = Math.min(table.getMaxColumnSize(), 5); // Limit to maximum 5 columns, or how many column names there are.

                for (Object[] row : table.getRows()) {
                    statement.setInt(1, tableID);
                    statement.setString(2, serverUUID.toString());
                    for (int i = 0; i < maxColumnSize; i++) {
                        Object value = row[i];
                        setStringOrNull(statement, 3 + i, value != null ? StringUtils.truncate(value.toString(), 250) : null);
                    }
                    // Rest are set null if not 5 columns wide.
                    for (int i = maxColumnSize; i < 5; i++) {
                        statement.setNull(3 + i, Types.VARCHAR);
                    }

                    statement.addBatch();
                }
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

    private Query<Integer> tableID() {
        String sql = SELECT + ExtensionTableProviderTable.ID +
                FROM + ExtensionTableProviderTable.TABLE_NAME +
                WHERE + ExtensionTableProviderTable.PROVIDER_NAME + "=?" +
                AND + ExtensionTableProviderTable.PLUGIN_ID + "=" + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID +
                " LIMIT 1";
        return new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionTableProviderTable.set3PluginValuesToStatement(statement, 1, providerName, pluginName, serverUUID);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    int id = set.getInt(ExtensionTableProviderTable.ID);
                    if (!set.wasNull()) {
                        return id;
                    }
                }
                throw new DBOpException("Table Provider was not saved before storing results. Please report this issue. Extension method: " + pluginName + "#" + providerName);
            }
        };
    }
}