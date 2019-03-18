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

import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProvider;

import java.util.UUID;

/**
 * Transaction to store information about a dobule {@link DataProvider}.
 * <p>
 * Includes:
 * {@link com.djrapitops.plan.extension.implementation.providers.DoubleDataProvider}.
 * {@link com.djrapitops.plan.extension.implementation.providers.PercentageDataProvider}.
 *
 * @author Rsl1122
 */
public class StoreDoubleProviderTransaction extends Transaction {

    private final DataProvider<Double> provider;
    private final UUID serverUUID;

    public StoreDoubleProviderTransaction(DataProvider<Double> provider, UUID serverUUID) {
        this.provider = provider;
        this.serverUUID = serverUUID;
    }

    @Override
    protected void performOperations() {
        ProviderInformation providerInformation = provider.getProviderInformation();

        // TODO Store provider in a table
    }
}