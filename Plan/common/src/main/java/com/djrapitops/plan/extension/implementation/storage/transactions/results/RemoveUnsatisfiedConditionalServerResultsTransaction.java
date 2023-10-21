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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTableProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to remove older results that violate an updated condition value.
 * <p>
 * How it works:
 * - Select all fulfilled conditions for all servers (conditionName when true and not_conditionName when false)
 * - Left join with server value and provider tables when plugin_ids match, and when condition matches a condition in the
 * query above. (plugin_ids can be linked to servers)
 * - Filter the join query for values where the condition did not match any provided condition in the join (Is null)
 * - Delete all server values with IDs that are returned by the left join query after filtering
 *
 * @author AuroraLS3
 */
public class RemoveUnsatisfiedConditionalServerResultsTransaction extends ThrowawayTransaction {

    private final String providerTable;
    private final String serverValueTable;
    private final String serverTableValueTable;
    private final String tableTable;

    public RemoveUnsatisfiedConditionalServerResultsTransaction() {
        providerTable = ExtensionProviderTable.TABLE_NAME;
        serverValueTable = ExtensionServerValueTable.TABLE_NAME;
        tableTable = ExtensionTableProviderTable.TABLE_NAME;
        serverTableValueTable = ExtensionServerTableValueTable.TABLE_NAME;
    }
    
    @Override
    protected void performOperations() {
        String selectSatisfiedConditions = getSatisfiedConditionsSQL();
        execute(deleteUnsatisfiedValues(selectSatisfiedConditions));
        execute(deleteUnsatisfiedTableValues(selectSatisfiedConditions));
    }

    private Executable deleteUnsatisfiedValues(String selectSatisfiedConditions) {
        // Query contents:
        // id | provider_id | q1.provider_id | condition | q1.provided_condition
        // -- | ----------- | -------------- | --------- | ---------------------
        // 1  | ...         | ...            | A         | A                     Satisfied condition
        // 2  | ...         | ...            | not_B     | not_B                 Satisfied condition
        // 3  | ...         | ...            | NULL      | NULL                  Satisfied condition
        // 4  | ...         | ...            | B         | NULL                  Unsatisfied condition, filtered to these in WHERE clause.
        // 5  | ...         | ...            | not_C     | NULL                  Unsatisfied condition
        String selectUnsatisfiedValueIDs = SELECT + serverValueTable + '.' + ExtensionServerValueTable.ID +
                FROM + providerTable +
                INNER_JOIN + serverValueTable + " on " + providerTable + '.' + ExtensionProviderTable.ID + "=" + ExtensionServerValueTable.PROVIDER_ID +
                LEFT_JOIN + selectSatisfiedConditions + // Left join to preserve values that don't have their condition fulfilled
                " on (" +
                ExtensionProviderTable.CONDITION + "=q1." + ExtensionProviderTable.PROVIDED_CONDITION +
                AND + providerTable + '.' + ExtensionProviderTable.PLUGIN_ID + "=q1." + ExtensionProviderTable.PLUGIN_ID +
                ')' +
                WHERE + "q1." + ExtensionProviderTable.PROVIDED_CONDITION + IS_NULL + // Conditions that were not in the satisfied condition query
                AND + ExtensionProviderTable.CONDITION + IS_NOT_NULL; // Ignore values that don't need condition

        // Nested query here is required because MySQL limits update statements with nested queries:
        // The nested query creates a temporary table that bypasses the same table query-update limit.
        // Note: MySQL versions 5.6.7+ might optimize this nested query away leading to an exception.
        String sql = DELETE_FROM + serverValueTable +
                WHERE + ExtensionServerValueTable.ID + " IN (" + SELECT + ExtensionServerValueTable.ID + FROM + '(' + selectUnsatisfiedValueIDs + ") as ids)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);  // Select provided conditions with 'true' value
                statement.setBoolean(2, false); // Select negated conditions with 'false' value
            }
        };
    }

    private String getSatisfiedConditionsSQL() {
        String reversedCondition = dbType == DBType.SQLITE ? "'not_' || " + ExtensionProviderTable.PROVIDED_CONDITION : "CONCAT('not_'," + ExtensionProviderTable.PROVIDED_CONDITION + ')';

        String selectSatisfiedPositiveConditions = SELECT +
                ExtensionProviderTable.PROVIDED_CONDITION + ',' +
                ExtensionProviderTable.PLUGIN_ID +
                FROM + providerTable +
                INNER_JOIN + serverValueTable + " on " + providerTable + '.' + ExtensionProviderTable.ID + "=" + ExtensionServerValueTable.PROVIDER_ID +
                WHERE + ExtensionServerValueTable.BOOLEAN_VALUE + "=?" +
                AND + ExtensionProviderTable.PROVIDED_CONDITION + IS_NOT_NULL;
        String selectSatisfiedNegativeConditions = SELECT +
                reversedCondition + " as " + ExtensionProviderTable.PROVIDED_CONDITION + ',' +
                ExtensionProviderTable.PLUGIN_ID +
                FROM + providerTable +
                INNER_JOIN + serverValueTable + " on " + providerTable + '.' + ExtensionProviderTable.ID + "=" + ExtensionServerValueTable.PROVIDER_ID +
                WHERE + ExtensionServerValueTable.BOOLEAN_VALUE + "=?" +
                AND + ExtensionProviderTable.PROVIDED_CONDITION + IS_NOT_NULL;

        // Query contents: Set of provided_conditions
        return '(' + selectSatisfiedPositiveConditions + " UNION " + selectSatisfiedNegativeConditions + ") q1";
    }

    private Executable deleteUnsatisfiedTableValues(String selectSatisfiedConditions) {
        String selectUnsatisfiedValueIDs = SELECT + ExtensionTableProviderTable.ID +
                FROM + tableTable +
                LEFT_JOIN + selectSatisfiedConditions + // Left join to preserve values that don't have their condition fulfilled
                " on (" +
                tableTable + '.' + ExtensionTableProviderTable.CONDITION +
                "=q1." + ExtensionProviderTable.PROVIDED_CONDITION +
                AND + tableTable + '.' + ExtensionTableProviderTable.PLUGIN_ID +
                "=q1." + ExtensionProviderTable.PLUGIN_ID +
                ')' +
                WHERE + "q1." + ExtensionProviderTable.PROVIDED_CONDITION + IS_NULL + // Conditions that were not in the satisfied condition query
                AND + ExtensionProviderTable.CONDITION + IS_NOT_NULL; // Ignore values that don't need condition

        // Nested query here is required because MySQL limits update statements with nested queries:
        // The nested query creates a temporary table that bypasses the same table query-update limit.
        // Note: MySQL versions 5.6.7+ might optimize this nested query away leading to an exception.
        String deleteValuesSQL = DELETE_FROM + serverTableValueTable +
                WHERE + ExtensionServerTableValueTable.TABLE_ID + " IN (" + SELECT + ExtensionTableProviderTable.ID + FROM + '(' + selectUnsatisfiedValueIDs + ") as ids)";

        return new ExecStatement(deleteValuesSQL) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);  // Select provided conditions with 'true' value
                statement.setBoolean(2, false); // Select negated conditions with 'false' value
            }
        };
    }
}