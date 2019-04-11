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

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.access.*;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.ExtensionTableProviderTable;
import com.djrapitops.plan.extension.table.Table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.AND;
import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;
import static com.djrapitops.plan.db.sql.tables.ExtensionPlayerTableValueTable.*;

/**
 * Transaction to store method result of a {@link com.djrapitops.plan.extension.implementation.providers.TableDataProvider}.
 *
 * @author Rsl1122
 */
public class StorePlayerTableResultTransaction extends Transaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final String providerName;
    private final UUID playerUUID;

    private final Table table;

    public StorePlayerTableResultTransaction(String pluginName, UUID serverUUID, String providerName, UUID playerUUID, Table table) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.providerName = providerName;
        this.playerUUID = playerUUID;
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
        String sql = "DELETE FROM " + TABLE_NAME +
                WHERE + TABLE_ID + "=?" +
                AND + USER_UUID + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tableID);
                statement.setString(2, playerUUID.toString());
            }
        };
    }

    private Executable insertNewValues(int tableID) {
        String sql = "INSERT INTO " + TABLE_NAME + '(' +
                TABLE_ID + ',' +
                USER_UUID + ',' +
                VALUE_1 + ',' +
                VALUE_2 + ',' +
                VALUE_3 + ',' +
                VALUE_4 +
                ") VALUES (?,?,?,?,?,?)";

        return new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                int maxColumnSize = Math.min(table.getMaxColumnSize(), 4); // Limit to maximum 4 columns, or how many column names there are.

                for (Object[] row : table.getRows()) {
                    statement.setInt(1, tableID);
                    statement.setString(2, playerUUID.toString());
                    for (int i = 0; i < maxColumnSize; i++) {
                        Object value = row[i];
                        setStringOrNull(statement, 3 + i, value != null ? value.toString() : null);
                    }
                    // Rest are set null if not 4 columns wide.
                    for (int i = maxColumnSize; i < 4; i++) {
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
        return new QueryStatement<Integer>(ExtensionTableProviderTable.STATEMENT_SELECT_TABLE_ID) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionTableProviderTable.set3PluginValuesToStatement(statement, 1, providerName, pluginName, serverUUID);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                int id = set.getInt(ExtensionTableProviderTable.ID);
                if (set.wasNull()) {
                    throw new DBOpException("Table Provider was not saved before storing results. Please report this issue. Extension method: " + pluginName + "#" + providerName);
                }
                return id;
            }
        };
    }
}