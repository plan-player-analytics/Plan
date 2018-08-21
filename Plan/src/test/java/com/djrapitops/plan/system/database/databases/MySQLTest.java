/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Tests MySQLDB.
 *
 * @author Rsl1122
 */
public class MySQLTest {

    private final MySQLDB mySQLDB;

    @Inject
    public MySQLTest(MySQLDB mySQLDB) {
        this.mySQLDB = mySQLDB;
    }

    @Test
    public void testMySQLGetConfigName() {
        assertEquals("mysql", mySQLDB.getConfigName());
    }

    @Test
    public void testMySQLGetName() {
        assertEquals("MySQL", mySQLDB.getName());
    }

}
