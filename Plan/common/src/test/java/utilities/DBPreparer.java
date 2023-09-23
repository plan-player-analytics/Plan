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
package utilities;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DBPreparer {

    private final Dependencies dependencies;
    private final int testPortNumber;

    public DBPreparer(Dependencies dependencies, int testPortNumber) {
        this.dependencies = dependencies;
        this.testPortNumber = testPortNumber;
    }

    public Optional<Database> prepareSQLite() {
        String dbName = DBType.SQLITE.getName();
        return Optional.of(prepareDBByName(dbName));
    }

    public static void assertNoTempTables(SQLDB db) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                assertFalse(hasTable("temp_ips"));
                assertFalse(hasTable("temp_geoinformation"));
                assertFalse(hasTable("temp_kills"));
                assertFalse(hasTable("temp_nicknames"));
                assertFalse(hasTable("temp_ping"));
                assertFalse(hasTable("temp_user_info"));
                assertFalse(hasTable("temp_world_times"));
                assertFalse(hasTable("temp_sessions"));
                assertFalse(hasTable("temp_security"));
                assertFalse(hasTable("temp_security_id_patch"));
                assertFalse(hasTable("temp_user_info_address_patching"));
                assertFalse(hasTable("temp_nicks"));
                assertFalse(hasTable("temp_kills"));
                assertFalse(hasTable("temp_users"));
                assertFalse(hasTable("temp_tps"));
                assertFalse(hasTable("temp_worlds"));
            }
        });
    }

    public Optional<String> setUpMySQLSettings(PlanConfig config) {
        String database = System.getenv(CIProperties.MYSQL_DATABASE);
        String user = System.getenv(CIProperties.MYSQL_USER);
        String pass = System.getenv(CIProperties.MYSQL_PASS);
        String port = System.getenv(CIProperties.MYSQL_PORT);
        if (database == null || user == null) {
            return Optional.empty();
        }

        String dbName = DBType.MYSQL.getName();

        config.set(DatabaseSettings.MYSQL_DATABASE, database);
        config.set(DatabaseSettings.MYSQL_USER, user);
        config.set(DatabaseSettings.MYSQL_PASS, pass != null ? pass : "");
        config.set(DatabaseSettings.MYSQL_HOST, "127.0.0.1");
        config.set(DatabaseSettings.MYSQL_PORT, port != null ? port : "3306");
        config.set(DatabaseSettings.TYPE, dbName);
        return Optional.of(database);
    }

    public Optional<Database> prepareMySQL() {
        PlanConfig config = dependencies.config();
        Optional<String> formattedDB = setUpMySQLSettings(config);
        if (formattedDB.isPresent()) {
            String formattedDatabase = formattedDB.get();
            SQLDB mysql = prepareDBByName(DBType.MYSQL.getName());
            mysql.executeTransaction(new Transaction() {
                @Override
                protected void performOperations() {
                    execute("SET GLOBAL innodb_file_per_table=0");
                    execute("SET GLOBAL innodb_fast_shutdown=2");

                    execute("DROP DATABASE " + formattedDatabase);
                    execute("CREATE DATABASE " + formattedDatabase + " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
                    execute("USE " + formattedDatabase);
                }
            });
            return Optional.of(mysql);
        }
        return Optional.empty();
    }

    private SQLDB prepareDBByName(String dbName) {
        PlanConfig config = dependencies.config();
        config.set(WebserverSettings.PORT, testPortNumber);
        config.set(DatabaseSettings.TYPE, dbName);
        dependencies.enable();

        DBSystem dbSystem = dependencies.dbSystem();
        SQLDB db = (SQLDB) dbSystem.getActiveDatabaseByName(dbName);
        db.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        db.init();
        assertNoTempTables(db);
        return db;
    }

    public void tearDown() {
        dependencies.disable();
    }

    public interface Dependencies extends SubSystem {
        PlanConfig config();

        DBSystem dbSystem();
    }
}
