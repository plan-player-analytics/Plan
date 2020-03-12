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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;
import org.junit.jupiter.api.BeforeEach;
import utilities.TestConstants;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface DatabaseTestPreparer {

    String[] worlds = new String[]{"TestWorld", "TestWorld2"};
    UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

    Database db();

    UUID serverUUID();

    PlanSystem system();

    @BeforeEach
    default void setUp() {
        db().executeTransaction(new Patch() {
            @Override
            public boolean hasBeenApplied() {
                return false;
            }

            @Override
            public void applyPatch() {
                dropTable("plan_world_times");
                dropTable("plan_kills");
                dropTable("plan_sessions");
                dropTable("plan_worlds");
                dropTable("plan_users");
            }
        });
        db().executeTransaction(new CreateTablesTransaction());
        db().executeTransaction(new RemoveEverythingTransaction());

        db().executeTransaction(new StoreServerInformationTransaction(new Server(-1, serverUUID(), "ServerName", "", 20)));
        assertEquals(serverUUID(), ((SQLDB) db()).getServerUUIDSupplier().get());
    }

    default void execute(Executable executable) {
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(executable);
            }
        });
    }

    default void executeTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            db().executeTransaction(transaction);
        }
    }

    default void forcePersistenceCheck() {
        db().close();
        db().init();
    }
}
