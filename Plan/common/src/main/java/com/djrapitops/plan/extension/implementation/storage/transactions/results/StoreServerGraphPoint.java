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

/**
 * Stores a server specific datapoint to extension graph table.
 *
 * @author AuroraLS3
 */
public class StoreServerGraphPoint extends Transaction {

    private final DataPoint dataPoint;
    private final ProviderIdentifier identifier;

    public StoreServerGraphPoint(DataPoint dataPoint, ProviderIdentifier identifier) {
        this.dataPoint = dataPoint;
        this.identifier = identifier;
    }

    @Override
    protected void performOperations() {
        int columnCount = dataPoint.getValues().size();
        executeOther(new ExpandGraphColumnCountTransaction(identifier, columnCount));

        execute(new ExecStatement(ExtensionGraphMetadataTable.insertToGraphTableSql(identifier.getPluginName(), identifier.getProviderName(), columnCount,
                ExtensionGraphMetadataTable.TableType.SERVER)) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getServerUUID().toString());
                statement.setLong(2, dataPoint.getX());
                for (int i = 0; i < dataPoint.getValues().size(); i++) {
                    Double value = dataPoint.getValues().get(i);
                    if (value == null) {
                        statement.setNull(i + 3, Types.DOUBLE);
                    } else {
                        statement.setDouble(i + 3, value);
                    }
                }
            }
        });
    }
}
