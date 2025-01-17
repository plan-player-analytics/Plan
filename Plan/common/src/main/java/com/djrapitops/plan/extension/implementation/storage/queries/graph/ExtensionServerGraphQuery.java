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
import com.djrapitops.plan.extension.graph.XAxisType;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;

import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query for fetching data of a server graph.
 *
 * @author AuroraLS3
 */
public class ExtensionServerGraphQuery implements Query<Optional<ExtensionGraphDto>> {

    private static final ExtensionGraphMetadataTable.TableType TABLE_TYPE = ExtensionGraphMetadataTable.TableType.SERVER;

    private final ServerUUID serverUUID;
    private final String graphTableName;

    public ExtensionServerGraphQuery(ServerUUID serverUUID, String graphTableName) {
        this.serverUUID = serverUUID;
        this.graphTableName = graphTableName;
    }

    @Override
    public Optional<ExtensionGraphDto> executeQuery(SQLDB db) {
        Optional<ExtensionGraphDto> metadata = db.query(getMetadata());
        if (metadata.isEmpty()) {
            return Optional.empty();
        }
        // Optional being found means that the table name should exist and is no longer an SQL injection risk.
        addLabels(metadata.get());
        addUnits(metadata.get());
        addFormats(metadata.get());
        addValues(metadata.get());

        return metadata;
    }

    private void addValues(ExtensionGraphDto extensionGraphDto) {

    }

    private void addFormats(ExtensionGraphDto extensionGraphDto) {

    }

    private void addUnits(ExtensionGraphDto extensionGraphDto) {

    }

    // TODO #2544 add series labels to the graph point provider
    private void addLabels(ExtensionGraphDto extensionGraphDto) {

    }

    private Query<Optional<ExtensionGraphDto>> getMetadata() {
        String sql = SELECT + "*" + FROM + ExtensionGraphMetadataTable.TABLE_NAME + " m " +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p ON p.id=m." + ExtensionGraphMetadataTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " pl ON pl.id=p." + ExtensionProviderTable.PLUGIN_ID +
                WHERE + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME + "=?" +
                AND + ExtensionGraphMetadataTable.TABLE_TYPE + "=?" +
                AND + ExtensionPluginTable.SERVER_UUID + "=?";
        return db -> db.queryOptional(sql, set -> new ExtensionGraphDto(
                set.getString(ExtensionProviderTable.TEXT),
                XAxisType.valueOf(set.getString(ExtensionGraphMetadataTable.X_AXIS_TYPE)),
                set.getInt(ExtensionGraphMetadataTable.X_AXIS_SOFT_MIN),
                set.getInt(ExtensionGraphMetadataTable.X_AXIS_SOFT_MAX),
                set.getInt(ExtensionGraphMetadataTable.Y_AXIS_SOFT_MIN),
                set.getInt(ExtensionGraphMetadataTable.Y_AXIS_SOFT_MAX),
                set.getInt(ExtensionGraphMetadataTable.COLUMN_COUNT),
                set.getBoolean(ExtensionGraphMetadataTable.SUPPORTS_STACKING)
        ), graphTableName, TABLE_TYPE.getType(), serverUUID);
    }
}
