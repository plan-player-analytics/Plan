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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.PingQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PingStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface PingQueriesTest extends DatabaseTestPreparer {

    private void prepareForPingStorage() {
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
    }

    @Test
    default void pingStoreTransactionOutOfOrderDoesNotFailDueToMissingUser() throws ExecutionException, InterruptedException {
        DateObj<Integer> saved = RandomData.randomIntDateObject();
        int value = saved.getValue();
        db().executeTransaction(new PingStoreTransaction(player2UUID, serverUUID(),
                Collections.singletonList(saved)
        )).get();

        Map<UUID, List<Ping>> expected = Collections.singletonMap(player2UUID, Collections.singletonList(
                new Ping(saved.getDate(), serverUUID(), value, value, value)
        ));
        Map<UUID, List<Ping>> fetched = db().query(PingQueries.fetchAllPingData());
        assertEquals(expected, fetched);
    }

    @Test
    default void pingStoreTransactionOutOfOrderUpdatesUserInformation() throws ExecutionException, InterruptedException {
        db().executeTransaction(new PingStoreTransaction(player2UUID, serverUUID(),
                Collections.singletonList(RandomData.randomIntDateObject())
        )).get();
        long registerDate = RandomData.randomTime();
        db().executeTransaction(new PlayerRegisterTransaction(player2UUID, () -> registerDate, TestConstants.PLAYER_ONE_NAME)).get();

        Optional<BaseUser> expected = Optional.of(new BaseUser(player2UUID, TestConstants.PLAYER_ONE_NAME, registerDate, 0));
        Optional<BaseUser> result = db().query(BaseUserQueries.fetchBaseUserOfPlayer(player2UUID));
        assertEquals(expected, result);
    }

    @Test
    default void singlePingIsStored() throws ExecutionException, InterruptedException {
        prepareForPingStorage();

        DateObj<Integer> saved = RandomData.randomIntDateObject(1, 4001); // accepted ping range 1-4000 ms
        int value = saved.getValue();
        db().executeTransaction(new PingStoreTransaction(playerUUID, serverUUID(),
                Collections.singletonList(saved)
        )).get();
        Map<UUID, List<Ping>> expected = Collections.singletonMap(playerUUID, Collections.singletonList(
                new Ping(saved.getDate(), serverUUID(), value, value, value)
        ));
        Map<UUID, List<Ping>> fetched = db().query(PingQueries.fetchAllPingData());
        assertEquals(expected, fetched);
    }

    @Test
    default void pingIsStored() {
        prepareForPingStorage();

        Map<UUID, List<Ping>> expected = Collections.singletonMap(playerUUID, RandomData.randomPings(serverUUID()));
        execute(LargeStoreQueries.storeAllPingData(expected));
        Map<UUID, List<Ping>> fetched = db().query(PingQueries.fetchAllPingData());
        assertEquals(expected, fetched);
    }

    @Test
    default void removeEverythingRemovesPing() {
        pingIsStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(PingQueries.fetchAllPingData()).isEmpty());
    }

}
