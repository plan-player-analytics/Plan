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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.delivery.domain.PluginHistoryMetadata;
import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.PluginVersionTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries to the {@link  com.djrapitops.plan.storage.database.sql.tables.PluginVersionTable} go here.
 *
 * @author AuroraLS3
 */
public class PluginMetadataQueries {

    private PluginMetadataQueries() {
        /* static method class */
    }

    public static Query<List<PluginMetadata>> getInstalledPlugins(ServerUUID serverUUID) {
        @Language("SQL")
        String sql = SELECT + "*" + FROM + PluginVersionTable.TABLE_NAME +
                WHERE + PluginVersionTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                ORDER_BY + PluginVersionTable.MODIFIED + " DESC";
        return new QueryStatement<>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<PluginMetadata> processResults(ResultSet set) throws SQLException {
                Set<String> foundPlugins = new HashSet<>();
                List<PluginMetadata> installedPlugins = new ArrayList<>();
                while (set.next()) {
                    String pluginName = set.getString(PluginVersionTable.PLUGIN_NAME);

                    // Only keep the latest information
                    if (foundPlugins.contains(pluginName)) continue;
                    foundPlugins.add(pluginName);

                    String version = set.getString(PluginVersionTable.VERSION);
                    if (!set.wasNull()) { // If version is null the plugin is marked as uninstalled.
                        installedPlugins.add(new PluginMetadata(pluginName, version));
                    }
                }
                return installedPlugins;
            }
        };
    }

    public static Query<List<PluginHistoryMetadata>> getPluginHistory(ServerUUID serverUUID) {
        @Language("SQL")
        String sql = SELECT + "*" + FROM + PluginVersionTable.TABLE_NAME +
                WHERE + PluginVersionTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                ORDER_BY + PluginVersionTable.MODIFIED + " DESC, " + PluginVersionTable.PLUGIN_NAME;
        return db -> db.queryList(sql, PluginMetadataQueries::extractHistoryMetadata, serverUUID);
    }

    public static Query<List<PluginHistoryMetadata>> getPluginHistory() {
        @Language("SQL")
        String sql = SELECT + "*" + FROM + PluginVersionTable.TABLE_NAME +
                ORDER_BY + PluginVersionTable.MODIFIED + " DESC, " + PluginVersionTable.PLUGIN_NAME;
        return db -> db.queryList(sql, PluginMetadataQueries::extractHistoryMetadata);
    }

    @NotNull
    private static PluginHistoryMetadata extractHistoryMetadata(ResultSet row) throws SQLException {
        String name = row.getString(PluginVersionTable.PLUGIN_NAME);
        String version = row.getString(PluginVersionTable.VERSION);
        if (row.wasNull()) version = null;
        long modified = row.getLong(PluginVersionTable.MODIFIED);
        return new PluginHistoryMetadata(name, version, modified);
    }

    public static Query<List<PluginVersionTable.Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(PluginVersionTable.TABLE_NAME)
                .where(PluginVersionTable.ID + '>' + currentId)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, PluginVersionTable.Row::extract);
    }
}
