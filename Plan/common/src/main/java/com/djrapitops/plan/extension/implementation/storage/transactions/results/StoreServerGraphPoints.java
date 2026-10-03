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

import com.djrapitops.plan.extension.graph.DataPoint;
import com.djrapitops.plan.extension.graph.HistoryStrategy;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.ExpandGraphColumnCountTransaction;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stores a server specific datapoints to extension graph table.
 *
 * @author AuroraLS3
 */
public class StoreServerGraphPoints extends Transaction {

    private final HistoryStrategy historyStrategy;
    private final int maxColumnCount;
    private final List<DataPoint> dataPoints;
    private final ProviderIdentifier identifier;

    public StoreServerGraphPoints(HistoryStrategy historyStrategy, int maxColumnCount, List<DataPoint> dataPoints, ProviderIdentifier identifier) {
        this.historyStrategy = historyStrategy;
        this.maxColumnCount = maxColumnCount;
        this.dataPoints = dataPoints;
        this.identifier = identifier;
    }

    @Override
    protected void performOperations() {
        executeOther(new ExpandGraphColumnCountTransaction(identifier, maxColumnCount));

        String selectXValues = ExtensionGraphMetadataTable.selectXValuesSql(identifier.getPluginName(), identifier.getProviderName(), ExtensionGraphMetadataTable.TableType.SERVER)
                + lockForUpdate();
        List<Long> existingX = query(db -> db.queryList(selectXValues, row -> row.getLong("x"), identifier.getServerUUID()));

        List<DataPoint> replace = new ArrayList<>();
        List<DataPoint> append = new ArrayList<>();
        if (historyStrategy == HistoryStrategy.ONLY_APPEND_MISSING) {
            append = dataPoints.stream()
                    .filter(Objects::nonNull)
                    .filter(point -> !existingX.contains(point.getX())).toList();
        }
        if (historyStrategy == HistoryStrategy.REPLACE_CHANGED_VALUES) {
            var replaceOrAppend = dataPoints.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(point -> existingX.contains(point.getX())));
            replace = replaceOrAppend.getOrDefault(true, List.of());
            append = replaceOrAppend.getOrDefault(false, List.of());
        }

        append(append);
        replace(replace);
    }

    private void replace(List<DataPoint> replace) {
        if (replace.isEmpty()) return;
        execute(new ExecBatchStatement(ExtensionGraphMetadataTable.updateGraphTableSql(identifier.getPluginName(), identifier.getProviderName(), maxColumnCount,
                ExtensionGraphMetadataTable.TableType.SERVER)) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(maxColumnCount + 1, getServerUUID().toString());
                for (DataPoint dataPoint : replace) {
                    statement.setLong(maxColumnCount + 2, dataPoint.getX());
                    int size = dataPoint.getValues().size();
                    for (int i = 0; i < maxColumnCount; i++) {
                        Double value = i < size ? dataPoint.getValues().get(i) : null;
                        if (value == null) {
                            statement.setNull(i + 1, Types.DOUBLE);
                        } else {
                            statement.setDouble(i + 1, value);
                        }
                        statement.addBatch();
                    }
                }
            }
        });
    }

    private void append(List<DataPoint> append) {
        if (append.isEmpty()) return;
        execute(new ExecBatchStatement(ExtensionGraphMetadataTable.insertToGraphTableSql(identifier.getPluginName(), identifier.getProviderName(), maxColumnCount,
                ExtensionGraphMetadataTable.TableType.SERVER)) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getServerUUID().toString());
                for (DataPoint dataPoint : append) {
                    statement.setLong(2, dataPoint.getX());
                    int size = dataPoint.getValues().size();
                    for (int i = 0; i < maxColumnCount; i++) {
                        Double value = i < size ? dataPoint.getValues().get(i) : null;
                        if (value == null) {
                            statement.setNull(i + 3, Types.DOUBLE);
                        } else {
                            statement.setDouble(i + 3, value);
                        }
                        statement.addBatch();
                    }
                }
            }
        });
    }
}
