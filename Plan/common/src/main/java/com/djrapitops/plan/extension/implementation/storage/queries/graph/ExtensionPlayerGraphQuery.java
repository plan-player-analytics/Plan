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
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Query for fetching data of a player graph.
 *
 * @author AuroraLS3
 */
public class ExtensionPlayerGraphQuery implements Query<Optional<ExtensionGraphDto>> {

    private final ServerUUID serverUUID;
    private final UUID playerUUID;
    @Untrusted
    private final String graphTableName;

    public ExtensionPlayerGraphQuery(ServerUUID serverUUID, UUID playerUUID, @Untrusted String graphTableName) {
        this.serverUUID = serverUUID;
        this.playerUUID = playerUUID;
        this.graphTableName = graphTableName;
    }

    @Override
    public Optional<ExtensionGraphDto> executeQuery(SQLDB db) {
        Optional<ExtensionGraphDto> metadata = db.query(ExtensionGraphQueries.getGraphMetadata(graphTableName, ExtensionGraphMetadataTable.TableType.PLAYER, serverUUID));
        if (metadata.isEmpty()) {
            return Optional.empty();
        }
        // Optional being found means that the table name should exist and is no longer an SQL injection risk.
        addLabels(db, metadata.get());
        addUnits(db, metadata.get());
        addFormats(db, metadata.get());
        addColors(db, metadata.get());
        addValues(db, metadata.get());

        return metadata;
    }

    private void addFormats(SQLDB db, ExtensionGraphDto extensionGraphDto) {
        extensionGraphDto.valueFormats().addAll(
                db.query(ExtensionGraphQueries.getGraphFormats(extensionGraphDto.providerId(), extensionGraphDto.columnCount()))
        );
    }

    private void addUnits(SQLDB db, ExtensionGraphDto extensionGraphDto) {
        extensionGraphDto.unitNames().addAll(
                db.query(ExtensionGraphQueries.getGraphUnits(extensionGraphDto.providerId(), extensionGraphDto.columnCount()))
        );
    }

    private void addLabels(SQLDB db, ExtensionGraphDto extensionGraphDto) {
        extensionGraphDto.seriesLabels().addAll(
                db.query(ExtensionGraphQueries.getGraphSeriesLabels(extensionGraphDto.providerId(), extensionGraphDto.columnCount()))
        );
    }

    private void addColors(SQLDB db, ExtensionGraphDto extensionGraphDto) {
        extensionGraphDto.seriesColors().addAll(
                db.query(ExtensionGraphQueries.getGraphColors(extensionGraphDto.providerId(), extensionGraphDto.columnCount()))
        );
    }

    private void addValues(SQLDB db, ExtensionGraphDto extensionGraphDto) {
        extensionGraphDto.dataPoints().addAll(db.queryList(ExtensionGraphMetadataTable.selectFromGraphTableSql(
                        extensionGraphDto.graphTableName(), ExtensionGraphMetadataTable.TableType.PLAYER),
                row -> extractDataPoint(row, extensionGraphDto), serverUUID, playerUUID));
    }

    private Number[] extractDataPoint(ResultSet row, ExtensionGraphDto extensionGraphDto) throws SQLException {
        Number[] point = new Number[extensionGraphDto.columnCount() + 1];
        point[0] = row.getLong("x");

        IntStream.range(0, extensionGraphDto.columnCount())
                .forEach(i -> {
                    try {
                        double value = row.getDouble("value_" + (i + 1));
                        point[i + 1] = row.wasNull() ? null : value;
                    } catch (SQLException e) {
                        throw DBOpException.forCause("Failed to get double", e);
                    }
                });
        return point;
    }
}
