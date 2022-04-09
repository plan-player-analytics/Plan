/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.storage.database.queries.schema;

import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.SELECT;

/**
 * Query to fetch server id for each session, used by 2 patches.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.storage.database.transactions.patches.KillsServerIDPatch
 * @see com.djrapitops.plan.storage.database.transactions.patches.WorldTimesSeverIDPatch
 */
public class SessionIDServerIDRelationQuery extends QueryAllStatement<Map<Integer, Integer>> {

    public SessionIDServerIDRelationQuery() {
        super(SELECT + SessionsTable.ID + ',' +
                "(SELECT plan_servers.id FROM plan_servers WHERE plan_servers.id=" + SessionsTable.SERVER_ID + ") as server_id" +
                FROM + SessionsTable.TABLE_NAME, 50000);
    }

    @Override
    public Map<Integer, Integer> processResults(ResultSet set) throws SQLException {
        Map<Integer, Integer> idServerIdMap = new HashMap<>();
        while (set.next()) {
            idServerIdMap.put(set.getInt(SessionsTable.ID), set.getInt("server_id"));
        }
        return idServerIdMap;
    }
}