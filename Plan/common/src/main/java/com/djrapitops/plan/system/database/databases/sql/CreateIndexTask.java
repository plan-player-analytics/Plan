package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.system.database.databases.DBType;
import com.djrapitops.plan.system.database.databases.sql.operation.Queries;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.tables.*;
import com.djrapitops.plugin.task.AbsRunnable;
import org.apache.commons.text.TextStringBuilder;

public class CreateIndexTask extends AbsRunnable {

    private final SQLDB db;

    public CreateIndexTask(SQLDB db) {
        this.db = db;
    }

    @Override
    public void run() {
        createIndex(UsersTable.TABLE_NAME, "plan_users_uuid_index",
                UsersTable.Col.UUID
        );
        createIndex(UserInfoTable.TABLE_NAME, "plan_user_info_uuid_index",
                UserInfoTable.Col.UUID,
                UserInfoTable.Col.SERVER_UUID
        );
        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_uuid_index",
                SessionsTable.Col.UUID,
                SessionsTable.Col.SERVER_UUID
        );
        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_date_index",
                SessionsTable.Col.SESSION_START
        );
        createIndex(WorldTimesTable.TABLE_NAME, "plan_world_times_uuid_index",
                WorldTimesTable.Col.UUID,
                WorldTimesTable.Col.SERVER_UUID
        );
        createIndex(KillsTable.TABLE_NAME, "plan_kills_uuid_index",
                KillsTable.Col.KILLER_UUID,
                KillsTable.Col.VICTIM_UUID,
                KillsTable.Col.SERVER_UUID
        );
        createIndex(KillsTable.TABLE_NAME, "plan_kills_date_index",
                KillsTable.Col.DATE
        );
        createIndex(PingTable.TABLE_NAME, "plan_ping_uuid_index",
                PingTable.Col.UUID,
                PingTable.Col.SERVER_UUID
        );
        createIndex(PingTable.TABLE_NAME, "plan_ping_date_index",
                PingTable.Col.DATE
        );
        createIndex(TPSTable.TABLE_NAME, "plan_tps_date_index",
                TPSTable.Col.DATE
        );
    }

    private void createIndex(String tableName, String indexName, Column... indexedColumns) {
        if (indexedColumns.length == 0) {
            throw new IllegalArgumentException("Can not create index without columns");
        }

        boolean isMySQL = db.getType() == DBType.MYSQL;
        if (isMySQL) {
            boolean indexExists = db.query(Queries.doesIndexExist(indexName, tableName));
            if (indexExists) return;
        }

        TextStringBuilder sql = new TextStringBuilder("CREATE INDEX ");
        if (!isMySQL) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(indexName).append(" ON ").append(tableName);

        sql.append(" (");
        sql.appendWithSeparators(indexedColumns, ",");
        sql.append(")");

        db.execute(sql.toString());
    }
}
