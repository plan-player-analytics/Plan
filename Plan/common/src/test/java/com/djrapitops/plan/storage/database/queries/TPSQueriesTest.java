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
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.java.Lists;
import org.junit.jupiter.api.Test;
import utilities.RandomData;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface TPSQueriesTest extends DatabaseTestPreparer {

    @Test
    default void tpsIsStored() {
        List<TPS> expected = RandomData.randomTPS();
        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID(), tps));
        }

        forcePersistenceCheck();

        assertEquals(expected, db().query(TPSQueries.fetchTPSDataOfServer(Long.MIN_VALUE, Long.MAX_VALUE, serverUUID())));
    }

    @Test
    default void removeEverythingRemovesTPS() {
        tpsIsStored();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(TPSQueries.fetchTPSDataOfAllServersBut(0, System.currentTimeMillis(), UUID.randomUUID())).isEmpty());
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
}
