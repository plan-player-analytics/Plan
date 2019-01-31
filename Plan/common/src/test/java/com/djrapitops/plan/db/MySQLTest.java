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
package com.djrapitops.plan.db;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.db.access.queries.ServerAggregateQueries;
import com.djrapitops.plan.db.sql.tables.UsersTable;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.CIProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link MySQLDB}.
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

    @Test
    public void networkGeolocationsAreCountedAppropriately() {
        UUID firstUuid = UUID.randomUUID();
        UUID secondUuid = UUID.randomUUID();
        UUID thirdUuid = UUID.randomUUID();

        UsersTable usersTable = db.getUsersTable();
        usersTable.registerUser(firstUuid, 0, "");
        usersTable.registerUser(secondUuid, 0, "");
        usersTable.registerUser(thirdUuid, 0, "");

        saveGeoInfo(firstUuid, new GeoInfo("-", "Norway", 0, "3"));
        saveGeoInfo(firstUuid, new GeoInfo("-", "Finland", 5, "3"));
        saveGeoInfo(secondUuid, new GeoInfo("-", "Sweden", 0, "3"));
        saveGeoInfo(thirdUuid, new GeoInfo("-", "Denmark", 0, "3"));

        Map<String, Integer> got = db.query(ServerAggregateQueries.networkGeolocationCounts());

        Map<String, Integer> expected = new HashMap<>();
        // first user has a more recent connection from Finland so their country should be counted as Finland.
        expected.put("Finland", 1);
        expected.put("Sweden", 1);
        expected.put("Denmark", 1);

        assertEquals(expected, got);
    }
}
