/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.store.database;

import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;

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