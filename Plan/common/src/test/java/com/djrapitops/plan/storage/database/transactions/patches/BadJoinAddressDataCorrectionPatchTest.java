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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.JoinAddressQueries;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreJoinAddressTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author AuroraLS3
 */
public interface BadJoinAddressDataCorrectionPatchTest extends DatabaseTestPreparer {

    @Test
    default void joinAddressWithBadDataIsNotCorrectedWhenDataIsCorrect() {
        Database db = db();
        String correct = "correct_address";
        db.executeTransaction(new StoreJoinAddressTransaction(correct));

        Set<String> preTestExpected = Set.of(correct, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> preTestResult = new HashSet<>(db.query(JoinAddressQueries.allJoinAddresses()));
        assertEquals(preTestExpected, preTestResult);

        BadJoinAddressDataCorrectionPatch patch = new BadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);

        assertFalse(patch.wasApplied());
    }

    @Test
    default void joinAddressWithBadDataIsCorrectedWithOriginal() {
        Database db = db();
        String correct = "correct_address";
        String bad = "correct_address\u000062.6.…zwyzyty0zmnlowzmmtqynmm";
        db.executeTransaction(new StoreJoinAddressTransaction(correct));
        db.executeTransaction(new StoreJoinAddressTransaction(bad));

        Set<String> preTestExpected = Set.of(correct, bad, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> preTestResult = new HashSet<>(db.query(JoinAddressQueries.allJoinAddresses()));
        assertEquals(preTestExpected, preTestResult);

        BadJoinAddressDataCorrectionPatch patch = new BadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        Set<String> expected = Set.of(correct, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> result = new HashSet<>(db.query(JoinAddressQueries.allJoinAddresses()));
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressWithBadDataIsCorrectedWithOriginalPlusSessions() {
        Database db = db();
        String correct = "correct_address";
        String bad = "correct_address\u000062.6.…zwyzyty0zmnlowzmmtqynmm";
        db.executeTransaction(new StoreJoinAddressTransaction(correct));
        db.executeTransaction(new StoreJoinAddressTransaction(bad));

        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData().put(JoinAddress.class, new JoinAddress(bad));
        executeTransactions(new StoreSessionTransaction(session));

        Set<String> preTestExpected = Set.of(bad);
        Set<String> preTestResult = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(preTestExpected, preTestResult);

        BadJoinAddressDataCorrectionPatch patch = new BadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        Set<String> expected = Set.of(correct);
        Set<String> result = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressWithBadDataIsCorrectedWithoutOriginal() {
        Database db = db();
        String correct = "correct_address";
        String bad = "correct_address\u000062.6.…zwyzyty0zmnlowzmmtqynmm";
        db.executeTransaction(new StoreJoinAddressTransaction(bad));

        Set<String> preTestExpected = Set.of(bad, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> preTestResult = new HashSet<>(db.query(JoinAddressQueries.allJoinAddresses()));
        assertEquals(preTestExpected, preTestResult);

        BadJoinAddressDataCorrectionPatch patch = new BadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        Set<String> expected = Set.of(correct, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> result = new HashSet<>(db.query(JoinAddressQueries.allJoinAddresses()));
        assertEquals(expected, result);
    }

    @Test
    default void joinAddressWithBadDataIsCorrectedPerformanceTest() {
        Database db = db();
        String correct = "correct_address";
        String badPrefix = "correct_address\u0000";

        List<String> randomEnds = RandomData.pickMultiple(50000, () -> RandomData.randomString(25));

        db.executeTransaction(new StoreJoinAddressTransaction(correct));
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                for (String randomEnd : randomEnds) {
                    executeOther(new StoreJoinAddressTransaction(badPrefix + randomEnd));
                }
            }
        });

        long start = System.currentTimeMillis();
        BadJoinAddressDataCorrectionPatch patch = new BadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());
        long end = System.currentTimeMillis();
        long diff = end - start;
        assertTrue(diff < TimeUnit.SECONDS.toMillis(10L), () -> "Took too long! " + diff + " ms");

        Set<String> expected = Set.of(correct, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> result = new HashSet<>(db.query(JoinAddressQueries.allJoinAddresses()));
        assertEquals(expected, result);
    }

}