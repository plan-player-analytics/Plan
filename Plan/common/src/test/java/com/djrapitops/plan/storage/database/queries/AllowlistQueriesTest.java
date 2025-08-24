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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.delivery.domain.datatransfer.AllowlistBounce;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.AllowlistQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreAllowlistBounceTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface AllowlistQueriesTest extends DatabaseTestPreparer {

    @Test
    @DisplayName("plan_allowlist_bounce is empty")
    default void allowListTableIsEmpty() {
        List<AllowlistBounce> expected = List.of();
        List<AllowlistBounce> result = db().query(AllowlistQueries.getBounces(serverUUID()));
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("plan_allowlist_bounce is cleared by RemoveEverythingTransaction")
    default void allowListTableIsEmptyAfterClear() throws ExecutionException, InterruptedException {
        allowListBounceIsStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        allowListTableIsEmpty();
    }

    @Test
    @DisplayName("Allowlist bounce is stored")
    default void allowListBounceIsStored() throws ExecutionException, InterruptedException {
        AllowlistBounce bounce = new AllowlistBounce(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, 1, System.currentTimeMillis());
        db().executeTransaction(new StoreAllowlistBounceTransaction(bounce.getPlayerUUID(), bounce.getPlayerName(), serverUUID(), bounce.getLastTime()))
                .get();

        List<AllowlistBounce> expected = List.of(bounce);
        List<AllowlistBounce> result = db().query(AllowlistQueries.getBounces(serverUUID()));
        assertEquals(expected, result);
    }

}