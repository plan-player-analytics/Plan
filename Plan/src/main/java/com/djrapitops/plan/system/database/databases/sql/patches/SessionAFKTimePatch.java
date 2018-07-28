package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.SessionsTable;

public class SessionAFKTimePatch extends Patch {

    public SessionAFKTimePatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(SessionsTable.TABLE_NAME, SessionsTable.Col.AFK_TIME.get());
    }

    @Override
    public void apply() {
        addColumns(SessionsTable.TABLE_NAME,
                SessionsTable.Col.AFK_TIME + " bigint NOT NULL DEFAULT 0"
        );
    }
}
