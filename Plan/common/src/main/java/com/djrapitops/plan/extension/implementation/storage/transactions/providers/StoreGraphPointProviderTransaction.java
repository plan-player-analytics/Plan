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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.annotation.GraphProvider;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.graph.Aggregates;
import com.djrapitops.plan.extension.graph.SeriesMetadata;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTabTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.*;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphColorTable.COLOR_MAX_LENGTH;
import static com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphLabelTable.LABEL_MAX_LENGTH;
import static com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphUnitTable.UNIT_MAX_LENGTH;

/**
 * Stores the metadata related to an extension graph.
 *
 * @author AuroraLS3
 */
public class StoreGraphPointProviderTransaction extends Transaction {

    private final ExtensionMethod method;
    private final ProviderInformation info;
    private final ServerUUID serverUUID;
    private final GraphProvider annotation;
    private final List<SeriesMetadata> metadata;
    private final ExtensionGraphMetadataTable.TableType tableType;

    public StoreGraphPointProviderTransaction(
            GraphProvider annotation,
            ExtensionMethod method,
            ProviderInformation info,
            ServerUUID serverUUID,
            List<SeriesMetadata> metadata,
            ExtensionGraphMetadataTable.TableType tableType
    ) {
        this.annotation = annotation;
        this.method = method;
        this.info = info;
        this.serverUUID = serverUUID;
        this.metadata = metadata;
        this.tableType = tableType;
    }

    @Override
    protected void performOperations() {
        if (dbType == DBType.MYSQL) {
            // Lock table for update to avoid deadlock
            query(db -> db.queryOptional(SELECT + ExtensionGraphMetadataTable.ID +
                            FROM + ExtensionGraphMetadataTable.TABLE_NAME +
                            WHERE + ExtensionGraphMetadataTable.PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + lockForUpdate(),
                    row -> null,
                    info.getName(), info.getPluginName(), serverUUID));
        }

        executeOther(new StoreProviderTransaction(info, serverUUID));
        execute(storeMetadata());
        execute(ExtensionGraphMetadataTable.createGraphTableSQL(dbType, info.getPluginName(), method.getMethodName(), tableType));

        storeAggregateTypes();
        storeAggregateTypeLinks();
        storeSeriesLabelLinks(storeSeriesLabels());
        storeColorLinks(storeColors());
        storeFormatLinks(storeFormats());
        storeUnitLinks(storeUnits());
    }

    private void storeSeriesLabelLinks(List<String> seriesLabels) {
        String selectColumnCount = ExtensionGraphLabelTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphLabelTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphLabelTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphLabelTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, seriesLabels, updateStatement, deleteStatement, insertStatement, LABEL_MAX_LENGTH);
    }

    private void storeColorLinks(List<String> colors) {
        String selectColumnCount = ExtensionGraphColorTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphColorTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphColorTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphColorTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, colors, updateStatement, deleteStatement, insertStatement, COLOR_MAX_LENGTH);
    }

    private void storeFormatLinks(List<String> formats) {
        String selectColumnCount = ExtensionGraphFormatTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphFormatTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphFormatTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphFormatTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, formats, updateStatement, deleteStatement, insertStatement, 20);
    }

    private void storeUnitLinks(List<String> units) {
        String selectColumnCount = ExtensionGraphUnitTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphUnitTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphUnitTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphUnitTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, units, updateStatement, deleteStatement, insertStatement, UNIT_MAX_LENGTH);
    }

    private void storeItemLinks(String selectColumnCount, List<String> values, String updateStatement, String deleteStatement, String insertStatement, int truncate) {
        int storedColumnCount = query(db -> db.queryOptional(selectColumnCount,
                set -> set.getInt(1), info.getName(), info.getPluginName(), serverUUID))
                .orElse(0);
        if (dbType == DBType.MYSQL) {
            // Lock rows for update
            query(db -> db.queryOptional(selectColumnCount.replace("COUNT(*)", "id") + lockForUpdate(),
                    set -> null));
        }
        int columnCount = values.size();
        if (storedColumnCount >= columnCount) {
            // More columns stored than what we have
            // update 0 -> count, delete count -> storedCount
            execute(new ExecBatchStatement(updateStatement) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = 0; i < columnCount; i++) {
                        statement.setString(1, StringUtils.truncate(values.get(i), truncate));
                        statement.setInt(2, i);
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, info.getName(), info.getPluginName(), serverUUID);
                    }
                }
            });
            execute(new ExecBatchStatement(deleteStatement) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = columnCount; i < storedColumnCount; i++) {
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, info.getName(), info.getPluginName(), serverUUID);
                        statement.setInt(4, i);
                    }
                }
            });
        } else {
            // Fewer columns stored than what we have.
            // update 0 -> storedCount, insert storedCount -> count
            execute(new ExecBatchStatement(updateStatement) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = 0; i < storedColumnCount; i++) {
                        statement.setString(1, values.get(i));
                        statement.setInt(2, i);
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, info.getName(), info.getPluginName(), serverUUID);
                    }
                }
            });
            execute(new ExecBatchStatement(insertStatement) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = storedColumnCount; i < columnCount; i++) {
                        statement.setString(1, values.get(i));
                        statement.setInt(2, i);
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, info.getName(), info.getPluginName(), serverUUID);
                    }
                }
            });
        }
    }

    private List<String> storeSeriesLabels() {
        List<String> seriesLabels = metadata.stream()
                .map(SeriesMetadata::getSeriesName)
                .map(s -> StringUtils.truncate(s, LABEL_MAX_LENGTH))
                .toList();
        String insertStatement = ExtensionGraphLabelTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphLabelTable.selectInSql(seriesLabels.size());
        storeItems(seriesLabels, insertStatement, selectStatement);
        return seriesLabels;
    }

    private List<String> storeColors() {
        List<String> colors = metadata.stream()
                .map(SeriesMetadata::getHexColor)
                .map(s -> StringUtils.truncate(s, COLOR_MAX_LENGTH))
                .toList();
        String insertStatement = ExtensionGraphColorTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphColorTable.selectInSql(colors.size());
        storeItems(colors, insertStatement, selectStatement);
        return colors;
    }

    private List<String> storeFormats() {
        List<String> formats = metadata.stream()
                .map(SeriesMetadata::getFormatType)
                .map(FormatType::name)
                .toList();
        String insertStatement = ExtensionGraphFormatTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphFormatTable.selectInSql(formats.size());
        storeItems(formats, insertStatement, selectStatement);
        return formats;
    }

    private List<String> storeUnits() {
        List<String> units = metadata.stream()
                .map(SeriesMetadata::getUnitLabel)
                .map(s -> StringUtils.truncate(s, UNIT_MAX_LENGTH))
                .toList();
        String insertStatement = ExtensionGraphUnitTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphUnitTable.selectInSql(units.size());
        storeItems(units, insertStatement, selectStatement);
        return units;
    }

    private void storeItems(Collection<String> items, String insertStatement,
                            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> selectStatement) {
        Set<String> storedItems = selectStatement.map(sql ->
                query(db -> db.querySet(sql, set -> set.getString(1), items))
        ).orElse(Set.of());
        if (storedItems.size() == items.size()) return;
        execute(new ExecBatchStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String color : items) {
                    if (storedItems.contains(color)) continue;
                    statement.setString(1, color);
                    statement.addBatch();
                }
            }
        });
    }

    private void storeAggregateTypeLinks() {
        Aggregates[] aggregates = annotation.supportedAggregateFunctions();
        if (aggregates.length > 0) {
            // Delete aggregates of this provider
            execute(new ExecStatement(ExtensionGraphAggregateTypeTable.ToProviderTable.DELETE_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, info.getName(), info.getPluginName(), serverUUID);
                }
            });
            // Insert new aggregates
            execute(new ExecStatement(ExtensionGraphAggregateTypeTable.ToProviderTable.INSERT_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (Aggregates aggregate : aggregates) {
                        statement.setString(1, aggregate.name());
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, info.getName(), info.getPluginName(), serverUUID);
                        statement.addBatch();
                    }
                }
            });
        }
    }

    private void storeAggregateTypes() {
        List<String> existingAggregates = query(db -> db.queryList(ExtensionGraphAggregateTypeTable.selectInSQL(Aggregates.values().length),
                row -> row.getString(ExtensionGraphAggregateTypeTable.AGGREGATE_TYPE),
                Arrays.stream(Aggregates.values()).map(Aggregates::name).collect(Collectors.toList())));
        List<String> newAggregates = Arrays.stream(Aggregates.values())
                .map(Aggregates::name)
                .filter(existingAggregates::contains)
                .collect(Collectors.toList());
        if (!newAggregates.isEmpty()) {
            execute(storeAggregateTypes(newAggregates));
        }
    }

    private Executable storeAggregateTypes(List<String> newAggregates) {
        return new ExecBatchStatement(ExtensionGraphAggregateTypeTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String newAggregate : newAggregates) {
                    statement.setString(1, newAggregate);
                    statement.addBatch();
                }
            }
        };
    }

    private Executable storeMetadata() {
        return connection -> {
            if (!updateMetadata().execute(connection)) {
                return insertMetadata().execute(connection);
            }
            return false;
        };
    }

    private Executable insertMetadata() {
        return executeStatement(ExtensionGraphMetadataTable.INSERT_STATEMENT);
    }

    private @NotNull ExecStatement executeStatement(@Language("SQL") String sql) {
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, annotation.supportsStacking());
                statement.setString(2, annotation.xAxisType().name());
                statement.setInt(3, annotation.yAxisSoftMax());
                statement.setInt(4, annotation.yAxisSoftMin());
                statement.setInt(5, annotation.xAxisSoftMax());
                statement.setInt(6, annotation.xAxisSoftMin());
                statement.setString(7, ExtensionGraphMetadataTable.getTableName(info.getPluginName(), method.getMethodName()));
                statement.setInt(8, tableType.getType());
                ExtensionTabTable.set3TabValuesToStatement(statement, 9, info.getTab().orElse(null), info.getPluginName(), serverUUID);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 12, info.getName(), info.getPluginName(), serverUUID);
            }
        };
    }

    private Executable updateMetadata() {
        return executeStatement(ExtensionGraphMetadataTable.UPDATE_STATEMENT);
    }

}
