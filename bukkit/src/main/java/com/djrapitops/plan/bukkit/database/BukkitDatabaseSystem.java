package com.djrapitops.plan.bukkit.database;

import com.djrapitops.plan.common.api.exceptions.database.DBInitException;
import com.djrapitops.plan.common.system.database.DBSystem;
import com.djrapitops.plan.common.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.common.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.common.system.settings.Settings;

public class BukkitDatabaseSystem extends DBSystem {
    @Override
    protected void initDatabase() throws DBInitException {
        databases.add(new MySQLDB());
        databases.add(new SQLiteDB());

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
    }
}
