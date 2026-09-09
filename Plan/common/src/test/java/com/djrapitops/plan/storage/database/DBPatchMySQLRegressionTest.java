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

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.KillsOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import utilities.*;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test for the patching of Plan 4.5.2 MySQL DB into the newest schema.
 *
 * @author AuroraLS3
 */
class DBPatchMySQLRegressionTest extends DBPatchRegressionTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    public static PluginMockComponent component;
    private static DBPreparer dbPreparer;
    private final String serverTable = "CREATE TABLE IF NOT EXISTS plan_servers (id integer NOT NULL AUTO_INCREMENT, uuid varchar(36) NOT NULL UNIQUE, name varchar(100), web_address varchar(100), is_installed boolean NOT NULL DEFAULT 1, max_players integer NOT NULL DEFAULT -1, PRIMARY KEY (id))";
    private final String usersTable = "CREATE TABLE IF NOT EXISTS plan_users (id integer NOT NULL AUTO_INCREMENT, uuid varchar(36) NOT NULL UNIQUE, registered bigint NOT NULL, name varchar(16) NOT NULL, times_kicked integer NOT NULL DEFAULT 0, PRIMARY KEY (id))";
    private final String userInfoTable = "CREATE TABLE IF NOT EXISTS plan_user_info (user_id integer NOT NULL, registered bigint NOT NULL, opped boolean NOT NULL DEFAULT 0, banned boolean NOT NULL DEFAULT 0, server_id integer NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String geoInfoTable = "CREATE TABLE IF NOT EXISTS plan_ips (user_id integer NOT NULL, ip varchar(39) NOT NULL, geolocation varchar(50) NOT NULL, ip_hash varchar(200), last_used bigint NOT NULL DEFAULT 0, FOREIGN KEY(user_id) REFERENCES plan_users(id))";
    private final String nicknameTable = "CREATE TABLE IF NOT EXISTS plan_nicknames (user_id integer NOT NULL, nickname varchar(75) NOT NULL, server_id integer NOT NULL, last_used bigint NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String sessionsTable = "CREATE TABLE IF NOT EXISTS plan_sessions (id integer NOT NULL AUTO_INCREMENT, user_id integer NOT NULL, server_id integer NOT NULL, session_start bigint NOT NULL, session_end bigint NOT NULL, mob_kills integer NOT NULL, deaths integer NOT NULL, afk_time bigint NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id), PRIMARY KEY (id))";
    private final String killsTable = "CREATE TABLE IF NOT EXISTS plan_kills (killer_id integer NOT NULL, victim_id integer NOT NULL, server_id integer NOT NULL, weapon varchar(30) NOT NULL, date bigint NOT NULL, session_id integer NOT NULL, FOREIGN KEY(killer_id) REFERENCES plan_users(id), FOREIGN KEY(victim_id) REFERENCES plan_users(id), FOREIGN KEY(session_id) REFERENCES plan_sessions(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String pingTable = "CREATE TABLE IF NOT EXISTS plan_ping (id integer NOT NULL AUTO_INCREMENT, user_id integer NOT NULL, server_id integer NOT NULL, date bigint NOT NULL, max_ping integer NOT NULL, min_ping integer NOT NULL, avg_ping double NOT NULL, PRIMARY KEY (id), FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String commandUseTable = "CREATE TABLE IF NOT EXISTS plan_commandusages (id integer NOT NULL AUTO_INCREMENT, command varchar(20) NOT NULL, times_used integer NOT NULL, server_id integer NOT NULL, PRIMARY KEY (id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String tpsTable = "CREATE TABLE IF NOT EXISTS plan_tps (server_id integer NOT NULL, date bigint NOT NULL, tps double NOT NULL, players_online integer NOT NULL, cpu_usage double NOT NULL, ram_usage bigint NOT NULL, entities integer NOT NULL, chunks_loaded integer NOT NULL, free_disk_space bigint NOT NULL, FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String worldsTable = "CREATE TABLE IF NOT EXISTS plan_worlds (id integer NOT NULL AUTO_INCREMENT, world_name varchar(100) NOT NULL, server_id integer NOT NULL, PRIMARY KEY (id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    private final String worldTimesTable = "CREATE TABLE IF NOT EXISTS plan_world_times (user_id integer NOT NULL, world_id integer NOT NULL, server_id integer NOT NULL, session_id integer NOT NULL, survival_time bigint NOT NULL DEFAULT 0, creative_time bigint NOT NULL DEFAULT 0, adventure_time bigint NOT NULL DEFAULT 0, spectator_time bigint NOT NULL DEFAULT 0, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(world_id) REFERENCES plan_worlds(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id), FOREIGN KEY(session_id) REFERENCES plan_sessions(id))";
    private final String securityTable = "CREATE TABLE IF NOT EXISTS plan_security (username varchar(100) NOT NULL UNIQUE, salted_pass_hash varchar(100) NOT NULL UNIQUE, permission_level integer NOT NULL)";
    private final String transferTable = "CREATE TABLE IF NOT EXISTS plan_transfer (sender_server_id integer NOT NULL, expiry_date bigint NOT NULL DEFAULT 0, type varchar(100) NOT NULL, extra_variables varchar(255) DEFAULT '', content_64 MEDIUMTEXT, part bigint NOT NULL DEFAULT 0, FOREIGN KEY(sender_server_id) REFERENCES plan_servers(id))";
    private MySQLDB underTest;

    @BeforeAll
    static void ensureMySQLAvailable(@TempDir Path tempDir) {
        assumeTrue(System.getenv(CIProperties.MYSQL_DATABASE) != null);
        dbPreparer = new DBPreparer(DaggerDatabaseTestComponent.builder()
                .bindTemporaryDirectory(tempDir)
                .build(), TEST_PORT_NUMBER);
    }

    @AfterAll
    static void closeSystem() {
        if (dbPreparer != null) dbPreparer.tearDown();
    }

    @AfterEach
    void noTempTables() {
        try {
            DBPreparer.assertNoTempTables(underTest);
        } finally {
            underTest.close();
        }
    }

    private void dropAllTables() {
        String dbName = System.getenv(CIProperties.MYSQL_DATABASE);
        underTest.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute("DROP DATABASE " + dbName);
                execute("CREATE DATABASE " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
                execute("USE " + dbName);
            }
        });
    }

    @BeforeEach
    void setUpDBWithOldSchema() {
        Optional<Database> db = dbPreparer.prepareMySQL();
        assumeTrue(db.isPresent());

        underTest = (MySQLDB) db.get();

        dropAllTables();

        // Initialize database with the old table schema
        underTest.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(serverTable);
                execute(usersTable);
                execute(userInfoTable);
                execute(geoInfoTable);
                execute(nicknameTable);
                execute(sessionsTable);
                execute(killsTable);
                execute(pingTable);
                execute(commandUseTable);
                execute(tpsTable);
                execute(worldsTable);
                execute(worldTimesTable);
                execute(securityTable);
                execute(transferTable);
            }
        });

        underTest.executeTransaction(new CreateTablesTransaction());

        insertData(underTest);
    }

    @Test
    void mysqlPatchesAreApplied() {
        Patch[] patches = underTest.patches();
        for (Patch patch : patches) {
            underTest.executeTransaction(patch);
        }

        assertPatchesHaveBeenApplied(patches);

        // Make sure that a fetch works.
        PlayerContainer player = underTest.query(ContainerFetchQueries.fetchPlayerContainer(TestConstants.PLAYER_ONE_UUID));
        OptionalAssert.equals(1, player.getValue(PlayerKeys.PLAYER_KILL_COUNT));

        // Make sure no foreign key checks fail on removal
        underTest.executeTransaction(new RemoveEverythingTransaction());
    }

    @Test
    void mysqlPatchesAreOnlyAppliedOnce() {
        Patch[] patches = underTest.patches();
        for (Patch patch : patches) {
            underTest.executeTransaction(patch);
        }
        assertPatchesHaveBeenApplied(patches);
        patches = underTest.patches();
        for (Patch patch : patches) {
            underTest.executeTransaction(patch);
        }
        assertPatchesWereNotApplied(patches);
    }

    @Test
    void mysqlDoesNotApplyKillsOptimizationPatchAgain() {
        mysqlPatchesAreApplied();

        KillsOptimizationPatch patch = new KillsOptimizationPatch();
        underTest.executeTransaction(patch);

        assertTrue(patch.isApplied());
        assertFalse(patch.wasApplied());
    }
}
