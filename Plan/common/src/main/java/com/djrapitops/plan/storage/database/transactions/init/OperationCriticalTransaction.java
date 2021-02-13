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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.exceptions.database.FatalDBException;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.transactions.Transaction;

/**
 * Transaction that is required to be executed before a database is operable.
 * <p>
 * If this transaction fails the database failed to open.
 *
 * @author AuroraLS3
 */
public abstract class OperationCriticalTransaction extends Transaction {

    @Override
    public void executeTransaction(SQLDB db) {
        try {
            super.executeTransaction(db);
            if (!success) {
                throw new FatalDBException(getClass().getName() + " failed to execute and database could not be opened.");
            }
        } catch (DBOpException e) {
            throw new FatalDBException(getClass().getName() + " failed to execute and database could not be opened: ", e);
        }
    }
}