package com.djrapitops.plan.system.database.databases.sql.tables.move;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;

/**
 * Abstract table used for transferring a whole table to a new table.
 *
 * @author Rsl1122
 */
public class TransferTable extends Table {

    public TransferTable(SQLDB db) {
        super("", db);
    }

    @Override
    public void createTable() {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    protected void renameTable(String from, String to) {
        String sql = usingMySQL ?
                "RENAME TABLE " + from + " TO " + to :
                "ALTER TABLE " + from + " RENAME TO " + to;
        execute(sql);
    }

    protected void dropTable(String name) {
        String sql = "DROP TABLE " + name;
        execute(sql);
    }

}