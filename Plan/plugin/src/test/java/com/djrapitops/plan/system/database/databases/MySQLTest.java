package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import org.junit.BeforeClass;
import utilities.CIProperties;

import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link com.djrapitops.plan.system.database.databases.sql.MySQLDB}.
 * <p>
 * These settings assume Travis CI environment with MySQL service running.
 * 'Plan' database should be created before the test.
 *
 * @author Rsl1122
 */
public class MySQLTest extends CommonDBTest {

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        boolean isTravis = Boolean.parseBoolean(System.getenv(CIProperties.IS_TRAVIS));
        assumeTrue(isTravis);

        PlanConfig config = component.getPlanSystem().getConfigSystem().getConfig();
        config.set(DatabaseSettings.MYSQL_DATABASE, "Plan");
        config.set(DatabaseSettings.MYSQL_USER, "travis");
        config.set(DatabaseSettings.MYSQL_PASS, "");
        config.set(DatabaseSettings.MYSQL_HOST, "127.0.0.1");
        config.set(DatabaseSettings.TYPE, "MySQL");

        handleSetup("MySQL");
    }
}
