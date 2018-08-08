package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.tables.WorldTimesTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class WorldTimesSeverIDPatch extends Patch {

    public WorldTimesSeverIDPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        String tableName = WorldTimesTable.TABLE_NAME;
        String columnName = WorldTimesTable.Col.SERVER_ID.get();
        return hasColumn(tableName, columnName)
                && allValuesHaveServerID(tableName, columnName);
    }

    private Boolean allValuesHaveServerID(String tableName, String columnName) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + "=? LIMIT 1";
        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, 0);
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return !set.next();
            }
        });
    }

    @Override
    public void apply() {
        Map<Integer, Integer> sessionIDServerIDRelation = db.getSessionsTable().getIDServerIDRelation();

        String sql = "UPDATE " + WorldTimesTable.TABLE_NAME + " SET " +
                WorldTimesTable.Col.SERVER_ID + "=?" +
                " WHERE " + WorldTimesTable.Col.SESSION_ID + "=?";

        db.executeBatch(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<Integer, Integer> entry : sessionIDServerIDRelation.entrySet()) {
                    Integer sessionID = entry.getKey();
                    Integer serverID = entry.getValue();
                    statement.setInt(1, serverID);
                    statement.setInt(2, sessionID);
                    statement.addBatch();
                }
            }
        });
    }
}