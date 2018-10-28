/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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