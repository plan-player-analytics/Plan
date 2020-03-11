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

import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.objects.NetworkTablePlayersQuery;
import com.djrapitops.plan.storage.database.queries.objects.ServerTablePlayersQuery;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.transactions.events.PlayerServerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public interface ActivityIndexQueriesTest extends DatabaseTestPreparer {

    default void storeSessions() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME, serverUUID()));
        db().executeTransaction(new WorldNameStoreTransaction(serverUUID(), worlds[0]));

        Session session = new Session(playerUUID, serverUUID(), 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);

        execute(DataStoreQueries.storeSession(session));
    }

    @Test
    default void activityIndexCoalesceSanityCheck() {
        storeSessions();
        Map<String, Integer> groupings = db().query(
                ActivityIndexQueries.fetchActivityIndexGroupingsOn(System.currentTimeMillis(), serverUUID(), TimeUnit.HOURS.toMillis(2L))
        );
        Map<String, Integer> expected = Collections.singletonMap(ActivityIndex.getDefaultGroups()[4], 1); // Inactive
        assertEquals(expected, groupings);
    }

    @Test
    default void activityIndexCalculationsMatch() {
        storeSessions();

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<Session> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new ServerTablePlayersQuery(serverUUID(), date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001);
    }

    @Test
    default void networkActivityIndexCalculationsMatch() {
        storeSessions();

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<Session> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new NetworkTablePlayersQuery(date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001);
    }

    @Test
    default void activeTurnedInactiveQueryHasAllParametersSet() {
        Integer result = db().query(ActivityIndexQueries.countRegularPlayersTurnedInactive(
                0, System.currentTimeMillis(), serverUUID(),
                TimeUnit.HOURS.toMillis(2L)
        ));
        assertNotNull(result);
    }

}