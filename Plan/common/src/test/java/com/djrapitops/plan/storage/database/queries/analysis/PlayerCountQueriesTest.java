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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for player count queries.
 *
 * @author AuroraLS3
 */
public interface PlayerCountQueriesTest extends DatabaseTestPreparer {

    @Test
    default void retainedPlayerCountCountsPlayersRegisteredAndActiveInLatterHalf() {
        long now = System.currentTimeMillis();
        long month = TimeUnit.DAYS.toMillis(30L);
        long after = now - month;
        long half = now - month / 2;

        // Player 1: registered in first half, session in latter half → retained
        long player1Register = after + TimeUnit.DAYS.toMillis(5L);
        long player1SessionStart = half + TimeUnit.DAYS.toMillis(5L);
        long player1SessionEnd = player1SessionStart + TimeUnit.HOURS.toMillis(2L);
        FinishedSession player1Session = RandomData.randomSession(serverUUID(), worlds, player1SessionStart, playerUUID);

        // Player 2: registered in first half, session in latter half → retained
        long player2Register = after + TimeUnit.DAYS.toMillis(10L);
        long player2SessionStart = half + TimeUnit.DAYS.toMillis(3L);
        long player2SessionEnd = player2SessionStart + TimeUnit.HOURS.toMillis(1L);
        FinishedSession player2Session = RandomData.randomSession(serverUUID(), worlds, player2SessionStart, player2UUID);

        // Setup
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> player1Register,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, () -> player2Register,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(player1Session));
        db().executeTransaction(new StoreSessionTransaction(player2Session));

        forcePersistenceCheck();

        // Both players are retained: registered within timeframe and active in latter half
        assertEquals(2, db().query(PlayerCountQueries.retainedPlayerCount(after, now, serverUUID())));
    }

    @Test
    default void retainedPlayerCountExcludesPlayersWithoutSessionInLatterHalf() {
        long now = System.currentTimeMillis();
        long month = TimeUnit.DAYS.toMillis(30L);
        long after = now - month;
        long half = now - month / 2;

        // Player 1: registered in timeframe, but session only in first half → NOT retained
        long player1Register = after + TimeUnit.DAYS.toMillis(5L);
        long player1SessionStart = after + TimeUnit.DAYS.toMillis(10L);
        long player1SessionEnd = player1SessionStart + TimeUnit.HOURS.toMillis(2L);
        FinishedSession player1Session = RandomData.randomSession(serverUUID(), worlds, player1SessionStart, playerUUID);

        // Setup
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> player1Register,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(player1Session));

        forcePersistenceCheck();

        // Player registered in timeframe but only played in first half → not retained
        assertEquals(0, db().query(PlayerCountQueries.retainedPlayerCount(after, now, serverUUID())));
    }

    @Test
    default void retainedPlayerCountExcludesPlayersRegisteredOutsideTimeframe() {
        long now = System.currentTimeMillis();
        long month = TimeUnit.DAYS.toMillis(30L);
        long after = now - month;
        long half = now - month / 2;

        // Player 1: registered BEFORE timeframe, session in latter half → NOT retained
        long player1Register = after - TimeUnit.DAYS.toMillis(10L);
        long player1SessionStart = half + TimeUnit.DAYS.toMillis(5L);
        long player1SessionEnd = player1SessionStart + TimeUnit.HOURS.toMillis(2L);
        FinishedSession player1Session = RandomData.randomSession(serverUUID(), worlds, player1SessionStart, playerUUID);

        // Setup
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> player1Register,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(player1Session));

        forcePersistenceCheck();

        // Player registered before timeframe → not retained
        assertEquals(0, db().query(PlayerCountQueries.retainedPlayerCount(after, now, serverUUID())));
    }

    @Test
    default void retainedPlayerCountReturnsZeroWhenNoPlayers() {
        assertEquals(0, db().query(PlayerCountQueries.retainedPlayerCount(0L, System.currentTimeMillis(), serverUUID())));
    }

    @Test
    default void retainedPlayerCountWithMixedRetentionStatus() {
        long now = System.currentTimeMillis();
        long month = TimeUnit.DAYS.toMillis(30L);
        long after = now - month;
        long half = now - month / 2;

        // Player 1: registered in timeframe, session in latter half → retained
        long player1Register = after + TimeUnit.DAYS.toMillis(5L);
        long player1SessionStart = half + TimeUnit.DAYS.toMillis(5L);
        long player1SessionEnd = player1SessionStart + TimeUnit.HOURS.toMillis(2L);
        FinishedSession player1Session = new FinishedSession(
                playerUUID, serverUUID(), player1SessionStart, player1SessionEnd, 0L,
                RandomData.randomSession(serverUUID(), worlds, player1SessionStart, playerUUID).extraData());

        // Player 2: registered in timeframe, session only in first half → NOT retained
        long player2Register = after + TimeUnit.DAYS.toMillis(10L);
        long player2SessionStart = after + TimeUnit.DAYS.toMillis(12L);
        long player2SessionEnd = player2SessionStart + TimeUnit.HOURS.toMillis(1L);
        FinishedSession player2Session = new FinishedSession(
                player2UUID, serverUUID(), player2SessionStart, player2SessionEnd, 0L,
                RandomData.randomSession(serverUUID(), worlds, player2SessionStart, player2UUID).extraData());

        // Setup
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> player1Register,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, () -> player2Register,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(player1Session));
        db().executeTransaction(new StoreSessionTransaction(player2Session));

        forcePersistenceCheck();

        // Only player 1 is retained (player 2 only played in first half)
        assertEquals(1, db().query(PlayerCountQueries.retainedPlayerCount(after, now, serverUUID())));
    }

    @Test
    default void retainedPlayerCountWithEmptyServerListReturnsNetworkResult() {
        long now = System.currentTimeMillis();
        long month = TimeUnit.DAYS.toMillis(30L);
        long after = now - month;
        long half = now - month / 2;

        // Player 1: registered in timeframe, session in latter half → retained
        long player1Register = after + TimeUnit.DAYS.toMillis(5L);
        long player1SessionStart = half + TimeUnit.DAYS.toMillis(5L);
        FinishedSession player1Session = RandomData.randomSession(serverUUID(), worlds, player1SessionStart, playerUUID);

        // Setup
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> player1Register,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(player1Session));

        forcePersistenceCheck();

        // Empty server list should query all servers (network-wide)
        assertEquals(1, db().query(PlayerCountQueries.retainedPlayerCount(after, now, List.of())));
    }
}