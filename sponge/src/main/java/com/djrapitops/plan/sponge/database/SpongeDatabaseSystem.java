package com.djrapitops.plan.sponge.database;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.sponge.database.databases.sql.SpongeMySQLDB;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.settings.Settings;

public class SpongeDatabaseSystem extends DBSystem {
    @Override
    protected void initDatabase() throws DBInitException {
        databases.add(new SpongeMySQLDB());
        databases.add(new SQLiteDB());

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
    }
}
