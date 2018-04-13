/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.database.databases.sql.SpongeMySQLDB;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.Check;

/**
 * Bukkit Database system that initializes SQLite and MySQL database objects.
 *
 * @author Rsl1122
 */
public class ServerDBSystem extends DBSystem {

    @Override
    protected void initDatabase() throws DBInitException {
        databases.add(Check.isSpongeAvailable() ? new SpongeMySQLDB() : new MySQLDB());
        databases.add(new SQLiteDB());

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
    }
}