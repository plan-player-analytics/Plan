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

import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.Collection;

/**
 * Transaction to store sessions on server shutdown.
 *
 * @author AuroraLS3
 */
public class ServerShutdownTransaction extends Transaction {

    private final Collection<Session> unsavedSessions;

    public ServerShutdownTransaction(Collection<Session> unsavedSessions) {
        this.unsavedSessions = unsavedSessions;
    }

    @Override
    protected void performOperations() {
        execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(unsavedSessions));
    }
}