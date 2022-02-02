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

import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.MethodType;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.extension.table.TableColumnFormat;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionTabTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;
import static com.djrapitops.plan.storage.database.sql.tables.ExtensionTableProviderTable.*;

/**
 * Transaction to store information about a Table.
 *
 * @author AuroraLS3
 */
public class StoreTableProviderTransaction extends ThrowawayTransaction {

    private final ServerUUID serverUUID;
    private final ProviderInformation information;
    private final Table table;
    private final boolean forPlayer;

    public StoreTableProviderTransaction(ProviderInformation information, Parameters parameters, Table table) {
        this(parameters.getServerUUID(), information, table, parameters.getMethodType() == MethodType.PLAYER);
    }

    public StoreTableProviderTransaction(ServerUUID serverUUID, ProviderInformation information, Table table, boolean forPlayer) {
        this.information = information;
        this.table = table;
        this.serverUUID = serverUUID;
        this.forPlayer = forPlayer;
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
        String[] columns = table.getColumns();
        Icon[] icons = table.getIcons();
        TableColumnFormat[] formats = table.getTableColumnFormats();

        String sql = "UPDATE " + TABLE_NAME + " SET " +
                COLOR + "=?," +
                COL_1 + "=?," +
                COL_2 + "=?," +
                COL_3 + "=?," +
                COL_4 + "=?," +
                COL_5 + "=?," +
                CONDITION + "=?," +
                TAB_ID + '=' + ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
                ICON_1_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_2_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_3_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_4_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ICON_5_ID + '=' + ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                FORMAT_1 + "=?," +
                FORMAT_2 + "=?," +
                FORMAT_3 + "=?," +
                FORMAT_4 + "=?," +
                FORMAT_5 + "=?," +
                VALUES_FOR + "=?" +
                WHERE + PROVIDER_NAME + "=?" +
                AND + PLUGIN_ID + '=' + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, information.getTableColor().name());
                setStringOrNull(statement, 2, columns[0]);
                setStringOrNull(statement, 3, columns[1]);
                setStringOrNull(statement, 4, columns[2]);
                setStringOrNull(statement, 5, columns[3]);
                setStringOrNull(statement, 6, columns[4]);
                setStringOrNull(statement, 7, information.getCondition().orElse(null));
                ExtensionTabTable.set3TabValuesToStatement(statement, 8, information.getTab().orElse("No Tab"), information.getPluginName(), serverUUID);
                ExtensionIconTable.set3IconValuesToStatement(statement, 11, icons[0]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 14, icons[1]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 17, icons[2]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 20, icons[3]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 23, icons[4]);
                setStringOrNull(statement, 26, formats[0] == TableColumnFormat.NONE ? null : formats[0].name());
                setStringOrNull(statement, 27, formats[1] == TableColumnFormat.NONE ? null : formats[1].name());
                setStringOrNull(statement, 28, formats[2] == TableColumnFormat.NONE ? null : formats[2].name());
                setStringOrNull(statement, 29, formats[3] == TableColumnFormat.NONE ? null : formats[3].name());
                setStringOrNull(statement, 30, formats[4] == TableColumnFormat.NONE ? null : formats[4].name());
                statement.setInt(31, forPlayer ? VALUES_FOR_PLAYER : VALUES_FOR_SERVER);
                statement.setString(32, information.getName());
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 33, information.getPluginName(), serverUUID);
            }
        };
    }

    private Executable insertProvider() {
        String[] columns = table.getColumns();
        Icon[] icons = table.getIcons();
        TableColumnFormat[] formats = table.getTableColumnFormats();

        String sql = "INSERT INTO " + TABLE_NAME + '(' +
                PROVIDER_NAME + ',' +
                COLOR + ',' +
                COL_1 + ',' +
                COL_2 + ',' +
                COL_3 + ',' +
                COL_4 + ',' +
                COL_5 + ',' +
                CONDITION + ',' +
                TAB_ID + ',' +
                PLUGIN_ID + ',' +
                ICON_1_ID + ',' +
                ICON_2_ID + ',' +
                ICON_3_ID + ',' +
                ICON_4_ID + ',' +
                ICON_5_ID + ',' +
                FORMAT_1 + ',' +
                FORMAT_2 + ',' +
                FORMAT_3 + ',' +
                FORMAT_4 + ',' +
                FORMAT_5 + ',' +
                VALUES_FOR +
                ") VALUES (?,?,?,?,?,?,?,?," +
                ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
                ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                ExtensionIconTable.STATEMENT_SELECT_ICON_ID + ',' +
                "?,?,?,?,?," +
                "?)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, information.getName());
                statement.setString(2, information.getTableColor().name());
                setStringOrNull(statement, 3, columns[0]);
                setStringOrNull(statement, 4, columns[1]);
                setStringOrNull(statement, 5, columns[2]);
                setStringOrNull(statement, 6, columns[3]);
                setStringOrNull(statement, 7, columns[4]);
                setStringOrNull(statement, 8, information.getCondition().orElse(null));
                ExtensionTabTable.set3TabValuesToStatement(statement, 9, information.getTab().orElse("No Tab"), information.getPluginName(), serverUUID);
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 12, information.getPluginName(), serverUUID);
                ExtensionIconTable.set3IconValuesToStatement(statement, 14, icons[0]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 17, icons[1]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 20, icons[2]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 23, icons[3]);
                ExtensionIconTable.set3IconValuesToStatement(statement, 26, icons[4]);
                setStringOrNull(statement, 29, formats[0] == TableColumnFormat.NONE ? null : formats[0].name());
                setStringOrNull(statement, 30, formats[1] == TableColumnFormat.NONE ? null : formats[1].name());
                setStringOrNull(statement, 31, formats[2] == TableColumnFormat.NONE ? null : formats[2].name());
                setStringOrNull(statement, 32, formats[3] == TableColumnFormat.NONE ? null : formats[3].name());
                setStringOrNull(statement, 33, formats[4] == TableColumnFormat.NONE ? null : formats[4].name());
                statement.setInt(34, forPlayer ? VALUES_FOR_PLAYER : VALUES_FOR_SERVER);
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
}