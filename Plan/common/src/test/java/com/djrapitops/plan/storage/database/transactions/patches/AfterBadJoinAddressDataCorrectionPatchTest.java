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
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.events.StoreJoinAddressTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static org.junit.jupiter.api.Assertions.*;

public interface AfterBadJoinAddressDataCorrectionPatchTest extends DatabaseTestPreparer {

    @Test
    default void missingIdsAreChangedToUnknown() {
        Database db = db();
        String joinAddress = "correct_address";
        db.executeTransaction(new StoreJoinAddressTransaction(joinAddress));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        executeTransactions(new StoreSessionTransaction(session));
        execute(new ExecStatement(DELETE_FROM + JoinAddressTable.TABLE_NAME + WHERE + JoinAddressTable.JOIN_ADDRESS + "=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, joinAddress);
            }
        });

        Set<String> preTestExpected = Set.of();
        Set<String> preTestResult = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(preTestExpected, preTestResult);

        AfterBadJoinAddressDataCorrectionPatch patch = new AfterBadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        Set<String> expected = Set.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> result = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Join address id correction: Last known join address updated to all undefined ids, for specific server")
    default void undefinedIdsAreUpdatedToLastKnownJoinAddressForThatServer() {
        Database db = db();
        String joinAddress = "correct_address";
        db.executeTransaction(new StoreServerPlayerTransaction(TestConstants.PLAYER_ONE_UUID, 0L, TestConstants.PLAYER_ONE_NAME, serverUUID(), joinAddress));
        db.executeTransaction(new StoreJoinAddressTransaction(joinAddress));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        executeTransactions(new StoreSessionTransaction(session));
        execute(new ExecStatement("UPDATE " + SessionsTable.TABLE_NAME + " SET " + SessionsTable.JOIN_ADDRESS_ID + "=-1" +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + "=(" +
                SELECT + JoinAddressTable.ID +
                FROM + JoinAddressTable.TABLE_NAME +
                WHERE + JoinAddressTable.JOIN_ADDRESS + "=?)") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, joinAddress);
            }
        });

        Set<String> preTestExpected = Set.of();
        Set<String> preTestResult = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(preTestExpected, preTestResult);

        AfterBadJoinAddressDataCorrectionPatch patch = new AfterBadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        Set<String> expected = Set.of(joinAddress);
        Set<String> result = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Join address id correction: Address updated to unknown when missing data for server")
    default void undefinedIdsAreUpdatedToUnkownIfJoinAddressDoesntExist() {
        Database db = db();
        String joinAddress = "correct_address";
        db.executeTransaction(new StoreJoinAddressTransaction(joinAddress));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        executeTransactions(new StoreSessionTransaction(session));
        execute(new ExecStatement("UPDATE " + SessionsTable.TABLE_NAME + " SET " + SessionsTable.JOIN_ADDRESS_ID + "=-1" +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + "=(" +
                SELECT + JoinAddressTable.ID +
                FROM + JoinAddressTable.TABLE_NAME +
                WHERE + JoinAddressTable.JOIN_ADDRESS + "=?)") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, joinAddress);
            }
        });

        Set<String> preTestExpected = Set.of();
        Set<String> preTestResult = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(preTestExpected, preTestResult);

        AfterBadJoinAddressDataCorrectionPatch patch = new AfterBadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertTrue(patch.wasApplied());

        Set<String> expected = Set.of(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        Set<String> result = db.query(JoinAddressQueries.latestJoinAddresses()).keySet();
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Join address id correction: Address not updated if it is already defined")
    default void definedIdsAreNotUpdated() {
        Database db = db();
        String joinAddress = "correct_address";
        db.executeTransaction(new StoreJoinAddressTransaction(joinAddress));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        executeTransactions(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        session.getExtraData().put(JoinAddress.class, new JoinAddress(joinAddress));
        executeTransactions(new StoreSessionTransaction(session));

        AfterBadJoinAddressDataCorrectionPatch patch = new AfterBadJoinAddressDataCorrectionPatch();
        db.executeTransaction(patch);
        assertFalse(patch.wasApplied());
    }
}