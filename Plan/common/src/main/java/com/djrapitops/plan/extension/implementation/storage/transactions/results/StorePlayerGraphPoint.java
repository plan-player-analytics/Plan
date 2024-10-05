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
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.ExpandGraphColumnCountTransaction;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * @author AuroraLS3
 */
public class StorePlayerGraphPoint extends Transaction {

    private final DataPoint dataPoint;
    private final UUID playerUUID;
    private final ProviderIdentifier identifier;

    public StorePlayerGraphPoint(DataPoint dataPoint, UUID playerUUID, ProviderIdentifier identifier) {
        this.dataPoint = dataPoint;
        this.playerUUID = playerUUID;
        this.identifier = identifier;
    }

    @Override
    protected void performOperations() {
        int columnCount = dataPoint.getValues().size();
        executeOther(new ExpandGraphColumnCountTransaction(identifier, columnCount));

        execute(new ExecStatement(ExtensionGraphMetadataTable.insertToGraphTableSql(identifier.getPluginName(), identifier.getProviderName(), columnCount,
                ExtensionGraphMetadataTable.TableType.PLAYER)) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getServerUUID().toString());
                statement.setLong(2, dataPoint.getX());
                for (int i = 0; i < columnCount; i++) {
                    Double value = dataPoint.getValues().get(i);
                    if (value == null) {
                        statement.setNull(i + 3, Types.DOUBLE);
                    } else {
                        statement.setDouble(i + 3, value);
                    }
                }
                statement.setString(columnCount + 1, playerUUID.toString());
            }
        });
    }
}
