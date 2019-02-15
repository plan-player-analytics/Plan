package com.djrapitops.plan.db.access.queries.schema;

import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.sql.tables.SessionsTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.djrapitops.plan.db.sql.parsing.Sql.FROM;
import static com.djrapitops.plan.db.sql.parsing.Sql.SELECT;

/**
 * Query to fetch server id for each session, used by 2 patches.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.db.patches.KillsServerIDPatch
 * @see com.djrapitops.plan.db.patches.WorldTimesSeverIDPatch
 */
public class SessionIDServerIDRelationQuery extends QueryAllStatement<Map<Integer, Integer>> {

    public SessionIDServerIDRelationQuery() {
        super(SELECT + SessionsTable.ID + ", " +
                "(SELECT plan_servers.id FROM plan_servers WHERE plan_servers.uuid=" + SessionsTable.SERVER_UUID + ") as server_id" +
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