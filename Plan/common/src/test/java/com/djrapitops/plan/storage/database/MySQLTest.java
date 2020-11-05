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
import com.djrapitops.plan.storage.database.queries.*;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.DBPreparer;
import utilities.RandomData;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for MySQL database.
 * <p>
 * The setup assumes CI environment with MySQL service running.
 * 'MYSQL_DB' database should be created before the test.
 *
 * @author Rsl1122
 * @see DatabaseTest
 * @see ExtensionsDatabaseTest
 * @see utilities.CIProperties for assumed MySQL setup.
 */
@ExtendWith(MockitoExtension.class)
class MySQLTest implements DatabaseTest,
        DatabaseBackupTest,
        //ExtensionsDatabaseTest, TODO Test hangs forever for some reason, investigate later.
        ActivityIndexQueriesTest,
        GeolocationQueriesTest,
        NicknameQueriesTest,
        PingQueriesTest,
        SessionQueriesTest,
        ServerQueriesTest,
        TPSQueriesTest,
        UserInfoQueriesTest,
        WebUserQueriesTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private static PlanSystem system;
    private static Database database;

    @BeforeAll
    static void setupDatabase(@TempDir Path temp) throws Exception {
        system = new PluginMockComponent(temp).getPlanSystem();
        Optional<Database> mysql = new DBPreparer(system, TEST_PORT_NUMBER).prepareMySQL();
        Assumptions.assumeTrue(mysql.isPresent());
        database = mysql.get();
    }

    @BeforeEach
    void setUp() {
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

        db().executeTransaction(new StoreServerInformationTransaction(new Server(serverUUID(), "ServerName", "")));
        assertEquals(serverUUID(), ((SQLDB) db()).getServerUUIDSupplier().get());
    }
    @AfterAll
    static void disableSystem() {
        if (database != null) database.close();
        system.disable();
    }

    @Override
    public Database db() {
        return database;
    }

    @Override
    public UUID serverUUID() {
        return system.getServerInfo().getServerUUID();
    }

    @Override
    public PlanSystem system() {
        return system;
    }
}
