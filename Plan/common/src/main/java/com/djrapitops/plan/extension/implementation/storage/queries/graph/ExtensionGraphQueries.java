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
package com.djrapitops.plan.extension.implementation.storage.queries.graph;

import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionGraphDto;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.graph.XAxisType;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.storage.queries.QueriedTabData;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTabTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphFormatTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphLabelTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphUnitTable;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author AuroraLS3
 */
public class ExtensionGraphQueries {

    private ExtensionGraphQueries() {
        /* Static method class */
    }

    public static Query<List<String>> findGraphTableNames() {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME + FROM + ExtensionGraphMetadataTable.TABLE_NAME;
        return db -> db.queryList(sql, row -> row.getString(1));
    }

    public static Query<List<String>> findGraphTableNames(ServerUUID serverUUID) {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME + " g" +
                JOIN + ExtensionProviderTable.TABLE_NAME + " p ON p.id=g." + ExtensionGraphMetadataTable.PROVIDER_ID +
                JOIN + ExtensionPluginTable.TABLE_NAME + " pl ON pl.id=p." + ExtensionProviderTable.PLUGIN_ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?";
        return db -> db.queryList(sql, row -> row.getString(1), serverUUID);
    }

    public static Query<List<String>> findGraphTableNames(ExtensionGraphMetadataTable.TableType type) {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME +
                WHERE + ExtensionGraphMetadataTable.TABLE_TYPE + "=?";
        return db -> db.queryList(sql, row -> row.getString(1), type.getType());
    }

    public static Query<List<String>> findGraphTableNames(Collection<Integer> providerIds) {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME +
                WHERE + ExtensionGraphMetadataTable.PROVIDER_ID + " IN (" + Sql.nParameters(providerIds.size()) + ")";
        return db -> db.queryList(sql, row -> row.getString(1), providerIds);
    }

    public static Query<Optional<ExtensionGraphDto>> getGraphMetadata(String graphTableName, ExtensionGraphMetadataTable.TableType tableType, ServerUUID serverUUID) {
        String sql = SELECT + "m.*, t.*" +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME + " m " +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p ON p.id=m." + ExtensionGraphMetadataTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " pl ON pl.id=p." + ExtensionProviderTable.PLUGIN_ID +
                WHERE + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME + "=?" +
                AND + ExtensionGraphMetadataTable.TABLE_TYPE + "=?" +
                AND + ExtensionPluginTable.SERVER_UUID + "=?";
        return db -> db.queryOptional(sql, set -> new ExtensionGraphDto(
                set.getInt(ExtensionGraphMetadataTable.PROVIDER_ID),
                set.getString(ExtensionGraphMetadataTable.GRAPH_TABLE_NAME),
                set.getString(ExtensionProviderTable.TEXT),
                XAxisType.valueOf(set.getString(ExtensionGraphMetadataTable.X_AXIS_TYPE)),
                set.getInt(ExtensionGraphMetadataTable.X_AXIS_SOFT_MIN),
                set.getInt(ExtensionGraphMetadataTable.X_AXIS_SOFT_MAX),
                set.getInt(ExtensionGraphMetadataTable.Y_AXIS_SOFT_MIN),
                set.getInt(ExtensionGraphMetadataTable.Y_AXIS_SOFT_MAX),
                set.getInt(ExtensionGraphMetadataTable.COLUMN_COUNT),
                set.getBoolean(ExtensionGraphMetadataTable.SUPPORTS_STACKING)
        ), graphTableName, tableType.getType(), serverUUID);
    }

    public static Query<List<FormatType>> getGraphFormats(int providerId, int columnCount) {
        String sql = SELECT +
                ExtensionGraphFormatTable.FORMAT + ',' +
                ExtensionGraphFormatTable.ToProviderTable.COLUMN_INDEX +
                FROM + ExtensionGraphFormatTable.ToProviderTable.TABLE_NAME + " tp" +
                JOIN + ExtensionGraphFormatTable.TABLE_NAME + " i ON i.id=tp." + ExtensionGraphFormatTable.ToProviderTable.FORMAT_ID +
                WHERE + ExtensionGraphFormatTable.ToProviderTable.PROVIDER_ID + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, providerId);
            }

            @Override
            public List<FormatType> processResults(ResultSet set) throws SQLException {
                List<FormatType> formats = new ArrayList<>(columnCount);
                while (set.next()) {
                    formats.set(set.getInt(ExtensionGraphFormatTable.ToProviderTable.COLUMN_INDEX),
                            FormatType.getByName(set.getString(ExtensionGraphFormatTable.FORMAT)).orElse(FormatType.NONE));
                }
                return formats;
            }
        };
    }

    public static Query<List<String>> getGraphUnits(int providerId, int columnCount) {
        String sql = SELECT +
                ExtensionGraphUnitTable.UNIT + ',' +
                ExtensionGraphUnitTable.ToProviderTable.COLUMN_INDEX +
                FROM + ExtensionGraphUnitTable.ToProviderTable.TABLE_NAME + " tp" +
                JOIN + ExtensionGraphUnitTable.TABLE_NAME + " i ON i.id=tp." + ExtensionGraphUnitTable.ToProviderTable.UNIT_ID +
                WHERE + ExtensionGraphUnitTable.ToProviderTable.PROVIDER_ID + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, providerId);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> units = new ArrayList<>(columnCount);
                while (set.next()) {
                    units.set(set.getInt(ExtensionGraphUnitTable.ToProviderTable.COLUMN_INDEX),
                            set.getString(ExtensionGraphUnitTable.UNIT));
                }
                return units;
            }
        };
    }

    public static Query<List<String>> getGraphSeriesLabels(int providerId, int columnCount) {
        String sql = SELECT +
                ExtensionGraphLabelTable.LABEL + ',' +
                ExtensionGraphLabelTable.ToProviderTable.COLUMN_INDEX +
                FROM + ExtensionGraphLabelTable.ToProviderTable.TABLE_NAME + " tp" +
                JOIN + ExtensionGraphLabelTable.TABLE_NAME + " i ON i.id=tp." + ExtensionGraphLabelTable.ToProviderTable.LABEL_ID +
                WHERE + ExtensionGraphLabelTable.ToProviderTable.PROVIDER_ID + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, providerId);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> units = new ArrayList<>(columnCount);
                while (set.next()) {
                    units.set(set.getInt(ExtensionGraphLabelTable.ToProviderTable.COLUMN_INDEX),
                            set.getString(ExtensionGraphLabelTable.LABEL));
                }
                return units;
            }
        };
    }

    public static Query<Map<Integer, ExtensionData.Builder>> findGraphTableNames(ServerUUID serverUUID, ExtensionGraphMetadataTable.TableType tableType) {
        String sql = SELECT +
                ExtensionProviderTable.PLUGIN_ID + ',' +
                ExtensionTabTable.TAB_NAME + " as tab_name," +
                ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME + " g" +
                JOIN + ExtensionProviderTable.TABLE_NAME + " p ON p.id=g." + ExtensionGraphMetadataTable.PROVIDER_ID +
                JOIN + ExtensionPluginTable.TABLE_NAME + " pl ON pl.id=p." + ExtensionProviderTable.PLUGIN_ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t ON t.id=p." + ExtensionProviderTable.TAB_ID +
                WHERE + ExtensionPluginTable.SERVER_UUID + "=?" +
                AND + ExtensionGraphMetadataTable.TABLE_TYPE + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setInt(2, tableType.getType());
            }

            @Override
            public Map<Integer, ExtensionData.Builder> processResults(ResultSet set) throws SQLException {
                QueriedTabData queriedTabData = new QueriedTabData();
                while (set.next()) {
                    int pluginId = set.getInt(ExtensionProviderTable.PLUGIN_ID);
                    String tabName = Optional.ofNullable(set.getString("tab_name")).orElse("");
                    ExtensionTabData.Builder tab = queriedTabData.getTab(pluginId, tabName, () -> new TabInformation(tabName, null, List.of(), 0));
                    tab.putGraphName(set.getString(ExtensionGraphMetadataTable.GRAPH_TABLE_NAME));
                }
                return queriedTabData.toExtensionDataByPluginID();
            }
        };
    }

    public static Query<Map<Integer, ExtensionData.Builder>> findGraphTableNames(UUID playerUUID, ExtensionGraphMetadataTable.TableType tableType) {
        String sql = SELECT +
                ExtensionProviderTable.PLUGIN_ID + ',' +
                ExtensionTabTable.TAB_NAME + " as tab_name," +
                ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME + " g" +
                JOIN + ExtensionProviderTable.TABLE_NAME + " p ON p.id=g." + ExtensionGraphMetadataTable.PROVIDER_ID +
                JOIN + ExtensionPluginTable.TABLE_NAME + " pl ON pl.id=p." + ExtensionProviderTable.PLUGIN_ID +
                LEFT_JOIN + ExtensionTabTable.TABLE_NAME + " t ON t.id=p." + ExtensionProviderTable.TAB_ID +
                WHERE + ExtensionGraphMetadataTable.TABLE_TYPE + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, tableType.getType());
            }

            @Override
            public Map<Integer, ExtensionData.Builder> processResults(ResultSet set) throws SQLException {
                QueriedTabData queriedTabData = new QueriedTabData();
                while (set.next()) {
                    int pluginId = set.getInt(ExtensionProviderTable.PLUGIN_ID);
                    String tabName = Optional.ofNullable(set.getString("tab_name")).orElse("");
                    ExtensionTabData.Builder tab = queriedTabData.getTab(pluginId, tabName, () -> new TabInformation(tabName, null, List.of(), 0));
                    tab.putGraphName(set.getString(ExtensionGraphMetadataTable.GRAPH_TABLE_NAME));
                }
                return queriedTabData.toExtensionDataByPluginID();
            }
        };
    }
}
