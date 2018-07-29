/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases;

import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.system.locale.Locale;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests MySQLDB.
 *
 * @author Rsl1122
 */
public class MySQLTest {

    @Test
    public void testMySQLGetConfigName() {
        assertEquals("mysql", new MySQLDB(Locale::new).getConfigName());
    }

    @Test
    public void testMySQLGetName() {
        assertEquals("MySQL", new MySQLDB(Locale::new).getName());
    }

}
