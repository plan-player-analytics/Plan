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
package com.djrapitops.plan.storage.database.queries.analysis;

import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface TopListQueriesTest extends DatabaseTestPreparer {

    private void storeSessionForTopListQueries() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));
    }

    @Test
    default void topActivePlaytimeListQueryReturnsSinglePlayer() {
        storeSessionForTopListQueries();

        String expected = TestConstants.PLAYER_ONE_NAME;
        String result = db().query(TopListQueries.fetchNthTop10ActivePlaytimePlayerOn(serverUUID(), 0, 0, System.currentTimeMillis()))
                .orElseThrow(AssertionError::new)
                .getPlayerName();
        assertEquals(expected, result);
    }

    @Test
    default void topPlaytimeListQueryReturnsSinglePlayer() {
        storeSessionForTopListQueries();

        String expected = TestConstants.PLAYER_ONE_NAME;
        String result = db().query(TopListQueries.fetchNthTop10ActivePlaytimePlayerOn(serverUUID(), 0, 0, System.currentTimeMillis()))
                .orElseThrow(AssertionError::new)
                .getPlayerName();
        assertEquals(expected, result);
    }

}