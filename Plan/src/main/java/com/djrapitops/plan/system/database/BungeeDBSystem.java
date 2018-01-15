/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.MySQLDB;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BungeeDBSystem extends DBSystem {

    @Override
    protected void initDatabase() throws DBInitException {
        db = new MySQLDB();
        databases.add(db);
        db.init();
    }
}