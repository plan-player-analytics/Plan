/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.file.database;

import com.djrapitops.plan.api.exceptions.DatabaseInitException;
import com.djrapitops.plan.database.databases.MySQLDB;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlanBungeeDBSystem extends DBSystem {


    @Override
    protected void initDatabase() throws DatabaseInitException {
        db = new MySQLDB();
        databases.add(db);
        db.init();
    }
}