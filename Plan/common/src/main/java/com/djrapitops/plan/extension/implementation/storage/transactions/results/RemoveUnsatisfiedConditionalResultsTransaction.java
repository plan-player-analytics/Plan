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

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.ExtensionPlayerValueTable;
import com.djrapitops.plan.db.sql.tables.ExtensionProviderTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Transaction to remove older results that violate an updated condition value.
 * <p>
 * How it works:
 * - Select all fulfilled conditions for all players (conditionName when true and not_conditionName when false)
 * - Left join with player value & provider tables when uuids match, and when condition matches a condition in the query above.
 * - Filter the join query for values where the condition did not match any provided condition in the join (Is null)
 * - Delete all player values with IDs that are returned by the left join query after filtering
 *
 * @author Rsl1122
 */
public class RemoveUnsatisfiedConditionalResultsTransaction extends Transaction {

    @Override
    protected void performOperations() {
        execute(deleteUnsatisfied());
    }

    private Executable deleteUnsatisfied() {
        String reversedCondition = dbType == DBType.SQLITE ? "'not_' || " + ExtensionProviderTable.PROVIDED_CONDITION : "CONCAT('not_'," + ExtensionProviderTable.PROVIDED_CONDITION + ')';

        String providerTable = ExtensionProviderTable.TABLE_NAME;
        String playerValueTable = ExtensionPlayerValueTable.TABLE_NAME;

        String selectSatisfiedPositiveConditions = SELECT +
                ExtensionProviderTable.PROVIDED_CONDITION + ',' +
                ExtensionPlayerValueTable.USER_UUID +
                FROM + providerTable +
                INNER_JOIN + playerValueTable + " on " + providerTable + '.' + ExtensionProviderTable.ID + "=" + ExtensionPlayerValueTable.PROVIDER_ID +
                WHERE + ExtensionPlayerValueTable.BOOLEAN_VALUE + "=?" +
                AND + ExtensionProviderTable.PROVIDED_CONDITION + " IS NOT NULL";
        String selectSatisfiedNegativeConditions = SELECT +
                reversedCondition + " as " + ExtensionProviderTable.PROVIDED_CONDITION + ',' +
                ExtensionPlayerValueTable.USER_UUID +
                FROM + providerTable +
                INNER_JOIN + playerValueTable + " on " + providerTable + '.' + ExtensionProviderTable.ID + "=" + ExtensionPlayerValueTable.PROVIDER_ID +
                WHERE + ExtensionPlayerValueTable.BOOLEAN_VALUE + "=?" +
                AND + ExtensionProviderTable.PROVIDED_CONDITION + " IS NOT NULL";

        // Query contents: Set of provided_conditions
        String selectSatisfiedConditions = '(' + selectSatisfiedPositiveConditions + " UNION " + selectSatisfiedNegativeConditions + ") q1";

        // Query contents:
        // id | uuid | q1.uuid | condition | q1.provided_condition
        // -- | ---- | ------- | --------- | ---------------------
        // 1  | ...  | ...     | A         | A                     Satisfied condition
        // 2  | ...  | ...     | not_B     | not_B                 Satisfied condition
        // 3  | ...  | ...     | NULL      | NULL                  Satisfied condition
        // 4  | ...  | ...     | B         | NULL                  Unsatisfied condition, filtered to these in WHERE clause.
        // 5  | ...  | ...     | not_C     | NULL                  Unsatisfied condition
        String selectUnsatisfiedValueIDs = SELECT + playerValueTable + '.' + ExtensionPlayerValueTable.ID +
                FROM + providerTable +
                INNER_JOIN + playerValueTable + " on " + providerTable + '.' + ExtensionProviderTable.ID + "=" + ExtensionPlayerValueTable.PROVIDER_ID +
                LEFT_JOIN + selectSatisfiedConditions + // Left join to preserve values that don't have their condition fulfilled
                " on (" +
                playerValueTable + '.' + ExtensionPlayerValueTable.USER_UUID +
                "=q1." + ExtensionPlayerValueTable.USER_UUID +
                AND + ExtensionProviderTable.CONDITION +
                "=q1." + ExtensionProviderTable.PROVIDED_CONDITION +
                ')' +
                WHERE + "q1." + ExtensionProviderTable.PROVIDED_CONDITION + " IS NULL" + // Conditions that were not in the satisfied condition query
                AND + ExtensionProviderTable.CONDITION + " IS NOT NULL"; // Ignore values that don't need condition

        String sql = "DELETE FROM " + playerValueTable +
                WHERE + ExtensionPlayerValueTable.ID + " IN (" + selectUnsatisfiedValueIDs + ')';

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);  // Select provided conditions with 'true' value
                statement.setBoolean(2, false); // Select negated conditions with 'false' value
            }
        };
    }
}