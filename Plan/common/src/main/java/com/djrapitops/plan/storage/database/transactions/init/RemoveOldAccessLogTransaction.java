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
package com.djrapitops.plan.storage.database.transactions.init;

import com.djrapitops.plan.storage.database.sql.tables.AccessLogTable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import static com.djrapitops.plan.storage.database.sql.building.Sql.DELETE_FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

public class RemoveOldAccessLogTransaction extends ThrowawayTransaction {

    private final long thresholdMs;

    public RemoveOldAccessLogTransaction(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    @Override
    protected void performOperations() {
        execute(DELETE_FROM + AccessLogTable.TABLE_NAME + WHERE + AccessLogTable.TIME + "<" + (System.currentTimeMillis() - thresholdMs));
    }
}
