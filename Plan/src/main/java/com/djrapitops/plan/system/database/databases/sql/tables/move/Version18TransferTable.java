package com.djrapitops.plan.system.database.databases.sql.tables.move;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.sql.SQLException;

/**
 * DB Schema v17 -> 18 table.
 * <p>
 * Required due to a bug where duplicate rows were inserted.
 *
 * @author Rsl1122
 */
public class Version18TransferTable extends TransferTable {

    public Version18TransferTable(SQLDB db) throws SQLException {
        super(db);
    }

    public void alterTableV18() throws SQLException, DBInitException {
        String tempTableName = "plan_ips_temp";
        String ipTableName = "plan_ips";
        try {
            renameTable(ipTableName, tempTableName);
        } catch (SQLException e) {
            // Temp table already exists
            if (!e.getMessage().contains("plan_ips_temp")) {
                throw e;
            }
        }
        db.getGeoInfoTable().createTable();
        execute("INSERT INTO plan_ips (" +
                "user_id, ip, ip_hash, geolocation, last_used" +
                ") SELECT user_id, ip, ip_hash, geolocation, MAX(last_used) FROM plan_ips_temp GROUP BY ip_hash, user_id");
        dropTable(tempTableName);
    }
}