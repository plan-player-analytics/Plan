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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.UUID;

/**
 * Transaction to store server's TPS data.
 *
 * @author AuroraLS3
 */
public class TPSStoreTransaction extends Transaction {

    private final UUID serverUUID;
    private final TPS tps;

    public TPSStoreTransaction(UUID serverUUID, TPS tps) {
        this.serverUUID = serverUUID;
        this.tps = tps;
    }

    @Override
    protected void performOperations() {
        execute(DataStoreQueries.storeTPS(serverUUID, tps));
    }
}