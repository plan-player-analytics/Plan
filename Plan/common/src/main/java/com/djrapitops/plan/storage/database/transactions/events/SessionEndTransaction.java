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

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

/**
 * Transaction for storing a session after a session has ended.
 *
 * @author Rsl1122
 */
public class SessionEndTransaction extends Transaction {

    private final Session session;

    public SessionEndTransaction(Session session) {
        this.session = session;
    }

    @Override
    protected void performOperations() {
        execute(DataStoreQueries.storeSession(session));

        session.getValue(SessionKeys.SERVER_UUID)
                .ifPresent(serverUUID -> JSONCache.invalidate(
                        serverUUID,
                        DataID.SESSIONS,
                        DataID.GRAPH_WORLD_PIE,
                        DataID.GRAPH_PUNCHCARD,
                        DataID.KILLS,
                        DataID.ONLINE_OVERVIEW,
                        DataID.SESSIONS_OVERVIEW,
                        DataID.PVP_PVE,
                        DataID.GRAPH_UNIQUE_NEW,
                        DataID.GRAPH_CALENDAR
                ));
    }
}