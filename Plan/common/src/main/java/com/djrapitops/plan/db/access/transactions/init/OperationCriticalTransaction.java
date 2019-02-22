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
package com.djrapitops.plan.db.access.transactions.init;

import com.djrapitops.plan.api.exceptions.database.FatalDBException;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.transactions.Transaction;

/**
 * Transaction that is required to be executed before a database is operable.
 * <p>
 * If this transaction fails the database failed to open.
 *
 * @author Rsl1122
 */
public abstract class OperationCriticalTransaction extends Transaction {

    @Override
    public void executeTransaction(SQLDB db) {
        super.executeTransaction(db);
        if (!success) {
            throw new FatalDBException(getClass().getSimpleName() + " failed to execute and database can not be opened.");
        }
    }
}