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

import com.djrapitops.plan.db.access.transactions.Transaction;

import java.util.UUID;

/**
 * Transaction to store method result of a {@link com.djrapitops.plan.extension.implementation.providers.PercentageDataProvider}.
 *
 * @author Rsl1122
 */
public class StorePlayerPercentageResultTransaction extends Transaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final String methodName;
    private final UUID playerUUID;

    private final double value;

    public StorePlayerPercentageResultTransaction(String pluginName, UUID serverUUID, String methodName, UUID playerUUID, double value) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.methodName = methodName;
        this.playerUUID = playerUUID;
        this.value = value;
    }

    @Override
    protected void performOperations() {
        // TODO Store data in a table
    }
}