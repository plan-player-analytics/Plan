package com.djrapitops.plan.system.database.databases.sql.tables.move;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

/**
 * DB Schema v17 -> 18 table.
 * <p>
 * Required due to a bug where duplicate rows were inserted.
 *
 * @author Rsl1122
 */
public class Version18TransferTable extends TransferTable {

    public Version18TransferTable(SQLDB db) {
        super(db);
    }

    public void alterTableV18() throws DBInitException {
        String tempTableName = "plan_ips_temp";
        String ipTableName = "plan_ips";
        try {
            renameTable(ipTableName, tempTableName);
        } catch (DBOpException e) {
            // Temp table already exists
            if (!e.getMessage().contains("plan_ips_temp")) {
                throw e;
            }
        }
        db.getGeoInfoTable().createTable();
        execute("INSERT INTO plan_ips (" +
                "user_id, ip, ip_hash, geolocation, last_used" +
                ") SELECT user_id, ip, ip_hash, geolocation, MAX(last_used) FROM plan_ips_temp GROUP BY ip_hash, user_id, ip, geolocation");
        dropTable(tempTableName);
    }
}