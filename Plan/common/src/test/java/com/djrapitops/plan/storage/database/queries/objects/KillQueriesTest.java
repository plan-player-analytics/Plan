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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AuroraLS3
 */
public interface KillQueriesTest extends DatabaseTestPreparer {

    default FinishedSession storeSessionForKillTest() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData(MobKillCounter.class).ifPresent(Counter::add);
        session.getExtraData(DeathCounter.class).ifPresent(Counter::add);
        session.getExtraData(PlayerKills.class).ifPresent(k -> k.add(expectedPlayerKill(session)));
        db().executeTransaction(new StoreSessionTransaction(session)).join();
        return session;
    }

    default PlayerKill expectedPlayerKill(FinishedSession session) {
        return new PlayerKill(
                new PlayerKill.Killer(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME),
                new PlayerKill.Victim(TestConstants.PLAYER_TWO_UUID, TestConstants.PLAYER_TWO_NAME, TestConstants.REGISTER_TIME),
                new ServerIdentifier(serverUUID(), TestConstants.SERVER_NAME),
                TestConstants.WEAPON_SWORD,
                session.getDate()
        );
    }

    @Test
    default void playerKillsOfServer() {
        FinishedSession session = storeSessionForKillTest();

        var expected = List.of(expectedPlayerKill(session));
        var result = db().query(KillQueries.fetchPlayerKillsOnServer(serverUUID(), 10));
        assertEquals(expected, result);
    }

    @Test
    default void playerKillsOfPlayer() {
        FinishedSession session = storeSessionForKillTest();

        var expected = List.of(expectedPlayerKill(session));
        var result = db().query(KillQueries.fetchPlayerKillsOfPlayer(TestConstants.PLAYER_ONE_UUID));
        assertEquals(expected, result);
    }

    @Test
    default void playerDeathsOfPlayer() {
        FinishedSession session = storeSessionForKillTest();

        var expected = List.of(expectedPlayerKill(session));
        var result = db().query(KillQueries.fetchPlayerDeathsOfPlayer(TestConstants.PLAYER_TWO_UUID));
        assertEquals(expected, result);
    }

    @Test
    default void playerKillCountOfServer() {
        storeSessionForKillTest();

        var result = db().query(KillQueries.playerKillCount(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(1, result);
    }

    @Test
    default void mobKillCountOfServer() {
        storeSessionForKillTest();

        var result = db().query(KillQueries.mobKillCount(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(1, result);
    }

    @Test
    default void deathCountOfServer() {
        storeSessionForKillTest();

        var result = db().query(KillQueries.deathCount(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(1, result);
    }

    @Test
    default void topWeaponsOfServer() {
        storeSessionForKillTest();

        var expected = List.of(TestConstants.WEAPON_SWORD);
        var result = db().query(KillQueries.topWeaponsOfServer(0L, Long.MAX_VALUE, serverUUID(), 3));
        assertEquals(expected, result);
    }

    @Test
    default void topWeaponsOfPlayer() {
        storeSessionForKillTest();

        var expected = List.of(TestConstants.WEAPON_SWORD);
        var result = db().query(KillQueries.topWeaponsOfPlayer(0L, Long.MAX_VALUE, TestConstants.PLAYER_ONE_UUID, 3));
        assertEquals(expected, result);
    }

}