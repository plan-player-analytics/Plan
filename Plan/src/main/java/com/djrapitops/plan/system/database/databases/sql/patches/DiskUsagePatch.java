package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.TPSTable;

public class DiskUsagePatch extends Patch {

    public DiskUsagePatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(TPSTable.TABLE_NAME, TPSTable.Col.FREE_DISK.get());
    }

    @Override
    public void apply() {
        addColumn(TPSTable.TABLE_NAME,
                TPSTable.Col.FREE_DISK + " bigint NOT NULL DEFAULT -1"
        );
    }
}
