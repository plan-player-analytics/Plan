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

import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.table.Table;

import java.util.UUID;

/**
 * Transaction to store information about a {@link com.djrapitops.plan.extension.implementation.providers.TableDataProvider}.
 *
 * @author Rsl1122
 */
public class StoreTableProviderTransaction extends Transaction {

    private final UUID serverUUID;
    private final String pluginName;
    private final String methodName;
    private final Color tableColor;
    private final Table table;

    public StoreTableProviderTransaction(UUID serverUUID, String pluginName, String methodName, Color tableColor, Table table) {
        this.pluginName = pluginName;
        this.methodName = methodName;
        this.tableColor = tableColor;
        this.table = table;
        this.serverUUID = serverUUID;
    }

    @Override
    protected void performOperations() {
        execute(storeProvider());
    }

    private Executable storeProvider() {
        return connection -> {
            if (!updateProvider().execute(connection)) {
                return insertProvider().execute(connection);
            }
            return false;
        };
    }

    private Executable updateProvider() {
        return connection -> false; // TODO
    }

    private Executable insertProvider() {
        return connection -> false; // TODO
    }
}