package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.GeoInfoTable;

public class GeoInfoLastUsedPatch extends Patch {

    public GeoInfoLastUsedPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(GeoInfoTable.TABLE_NAME, GeoInfoTable.Col.LAST_USED.get());
    }

    @Override
    public void apply() {
        addColumns(GeoInfoTable.TABLE_NAME,
                GeoInfoTable.Col.LAST_USED + " bigint NOT NULL DEFAULT 0"
        );
    }
}
