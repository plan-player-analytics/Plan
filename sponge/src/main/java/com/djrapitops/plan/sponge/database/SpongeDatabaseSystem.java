package com.djrapitops.plan.sponge.database;

import com.djrapitops.plan.common.api.exceptions.database.DBInitException;
import com.djrapitops.plan.common.system.database.DBSystem;
import com.djrapitops.plan.common.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.common.system.settings.Settings;
import com.djrapitops.plan.sponge.database.databases.sql.SpongeMySQLDB;

public class SpongeDatabaseSystem extends DBSystem {
    @Override
    protected void initDatabase() throws DBInitException {
        databases.add(new SpongeMySQLDB());
        databases.add(new SQLiteDB());

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
    }
}
