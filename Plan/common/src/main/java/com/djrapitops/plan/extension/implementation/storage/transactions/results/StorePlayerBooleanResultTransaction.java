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
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerValueTable.*;

/**
 * Transaction to store method result of a boolean.
 *
 * @author AuroraLS3
 */
public class StorePlayerBooleanResultTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final String providerName;
    private final UUID playerUUID;

    private final boolean value;

    public StorePlayerBooleanResultTransaction(String pluginName, ServerUUID serverUUID, String providerName, UUID playerUUID, boolean value) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.providerName = providerName;
        this.playerUUID = playerUUID;
        this.value = value;
    }

    public StorePlayerBooleanResultTransaction(ProviderInformation information, Parameters parameters, boolean value) {
        this(information.getPluginName(), parameters.getServerUUID(), information.getName(), parameters.getPlayerUUID(), value);
    }

    @Override
    protected void performOperations() {
        execute(storeValue());
        commitMidTransaction();
        List<Integer> providerIds = selectUnfulfilledProviderIds();
        if (!providerIds.isEmpty()) {
            execute(deleteUnsatisfiedConditionalResults(providerIds));
            execute(deleteUnsatisfiedConditionalGroups(providerIds));
        }
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
                WHERE + USER_UUID + "=?" +
                AND + PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, value);
                statement.setString(2, playerUUID.toString());
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable insertValue() {
        String sql = INSERT_INTO + TABLE_NAME + "(" +
                BOOLEAN_VALUE + "," +
                USER_UUID + "," +
                PROVIDER_ID +
                ") VALUES (?,?," + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + ")";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, value);
                statement.setString(2, playerUUID.toString());
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, providerName, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteUnsatisfiedConditionalResults(List<Integer> providerIds) {
        @Language("SQL") String deleteUnsatisfiedValues = "DELETE FROM plan_extension_user_values " +
                "WHERE uuid=? " +
                "AND provider_id IN (" + Sql.nParameters(providerIds.size()) + ")";

        return deleteIds(providerIds, deleteUnsatisfiedValues);
    }

    @NotNull
    private ExecStatement deleteIds(List<Integer> providerIds, @Language("SQL") String deleteUnsatisfiedValues) {
        return new ExecStatement(deleteUnsatisfiedValues) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                for (int i = 0; i < providerIds.size(); i++) {
                    statement.setInt(i + 2, providerIds.get(i));
                }
            }
        };
    }

    private Executable deleteUnsatisfiedConditionalGroups(List<Integer> providerIds) {
        @Language("SQL") String deleteUnsatisfiedValues = "DELETE FROM plan_extension_groups " +
                "WHERE uuid=? " +
                "AND provider_id IN (" + Sql.nParameters(providerIds.size()) + ")";

        return deleteIds(providerIds, deleteUnsatisfiedValues);
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
                " AND indb.provided_condition IS NOT NULL";

        return extractIds(selectUnsatisfiedProviderIds);
    }

    private Executable deleteUnsatisfiedConditionalTables() {
        List<Integer> tableIds = selectUnfulfilledTableIds();
        if (tableIds.isEmpty()) return Executable.empty();

        @Language("SQL") String deleteUnsatisfiedValues = "DELETE FROM plan_extension_user_table_values " +
                "WHERE uuid=? " +
                "AND table_id IN (" + Sql.nParameters(tableIds.size()) + ")";

        return deleteIds(tableIds, deleteUnsatisfiedValues);
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
                " AND indb.provided_condition IS NOT NULL";

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