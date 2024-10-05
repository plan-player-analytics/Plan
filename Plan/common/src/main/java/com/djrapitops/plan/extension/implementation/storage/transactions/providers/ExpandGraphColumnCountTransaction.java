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
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author AuroraLS3
 */
public class ExpandGraphColumnCountTransaction extends Transaction {

    private final ProviderIdentifier identifier;
    private final int colCount;

    public ExpandGraphColumnCountTransaction(ProviderIdentifier identifier, int colCount) {
        this.identifier = identifier;
        this.colCount = colCount;
    }

    @Override
    protected void performOperations() {
        Integer existingColCount = query(db -> db.queryOptional(ExtensionGraphMetadataTable.STATEMENT_SELECT_COLUMN_COUNT,
                row -> row.getInt(ExtensionGraphMetadataTable.COLUMN_COUNT),
                identifier.getProviderName(), identifier.getPluginName(), identifier.getServerUUID()))
                .orElseThrow(() -> new DBOpException("Graph table metadata does not exist"));
        if (existingColCount < colCount) {
            ExtensionGraphMetadataTable.addColumnsStatements(identifier.getPluginName(), identifier.getProviderName(), existingColCount, colCount)
                    .forEach(this::execute);
            execute(updateColumnCount());
        }
    }

    private @NotNull ExecStatement updateColumnCount() {
        return new ExecStatement(ExtensionGraphMetadataTable.UPDATE_COLUMN_COUNT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, colCount);
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 2, identifier.getProviderName(), identifier.getPluginName(), identifier.getServerUUID());
            }
        };
    }
}
