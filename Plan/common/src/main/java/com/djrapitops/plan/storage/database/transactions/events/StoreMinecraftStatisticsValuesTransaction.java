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

import com.djrapitops.plan.gathering.domain.MinecraftStatistics;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.sql.tables.StatisticValueTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Stores values to {@link com.djrapitops.plan.storage.database.sql.tables.StatisticValueTable}.
 *
 * @author AuroraLS3
 */
public class StoreMinecraftStatisticsValuesTransaction extends Transaction {

    private final MinecraftStatistics statistics;

    public StoreMinecraftStatisticsValuesTransaction(MinecraftStatistics statistics) {this.statistics = statistics;}

    @Override
    protected void performOperations() {
        Integer userId = query(UserIdentifierQueries.fetchUserId(statistics.getPlayerUUID()))
                .orElseThrow(() -> new IllegalArgumentException("Player not stored."));
        Integer serverId = query(ServerQueries.fetchServerId(statistics.getServerUUID()))
                .orElseThrow(() -> new IllegalArgumentException("Server not stored."));
        String selectStatisticsForUpdate = SELECT + StatisticValueTable.STATISTIC_ID +
                FROM + StatisticValueTable.TABLE_NAME +
                WHERE + StatisticValueTable.USER_ID + "=?" +
                AND + StatisticValueTable.SERVER_ID + "=?" +
                lockForUpdate();

        Set<Integer> existingStatisticIds = query(db -> db.querySet(selectStatisticsForUpdate, row -> row.getInt(1), userId, serverId));
        Map<Integer, Integer> valuesById = statistics.getValuesById();
        List<Integer> removedIds = new ArrayList<>();
        execute(new ExecBatchStatement(StatisticValueTable.UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, userId);
                statement.setInt(4, serverId);
                for (Integer id : existingStatisticIds) {
                    Integer value = valuesById.get(id);
                    if (value == null) {
                        removedIds.add(id);
                        continue; // Value has been removed.
                    }
                    statement.setInt(1, value);
                    statement.setInt(2, id);
                    statement.addBatch();
                }
            }
        });
        if (!removedIds.isEmpty()) {
            execute(new ExecBatchStatement(StatisticValueTable.DELETE_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setInt(2, userId);
                    statement.setInt(3, serverId);
                    for (Integer id : removedIds) {
                        statement.setInt(1, id);
                        statement.addBatch();
                    }
                }
            });
        }
        execute(new ExecBatchStatement(StatisticValueTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, userId);
                statement.setInt(4, serverId);
                for (Map.Entry<Integer, Integer> entry : valuesById.entrySet()) {
                    Integer id = entry.getKey();
                    if (!existingStatisticIds.contains(id)) {
                        statement.setInt(1, entry.getValue());
                        statement.setInt(2, id);
                        statement.addBatch();
                    }
                }
            }
        });
    }
}
