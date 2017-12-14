/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.file.database;

import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.settings.Settings;

import java.util.HashSet;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlanDBSystem extends DBSystem {

    @Override
    protected void initDatabase() throws DatabaseInitException {
        databases = new HashSet<>();
        databases.add(new MySQLDB());
        databases.add(new SQLiteDB());

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();
        db = getActiveDatabase(dbType);
        db.init();
    }
}