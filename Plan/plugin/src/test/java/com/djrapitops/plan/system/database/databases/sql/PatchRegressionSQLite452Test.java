package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import rules.PluginComponentMocker;
import utilities.OptionalAssert;
import utilities.TestConstants;

import java.sql.SQLException;

/**
 * Test for the patching of Plan 4.5.2 SQLite DB into the newest schema.
 *
 * @author Rsl1122
 */
public class PatchRegressionSQLite452Test extends PatchRegression452Test {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public PluginComponentMocker component = new PluginComponentMocker(temporaryFolder);

    String serverTable = "CREATE TABLE IF NOT EXISTS plan_servers (id integer PRIMARY KEY, uuid varchar(36) NOT NULL UNIQUE, name varchar(100), web_address varchar(100), is_installed boolean NOT NULL DEFAULT 1, max_players integer NOT NULL DEFAULT -1)";
    String usersTable = "CREATE TABLE IF NOT EXISTS plan_users (id integer PRIMARY KEY, uuid varchar(36) NOT NULL UNIQUE, registered bigint NOT NULL, name varchar(16) NOT NULL, times_kicked integer NOT NULL DEFAULT 0)";
    String userInfoTable = "CREATE TABLE IF NOT EXISTS plan_user_info (user_id integer NOT NULL, registered bigint NOT NULL, opped boolean NOT NULL DEFAULT 0, banned boolean NOT NULL DEFAULT 0, server_id integer NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String geoInfoTable = "CREATE TABLE IF NOT EXISTS plan_ips (user_id integer NOT NULL, ip varchar(39) NOT NULL, geolocation varchar(50) NOT NULL, ip_hash varchar(200), last_used bigint NOT NULL DEFAULT 0, FOREIGN KEY(user_id) REFERENCES plan_users(id))";
    String nicknameTable = "CREATE TABLE IF NOT EXISTS plan_nicknames (user_id integer NOT NULL, nickname varchar(75) NOT NULL, server_id integer NOT NULL, last_used bigint NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String sessionsTable = "CREATE TABLE IF NOT EXISTS plan_sessions (id integer PRIMARY KEY, user_id integer NOT NULL, server_id integer NOT NULL, session_start bigint NOT NULL, session_end bigint NOT NULL, mob_kills integer NOT NULL, deaths integer NOT NULL, afk_time bigint NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String killsTable = "CREATE TABLE IF NOT EXISTS plan_kills (killer_id integer NOT NULL, victim_id integer NOT NULL, server_id integer NOT NULL, weapon varchar(30) NOT NULL, date bigint NOT NULL, session_id integer NOT NULL, FOREIGN KEY(killer_id) REFERENCES plan_users(id), FOREIGN KEY(victim_id) REFERENCES plan_users(id), FOREIGN KEY(session_id) REFERENCES plan_sessions(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String pingTable = "CREATE TABLE IF NOT EXISTS plan_ping (id integer PRIMARY KEY, user_id integer NOT NULL, server_id integer NOT NULL, date bigint NOT NULL, max_ping integer NOT NULL, min_ping integer NOT NULL, avg_ping double NOT NULL, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String commandUseTable = "CREATE TABLE IF NOT EXISTS plan_commandusages (id integer PRIMARY KEY, command varchar(20) NOT NULL, times_used integer NOT NULL, server_id integer NOT NULL, FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String tpsTable = "CREATE TABLE IF NOT EXISTS plan_tps (server_id integer NOT NULL, date bigint NOT NULL, tps double NOT NULL, players_online integer NOT NULL, cpu_usage double NOT NULL, ram_usage bigint NOT NULL, entities integer NOT NULL, chunks_loaded integer NOT NULL, free_disk_space bigint NOT NULL, FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String worldsTable = "CREATE TABLE IF NOT EXISTS plan_worlds (id integer PRIMARY KEY, world_name varchar(100) NOT NULL, server_id integer NOT NULL, FOREIGN KEY(server_id) REFERENCES plan_servers(id))";
    String worldTimesTable = "CREATE TABLE IF NOT EXISTS plan_world_times (user_id integer NOT NULL, world_id integer NOT NULL, server_id integer NOT NULL, session_id integer NOT NULL, survival_time bigint NOT NULL DEFAULT 0, creative_time bigint NOT NULL DEFAULT 0, adventure_time bigint NOT NULL DEFAULT 0, spectator_time bigint NOT NULL DEFAULT 0, FOREIGN KEY(user_id) REFERENCES plan_users(id), FOREIGN KEY(world_id) REFERENCES plan_worlds(id), FOREIGN KEY(server_id) REFERENCES plan_servers(id), FOREIGN KEY(session_id) REFERENCES plan_sessions(id))";
    String securityTable = "CREATE TABLE IF NOT EXISTS plan_security (username varchar(100) NOT NULL UNIQUE, salted_pass_hash varchar(100) NOT NULL UNIQUE, permission_level integer NOT NULL)";
    String transferTable = "CREATE TABLE IF NOT EXISTS plan_transfer (sender_server_id integer NOT NULL, expiry_date bigint NOT NULL DEFAULT 0, type varchar(100) NOT NULL, extra_variables varchar(255) DEFAULT '', content_64 varchar(1), part bigint NOT NULL DEFAULT 0, FOREIGN KEY(sender_server_id) REFERENCES plan_servers(id))";

    private SQLiteDB underTest;

    @Before
    public void setUpDBWithOldSchema() throws DBInitException, SQLException {
        underTest = component.getPlanSystem().getDatabaseSystem().getSqLiteFactory()
                .usingFileCalled("test");

        underTest.setOpen(true);
        underTest.setupDataSource();

        // Initialize database with the old table schema
        underTest.execute(serverTable);
        underTest.execute(usersTable);
        underTest.execute(userInfoTable);
        underTest.execute(geoInfoTable);
        underTest.execute(nicknameTable);
        underTest.execute(sessionsTable);
        underTest.execute(killsTable);
        underTest.execute(pingTable);
        underTest.execute(commandUseTable);
        underTest.execute(tpsTable);
        underTest.execute(worldsTable);
        underTest.execute(worldTimesTable);
        underTest.execute(securityTable);
        underTest.execute(transferTable);

        underTest.createTables();

        insertData(underTest);
    }

    @After
    public void closeDatabase() {
        underTest.close();
    }

    @Test
    public void sqlitePatchTaskWorksWithoutErrors() {
        PatchTask patchTask = new PatchTask(underTest.patches(), new Locale(), new TestPluginLogger(), new ErrorHandler() {
            @Override
            public void log(L l, Class aClass, Throwable throwable) {
                throw new AssertionError(throwable);
            }
        });

        // Patching might fail due to exception.
        patchTask.run();

        // Make sure that a fetch works.
        ServerContainer server = underTest.fetch().getServerContainer(TestConstants.SERVER_UUID);
        OptionalAssert.equals(1, server.getValue(ServerKeys.PLAYER_KILL_COUNT));
    }
}
