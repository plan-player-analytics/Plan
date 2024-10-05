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

import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionGraphQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.List;

/**
 * Drops all existing extension graph tables.
 *
 * @author AuroraLS3
 */
public class RemoveGraphTablesTransaction extends Transaction {

    @Override
    protected void performOperations() {
        List<String> tableNames = query(ExtensionGraphQueries.findGraphTableNames());
        tableNames.forEach(this::dropTable);
    }

    private void dropTable(String tableName) {
        execute("DROP TABLE IF EXISTS " + tableName);
    }
}
