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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.storage.database.queries.objects.StatisticsQueries;
import com.djrapitops.plan.storage.database.sql.tables.StatisticTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores missing statistic keys to {@link StatisticTable}
 *
 * @author AuroraLS3
 */
public class StoreMinecraftStatisticsTransaction extends Transaction {

    private final Set<String> statistics;

    public StoreMinecraftStatisticsTransaction(Set<String> statistics) {this.statistics = statistics;}

    public Set<String> missingKeys(Collection<String> keys) {
        Set<String> result = new HashSet<>(keys);
        result.removeAll(query(StatisticsQueries.fetchStatisticNameToId()).keySet());
        return result;
    }

    @Override
    protected void performOperations() {
        Set<String> store = missingKeys(statistics);
        execute(new ExecBatchStatement(StatisticTable.Row.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String stat : store) {
                    statement.setString(1, stat);
                    statement.addBatch();
                }
            }
        });
    }
}
