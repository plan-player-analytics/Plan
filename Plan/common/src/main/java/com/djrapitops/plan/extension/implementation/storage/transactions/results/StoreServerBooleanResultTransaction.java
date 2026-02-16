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
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerValueTable.*;

/**
 * Transaction to store method result of a boolean.
 *
 * @author AuroraLS3
 */
public class StoreServerBooleanResultTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final String providerName;

    private final boolean value;

    public StoreServerBooleanResultTransaction(String pluginName, ServerUUID serverUUID, String providerName, boolean value) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.providerName = providerName;
        this.value = value;
    }

    public StoreServerBooleanResultTransaction(ProviderInformation information, Parameters parameters, boolean value) {
        this(information.getPluginName(),
                parameters.getServerUUID(),
                information.getName(),
                value
        );
    }

    @Override
    protected void performOperations() {
        execute(storeValue());
        commitMidTransaction();
        execute(deleteUnsatisfiedConditionalResults());
        execute(deleteUnsatisfiedConditionalTables());
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
                BOOLEAN_VALUE + "=?" +
                WHERE + PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, value);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable insertValue() {
        String sql = INSERT_INTO + TABLE_NAME + "(" +
                BOOLEAN_VALUE + "," +
                PROVIDER_ID +
                ") VALUES (?," + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, value);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteUnsatisfiedConditionalResults() {
        List<Integer> providerIds = selectUnfulfilledProviderIds();
        if (providerIds.isEmpty()) return Executable.empty();

        @Language("SQL") String deleteUnsatisfiedValues = "DELETE FROM plan_extension_server_values " +
                "WHERE provider_id IN (" + Sql.nParameters(providerIds.size()) + ")";

        return new ExecStatement(deleteUnsatisfiedValues) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (int i = 0; i < providerIds.size(); i++) {
                    statement.setInt(i + 1, providerIds.get(i));
                }
            }
        };
    }

    private List<Integer> selectUnfulfilledProviderIds() {
        // Need to select:
        // Provider IDs where condition of this provider is met
        @Language("SQL") String selectUnsatisfiedProviderIds = "SELECT unfulfilled.id " +
                "FROM plan_extension_providers indb " +
                "JOIN plan_extension_providers unfulfilled ON unfulfilled.condition_name=" +
                // This gives the unfulfilled condition, eg. if value is true not_condition is unfulfilled.
                (value ? Sql.concat(dbType, "'not_'", "indb.provided_condition") : "indb.provided_condition") +
                " AND indb.plugin_id=unfulfilled.plugin_id" +
                " WHERE indb.id=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID +
                " AND indb.provided_condition IS NOT NULL" + lockForUpdate();

        return extractIds(selectUnsatisfiedProviderIds);
    }

    private Executable deleteUnsatisfiedConditionalTables() {
        List<Integer> tableIds = selectUnfulfilledTableIds();
        if (tableIds.isEmpty()) return Executable.empty();

        @Language("SQL") String deleteUnsatisfiedValues = "DELETE FROM plan_extension_server_table_values " +
                "WHERE table_id IN (" + Sql.nParameters(tableIds.size()) + ")";

        return new ExecStatement(deleteUnsatisfiedValues) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (int i = 0; i < tableIds.size(); i++) {
                    statement.setInt(i + 1, tableIds.get(i));
                }
            }
        };
    }

    private List<Integer> selectUnfulfilledTableIds() {
        // Need to select:
        // Provider IDs where condition of this provider is met
        @Language("SQL") String selectUnsatisfiedProviderIds = "SELECT unfulfilled.id " +
                "FROM plan_extension_providers indb " +
                "JOIN plan_extension_tables unfulfilled ON unfulfilled.condition_name=" +
                // This gives the unfulfilled condition, eg. if value is true not_condition is unfulfilled.
                (value ? Sql.concat(dbType, "'not_'", "indb.provided_condition") : "indb.provided_condition") +
                " AND indb.plugin_id=unfulfilled.plugin_id" +
                " WHERE indb.id=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID +
                " AND indb.provided_condition IS NOT NULL" + lockForUpdate();

        return extractIds(selectUnsatisfiedProviderIds);
    }

    private List<Integer> extractIds(@Language("SQL") String selectUnsatisfiedProviderIds) {
        return query(new QueryStatement<>(selectUnsatisfiedProviderIds) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, providerName, pluginName, serverUUID);
            }

            @Override
            public List<Integer> processResults(ResultSet set) throws SQLException {
                List<Integer> ids = new ArrayList<>();
                while (set.next()) {
                    ids.add(set.getInt(1));
                }
                return ids;
            }
        });
    }
}