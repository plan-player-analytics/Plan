package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.TransferTable;

public class TransferPartitionPatch extends Patch {

    public TransferPartitionPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(TransferTable.TABLE_NAME, TransferTable.Col.PART.get());
    }

    @Override
    public void apply() {
        addColumns(TransferTable.TABLE_NAME, TransferTable.Col.PART + " bigint NOT NULL DEFAULT 0");
    }
}
