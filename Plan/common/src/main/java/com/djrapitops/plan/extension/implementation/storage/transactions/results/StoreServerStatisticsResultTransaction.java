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
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionInformationQueries;
import com.djrapitops.plan.extension.statistic.Statistics;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionStatisticServerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionStatisticTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.jspecify.annotations.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Stores plugin statistics of a server.
 *
 * @author AuroraLS3
 */
public class StoreServerStatisticsResultTransaction extends Transaction {

    private final ProviderInformation information;
    private final Parameters parameters;
    private final Statistics value;

    public StoreServerStatisticsResultTransaction(ProviderInformation information, Parameters parameters, Statistics value) {
        this.information = information;
        this.parameters = parameters;
        this.value = value;
    }

    @Override
    protected void performOperations() {
        var serverUUID = parameters.getServerUUID();

        var serverId = query(ServerQueries.fetchServerId(serverUUID))
                .orElseThrow(() -> new DBOpException("Server not stored."));
        var providerId = query(ExtensionInformationQueries.findProviderId(information.getPluginName(), information.getName(), parameters.getServerUUID()))
                .orElseThrow(() -> new DBOpException("Provider not stored."));

        var statisticNameToId = storeMissingStatistics(providerId, value.getValueByKey().keySet());

        var valueById = value.getValueByKey().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> statisticNameToId.get(entry.getKey()),
                        Map.Entry::getValue));

        var existingStatisticIds = getStoredValueStatisticIds(serverId, providerId);
        var removedIds = updateValues(serverId, existingStatisticIds, valueById);
        if (!removedIds.isEmpty()) {
            removeValues(serverId, removedIds);
        }
        insertValues(serverId, valueById, existingStatisticIds);
    }

    private void insertValues(Integer serverId, Map<Integer, Long> valuesById, Set<Integer> existingStatisticIds) {
        execute(new ExecBatchStatement(ExtensionStatisticServerValueTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, serverId);
                for (var entry : valuesById.entrySet()) {
                    Integer id = entry.getKey();
                    if (!existingStatisticIds.contains(id)) {
                        statement.setLong(1, entry.getValue());
                        statement.setInt(2, id);
                        statement.addBatch();
                    }
                }
            }
        });
    }

    private void removeValues(Integer serverId, List<Integer> removedIds) {
        execute(new ExecBatchStatement(ExtensionStatisticServerValueTable.DELETE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(2, serverId);
                for (Integer id : removedIds) {
                    statement.setInt(1, id);
                    statement.addBatch();
                }
            }
        });
    }

    private @NonNull List<Integer> updateValues(Integer serverId, Set<Integer> existingStatisticIds, Map<Integer, Long> valuesById) {
        List<Integer> removedIds = new ArrayList<>();
        execute(new ExecBatchStatement(ExtensionStatisticServerValueTable.UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, serverId);
                for (Integer id : existingStatisticIds) {
                    var val = valuesById.get(id);
                    if (val == null) {
                        removedIds.add(id);
                        continue;
                    }
                    statement.setLong(1, val);
                    statement.setInt(2, id);
                    statement.addBatch();
                }
            }
        });
        return removedIds;
    }

    private Set<Integer> getStoredValueStatisticIds(Integer serverId, Integer providerId) {
        String selectExistingStatsForUpdate = SELECT + ExtensionStatisticServerValueTable.STATISTIC_ID +
                FROM + ExtensionStatisticServerValueTable.TABLE_NAME +
                WHERE + ExtensionStatisticServerValueTable.SERVER_ID + "=?" +
                " AND " + ExtensionStatisticServerValueTable.STATISTIC_ID + " IN (" +
                SELECT + ExtensionStatisticTable.ID +
                FROM + ExtensionStatisticTable.TABLE_NAME +
                WHERE + ExtensionStatisticTable.PROVIDER_ID + "=?" +
                ')' +
                lockForUpdate();

        return query(db -> db.querySet(
                selectExistingStatsForUpdate,
                row -> row.getInt(1),
                serverId,
                providerId
        ));
    }

    private Map<String, Integer> storeMissingStatistics(Integer providerId, Set<String> statisticNames) {
        // 1. Fetch existing statistic name to ID for this provider
        String selectStatistics = ExtensionStatisticTable.SELECT_STATISTICS +
                WHERE + ExtensionStatisticTable.PROVIDER_ID + "=?";
        Map<String, Integer> statisticNameToId = query(db -> db.queryMap(
                selectStatistics,
                (row, map) -> map.put(row.getString(ExtensionStatisticTable.STATISTIC_NAME), row.getInt(ExtensionStatisticTable.ID)),
                providerId
        ));

        // 2. Identify missing keys and insert them
        Set<String> missingKeys = new HashSet<>(statisticNames);
        missingKeys.removeAll(statisticNameToId.keySet());

        if (!missingKeys.isEmpty()) {
            execute(new ExecBatchStatement(ExtensionStatisticTable.Row.INSERT_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setInt(1, providerId);
                    for (String key : missingKeys) {
                        statement.setString(2, key);
                        statement.addBatch();
                    }
                }
            });

            // Re-fetch mapping after inserting missing keys
            statisticNameToId = query(db -> db.queryMap(
                    selectStatistics,
                    (row, map) -> map.put(row.getString(ExtensionStatisticTable.STATISTIC_NAME), row.getInt(ExtensionStatisticTable.ID)),
                    providerId
            ));
        }
        return statisticNameToId;
    }
}
