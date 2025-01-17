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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.annotation.GraphPointProvider;
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.graph.Aggregates;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.identification.ServerUUID;
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

/**
 * Stores the metadata related to an extension graph.
 *
 * @author AuroraLS3
 */
public class StoreGraphPointProviderTransaction extends Transaction {

    private final ExtensionMethod method;
    private final ProviderInformation info;
    private final ServerUUID serverUUID;
    private final GraphPointProvider annotation;

    public StoreGraphPointProviderTransaction(
            GraphPointProvider annotation,
            ExtensionMethod method,
            ProviderInformation info,
            ServerUUID serverUUID
    ) {
        this.annotation = annotation;
        this.method = method;
        this.info = info;
        this.serverUUID = serverUUID;
    }

    private static @NotNull List<String> truncate(String[] colors, int colorMaxLength) {
        return Arrays.stream(colors).map(s -> StringUtils.truncate(s, colorMaxLength))
                .collect(Collectors.toList());
    }

    @Override
    protected void performOperations() {
        executeOther(new StoreProviderTransaction(info, serverUUID));
        commitMidTransaction();
        execute(storeMetadata());
        execute(ExtensionGraphMetadataTable.createGraphTableSQL(dbType, info.getPluginName(), method.getMethodName(), ExtensionGraphMetadataTable.TableType.SERVER));

        storeAggregateTypes();
        storeAggregateTypeLinks();
        storeColors();
        storeColorLinks();
        storeFormats();
        storeFormatLinks();
        storeUnits();
        storeUnitLinks();
    }

    private void storeColorLinks() {
        List<String> colors = truncate(annotation.seriesColors(), ExtensionGraphColorTable.COLOR_MAX_LENGTH);
        String selectColumnCount = ExtensionGraphColorTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphColorTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphColorTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphColorTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, colors, updateStatement, deleteStatement, insertStatement);
    }

    private void storeFormatLinks() {
        List<String> formats = Arrays.stream(annotation.valueFormats()).map(FormatType::name).collect(Collectors.toList());
        String selectColumnCount = ExtensionGraphFormatTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphFormatTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphFormatTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphFormatTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, formats, updateStatement, deleteStatement, insertStatement);
    }

    private void storeUnitLinks() {
        List<String> units = truncate(annotation.unitNames(), ExtensionGraphUnitTable.UNIT_MAX_LENGTH);
        String selectColumnCount = ExtensionGraphUnitTable.ToProviderTable.SELECT_COLUMN_COUNT;
        String updateStatement = ExtensionGraphUnitTable.ToProviderTable.UPDATE_STATEMENT;
        String deleteStatement = ExtensionGraphUnitTable.ToProviderTable.DELETE_STATEMENT;
        String insertStatement = ExtensionGraphUnitTable.ToProviderTable.INSERT_STATEMENT;
        storeItemLinks(selectColumnCount, units, updateStatement, deleteStatement, insertStatement);
    }

    private void storeItemLinks(String selectColumnCount, List<String> colors, String updateStatement, String deleteStatement, String insertStatement) {
        int storedColumnCount = query(db -> db.queryOptional(selectColumnCount,
                set -> set.getInt(1), info.getName(), info.getPluginName(), serverUUID))
                .orElse(0);
        int columnCount = colors.size();
        if (storedColumnCount >= columnCount) {
            // More columns stored than what we have
            // update 0 -> count, delete count -> storedCount
            execute(new ExecBatchStatement(updateStatement) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = 0; i < columnCount; i++) {
                        statement.setString(1, colors.get(i));
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
                        statement.setString(1, colors.get(i));
                        statement.setInt(2, i);
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, info.getName(), info.getPluginName(), serverUUID);
                    }
                }
            });
            execute(new ExecBatchStatement(insertStatement) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = storedColumnCount; i < columnCount; i++) {
                        statement.setString(1, colors.get(i));
                        statement.setInt(2, i);
                        ExtensionProviderTable.set3PluginValuesToStatement(statement, 3, info.getName(), info.getPluginName(), serverUUID);
                    }
                }
            });
        }
    }

    private void storeColors() {
        String[] colors = annotation.seriesColors();
        String insertStatement = ExtensionGraphColorTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphColorTable.selectInSql(colors.length);
        storeItems(truncate(colors, ExtensionGraphColorTable.COLOR_MAX_LENGTH), insertStatement, selectStatement);
    }

    private void storeFormats() {
        FormatType[] formats = annotation.valueFormats();
        String insertStatement = ExtensionGraphFormatTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphFormatTable.selectInSql(formats.length);
        storeItems(Arrays.stream(formats).map(FormatType::name).collect(Collectors.toList()), insertStatement, selectStatement);
    }


    private void storeUnits() {
        String[] units = annotation.unitNames();
        String insertStatement = ExtensionGraphUnitTable.INSERT_STATEMENT;
        Optional<String> selectStatement = ExtensionGraphUnitTable.selectInSql(units.length);
        storeItems(truncate(units, ExtensionGraphUnitTable.UNIT_MAX_LENGTH), insertStatement, selectStatement);
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
                statement.setBoolean(1, ExtensionExtractor.actuallySupportsStacking(annotation));
                statement.setString(2, annotation.xAxisType().name());
                statement.setInt(3, annotation.yAxisSoftMax());
                statement.setInt(4, annotation.yAxisSoftMin());
                statement.setInt(5, annotation.xAxisSoftMax());
                statement.setInt(6, annotation.xAxisSoftMin());
                statement.setString(7, ExtensionGraphMetadataTable.getTableName(info.getPluginName(), method.getMethodName()));
                statement.setInt(8, getTableType().getType());
                ExtensionTabTable.set3TabValuesToStatement(statement, 9, info.getTab().orElse(null), info.getPluginName(), serverUUID);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 12, info.getName(), info.getPluginName(), serverUUID);
            }
        };
    }

    private ExtensionGraphMetadataTable.TableType getTableType() {
        switch (method.getParameterType()) {
            case SERVER_NONE:
                return ExtensionGraphMetadataTable.TableType.SERVER;
            case PLAYER_STRING:
            case PLAYER_UUID:
                return ExtensionGraphMetadataTable.TableType.PLAYER;
            case GROUP:
                return ExtensionGraphMetadataTable.TableType.GROUP;
            default:
                throw new DBOpException("Unsupported method type " + method.getParameterType());
        }
    }

    private Executable updateMetadata() {
        return executeStatement(ExtensionGraphMetadataTable.UPDATE_STATEMENT);
    }

}
