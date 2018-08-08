package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;

public class VersionTableRemovalPatch extends Patch {

    public VersionTableRemovalPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasTable("plan_version");
    }

    @Override
    public void apply() {
        dropTable("plan_version");
    }
}
