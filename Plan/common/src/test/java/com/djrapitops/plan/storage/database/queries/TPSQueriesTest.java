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
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.comparators.TPSComparator;
import com.djrapitops.plan.utilities.java.Lists;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utilities.RandomData;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public interface TPSQueriesTest extends DatabaseTestPreparer {

    @Test
    default void tpsIsStored() {
        List<TPS> expected = RandomData.randomTPS();
        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID(), tps));
        }

        forcePersistenceCheck();

        expected.sort(new TPSComparator());
        assertEquals(expected, db().query(TPSQueries.fetchTPSDataOfServer(Long.MIN_VALUE, Long.MAX_VALUE, serverUUID())));
    }

    @Test
    default void removeEverythingRemovesTPS() {
        tpsIsStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(TPSQueries.fetchTPSDataOfAllServersBut(0, System.currentTimeMillis(), ServerUUID.randomUUID())).isEmpty());
    }

    @Test
    default void playerMaxPeakIsCorrect() {
        List<TPS> tpsData = RandomData.randomTPS();

        for (TPS tps : tpsData) {
            db().executeTransaction(new TPSStoreTransaction(serverUUID(), tps));
        }

        tpsData.sort(Comparator.comparingInt(TPS::getPlayers));
        int expected = tpsData.get(tpsData.size() - 1).getPlayers();
        int actual = db().query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID())).map(DateObj::getValue).orElse(-1);
        assertEquals(expected, actual, () -> "Wrong return value. " + Lists.map(tpsData, TPS::getPlayers).toString());
    }

    @Test
    default void maxDateIsFetched() {
        List<TPS> tpsData = RandomData.randomTPS();

        for (TPS tps : tpsData) {
            db().executeTransaction(new TPSStoreTransaction(serverUUID(), tps));
        }

        long expected = tpsData.stream()
                .mapToLong(TPS::getDate)
                .max()
                .orElseThrow(AssertionError::new);
        long result = db().query(TPSQueries.fetchLastStoredTpsDate(serverUUID()))
                .orElseThrow(AssertionError::new);
        assertEquals(expected, result);
    }

    @Test
    default void sameServerIsDetected() {
        int value = ThreadLocalRandom.current().nextInt();
        long time = System.currentTimeMillis() - 50;
        TPS tps = new TPS(time, time, value, time, time, value, value, time);
        PluginLogger logger = Mockito.mock(PluginLogger.class);
        db().executeTransaction(new TPSStoreTransaction(logger, serverUUID(), tps));

        TPSStoreTransaction.setLastStorageCheck(0L);

        db().executeTransaction(new TPSStoreTransaction(logger, serverUUID(), tps));
        db().executeTransaction(new TPSStoreTransaction(logger, serverUUID(), tps));

        verify(logger, times(1)).warn(anyString());
    }

    @Test
    default void serverStartDateIsFetched() {
        List<TPS> tpsData = RandomData.randomTPS();
        TPS stored = tpsData.get(0);
        TPS stored2 = TPSBuilder.get().date(stored.getDate() + TimeUnit.MINUTES.toMillis(1L)).toTPS();
        db().executeTransaction(new TPSStoreTransaction(serverUUID(), stored));
        db().executeTransaction(new TPSStoreTransaction(serverUUID(), stored2));

        Optional<Long> result = db().query(TPSQueries.fetchLatestServerStartTime(serverUUID(), TimeUnit.MINUTES.toMillis(3)));
        assertTrue(result.isPresent());
        assertEquals(stored.getDate(), result.get());
    }

    @Test
    default void serverStartDateIsCorrect() {
        List<TPS> tpsData = RandomData.randomTPS();
        TPS stored = tpsData.get(0);
        TPS stored2 = TPSBuilder.get().date(stored.getDate() + TimeUnit.MINUTES.toMillis(4L)).toTPS();
        TPS stored3 = TPSBuilder.get().date(stored.getDate() + TimeUnit.MINUTES.toMillis(5L)).toTPS();
        db().executeTransaction(new TPSStoreTransaction(serverUUID(), stored));
        db().executeTransaction(new TPSStoreTransaction(serverUUID(), stored2));
        db().executeTransaction(new TPSStoreTransaction(serverUUID(), stored3));

        Optional<Long> result = db().query(TPSQueries.fetchLatestServerStartTime(serverUUID(), TimeUnit.MINUTES.toMillis(3)));
        assertTrue(result.isPresent());
        assertEquals(stored2.getDate(), result.get());
    }
}
