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
package com.djrapitops.plan.system.database.databases.sql;

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
        clearDatabase();
    }

    private static void clearDatabase() {
        db.execute("DROP DATABASE Plan");
        db.execute("CREATE DATABASE Plan");
        db.execute("USE Plan");
    }
}
