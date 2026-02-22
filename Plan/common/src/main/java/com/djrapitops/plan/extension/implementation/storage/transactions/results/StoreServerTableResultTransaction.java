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
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTableProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable.*;

/**
 * Transaction to store method result of a Table.
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

    public StoreServerTableResultTransaction(ProviderInformation information, Parameters parameters, Table value) {
        this(information.getPluginName(), parameters.getServerUUID(), information.getName(), value);
    }

    @Override
    protected void performOperations() {
        execute(storeValue());
    }

    @Override
    protected IsolationLevel getDesiredIsolationLevel() {
        return IsolationLevel.READ_COMMITTED;
    }

    private Executable storeValue() {
        return connection -> {
            int maxColumnSize = table.getMaxColumnSize();
            if (maxColumnSize == 0) {
                return false;
            }

            Integer tableID = query(tableID());

            List<Object[]> rows = table.getRows();
            Integer oldRowCount = query(currentRowCount(tableID));
            int newRowCount = rows.size();

            if (oldRowCount < newRowCount) {
                insertNewRows(tableID, oldRowCount, rows);
                updateRows(tableID, oldRowCount, rows);
            } else if (oldRowCount == newRowCount) {
                // No need to delete or insert rows
                updateRows(tableID, oldRowCount, rows);
            } else {
                // oldRowCount > newRowCount
                deleteOldRows(tableID, newRowCount);
                updateRows(tableID, newRowCount, rows);
            }
            return false;
        };
    }

    private void deleteOldRows(Integer tableID, int afterRow) {
        String sql = DELETE_FROM + TABLE_NAME +
                WHERE + TABLE_ID + "=?" +
                AND + SERVER_UUID + "=?" +
                AND + TABLE_ROW + ">=?"; // Since row count is zero indexed and afterRow is size the value should be removed.

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tableID);
                statement.setString(2, serverUUID.toString());
                statement.setInt(3, afterRow);
            }
        });
    }

    private void insertNewRows(Integer tableID, Integer afterRow, List<Object[]> rows) {
        String sql = INSERT_INTO + TABLE_NAME + '(' +
                TABLE_ID + ',' +
                SERVER_UUID + ',' +
                VALUE_1 + ',' +
                VALUE_2 + ',' +
                VALUE_3 + ',' +
                VALUE_4 + ',' +
                VALUE_5 + ',' +
                TABLE_ROW +
                ") VALUES (?,?,?,?,?,?,?,?)";

        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                int maxColumnSize = Math.min(table.getMaxColumnSize(), 5); // Limit to maximum 5 columns, or how many column names there are.

                statement.setInt(1, tableID);
                for (int rowNumber = afterRow; rowNumber < rows.size(); rowNumber++) {
                    Object[] row = rows.get(rowNumber);
                    statement.setString(2, serverUUID.toString());
                    for (int i = 0; i < maxColumnSize; i++) {
                        Object value = row[i];
                        setStringOrNull(statement, 3 + i, value != null ? StringUtils.truncate(value.toString(), 250) : null);
                    }
                    // Rest are set null if not 5 columns wide.
                    for (int i = maxColumnSize; i < 5; i++) {
                        statement.setNull(3 + i, Types.VARCHAR);
                    }

                    statement.setInt(8, rowNumber);

                    statement.addBatch();
                }
            }
        });
    }

    private void updateRows(Integer tableID, Integer untilRow, List<Object[]> rows) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                VALUE_1 + "=?," +
                VALUE_2 + "=?," +
                VALUE_3 + "=?," +
                VALUE_4 + "=?," +
                VALUE_5 + "=?" +
                WHERE + TABLE_ID + "=?" +
                AND + SERVER_UUID + "=?" +
                AND + TABLE_ROW + "=?";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                int maxColumnSize = Math.min(table.getMaxColumnSize(), 5); // Limit to maximum 5 columns, or how many column names there are.

                statement.setInt(6, tableID);
                for (int rowNumber = 0; rowNumber < untilRow; rowNumber++) {
                    Object[] row = rows.get(rowNumber);

                    for (int valueIndex = 0; valueIndex < maxColumnSize; valueIndex++) {
                        Object value = row[valueIndex];
                        setStringOrNull(statement, 1 + valueIndex, value != null ? StringUtils.truncate(value.toString(), 250) : null);
                    }
                    // Rest are set null if not 5 columns wide.
                    for (int valueIndex = maxColumnSize; valueIndex < 5; valueIndex++) {
                        statement.setNull(1 + valueIndex, Types.VARCHAR);
                    }

                    statement.setString(7, serverUUID.toString());
                    statement.setInt(8, rowNumber);

                    statement.addBatch();
                }
            }
        });
    }

    private Query<Integer> currentRowCount(Integer tableID) {
        String sql = SELECT + "COALESCE(MAX(" + TABLE_ROW + "), -1) as m" +
                FROM + TABLE_NAME +
                WHERE + TABLE_ID + "=?" +
                AND + SERVER_UUID + "=?" + lockForUpdate();
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tableID);
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                // add one to the row number, which is 0 indexed
                return set.next() ? set.getInt("m") + 1 : 0;
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
                LIMIT_1 + lockForUpdate();
        return new QueryStatement<>(sql) {
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