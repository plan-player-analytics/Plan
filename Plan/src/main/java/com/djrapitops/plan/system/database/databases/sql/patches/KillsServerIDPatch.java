package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.tables.KillsTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class KillsServerIDPatch extends Patch {

    public KillsServerIDPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        String tableName = KillsTable.TABLE_NAME;
        String columnName = KillsTable.Col.SERVER_ID.get();
        return hasColumn(tableName, columnName) && allValuesHaveServerID(tableName, columnName);
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
        addColumns(KillsTable.Col.SERVER_ID + " integer NOT NULL DEFAULT 0");

        Map<Integer, Integer> sessionIDServerIDRelation = db.getSessionsTable().getIDServerIDRelation();

        String sql = "UPDATE " + KillsTable.TABLE_NAME + " SET " +
                KillsTable.Col.SERVER_ID + "=?" +
                " WHERE " + KillsTable.Col.SESSION_ID + "=?";

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
