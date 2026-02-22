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
package com.djrapitops.plan.storage.database.transactions.init;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPlayerTableValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.text.TextStringBuilder;

/**
 * Transaction that creates the database index if it has not yet been created.
 *
 * @author AuroraLS3
 */
public class CreateIndexTransaction extends Transaction {

    @Override
    protected void performOperations() {
        createIndex(UsersTable.TABLE_NAME, "plan_users_uuid_index",
                UsersTable.USER_UUID
        );

        // replaced by foreign keys
        dropIndex(UserInfoTable.TABLE_NAME, "plan_user_info_uuid_index");
        // replaced by foreign keys
        dropIndex(SessionsTable.TABLE_NAME, "plan_sessions_uuid_index");

        createIndex(SessionsTable.TABLE_NAME, "plan_sessions_date_index",
                SessionsTable.SESSION_START
        );
        // Replaced by foreign keys
        dropIndex(WorldTimesTable.TABLE_NAME, "plan_world_times_uuid_index");

        createIndex(KillsTable.TABLE_NAME, "plan_kills_uuid_index",
                KillsTable.KILLER_UUID,
                KillsTable.VICTIM_UUID,
                KillsTable.SERVER_UUID
        );
        createIndex(KillsTable.TABLE_NAME, "plan_kills_date_index",
                KillsTable.DATE
        );
        // Replaced with foreign keys.
        dropIndex(PingTable.TABLE_NAME, "plan_ping_uuid_index");

        createIndex(PingTable.TABLE_NAME, "plan_ping_date_index",
                PingTable.DATE
        );
        createIndex(TPSTable.TABLE_NAME, "plan_tps_date_index",
                TPSTable.DATE
        );

        createIndex(SessionsTable.TABLE_NAME, "plan_session_join_address_index",
                SessionsTable.JOIN_ADDRESS_ID);

        createIndex(ExtensionPlayerTableValueTable.TABLE_NAME, "plan_extension_player_table_value_player_index",
                ExtensionPlayerTableValueTable.TABLE_ID,
                ExtensionPlayerTableValueTable.USER_UUID);
        createIndex(ExtensionServerTableValueTable.TABLE_NAME, "plan_extension_server_table_value_server_index",
                ExtensionServerTableValueTable.TABLE_ID,
                ExtensionServerTableValueTable.SERVER_UUID);
    }

    private void createIndex(String tableName, String indexName, String... indexedColumns) {
        if (indexedColumns.length == 0) {
            throw new IllegalArgumentException("Can not create index without columns");
        }

        boolean isMySQL = dbType == DBType.MYSQL;
        if (isMySQL) {
            boolean indexExists = query(MySQLSchemaQueries.doesIndexExist(indexName, tableName));
            if (indexExists) return;
        }

        TextStringBuilder sql = new TextStringBuilder("CREATE INDEX ");
        if (!isMySQL) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(indexName).append(" ON ").append(tableName);

        sql.append(" (");
        sql.appendWithSeparators(indexedColumns, ",");
        sql.append(')');

        execute(sql.toString());
    }

    private void dropIndex(String tableName, String indexName) {
        boolean isMySQL = dbType == DBType.MYSQL;
        if (isMySQL) {
            boolean indexExists = query(MySQLSchemaQueries.doesIndexExist(indexName, tableName));
            if (!indexExists) return;
            execute("DROP INDEX " + indexName + " ON " + tableName);
        } else {
            execute("DROP INDEX IF EXISTS " + indexName);
        }
    }
}