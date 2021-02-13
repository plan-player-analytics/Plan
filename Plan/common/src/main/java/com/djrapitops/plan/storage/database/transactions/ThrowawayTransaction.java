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
package com.djrapitops.plan.storage.database.transactions;

import com.djrapitops.plan.storage.database.Database;

/**
 * {@link Transaction} that can be thrown away if it is in execution when the database is closing.
 * <p>
 * This transaction type is for storing data that is not critical to be saved on plugin shutdown.
 *
 * @author AuroraLS3
 */
public abstract class ThrowawayTransaction extends Transaction {

    @Override
    protected boolean shouldBeExecuted() {
        return getDBState() != Database.State.CLOSING && dbIsNotUnderHeavyLoad();
    }
}