package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.GeoInfoTable;

public class IPHashPatch extends Patch {

    public IPHashPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(GeoInfoTable.TABLE_NAME, GeoInfoTable.Col.IP_HASH.get());
    }

    @Override
    public void apply() {
        addColumns(GeoInfoTable.Col.IP_HASH.get() + " varchar(200) DEFAULT ''");
    }
}
